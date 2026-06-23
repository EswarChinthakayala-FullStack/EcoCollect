import os
from dotenv import load_dotenv

# Load configurations from .env file if it exists
load_dotenv()

class Settings:
    PROJECT_NAME: str = "Smart Waste Management Backend"
    
    # MySQL configurations
    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: str = os.getenv("DB_PORT", "3306")
    DB_USER: str = os.getenv("DB_USER", "root")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "")
    DB_NAME: str = os.getenv("DB_NAME", "wastereporting")
    
    @property
    def DATABASE_URL(self) -> str:
        # mysql+pymysql://root:password@localhost:3306/wastereporting
        # Handles empty password correctly
        password_part = f":{self.DB_PASSWORD}" if self.DB_PASSWORD else ""
        return f"mysql+pymysql://{self.DB_USER}{password_part}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
    
    # JWT authentication settings
    SECRET_KEY: str = os.getenv("SECRET_KEY", "super_secret_key_change_me_in_production")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    # SMTP Mail settings
    SMTP_HOST: str = os.getenv("SMTP_HOST", "smtp.gmail.com")
    SMTP_PORT: int = int(os.getenv("SMTP_PORT", "587"))
    SMTP_USER: str = os.getenv("SMTP_USER", "app.services.v1@gmail.com")
    SMTP_PASSWORD: str = os.getenv("SMTP_PASSWORD", "ubzx xugw lowv bgcv")
    SMTP_FROM: str = os.getenv("SMTP_FROM", "app.services.v1@gmail.com")

settings = Settings()
