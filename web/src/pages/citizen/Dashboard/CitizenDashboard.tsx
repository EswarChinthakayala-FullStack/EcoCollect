import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { 
  ClipboardList, Hourglass, CheckCircle2, Award, 
  PlusCircle, ArrowRight, AlertTriangle 
} from 'lucide-react';

interface DashboardStats {
  total_reports: number;
  resolved_reports: number;
  pending_reports: number;
  eco_points: number;
}

interface Issue {
  id: number;
  category: string;
  address?: string;
  status: string;
  created_at: string;
  image_url?: string;
}

export const CitizenDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user, apiCall } = useAuth();

  const [stats, setStats] = useState<DashboardStats>({
    total_reports: 0,
    resolved_reports: 0,
    pending_reports: 0,
    eco_points: 0
  });
  const [issues, setIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const statsData = await apiCall<DashboardStats>('/citizen/dashboard');
        setStats(statsData);

        const issuesData = await apiCall<Issue[]>('/citizen/issues');
        // Sort descending by id to get newest first, limit to 4
        const sorted = issuesData.sort((a, b) => b.id - a.id).slice(0, 4);
        setIssues(sorted);
      } catch (err) {
        console.error('Failed to load citizen dashboard data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [apiCall]);

  const getBadgeClass = (status: string) => {
    const base = "inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-semibold uppercase tracking-wider w-fit border";
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return `${base} bg-amber-50 dark:bg-amber-950/20 text-amber-600 dark:text-amber-400 border-amber-200 dark:border-amber-900/30`;
      case 'in_progress':
        return `${base} bg-blue-50 dark:bg-blue-950/20 text-blue-600 dark:text-blue-400 border-blue-200 dark:border-blue-900/30`;
      case 'completed':
      case 'resolved':
        return `${base} bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/30`;
      default:
        return `${base} bg-slate-50 dark:bg-slate-950/20 text-slate-600 dark:text-slate-400 border-slate-200 dark:border-slate-800/40`;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 dark:border-slate-800 border-t-emerald-600 dark:border-t-emerald-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-8 w-full animate-slide-up pb-12 text-foreground">
      {/* Greetings Block */}
      <div className="relative overflow-hidden bg-gradient-to-r from-emerald-600 to-teal-600 dark:from-emerald-700 dark:to-teal-800 border-none text-white shadow-lg rounded-2xl p-6 md:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-5">
        <div className="absolute top-[-20%] right-[-10%] w-[300px] h-[300px] bg-white/10 rounded-full blur-[80px] pointer-events-none" />
        <div className="relative z-10 text-left">
          <h2 className="text-2xl md:text-3xl font-extrabold tracking-tight mb-2">Welcome back, {user?.full_name || 'Citizen'}!</h2>
          <p className="text-sm text-emerald-100/90 font-medium">Your reports keep our neighborhoods safe and clean.</p>
        </div>
        <div className="relative z-10 flex items-center gap-4 bg-white/10 backdrop-blur-sm p-4 rounded-xl border border-white/10 shrink-0 self-stretch md:self-auto justify-between md:justify-start">
          <div className="text-left">
            <span className="text-[10px] text-emerald-200 font-bold uppercase tracking-wider block mb-1">Eco Balance</span>
            <span className="text-xl md:text-2xl font-extrabold flex items-center gap-1.5 text-white">
              <Award className="text-amber-300 fill-amber-300" size={24} />
              {stats.eco_points} Points
            </span>
          </div>
        </div>
      </div>

      {/* Main Action Banner */}
      <div 
        className="bg-card text-card-foreground border border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 hover:border-emerald-500/50 transition-all duration-200 rounded-xl p-6 flex items-center gap-5 cursor-pointer group" 
        onClick={() => navigate('/citizen/report')}
      >
        <div className="w-14 h-14 rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 dark:bg-emerald-500/20 flex items-center justify-center shrink-0">
          <PlusCircle size={28} />
        </div>
        <div className="flex-1 text-left">
          <h3 className="text-lg font-bold text-foreground mb-1">Submit a Waste Report</h3>
          <p className="text-xs text-muted-foreground">Spotted an issue? Take a photo and let our team handle it.</p>
        </div>
        <ArrowRight className="text-muted-foreground transition-transform duration-150 ease-out group-hover:translate-x-1 group-hover:text-emerald-600 dark:group-hover:text-emerald-400" size={20} />
      </div>

      {/* Quick Stats Grid */}
      <div className="text-sm font-bold text-muted-foreground mb-[-16px] uppercase tracking-wider text-left">Overview Statistics</div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 rounded-xl p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-purple-500/10 text-purple-600 dark:text-purple-400 dark:bg-purple-500/20 shrink-0">
            <ClipboardList size={22} />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-foreground leading-none">{stats.total_reports}</div>
            <div className="text-xs text-muted-foreground mt-1 font-medium">Total Submissions</div>
          </div>
        </div>

        <div className="bg-card border border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 rounded-xl p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-amber-500/10 text-amber-600 dark:text-amber-400 dark:bg-amber-500/20 shrink-0">
            <Hourglass size={22} />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-foreground leading-none">{stats.pending_reports}</div>
            <div className="text-xs text-muted-foreground mt-1 font-medium">Pending Reviews</div>
          </div>
        </div>

        <div className="bg-card border border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 rounded-xl p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 dark:bg-emerald-500/20 shrink-0">
            <CheckCircle2 size={22} />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-foreground leading-none">{stats.resolved_reports}</div>
            <div className="text-xs text-muted-foreground mt-1 font-medium">Resolved Issues</div>
          </div>
        </div>
      </div>

      {/* Recent Issues Feed */}
      <div className="flex justify-between items-center mb-[-16px]">
        <span className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Recent Submissions</span>
        <button 
          className="bg-transparent border-none text-emerald-600 dark:text-emerald-400 text-sm font-semibold cursor-pointer transition-colors hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline" 
          onClick={() => navigate('/citizen/history')}
        >
          View All History
        </button>
      </div>

      {issues.length === 0 ? (
        <div className="bg-card border border-border shadow-sm rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4 w-full">
          <AlertTriangle size={32} className="text-muted-foreground" />
          <p className="text-sm text-muted-foreground">You have not submitted any reports yet.</p>
          <button 
            className="bg-emerald-600 hover:bg-emerald-700 text-white px-5 py-2.5 rounded-xl border-none font-semibold text-sm cursor-pointer transition-all duration-150 ease-out hover:-translate-y-0.5 dark:bg-emerald-600 dark:hover:bg-emerald-700" 
            onClick={() => navigate('/citizen/report')}
          >
            Report Now
          </button>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {issues.map((issue) => (
            <div 
              key={issue.id} 
              className="bg-card border border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 hover:border-emerald-500/50 transition-all duration-200 rounded-xl p-5 lg:p-6 flex justify-between items-center cursor-pointer" 
              onClick={() => navigate(`/citizen/history/${issue.id}`)}
            >
              <div className="flex flex-col gap-1.5 text-left">
                <div className="flex items-center gap-2.5">
                  <span className="text-sm font-bold text-foreground">{issue.category}</span>
                  <span className="text-[10px] font-bold text-muted-foreground bg-muted/60 border border-border px-1.5 py-0.5 rounded">#ID-{issue.id}</span>
                </div>
                <div className="text-sm text-muted-foreground">{issue.address || 'Location Specified'}</div>
                <div className="text-xs text-muted-foreground/80">{new Date(issue.created_at).toLocaleDateString(undefined, {
                  month: 'short',
                  day: 'numeric',
                  year: 'numeric'
                })}</div>
              </div>
              <div className="shrink-0">
                <span className={getBadgeClass(issue.status)}>
                  {issue.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
