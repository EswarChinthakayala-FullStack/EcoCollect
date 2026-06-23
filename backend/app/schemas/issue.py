from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from decimal import Decimal

class IssueReportBase(BaseModel):
    title: Optional[str] = None
    category: str
    description: Optional[str] = None
    latitude: Decimal
    longitude: Decimal
    address: Optional[str] = None
    location: Optional[str] = None
    image_url: Optional[str] = None
    completion_image_url: Optional[str] = None

class IssueReportCreate(IssueReportBase):
    pass

class IssueReportResponse(IssueReportBase):
    id: int
    citizen_id: int
    assigned_supervisor_id: Optional[int] = None
    status: str
    created_at: datetime
    updated_at: datetime
    resolved_at: Optional[datetime] = None
    reporter_name: Optional[str] = None

    class Config:
        from_attributes = True

class IssueHistoryBase(BaseModel):
    status: str
    remarks: Optional[str] = None

class IssueHistoryCreate(IssueHistoryBase):
    issue_id: int
    updated_by: int

class IssueHistoryResponse(IssueHistoryBase):
    id: int
    issue_id: int
    updated_by: int
    created_at: datetime

    class Config:
        from_attributes = True

class SupervisorDetailsResponse(BaseModel):
    id: int
    name: str
    employee_id: str
    assigned_area: Optional[str] = None
    latitude: Optional[Decimal] = None
    longitude: Optional[Decimal] = None
    phone: Optional[str] = None

    class Config:
        from_attributes = True

class SingleReportResponse(BaseModel):
    issue: Optional[IssueReportResponse] = None
    report: Optional[IssueReportResponse] = None
    history: List[IssueHistoryResponse] = []
    assigned_supervisor: Optional[SupervisorDetailsResponse] = None
