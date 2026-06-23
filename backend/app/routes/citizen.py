from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User, Supervisor
from app.models.issue import IssueReport, IssueHistory
from app.schemas.user import CitizenDashboardResponse
from app.schemas.issue import IssueReportCreate, IssueReportResponse
from app.utils.security import get_current_user, require_role
from app.utils.geo import calculate_haversine_distance
from typing import List

router = APIRouter(prefix="/citizen", tags=["Citizen Operations"])

@router.get("/dashboard", response_model=CitizenDashboardResponse)
def get_dashboard(
    db: Session = Depends(get_db), 
    current_user: User = Depends(require_role(["citizen"]))
):
    total = db.query(IssueReport).filter(IssueReport.citizen_id == current_user.id).count()
    resolved = db.query(IssueReport).filter(
        IssueReport.citizen_id == current_user.id,
        IssueReport.status == "Completed"
    ).count()
    pending = total - resolved
    
    return CitizenDashboardResponse(
        total_reports=total,
        resolved_reports=resolved,
        pending_reports=pending,
        eco_points=current_user.eco_points
    )

@router.post("/issues", response_model=IssueReportResponse)
def submit_issue(
    request: IssueReportCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["citizen"]))
):
    # Check if any supervisor covers this location
    supervisors = db.query(Supervisor).all()
    has_supervisor = False
    
    for s in supervisors:
        if s.latitude is not None and s.longitude is not None:
            dist = calculate_haversine_distance(
                request.latitude, request.longitude,
                s.latitude, s.longitude
            )
            radius = float(s.coverage_radius) if s.coverage_radius is not None else 10.0
            if dist <= radius:
                has_supervisor = True
                break
                
    if not has_supervisor:
        raise HTTPException(
            status_code=400,
            detail="Service unavailable. No municipal supervisor is assigned to cover this area."
        )

    issue = IssueReport(
        citizen_id=current_user.id,
        title=request.title,
        category=request.category,
        description=request.description,
        latitude=request.latitude,
        longitude=request.longitude,
        address=request.address,
        location=request.location,
        image_url=request.image_url,
        status="Pending"
    )
    db.add(issue)
    
    # Audit log
    history = IssueHistory(
        issue=issue,
        status="Pending",
        updated_by=current_user.id,
        remarks="Issue reported by citizen"
    )
    db.add(history)
    
    # Award eco points
    current_user.eco_points += 10
    
    # Notifications
    from app.models.notification import Notification
    
    # 1. Confirmation to the citizen
    citizen_notif = Notification(
        user_id=current_user.id,
        title="Report Submitted Successfully",
        message=f"Your report for '{issue.category}' at '{issue.address or 'Address unspecified'}' has been received. You earned 10 Eco-Points!",
        read_status=0
    )
    db.add(citizen_notif)
    
    # 2. Alert to all admins
    admins = db.query(User).filter(User.role == "admin").all()
    for admin in admins:
        admin_notif = Notification(
            user_id=admin.id,
            title="New Waste Report Filed",
            message=f"A new report regarding '{issue.category}' has been filed by {current_user.full_name} at '{issue.address or 'Address unspecified'}'.",
            read_status=0
        )
        db.add(admin_notif)
        
    db.commit()
    db.refresh(issue)
    
    issue.reporter_name = current_user.full_name
    return issue

@router.get("/issues", response_model=List[IssueReportResponse])
def get_my_issues(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["citizen"]))
):
    issues = db.query(IssueReport).filter(IssueReport.citizen_id == current_user.id).all()
    for issue in issues:
        issue.reporter_name = current_user.full_name
    return issues

@router.get("/issues/public", response_model=List[IssueReportResponse])
def get_public_issues(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["citizen"]))
):
    issues = db.query(IssueReport).all()
    for issue in issues:
        issue.reporter_name = issue.citizen.full_name
    return issues

@router.get("/supervisors/zones")
def get_supervisor_zones(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["citizen"]))
):
    from app.schemas.user import SupervisorZonePublic
    supervisors = db.query(Supervisor).all()
    zones = []
    for s in supervisors:
        user = s.user
        zones.append(SupervisorZonePublic(
            id=s.id,
            name=user.full_name if user else "Unknown",
            email=user.email if user else None,
            phone=user.phone if user else None,
            profile_image_url=user.profile_image_url if user else None,
            assigned_area=s.assigned_area,
            department=s.department,
            latitude=float(s.latitude) if s.latitude is not None else None,
            longitude=float(s.longitude) if s.longitude is not None else None,
            coverage_radius=float(s.coverage_radius) if s.coverage_radius is not None else None,
            performance_score=s.performance_score
        ))
    return zones
