from sqlalchemy import Column, Integer, String, Text, DateTime, func
from app.database import Base

class ActivityLog(Base):
    __tablename__ = "activity_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    action = Column(String(255), nullable=False)
    type = Column(String(50), nullable=False)  # 'login', 'create', 'update', 'delete', 'report', 'security'
    details = Column(Text, nullable=True)
    created_at = Column(DateTime, server_default=func.now())
