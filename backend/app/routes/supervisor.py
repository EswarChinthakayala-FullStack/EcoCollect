from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User, Supervisor
from app.models.issue import IssueReport, IssueHistory
from app.schemas.user import SupervisorDashboardResponse
from app.schemas.issue import IssueReportResponse
from app.utils.security import require_role
from datetime import datetime
from typing import List, Optional

router = APIRouter(prefix="/supervisor", tags=["Supervisor Operations"])

def get_current_supervisor(
    db: Session = Depends(get_db), 
    current_user: User = Depends(require_role(["supervisor"]))
) -> Supervisor:
    sup = db.query(Supervisor).filter(Supervisor.user_id == current_user.id).first()
    if not sup:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Supervisor profile not found"
        )
    return sup

@router.get("/dashboard", response_model=SupervisorDashboardResponse)
def get_dashboard(
    db: Session = Depends(get_db),
    supervisor: Supervisor = Depends(get_current_supervisor)
):
    assigned = db.query(IssueReport).filter(IssueReport.assigned_supervisor_id == supervisor.id).count()
    completed = db.query(IssueReport).filter(
        IssueReport.assigned_supervisor_id == supervisor.id,
        IssueReport.status == "Completed"
    ).count()
    pending = assigned - completed
    
    return SupervisorDashboardResponse(
        assigned_reports=assigned,
        completed_reports=completed,
        pending_reports=pending,
        performance_score=supervisor.performance_score
    )

@router.get("/issues", response_model=List[IssueReportResponse])
def get_assigned_issues(
    status_filter: Optional[str] = Query(None, alias="status"),
    db: Session = Depends(get_db),
    supervisor: Supervisor = Depends(get_current_supervisor)
):
    query = db.query(IssueReport).filter(IssueReport.assigned_supervisor_id == supervisor.id)
    if status_filter and status_filter != "All":
        query = query.filter(IssueReport.status == status_filter)
        
    issues = query.all()
    for issue in issues:
        issue.reporter_name = issue.citizen.full_name
    return issues

@router.put("/issues/{issue_id}/complete", response_model=IssueReportResponse)
def complete_issue(
    issue_id: int,
    completion_image_url: Optional[str] = None,
    remarks: Optional[str] = None,
    db: Session = Depends(get_db),
    supervisor: Supervisor = Depends(get_current_supervisor)
):
    issue = db.query(IssueReport).filter(
        IssueReport.id == issue_id,
        IssueReport.assigned_supervisor_id == supervisor.id
    ).first()
    
    if not issue:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Issue report not assigned to you or not found"
        )
        
    issue.status = "Completed"
    issue.completion_image_url = completion_image_url
    issue.resolved_at = datetime.utcnow()
    
    # Audit log
    history = IssueHistory(
        issue_id=issue.id,
        status="Completed",
        updated_by=supervisor.user_id,
        remarks=remarks or "Issue resolved by supervisor"
    )
    db.add(history)
    
    try:
        log = ActivityLog(
            action="Report Status Updated",
            type="report",
            details=f"Supervisor {supervisor.employee_id} resolved RPT-{issue.id} ({issue.category}). Remarks: {remarks or 'None'}"
        )
        db.add(log)
    except Exception as e:
        print(f"[DEBUG] Error adding activity log: {e}")
        
    # Notifications
    from app.models.notification import Notification
    
    # 1. Congratulate the reporting citizen
    if issue.citizen:
        citizen_notif = Notification(
            user_id=issue.citizen.id,
            title="Report Resolved Successfully",
            message=f"Great news! Your report for '{issue.category}' at '{issue.address or 'Address unspecified'}' has been resolved by supervisor '{supervisor.user.full_name}'. Thank you for clean-keeping our city!",
            read_status=0
        )
        db.add(citizen_notif)
        
    # 2. Inform all admins
    admins = db.query(User).filter(User.role == "admin").all()
    for admin in admins:
        admin_notif = Notification(
            user_id=admin.id,
            title="Task Resolved by Supervisor",
            message=f"Supervisor '{supervisor.user.full_name}' has completed and resolved task #ID-{issue.id} ({issue.category}) at '{issue.address or 'Address unspecified'}'.",
            read_status=0
        )
        db.add(admin_notif)

    db.commit()
    db.refresh(issue)
    issue.reporter_name = issue.citizen.full_name
    return issue

@router.put("/location")
def update_location(
    latitude: float,
    longitude: float,
    db: Session = Depends(get_db),
    supervisor: Supervisor = Depends(get_current_supervisor)
):
    from decimal import Decimal
    supervisor.latitude = Decimal(str(latitude))
    supervisor.longitude = Decimal(str(longitude))
    db.commit()
    return {"status": "success", "latitude": latitude, "longitude": longitude}

@router.get("/history")
def get_supervisor_history(
    db: Session = Depends(get_db),
    supervisor: Supervisor = Depends(get_current_supervisor)
):
    from app.models.issue import IssueHistory
    
    # Get histories updated by this supervisor user
    histories = db.query(IssueHistory).filter(
        IssueHistory.updated_by == supervisor.user_id
    ).order_by(IssueHistory.created_at.desc()).all()
    
    results = []
    for h in histories:
      results.append({
          "id": h.id,
          "issue_id": h.issue_id,
          "status": h.status,
          "remarks": h.remarks,
          "created_at": h.created_at.isoformat(),
          "category": h.issue.category,
          "title": h.issue.title,
          "address": h.issue.address,
      })
    return results

@router.get("/issues/nearby", response_model=List[IssueReportResponse])
def get_nearby_issues(
    db: Session = Depends(get_db),
    supervisor: Supervisor = Depends(get_current_supervisor)
):
    issues = db.query(IssueReport).all()
    for issue in issues:
        issue.reporter_name = issue.citizen.full_name
    return issues

