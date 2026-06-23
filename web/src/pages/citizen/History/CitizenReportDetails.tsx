import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../../components/Button';
import { 
  ArrowLeft, Calendar, MapPin, ClipboardList, 
  ImageIcon, CheckCircle
} from 'lucide-react';
import { ImageGalleryCarousel } from '../../../components/ImageGalleryCarousel';
import { motion } from 'framer-motion';
import { DeliveryTracker } from '../../../components/ui/delivery-tracker';

interface Issue {
  id: number;
  category: string;
  title?: string;
  description?: string;
  address?: string;
  status: string;
  created_at: string;
  image_url?: string;
  resolved_at?: string;
  completion_image_url?: string;
  reporter_name?: string;
  latitude?: string | number;
  longitude?: string | number;
}

interface AuditHistory {
  id: number;
  status: string;
  remarks?: string;
  created_at: string;
}

const TimelineNode: React.FC<{ status: string }> = ({ status }) => {
  const isCompleted = status.toLowerCase() === 'completed' || status.toLowerCase() === 'resolved';
  const isInProgress = status.toLowerCase() === 'in_progress' || status.toLowerCase() === 'in progress';
  
  return (
    <div className="absolute -left-[36px] top-0.5 w-6 h-6 flex items-center justify-center bg-card rounded-full z-10 select-none">
      {isCompleted ? (
        <svg className="w-5 h-5 text-emerald-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-emerald-500/10 dark:fill-emerald-500/20"
          />
          <motion.path 
            d="M6 10 L9 13 L14 7" 
            stroke="currentColor" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round"
            initial={{ pathLength: 0 }}
            animate={{ pathLength: 1 }}
            transition={{ delay: 0.2, duration: 0.3 }}
          />
        </svg>
      ) : isInProgress ? (
        <svg className="w-5 h-5 text-blue-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-blue-500/10 dark:fill-blue-500/20"
          />
          <motion.circle
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="1"
            initial={{ opacity: 0.6, scale: 1 }}
            animate={{ opacity: 0, scale: 1.4 }}
            transition={{ repeat: Infinity, duration: 1.5, ease: "easeOut" }}
          />
          <motion.circle 
            cx="10" 
            cy="10" 
            r="3.5" 
            fill="currentColor"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.1, duration: 0.2 }}
          />
        </svg>
      ) : (
        <svg className="w-5 h-5 text-amber-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-amber-500/10 dark:fill-amber-500/20"
          />
          <motion.circle 
            cx="10" 
            cy="10" 
            r="3.5" 
            fill="currentColor"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.1, duration: 0.2 }}
          />
        </svg>
      )}
    </div>
  );
};

