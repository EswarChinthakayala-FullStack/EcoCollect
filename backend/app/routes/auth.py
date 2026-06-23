from fastapi import APIRouter, Depends, HTTPException, status, Body
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User, Supervisor
from app.models.activity_log import ActivityLog
from app.models.otp import OTP
from app.schemas.user import (
    LoginRequest, AuthResponse, SupervisorLoginRequest, 
    SupervisorAuthResponse, SupervisorRegisterRequest, SupervisorDetailsResponse
)
from app.utils.security import get_password_hash, verify_password, create_access_token, require_role
from app.utils.email import send_otp_email
from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime, timedelta
import random

router = APIRouter(prefix="/auth", tags=["Authentication"])

class RegisterRequest(BaseModel):
    email: EmailStr
    full_name: str
    phone: str
    dob: str
    password: str

@router.post("/register", response_model=AuthResponse)
def register(request: RegisterRequest, db: Session = Depends(get_db)):
    print(f"[DEBUG] Registration request received: email='{request.email}', name='{request.full_name}', phone='{request.phone}'")
    db_user = db.query(User).filter(User.email == request.email).first()
    if db_user:
        print(f"[DEBUG] Registration failed: Email '{request.email}' is already registered in the database")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )
    
    new_user = User(
        email=request.email,
        full_name=request.full_name,
        phone=request.phone,
        dob=request.dob if request.dob else None,
        role="citizen",
        password_hash=get_password_hash(request.password)
    )
    db.add(new_user)
    try:
        log = ActivityLog(
            action="Citizen Registered",
            type="create",
            details=f"New citizen account registered: {request.email} ({request.full_name})"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
    db.commit()
    db.refresh(new_user)
    print(f"[DEBUG] User '{request.email}' registered successfully with ID {new_user.id}")
    
    token = create_access_token({"sub": new_user.email})
    return AuthResponse(
        message="Registration successful",
        token=token,
        user=new_user
    )

@router.post("/login", response_model=AuthResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    print(f"[DEBUG] Login request received: email='{request.email}'")
    user = db.query(User).filter(User.email == request.email).first()
    if not user:
        print(f"[DEBUG] Login failed: User with email '{request.email}' not found")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password"
        )
    
    if hasattr(user, 'is_active') and user.is_active == 0:
        print(f"[DEBUG] Login failed: User '{request.email}' is deactivated")
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account is deactivated. Please contact administrator."
        )
    
    password_ok = verify_password(request.password, user.password_hash)
    if not password_ok:
        print(f"[DEBUG] Login failed: Password verification failed for user '{request.email}'")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password"
        )
        
    print(f"[DEBUG] User '{request.email}' verified. Login successful")
    token = create_access_token({"sub": user.email})
    try:
        log = ActivityLog(
            action="Admin Login" if user.role == "admin" else "Citizen Login",
            type="login",
            details=f"Successful login for {user.role}: {user.email}"
        )
        db.add(log)
        db.commit()
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
    return AuthResponse(
        message="Login successful",
        token=token,
        user=user
    )

@router.post("/send-otp")
def send_otp():
    return {"message": "OTP sent", "otp": "123456"}

class VerifyOtpRequest(BaseModel):
    email: EmailStr
    otp: str

@router.post("/verify-otp")
def verify_otp(
    email: Optional[str] = None,
    otp: Optional[str] = None,
    request: Optional[VerifyOtpRequest] = Body(None),
    db: Session = Depends(get_db)
):
    req_email = request.email if request else email
    req_otp = request.otp if request else otp
    
    if not req_email or not req_otp:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Email and OTP are required"
        )
        
    print(f"[DEBUG] Verify OTP request received for email: '{req_email}', OTP: '{req_otp}'")
    
    # 1. Try real OTP check against database first (Forgot Password flow)
    otp_entry = db.query(OTP).filter(
        OTP.email == req_email,
        OTP.otp_code == req_otp,
        OTP.is_verified == False,
        OTP.expires_at > datetime.utcnow()
    ).order_by(OTP.id.desc()).first()
    
    if otp_entry:
        otp_entry.is_verified = True
        db.commit()
        print(f"[DEBUG] Real OTP verification successful for {req_email}")
        return {"message": "OTP verified successfully."}
        
    # 2. If not found in DB, try mock check (Registration/verification flow)
    if req_otp == "123456":
        user = db.query(User).filter(User.email == req_email).first()
        if not user:
            print(f"[DEBUG] Mock OTP user not found: {req_email}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )
        print(f"[DEBUG] Mock OTP verification successful for {req_email}")
        token = create_access_token({"sub": user.email})
        return AuthResponse(
            message="Verified",
            token=token,
            user=user
        )
        
    # 3. Both failed
    print(f"[DEBUG] OTP verification failed for {req_email}")
    raise HTTPException(
        status_code=status.HTTP_400_BAD_REQUEST,
        detail="Invalid or expired verification code."
    )

@router.post("/supervisor/login", response_model=SupervisorAuthResponse)
def supervisor_login(request: SupervisorLoginRequest, db: Session = Depends(get_db)):
    print(f"[DEBUG] Supervisor login request: employee_id='{request.employee_id}'")
    sup = db.query(Supervisor).filter(Supervisor.employee_id == request.employee_id).first()
    if not sup:
        print(f"[DEBUG] Supervisor login failed: No supervisor found with employee_id='{request.employee_id}'")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect Employee ID or password"
        )
        
    if hasattr(sup.user, 'is_active') and sup.user.is_active == 0:
        print(f"[DEBUG] Supervisor login failed: Supervisor '{request.employee_id}' is deactivated")
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account is deactivated. Please contact administrator."
        )
        
    pw_ok = verify_password(request.password, sup.user.password_hash)
    if not pw_ok:
        print(f"[DEBUG] Supervisor login failed: Password verification failed for employee_id='{request.employee_id}'")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect Employee ID or password"
        )
        
    print(f"[DEBUG] Supervisor login successful: {request.employee_id}")
        
    token = create_access_token({"sub": sup.user.email})
    try:
        log = ActivityLog(
            action="Supervisor Login",
            type="login",
            details=f"Successful login for supervisor: {sup.employee_id} ({sup.user.full_name})"
        )
        db.add(log)
        db.commit()
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
        
    sup_details = SupervisorDetailsResponse(
        id=sup.id,
        employee_id=sup.employee_id,
        user_id=sup.user_id,
        assigned_area=sup.assigned_area,
        created_at=sup.created_at.isoformat(),
        name=sup.user.full_name,
        email=sup.user.email,
        phone=sup.user.phone,
        profile_picture=sup.user.profile_image_url
    )
    
    return SupervisorAuthResponse(
        message="Login successful",
        token=token,
        supervisor=sup_details
    )

