import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../../components/Button';
import { 
  ArrowLeft, Calendar, User, MapPin, 
  Info, CheckCircle2, ClipboardList, ImageIcon
} from 'lucide-react';
import { ImageGalleryCarousel } from '../../../components/ImageGalleryCarousel';

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
}

export const SupervisorReportDetails: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { apiCall, backendHost } = useAuth();
  
  const [issue, setIssue] = useState<Issue | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchIssueDetails = async () => {
      try {
        const data = await apiCall<{ issue: Issue }>(`/issues/${id}`);
        setIssue(data.issue);
      } catch (err) {
        console.error('Failed to load issue details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchIssueDetails();
  }, [id, apiCall]);

  const handleStartCleaning = () => {
    navigate(`/supervisor/report/${id}/status`);
  };

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
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 border-t-blue-600 animate-spin" />
      </div>
    );
  }

  if (!issue) {
    return (
      <div className="bg-card border border-border shadow-sm rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
        <h3 className="text-lg font-bold text-foreground">Report Not Found</h3>
        <Button theme="blue" onClick={() => navigate('/supervisor/dashboard')}>Back to Dashboard</Button>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up text-foreground">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 hover:border-blue-600 dark:hover:border-blue-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider font-sans">Task #ID-{issue.id} Details</h2>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1.2fr_1fr] gap-6">
        
        {/* Left Column: Media & Primary Details */}
        <div className="flex flex-col gap-5">
          {/* Main Photo */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-4 flex flex-col text-left">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground/80 tracking-wider mb-2">Reported Attachment</span>
            {issue.image_url ? (
              <ImageGalleryCarousel images={issue.image_url} />
            ) : (
              <div className="h-[200px] rounded-xl bg-muted/40 border border-dashed border-border flex flex-col items-center justify-center gap-2 text-muted-foreground text-sm font-medium">
                <ImageIcon size={32} className="text-muted-foreground/50" />
                <span>No Photo Uploaded</span>
              </div>
            )}
          </div>

          {/* Description */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md text-left">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground/80 tracking-wider">Description from Citizen</span>
            <div className="text-sm text-foreground leading-relaxed bg-muted/40 rounded-xl p-3.5 border border-border mt-2">
              {issue.description || 'No description provided.'}
            </div>
          </div>
        </div>

        {/* Right Column: Information list & Actions */}
        <div className="flex flex-col gap-5">
          
          {/* Properties Card */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col gap-4 text-left">
            <div className="flex gap-2.5 text-muted-foreground">
              <ClipboardList size={18} className="shrink-0 text-blue-500 dark:text-blue-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Report Category</span>
                <span className="text-sm font-semibold text-foreground">{issue.category}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <Calendar size={18} className="shrink-0 text-blue-500 dark:text-blue-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Reported On</span>
                <span className="text-sm font-semibold text-foreground">{new Date(issue.created_at).toLocaleString()}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <MapPin size={18} className="shrink-0 text-blue-500 dark:text-blue-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Location Address</span>
                <span className="text-sm font-semibold text-foreground">{issue.address}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <User size={18} className="shrink-0 text-blue-500 dark:text-blue-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground/75 tracking-wider mb-0.5">Citizen Reporter</span>
                <span className="text-sm font-semibold text-foreground">{issue.reporter_name || 'Anonymous Citizen'}</span>
              </div>
            </div>
            
            <div className="flex items-center gap-3 pt-2 border-t border-border">
              <span className="text-[11px] uppercase font-bold text-muted-foreground/80 tracking-wider">Current Status:</span>
              <span className={getBadgeClass(issue.status)}>
                {issue.status}
              </span>
            </div>
          </div>

          {/* Instructions banner or Completion logs */}
          {issue.status === 'Completed' ? (
            <div className="bg-emerald-500/10 border border-emerald-500/20 dark:border-emerald-500/10 rounded-xl p-4 flex flex-col gap-2.5 text-left">
              <h3 className="text-sm font-bold text-emerald-800 dark:text-emerald-400">Resolution Summary</h3>
              <div className="text-xs text-emerald-600 dark:text-emerald-500 font-medium">Completed Date: {issue.resolved_at ? new Date(issue.resolved_at).toLocaleString() : ''}</div>
              
              {issue.completion_image_url && (
                <div className="mt-2">
                  <ImageGalleryCarousel images={issue.completion_image_url} />
                </div>
              )}
            </div>
          ) : (
            <>
              <div className="flex gap-3 items-center bg-blue-500/10 border border-blue-500/20 text-blue-800 dark:text-blue-300 p-4 rounded-xl text-left">
                <Info size={24} className="shrink-0 text-blue-600 dark:text-blue-400" />
                <span className="text-xs font-semibold leading-relaxed">
                  Completing this task will submit resolution logs, award eco points, and update the citizen's audit timeline.
                </span>
              </div>

              <Button 
                fullWidth 
                theme="blue" 
                onClick={handleStartCleaning}
              >
                <div className="flex items-center justify-center gap-2">
                  <CheckCircle2 size={20} />
                  Complete Cleaning Task
                </div>
              </Button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
