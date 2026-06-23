import sys
import os
import requests

# Add the backend root directory to Python system path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.database import SessionLocal
from app.models.user import User, Supervisor
from app.models.issue import IssueReport, IssueHistory
from app.models.notification import Notification
from app.models.otp import OTP
from app.utils.security import get_password_hash, verify_password

def run_tests():
    print("Initializing test database session...")
    db = SessionLocal()
    
    test_email = "eswarchinthakayala2004@gmail.com"
    
    # 1. Ensure test user exists
    user = db.query(User).filter(User.email == test_email).first()
    if not user:
        user = User(
            email=test_email,
            full_name="Eswar Chinthakayala",
            role="citizen",
            password_hash=get_password_hash("Citizen@123")
        )
        db.add(user)
        db.commit()
        db.refresh(user)
        print(f"Created test citizen user: {test_email}")
    else:
        user.password_hash = get_password_hash("Citizen@123")
        db.commit()
        print(f"Test citizen user verified and reset: {test_email}")

    api_url = "http://localhost:8000/api"
    
    # 2. Trigger forgot-password API
    print("Testing /auth/forgot-password endpoint...")
    res = requests.post(f"{api_url}/auth/forgot-password", json={"email": test_email})
    assert res.status_code == 200, f"Forgot password request failed: {res.text}"
    print("Forgot password request completed successfully (OTP email sent).")

    # 3. Retrieve code from DB
    otp_entry = db.query(OTP).filter(OTP.email == test_email).order_by(OTP.id.desc()).first()
    assert otp_entry is not None, "No OTP entry found in database"
    assert otp_entry.is_verified is False, "OTP should be initially unverified"
    otp_code = otp_entry.otp_code
    print(f"Successfully retrieved generated OTP from DB: {otp_code}")

    # 4. Trigger verify-otp API with wrong code
    print("Testing /auth/verify-otp with incorrect OTP...")
    res = requests.post(f"{api_url}/auth/verify-otp", json={"email": test_email, "otp": "000000"})
    assert res.status_code == 400, f"Verify OTP with wrong code should fail but returned {res.status_code}"
    print("Verify OTP failed correctly for invalid code.")

    # 5. Trigger verify-otp API with correct code
    print("Testing /auth/verify-otp with correct OTP...")
    res = requests.post(f"{api_url}/auth/verify-otp", json={"email": test_email, "otp": otp_code})
    assert res.status_code == 200, f"Verify OTP failed: {res.text}"
    
    db.rollback()
    db.refresh(otp_entry)
    assert otp_entry.is_verified is True, "OTP is_verified should be True in database after success"
    print("OTP verified successfully in database.")

    # 6. Trigger reset-password API
    new_pwd = "NewCitizenPassword@123"
    print("Testing /auth/reset-password...")
    res = requests.post(
        f"{api_url}/auth/reset-password", 
        json={"email": test_email, "otp": otp_code, "new_password": new_pwd}
    )
    assert res.status_code == 200, f"Reset password failed: {res.text}"
    
    db.rollback()
    db.refresh(user)
    assert verify_password(new_pwd, user.password_hash), "Password hash should match the newly reset password"
    print("Password reset verified successfully.")

    # 7. Clean up
    print("Cleaning up test database records...")
    db.query(OTP).filter(OTP.email == test_email).delete()
    user.password_hash = get_password_hash("Citizen@123")  # Reset to default for active login sessions
    db.commit()
    db.close()
    
    print("\n[SUCCESS] All backend OTP password reset flow tests passed successfully!")

if __name__ == "__main__":
    try:
        run_tests()
    except AssertionError as e:
        print(f"\n[FAILED] Test verification failed: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"\n[ERROR] Error executing verification script: {e}")
        sys.exit(1)