export const CitizenReportDetails: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { apiCall } = useAuth();
  
  const [issue, setIssue] = useState<Issue | null>(null);
  const [history, setHistory] = useState<AuditHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [assignedSupervisor, setAssignedSupervisor] = useState<any>(null);

  useEffect(() => {
    let active = true;
    const fetchIssueDetails = async () => {
      try {
        const response = await apiCall<{ issue: Issue; report?: Issue; history: AuditHistory[]; assigned_supervisor?: any }>(`/issues/${id}`);
        if (!active) return;
        const currentIssue = response.issue || response.report || null;
        setIssue(currentIssue);
        setHistory(response.history.sort((a, b) => b.id - a.id));
        setAssignedSupervisor(response.assigned_supervisor || null);
      } catch (err) {
        console.error('Failed to load issue details:', err);
      } finally {
        if (active) setLoading(false);
      }
    };

    fetchIssueDetails();
    
    // Poll location updates every 5 seconds for live tracking coordinates sync
    const intervalId = setInterval(fetchIssueDetails, 5000);

    return () => {
      active = false;
      clearInterval(intervalId);
    };
  }, [id, apiCall]);

  const getBadgeClass = (status: string) => {
    const base = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold capitalize w-fit border";
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return `${base} bg-amber-50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-400 border-amber-200 dark:border-amber-900/30`;
      case 'in_progress':
        return `${base} bg-blue-50 dark:bg-blue-950/20 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-900/30`;
      case 'completed':
      case 'resolved':
        return `${base} bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/30`;
      default:
        return `${base} bg-slate-50 dark:bg-slate-950/20 text-slate-700 dark:text-slate-400 border-slate-200 dark:border-slate-800`;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 border-t-emerald-600 dark:border-t-emerald-500 animate-spin" />
      </div>
    );
  }

  if (!issue) {
    return (
      <div className="bg-card border border-border shadow-sm rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
        <h3 className="text-lg font-bold text-foreground">Report Not Found</h3>
        <Button theme="green" onClick={() => navigate('/citizen/history')}>Back to History</Button>
      </div>
    );
  }

  // Animation variants
  const listContainerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.12
      }
    }
  };

  const listItemVariants = {
    hidden: { opacity: 0, x: -12 },
    show: { 
      opacity: 1, 
      x: 0, 
      transition: { 
        type: "spring" as const, 
        stiffness: 90, 
        damping: 15 
      } 
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up text-foreground">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-emerald-600 dark:hover:text-emerald-400 hover:border-emerald-600 dark:hover:border-emerald-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider font-sans">Ticket #ID-{issue.id} Details</h2>
      </div>

      {issue && (issue.status.toLowerCase() === 'in_progress' || issue.status.toLowerCase() === 'in progress' || issue.status.toLowerCase() === 'assigned') && assignedSupervisor && assignedSupervisor.latitude && assignedSupervisor.longitude && (
        <div className="w-full">
          <DeliveryTracker
            supervisorName={assignedSupervisor.name}
            employeeId={assignedSupervisor.employee_id}
            assignedArea={assignedSupervisor.assigned_area || ''}
            supervisorCoords={{ lng: parseFloat(assignedSupervisor.longitude), lat: parseFloat(assignedSupervisor.latitude) }}
            incidentCoords={{ lng: parseFloat(issue.longitude as string), lat: parseFloat(issue.latitude as string) }}
            reportId={issue.id}
            category={issue.category}
            reporterName={issue.reporter_name}
            address={issue.address}
            status={issue.status}
            createdAt={issue.created_at}
            description={issue.description}
          />
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-[1.2fr_1fr] gap-6">
        
        {/* Left Column: Media & Primary Details */}
        <div className="flex flex-col gap-5">
          {/* Photos Box */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-4 flex flex-col text-left">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground/80 tracking-wider mb-2">Cleanup Images</span>
            <div className={`grid gap-4 ${issue.status === 'Completed' ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1'}`}>
              <div>
                <span className="block text-[10px] uppercase font-bold text-muted-foreground/70 tracking-wider mb-2">My Submission (Before)</span>
                {issue.image_url ? (
                  <ImageGalleryCarousel images={issue.image_url} />
                ) : (
                  <div className="h-[200px] rounded-xl bg-muted/40 border border-dashed border-border flex flex-col items-center justify-center gap-2 text-muted-foreground text-sm font-medium">
                    <ImageIcon size={32} className="text-muted-foreground/50" />
                    <span>No Photo Uploaded</span>
                  </div>
                )}
              </div>

              {issue.status === 'Completed' && (
                <div>
                  <span className="block text-[10px] uppercase font-bold text-muted-foreground/70 tracking-wider mb-2">Resolved Cleanup (After)</span>
                  {issue.completion_image_url ? (
                    <ImageGalleryCarousel images={issue.completion_image_url} />
                  ) : (
                    <div className="h-[200px] rounded-xl bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900/30 flex flex-col items-center justify-center gap-2 text-emerald-850 dark:text-emerald-400 text-sm font-medium">
                      <CheckCircle size={32} className="text-emerald-600 dark:text-emerald-400" />
                      <span>Resolved (No Photo)</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Description */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md text-left">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground/80 tracking-wider">My Description remarks</span>
            <div className="text-sm text-foreground leading-relaxed bg-muted/40 rounded-xl p-3.5 border border-border mt-2">
              {issue.description || 'No detailed description remarks filed.'}
            </div>
          </div>
        </div>

        {/* Right Column: Information list & Resolution timeline */}
        <div className="flex flex-col gap-5 text-left">
          {/* Properties Card */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col gap-4">
            <div className="flex gap-2.5 text-muted-foreground">
              <ClipboardList size={18} className="shrink-0 text-emerald-600 dark:text-emerald-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Filing Category</span>
                <span className="text-sm font-semibold text-foreground">{issue.category}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <Calendar size={18} className="shrink-0 text-emerald-600 dark:text-emerald-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Filing Date</span>
                <span className="text-sm font-semibold text-foreground">{new Date(issue.created_at).toLocaleString()}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <MapPin size={18} className="shrink-0 text-emerald-600 dark:text-emerald-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Location Address</span>
                <span className="text-sm font-semibold text-foreground">{issue.address || 'Address unspecified'}</span>
              </div>
            </div>
            
            <div className="flex items-center gap-3 pt-2 border-t border-border">
              <span className="text-[11px] uppercase font-bold text-muted-foreground/80 tracking-wider">Current Status:</span>
              <span className={getBadgeClass(issue.status)}>
                {issue.status}
              </span>
            </div>
          </div>

          {/* Timeline Audit Logs */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col gap-4">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-2">Resolution Timeline</span>
            {history.length === 0 ? (
              <p className="text-xs text-muted-foreground italic">No history logs compiled.</p>
            ) : (
              <motion.div 
                variants={listContainerVariants}
                initial="hidden"
                animate="show"
                className="relative pl-6 ml-2 flex flex-col gap-6"
              >
                {/* Self-drawing vertical timeline track line */}
                <motion.div 
                  className="absolute left-0 top-2 bottom-2 w-0.5 bg-slate-200 dark:bg-slate-800 origin-top"
                  initial={{ scaleY: 0 }}
                  animate={{ scaleY: 1 }}
                  transition={{ duration: 0.8, ease: "easeInOut" }}
                />

                {history.map((h) => (
                  <motion.div 
                    key={h.id} 
                    variants={listItemVariants}
                    className="relative"
                  >
                    <TimelineNode status={h.status} />
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-foreground">{h.status}</span>
                        <span className="text-[10px] text-muted-foreground">{new Date(h.created_at).toLocaleDateString()}</span>
                      </div>
                      {h.remarks && <p className="text-xs text-muted-foreground leading-normal">{h.remarks}</p>}
                    </div>
                  </motion.div>
                ))}
              </motion.div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
