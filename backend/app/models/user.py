from sqlalchemy import Column, Integer, String, Text, Date, DateTime, ForeignKey, Numeric, func
from sqlalchemy.orm import relationship
from app.database import Base

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    role = Column(String(50), nullable=False, default="citizen") # 'citizen', 'supervisor', 'admin'
    full_name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False, index=True)
    phone = Column(String(20), nullable=True)
    dob = Column(Date, nullable=True)
    gender = Column(String(20), nullable=True)
    address = Column(Text, nullable=True)
    city = Column(String(100), nullable=True)
    country = Column(String(100), nullable=True)
    profile_image_url = Column(String(500), nullable=True)
    eco_points = Column(Integer, nullable=False, default=0)
    password_hash = Column(String(255), nullable=False)
    is_active = Column(Integer, nullable=False, default=1)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    supervisor_profile = relationship("Supervisor", back_populates="user", uselist=False, cascade="all, delete-orphan")
    reported_issues = relationship("IssueReport", back_populates="citizen", foreign_keys="[IssueReport.citizen_id]")
    history_updates = relationship("IssueHistory", back_populates="updater")
    notifications = relationship("Notification", back_populates="user", cascade="all, delete-orphan")

    @property
    def employee_id(self):
        return self.supervisor_profile.employee_id if self.supervisor_profile else None

    @property
    def assigned_area(self):
        return self.supervisor_profile.assigned_area if self.supervisor_profile else None


class Supervisor(Base):
    __tablename__ = "supervisors"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    employee_id = Column(String(100), unique=True, nullable=False, index=True)
    assigned_area = Column(String(255), nullable=True)
    latitude = Column(Numeric(10, 8), nullable=True)
    longitude = Column(Numeric(11, 8), nullable=True)
    coverage_radius = Column(Numeric(5, 2), nullable=True, default=10.00)
    department = Column(String(100), nullable=True)
    performance_score = Column(Integer, nullable=False, default=100)
    created_at = Column(DateTime, server_default=func.now())
    
    # Relationships
    user = relationship("User", back_populates="supervisor_profile")
    assigned_issues = relationship("IssueReport", back_populates="assigned_supervisor")
