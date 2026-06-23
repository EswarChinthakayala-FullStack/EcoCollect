from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Numeric, func
from sqlalchemy.orm import relationship
from app.database import Base

class IssueReport(Base):
    __tablename__ = "issue_reports"
    
    id = Column(Integer, primary_key=True, index=True)
    citizen_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    assigned_supervisor_id = Column(Integer, ForeignKey("supervisors.id", ondelete="SET NULL"), nullable=True)
    title = Column(String(255), nullable=True)
    category = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    latitude = Column(Numeric(10, 8), nullable=False)
    longitude = Column(Numeric(11, 8), nullable=False)
    address = Column(String(500), nullable=True)
    location = Column(String(255), nullable=True)
    image_url = Column(String(500), nullable=True)
    completion_image_url = Column(String(500), nullable=True)
    status = Column(String(50), nullable=False, default="Pending") # 'Pending', 'In Progress', 'Completed'
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    resolved_at = Column(DateTime, nullable=True)
    
    # Relationships
    citizen = relationship("User", back_populates="reported_issues", foreign_keys=[citizen_id])
    assigned_supervisor = relationship("Supervisor", back_populates="assigned_issues")
    history = relationship("IssueHistory", back_populates="issue", cascade="all, delete-orphan")


class IssueHistory(Base):
    __tablename__ = "issue_history"
    
    id = Column(Integer, primary_key=True, index=True)
    issue_id = Column(Integer, ForeignKey("issue_reports.id", ondelete="CASCADE"), nullable=False)
    status = Column(String(50), nullable=False)
    updated_by = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    remarks = Column(Text, nullable=True)
    created_at = Column(DateTime, server_default=func.now())
    
    # Relationships
    issue = relationship("IssueReport", back_populates="history")
    updater = relationship("User", back_populates="history_updates")
