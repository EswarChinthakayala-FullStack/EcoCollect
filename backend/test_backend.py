import sys
import os

# Add the app folder to Python system path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

print("Testing backend package imports...")
try:
    from app.config import settings
    from app.database import engine, Base
    from app.models.user import User, Supervisor
    from app.models.issue import IssueReport, IssueHistory
    from app.models.notification import Notification
    from app.schemas.user import UserResponse
    from app.schemas.issue import IssueReportResponse
    from app.utils.security import get_password_hash, verify_password
    from app.routes import auth, citizen, supervisor, admin, common
    
    print("All modules and packages imported successfully!")
    print("Configuration info:")
    print(f"  Project Name: {settings.PROJECT_NAME}")
    print(f"  Database Name: {settings.DB_NAME}")
    print("Verification script ran successfully.")
except Exception as e:
    print(f"Verification failed: {e}")
    sys.exit(1)
