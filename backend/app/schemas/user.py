from pydantic import BaseModel, EmailStr, field_validator
from typing import Optional
from datetime import date, datetime

class UserBase(BaseModel):
    full_name: str
    email: EmailStr
    phone: Optional[str] = None
    dob: Optional[date] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    city: Optional[str] = None
    country: Optional[str] = None
    profile_image_url: Optional[str] = None

    @field_validator("dob", mode="before")
    @classmethod
    def validate_dob(cls, v):
        if v == "0000-00-00" or v == "":
            return None
        return v

class UserCreate(UserBase):
    password: str

class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    phone: Optional[str] = None
    dob: Optional[date] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    city: Optional[str] = None
    country: Optional[str] = None
    profile_image_url: Optional[str] = None

    @field_validator("dob", mode="before")
    @classmethod
    def validate_dob(cls, v):
        if v == "0000-00-00" or v == "":
            return None
        return v

class UserResponse(UserBase):
    id: int
    role: str
    eco_points: int
    is_active: Optional[bool] = True
    created_at: datetime
    updated_at: datetime
    employee_id: Optional[str] = None
    assigned_area: Optional[str] = None

    class Config:
        from_attributes = True

# Supervisor Specific
class SupervisorBase(BaseModel):
    employee_id: str
    assigned_area: Optional[str] = None
    department: Optional[str] = None

class SupervisorCreate(SupervisorBase):
    user_id: int
    performance_score: Optional[int] = 100

class SupervisorResponse(SupervisorBase):
    id: int
    user_id: int
    performance_score: int
    created_at: datetime
    user: UserResponse

    class Config:
        from_attributes = True

class SupervisorRegisterRequest(BaseModel):
    full_name: str
    employee_id: str
    email: EmailStr
    phone: Optional[str] = None
    assigned_area: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    coverage_radius: Optional[float] = None
    password: str

# Auth Request/Response
class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class SupervisorLoginRequest(BaseModel):
    employee_id: str
    password: str

class AuthResponse(BaseModel):
    message: str
    token: str
    user: UserResponse

# This matches what Supervisor Kotlin class expects
class SupervisorDetailsResponse(BaseModel):
    id: int
    employee_id: str
    user_id: int
    assigned_area: Optional[str] = None
    created_at: str # Needs to be formatted ISO datetime string
    name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    profile_picture: Optional[str] = None

class SupervisorAuthResponse(BaseModel):
    message: str
    token: Optional[str] = None
    supervisor: SupervisorDetailsResponse

class CitizenDashboardResponse(BaseModel):
    total_reports: int
    resolved_reports: int
    pending_reports: int
    eco_points: int

class SupervisorDashboardResponse(BaseModel):
    assigned_reports: int
    completed_reports: int
    pending_reports: int
    performance_score: int

class AdminDashboardResponse(BaseModel):
    total_citizens: int
    total_supervisors: int
    total_reports: int
    resolved_reports: int
    pending_reports: int
    cleanliness_score: int

class AdminSupervisorStats(BaseModel):
    id: int
    full_name: str
    email: EmailStr
    employee_id: str
    assigned_area: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    coverage_radius: Optional[float] = None
    assigned_reports: int
    resolved_reports: int
    distance_km: Optional[float] = None
    is_active: Optional[bool] = True

class ActivityLogResponse(BaseModel):
    id: int
    action: str
    type: str
    details: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True

class SupervisorZonePublic(BaseModel):
    id: int
    name: str
    email: Optional[str] = None
    phone: Optional[str] = None
    profile_image_url: Optional[str] = None
    assigned_area: Optional[str] = None
    department: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    coverage_radius: Optional[float] = None
    performance_score: int = 100

class SupervisorUpdateRequest(BaseModel):
    full_name: str
    employee_id: str
    email: EmailStr
    phone: Optional[str] = None
    assigned_area: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    coverage_radius: Optional[float] = None
    password: Optional[str] = None