@router.post("/supervisor/register", response_model=SupervisorAuthResponse)
def supervisor_register(
    request: SupervisorRegisterRequest, 
    db: Session = Depends(get_db),
    current_admin: User = Depends(require_role(["admin"]))
):
    print(f"[DEBUG] Supervisor registration request received: email='{request.email}', employee_id='{request.employee_id}', name='{request.full_name}'")
    existing_user = db.query(User).filter(User.email == request.email).first()
    if existing_user:
        print(f"[DEBUG] Supervisor registration failed: A user with email '{request.email}' already exists")
        raise HTTPException(status_code=400, detail="A user with this Email already exists")
        
    existing_sup = db.query(Supervisor).filter(Supervisor.employee_id == request.employee_id).first()
    if existing_sup:
        print(f"[DEBUG] Supervisor registration failed: A supervisor with employee_id '{request.employee_id}' already exists")
        raise HTTPException(status_code=400, detail="A supervisor with this Employee ID already exists")
        
    user = User(
        email=request.email,
        full_name=request.full_name,
        phone=request.phone,
        role="supervisor",
        password_hash=get_password_hash(request.password)
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    
    sup = Supervisor(
        user_id=user.id,
        employee_id=request.employee_id,
        assigned_area=request.assigned_area,
        latitude=request.latitude,
        longitude=request.longitude,
        coverage_radius=request.coverage_radius
    )
    db.add(sup)
    
    try:
        from app.models.activity_log import ActivityLog
        log = ActivityLog(
            action="Supervisor Created",
            type="create",
            details=f"Added new supervisor: {request.employee_id} ({request.full_name})"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
        
    db.commit()
    db.refresh(sup)
    
    sup_details = SupervisorDetailsResponse(
        id=sup.id,
        employee_id=sup.employee_id,
        user_id=sup.user_id,
        assigned_area=sup.assigned_area,
        created_at=sup.created_at.isoformat(),
        name=user.full_name,
        email=user.email,
        phone=user.phone,
        profile_picture=user.profile_image_url
    )
    
    return SupervisorAuthResponse(
        message="Supervisor created successfully",
        token=None,
        supervisor=sup_details
    )

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class ResetPasswordRequest(BaseModel):
    email: EmailStr
    otp: str
    new_password: str

@router.post("/forgot-password")
def forgot_password(request: ForgotPasswordRequest, db: Session = Depends(get_db)):
    print(f"[DEBUG] Forgot password request received for email: '{request.email}'")
    user = db.query(User).filter(User.email == request.email).first()
    if not user:
        print(f"[DEBUG] Forgot password failed: Email '{request.email}' not found in database")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Email address not registered."
        )
    
    # Generate 6 digit code
    otp_code = "".join(random.choices("0123456789", k=6))
    expires_at = datetime.utcnow() + timedelta(minutes=10)
    
    otp_entry = OTP(
        email=request.email,
        otp_code=otp_code,
        expires_at=expires_at,
        is_verified=False
    )
    db.add(otp_entry)
    db.commit()
    
    # Send email
    email_sent = send_otp_email(request.email, otp_code)
    if not email_sent:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to send verification email. Please try again."
        )
        
    return {"message": "Verification code sent to your email."}

@router.post("/reset-password")
def reset_password(request: ResetPasswordRequest, db: Session = Depends(get_db)):
    print(f"[DEBUG] Reset password request received for email: '{request.email}'")
    
    # Check if there is a verified OTP
    otp_entry = db.query(OTP).filter(
        OTP.email == request.email,
        OTP.otp_code == request.otp,
        OTP.is_verified == True
    ).order_by(OTP.id.desc()).first()
    
    if not otp_entry:
        print(f"[DEBUG] Reset password verification failed: no verified OTP match for {request.email}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Unauthorized password reset attempt. Verify OTP first."
        )
        
    user = db.query(User).filter(User.email == request.email).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_444_NOT_FOUND if hasattr(status, 'HTTP_444_NOT_FOUND') else 404,
            detail="User not found."
        )
        
    # Reset password
    user.password_hash = get_password_hash(request.new_password)
    
    # Optional: Log activity
    try:
        from app.models.activity_log import ActivityLog
        log = ActivityLog(
            action="Password Reset",
            type="update",
            details=f"Successful password reset for user: {user.email}"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
        
    db.commit()
    print(f"[DEBUG] Password reset successful for {request.email}")
    return {"message": "Password reset successfully."}
