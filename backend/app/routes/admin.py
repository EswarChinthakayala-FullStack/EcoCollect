from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User, Supervisor
from app.models.activity_log import ActivityLog
from app.models.issue import IssueReport, IssueHistory
from app.schemas.user import AdminDashboardResponse, AdminSupervisorStats, ActivityLogResponse, SupervisorUpdateRequest
from app.schemas.issue import IssueReportResponse
from app.utils.security import require_role
from typing import List, Optional

router = APIRouter(prefix="/admin", tags=["Admin Operations"])

@router.get("/dashboard", response_model=AdminDashboardResponse)
def get_dashboard(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    total_citizens = db.query(User).filter(User.role == "citizen").count()
    total_supervisors = db.query(Supervisor).count()
    total_reports = db.query(IssueReport).count()
    resolved = db.query(IssueReport).filter(IssueReport.status == "Completed").count()
    pending = total_reports - resolved
    
    cleanliness = int((resolved / total_reports * 100)) if total_reports > 0 else 100
    
    return AdminDashboardResponse(
        total_citizens=total_citizens,
        total_supervisors=total_supervisors,
        total_reports=total_reports,
        resolved_reports=resolved,
        pending_reports=pending,
        cleanliness_score=cleanliness
    )

@router.get("/issues", response_model=List[IssueReportResponse])
def get_all_issues(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    issues = db.query(IssueReport).all()
    for issue in issues:
        issue.reporter_name = issue.citizen.full_name
    return issues

@router.put("/issues/{issue_id}/assign", response_model=IssueReportResponse)
def assign_issue(
    issue_id: int,
    supervisor_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    issue = db.query(IssueReport).filter(IssueReport.id == issue_id).first()
    if not issue:
        raise HTTPException(status_code=404, detail="Issue report not found")
        
    sup = db.query(Supervisor).filter(Supervisor.id == supervisor_id).first()
    if not sup:
        raise HTTPException(status_code=404, detail="Supervisor not found")
        
    issue.assigned_supervisor_id = supervisor_id
    issue.status = "In Progress"
    
    history = IssueHistory(
        issue_id=issue.id,
        status="In Progress",
        updated_by=current_user.id,
        remarks=f"Issue assigned to supervisor {sup.user.full_name}"
    )
    db.add(history)
    try:
        log = ActivityLog(
            action="Report Status Updated",
            type="report",
            details=f"Changed RPT-{issue.id} status to In Progress and allocated to supervisor {sup.user.full_name}"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
    # Notifications
    from app.models.notification import Notification
    
    # 1. Alert the assigned supervisor
    if sup.user:
        supervisor_notif = Notification(
            user_id=sup.user.id,
            title="New Task Assigned",
            message=f"You have been assigned a new task: '{issue.category}' report #ID-{issue.id} at '{issue.address or 'Address unspecified'}'.",
            read_status=0
        )
        db.add(supervisor_notif)
        
    # 2. Inform the citizen reporter
    if issue.citizen:
        citizen_notif = Notification(
            user_id=issue.citizen.id,
            title="Supervisor Dispatched",
            message=f"Supervisor '{sup.user.full_name}' has been assigned to resolve your report for '{issue.category}'.",
            read_status=0
        )
        db.add(citizen_notif)

    db.commit()
    db.refresh(issue)
    
    issue.reporter_name = issue.citizen.full_name
    return issue

@router.get("/supervisors", response_model=List[AdminSupervisorStats])
def get_supervisors(
    issue_id: Optional[int] = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    issue = None
    if issue_id:
        issue = db.query(IssueReport).filter(IssueReport.id == issue_id).first()

    sups = db.query(Supervisor).all()
    results = []
    for s in sups:
        assigned = db.query(IssueReport).filter(
            IssueReport.assigned_supervisor_id == s.id,
            IssueReport.status != "Completed"
        ).count()
        resolved = db.query(IssueReport).filter(
            IssueReport.assigned_supervisor_id == s.id,
            IssueReport.status == "Completed"
        ).count()
        
        distance = None
        if issue and s.latitude is not None and s.longitude is not None:
            from app.utils.geo import calculate_haversine_distance
            distance = calculate_haversine_distance(
                issue.latitude, issue.longitude,
                s.latitude, s.longitude
            )
        
        results.append(
            AdminSupervisorStats(
                id=s.id,
                full_name=s.user.full_name,
                email=s.user.email,
                employee_id=s.employee_id,
                assigned_area=s.assigned_area,
                latitude=float(s.latitude) if s.latitude is not None else None,
                longitude=float(s.longitude) if s.longitude is not None else None,
                coverage_radius=float(s.coverage_radius) if s.coverage_radius is not None else None,
                assigned_reports=assigned,
                resolved_reports=resolved,
                distance_km=distance,
                is_active=bool(s.user.is_active) if hasattr(s.user, 'is_active') else True
            )
        )
        
    if issue_id:
        results.sort(key=lambda x: x.distance_km if x.distance_km is not None else float('inf'))
        
    return results

@router.get("/heatmap")
def get_heatmap(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    issues = db.query(IssueReport).all()
    hotspots = []
    for issue in issues:
        weight = 0.8 if issue.status == "Pending" else 0.4
        hotspots.append({
            "lat": float(issue.latitude),
            "lng": float(issue.longitude),
            "weight": weight,
            "status": issue.status
        })
        
    recommendations = ["Analyze hotspots in pending zones", "Increase supervisor rotation in high-density areas"]
    if len(hotspots) > 5:
        recommendations.append("High report density detected near central area. Consider setting up a fixed waste collection hub.")
        
    return {
        "hotspots": hotspots,
        "ai_recommendations": recommendations
    }

@router.get("/analytics")
def get_analytics(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    issues = db.query(IssueReport).all()
    total = len(issues)
    completed = sum(1 for i in issues if i.status == "Completed")
    recycling_rate = float((completed / total * 100)) if total > 0 else 35.0
    
    breakdown = {}
    for issue in issues:
        breakdown[issue.category] = breakdown.get(issue.category, 0) + 1
        
    return {
        "total_volume": float(total),
        "volume_trend": "+5%",
        "recycling_rate": recycling_rate,
        "recycling_trend": "+2%",
        "weeks": [
            {"label": "W1", "general": 12.0, "recycling": 4.0},
            {"label": "W2", "general": 15.0, "recycling": 5.0},
            {"label": "W3", "general": 10.0, "recycling": 3.0},
            {"label": "W4", "general": 18.0, "recycling": 6.0}
        ],
        "total_waste_collected": total + 450,
        "avg_resolution_time_hrs": "12 hrs",
        "recycling_rate_percent": f"{int(recycling_rate)}%",
        "citizen_engagement_score": "A",
        "issues_per_month": [10, 15, 20, 18, 25, total],
        "category_breakdown": breakdown
    }

@router.get("/logs", response_model=List[ActivityLogResponse])
def get_activity_logs(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    logs = db.query(ActivityLog).order_by(ActivityLog.created_at.desc()).all()
    return logs

@router.put("/supervisors/{supervisor_id}/toggle-status")
def toggle_supervisor_status(
    supervisor_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    sup = db.query(Supervisor).filter(Supervisor.id == supervisor_id).first()
    if not sup:
        raise HTTPException(status_code=404, detail="Supervisor not found")
    
    user = sup.user
    if hasattr(user, 'is_active'):
        user.is_active = 0 if user.is_active == 1 else 1
    else:
        user.is_active = 0
    
    action = "Deactivated" if user.is_active == 0 else "Activated"
    try:
        log = ActivityLog(
            action=f"Supervisor {action}",
            type="update",
            details=f"Supervisor {sup.employee_id} ({user.full_name}) was {action.lower()} by admin {current_user.email}"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error logging toggle supervisor status: {e}")
        
    db.commit()
    return {"message": f"Supervisor {action.lower()} successfully", "is_active": user.is_active}

@router.put("/supervisors/{supervisor_id}")
def update_supervisor(
    supervisor_id: int,
    request: SupervisorUpdateRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(["admin"]))
):
    sup = db.query(Supervisor).filter(Supervisor.id == supervisor_id).first()
    if not sup:
        raise HTTPException(status_code=404, detail="Supervisor not found")
        
    user = sup.user
    if not user:
        raise HTTPException(status_code=404, detail="User associated with supervisor not found")
        
    # Check if email is already taken by another user
    existing_user = db.query(User).filter(User.email == request.email, User.id != user.id).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="A user with this Email already exists")
        
    # Check if employee_id is already taken by another supervisor
    existing_sup = db.query(Supervisor).filter(Supervisor.employee_id == request.employee_id, Supervisor.id != sup.id).first()
    if existing_sup:
        raise HTTPException(status_code=400, detail="A supervisor with this Employee ID already exists")
        
    user.full_name = request.full_name
    user.email = request.email
    user.phone = request.phone
    if request.password:
        from app.utils.security import get_password_hash
        user.password_hash = get_password_hash(request.password)
        
    sup.employee_id = request.employee_id
    sup.assigned_area = request.assigned_area
    sup.latitude = request.latitude
    sup.longitude = request.longitude
    sup.coverage_radius = request.coverage_radius
    
    try:
        log = ActivityLog(
            action="Supervisor Updated",
            type="update",
            details=f"Supervisor {sup.employee_id} ({user.full_name}) profile details were updated by admin {current_user.email}"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error logging update supervisor: {e}")
        
    db.commit()
    return {"message": "Supervisor updated successfully"}


