import os
import uuid
import shutil
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User
from app.models.activity_log import ActivityLog
from app.models.notification import Notification
from app.schemas.user import UserResponse, UserUpdate
from app.schemas.notification import NotificationResponse
from app.schemas.issue import SingleReportResponse
from app.models.issue import IssueReport, IssueHistory
from app.utils.security import get_current_user
from typing import List

router = APIRouter(prefix="", tags=["Common Operations"])

# Serve upload folder location relative to this file
UPLOAD_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "uploads")

@router.post("/upload")
def upload_file(file: UploadFile = File(...)):
    # Create folder if it doesn't exist for safety
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    
    # Generate unique filename to avoid collision
    file_extension = os.path.splitext(file.filename)[1]
    new_filename = f"{uuid.uuid4()}{file_extension}"
    file_path = os.path.join(UPLOAD_DIR, new_filename)
    
    # Save the file
    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Could not save file: {str(e)}"
        )
        
    # Return the static file url (served as /uploads/filename)
    return {"file_url": f"/uploads/{new_filename}"}

@router.get("/profile", response_model=UserResponse)
def get_profile(current_user: User = Depends(get_current_user)):
    return current_user

@router.put("/profile", response_model=UserResponse)
def update_profile(
    request: UserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    update_data = request.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(current_user, key, value)
        
    try:
        log = ActivityLog(
            action="Profile Updated",
            type="update",
            details=f"User {current_user.email} updated profile settings"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
        
    db.commit()
    db.refresh(current_user)
    return current_user

from pydantic import BaseModel

class PasswordChangeRequest(BaseModel):
    current_password: str
    new_password: str

@router.put("/change-password")
def change_password(
    request: PasswordChangeRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    from app.utils.security import verify_password, get_password_hash
    
    if not verify_password(request.current_password, current_user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Incorrect current password"
        )
        
    current_user.password_hash = get_password_hash(request.new_password)
    
    try:
        log = ActivityLog(
            action="Password Changed",
            type="security",
            details=f"User {current_user.email} updated account password"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
        
    db.commit()
    return {"message": "Password updated successfully"}

@router.get("/notifications", response_model=List[NotificationResponse])
def get_notifications(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    notifications = db.query(Notification).filter(Notification.user_id == current_user.id).all()
    return notifications

@router.put("/notifications/{notification_id}/read")
def read_notification(
    notification_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    noti = db.query(Notification).filter(
        Notification.id == notification_id,
        Notification.user_id == current_user.id
    ).first()
    
    if not noti:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Notification not found"
        )
        
    noti.read_status = 1
    db.commit()
    return {"message": "Notification marked as read"}

@router.get("/issues/{issue_id}", response_model=SingleReportResponse)
def get_issue_by_id(
    issue_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    issue = db.query(IssueReport).filter(IssueReport.id == issue_id).first()
    if not issue:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Issue report not found"
        )
    
    # Retrieve audit history
    history = db.query(IssueHistory).filter(IssueHistory.issue_id == issue_id).all()
    
    # Populate dynamically calculated fields expected by frontend
    issue.reporter_name = issue.citizen.full_name
    
    assigned_sup = None
    if issue.assigned_supervisor:
        assigned_sup = {
            "id": issue.assigned_supervisor.id,
            "name": issue.assigned_supervisor.user.full_name,
            "employee_id": issue.assigned_supervisor.employee_id,
            "assigned_area": issue.assigned_supervisor.assigned_area,
            "latitude": issue.assigned_supervisor.latitude,
            "longitude": issue.assigned_supervisor.longitude,
            "phone": issue.assigned_supervisor.user.phone
        }
    
    return SingleReportResponse(
        issue=issue,
        report=issue,
        history=history,
        assigned_supervisor=assigned_sup
    )
