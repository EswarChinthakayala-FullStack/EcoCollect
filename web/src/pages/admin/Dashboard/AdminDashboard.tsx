import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { 
  BarChart3, Hourglass, CheckCircle, Users, Award, 
  AlertTriangle, ArrowRight, ClipboardList, UserPlus2
} from 'lucide-react';

interface DashboardStats {
  total_citizens: number;
  total_supervisors: number;
  total_reports: number;
  resolved_reports: number;
  pending_reports: number;
  cleanliness_score: number;
}

interface Issue {
  id: number;
  title: string;
  category: string;
  address?: string;
  status: string;
  created_at: string;
  reporter_name?: string;
}

export const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();

  const [stats, setStats] = useState<DashboardStats>({
    total_citizens: 0,
    total_supervisors: 0,
    total_reports: 0,
    resolved_reports: 0,
    pending_reports: 0,
    cleanliness_score: 100
  });
  const [issues, setIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        const statsData = await apiCall<DashboardStats>('/admin/dashboard');
        setStats(statsData);

        const issuesData = await apiCall<Issue[]>('/admin/issues');
        // Sort newest first, take 4
        const sorted = issuesData.sort((a, b) => b.id - a.id).slice(0, 4);
        setIssues(sorted);
      } catch (err) {
        console.error('Failed to load admin dashboard:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchAdminData();
  }, [apiCall]);

  const getBadgeClass = (status: string) => {
    const base = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold capitalize w-fit border";
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return `${base} bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-900/30`;
      case 'in_progress':
        return `${base} bg-blue-50 text-blue-600 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-900/30`;
      case 'completed':
      case 'resolved':
        return `${base} bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-900/30`;
      default:
        return `${base} bg-slate-50 text-slate-600 border-slate-200`;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 border-t-indigo-600 dark:border-t-indigo-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-8 w-full animate-slide-up">
      {/* City Overview Metrics Header */}
      <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex flex-col md:flex-row justify-between items-start md:items-center bg-gradient-to-br from-indigo-500/10 via-indigo-700/5 to-indigo-900/5 dark:from-indigo-950/10 dark:via-indigo-900/5 dark:to-indigo-950/5 border-indigo-500/20 dark:border-indigo-500/10 gap-5 md:gap-0">
        <div>
          <h2 className="text-2xl font-extrabold text-indigo-900 dark:text-indigo-300 mb-1.5">City Operations Center</h2>
          <p className="text-sm text-slate-500">Overviewing municipal waste tickets, engagement, and staff allocations</p>
        </div>
        <div className="flex flex-col items-start md:items-end md:text-right gap-1">
          <div className="text-[11px] uppercase font-bold text-slate-500 tracking-wider">Municipal Health</div>
          <div className="flex items-center gap-2 text-xl font-extrabold text-indigo-900 dark:text-indigo-100">
            <Award className="text-indigo-600 dark:text-indigo-400" size={24} />
            <span>{stats.cleanliness_score}% Cleanliness</span>
          </div>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-indigo-500/10 dark:bg-indigo-500/20">
            <BarChart3 size={22} className="text-indigo-600 dark:text-indigo-400" />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-slate-900 leading-none">{stats.total_reports}</div>
            <div className="text-xs text-slate-500 mt-1 font-medium">Total Tickets</div>
          </div>
        </div>

        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-amber-500/10 dark:bg-amber-500/20">
            <Hourglass size={22} className="text-amber-600 dark:text-amber-400" />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-slate-900 leading-none">{stats.pending_reports}</div>
            <div className="text-xs text-slate-500 mt-1 font-medium">Pending Review</div>
          </div>
        </div>

        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-emerald-500/10 dark:bg-emerald-500/20">
            <CheckCircle size={22} className="text-emerald-600 dark:text-emerald-400" />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-slate-900 leading-none">{stats.resolved_reports}</div>
            <div className="text-xs text-slate-500 mt-1 font-medium">Resolved Actions</div>
          </div>
        </div>

        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4">
          <div className="w-12 h-12 rounded-full flex items-center justify-center bg-blue-500/10 dark:bg-blue-500/20">
            <Users size={22} className="text-blue-600 dark:text-blue-400" />
          </div>
          <div className="flex flex-col text-left">
            <div className="text-2xl font-extrabold text-slate-900 leading-none">{stats.total_supervisors}</div>
            <div className="text-xs text-slate-500 mt-1 font-medium">Field Supervisors</div>
          </div>
        </div>
      </div>

      {/* Control Actions */}
      <div className="text-sm font-bold text-slate-400 mb-[-16px] uppercase tracking-wider text-left">Control Room Links</div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-5 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4 cursor-pointer hover:border-indigo-600 dark:hover:border-indigo-500" onClick={() => navigate('/admin/reports')}>
          <div className="w-11 h-11 rounded-lg flex items-center justify-center shrink-0 bg-indigo-500/10 dark:bg-indigo-500/20">
            <ClipboardList size={22} className="text-indigo-600 dark:text-indigo-400" />
          </div>
          <div className="flex flex-col text-left">
            <h4 className="text-sm font-bold text-slate-900">Manage Tickets</h4>
            <p className="text-xs text-slate-500 mt-0.5">Allocate supervisors & audits</p>
          </div>
        </div>

        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-5 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4 cursor-pointer hover:border-indigo-600 dark:hover:border-indigo-500" onClick={() => navigate('/admin/supervisors')}>
          <div className="w-11 h-11 rounded-lg flex items-center justify-center shrink-0 bg-blue-500/10 dark:bg-blue-500/20">
            <Users size={22} className="text-blue-600 dark:text-blue-400" />
          </div>
          <div className="flex flex-col text-left">
            <h4 className="text-sm font-bold text-slate-900">Supervisors</h4>
            <p className="text-xs text-slate-500 mt-0.5">View stats & scores</p>
          </div>
        </div>

        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-5 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex items-center gap-4 cursor-pointer hover:border-indigo-600 dark:hover:border-indigo-500" onClick={() => navigate('/admin/supervisors/add')}>
          <div className="w-11 h-11 rounded-lg flex items-center justify-center shrink-0 bg-emerald-500/10 dark:bg-emerald-500/20">
            <UserPlus2 size={22} className="text-emerald-600 dark:text-emerald-400" />
          </div>
          <div className="flex flex-col text-left">
            <h4 className="text-sm font-bold text-slate-900">Add Supervisor</h4>
            <p className="text-xs text-slate-500 mt-0.5">Create staff accounts</p>
          </div>
        </div>
      </div>

      {/* Recent Submissions */}
      <div className="flex justify-between items-center mb-[-16px]">
        <span className="text-sm font-bold text-slate-500 uppercase tracking-wider">Recent System Incidents</span>
        <button className="bg-transparent border-none text-indigo-600 dark:text-indigo-400 text-sm font-semibold cursor-pointer transition-colors hover:text-indigo-700 dark:hover:text-indigo-300 hover:underline" onClick={() => navigate('/admin/reports')}>
          View All Tickets
        </button>
      </div>

      {issues.length === 0 ? (
        <div className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
          <AlertTriangle size={36} className="text-slate-400" />
          <p className="text-sm text-slate-500">No waste report tickets have been filed in the system.</p>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {issues.map((issue) => (
            <div 
              key={issue.id} 
              className="bg-white/70 dark:bg-card/70 backdrop-blur-md border border-slate-200/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex justify-between items-center cursor-pointer border-l-4 border-l-indigo-600 dark:border-l-indigo-500 hover:border-l-indigo-700 dark:hover:border-l-indigo-400" 
              onClick={() => navigate('/admin/reports')}
            >
              <div className="flex flex-col gap-1.5 text-left">
                <div className="flex items-center gap-2.5">
                  <span className="text-sm font-bold text-slate-900">{issue.category}</span>
                  <span className="text-[11px] font-semibold text-slate-600 bg-slate-100 px-1.5 py-0.5 rounded">#ID-{issue.id}</span>
                </div>
                <div className="text-base font-bold text-slate-900">
                  {issue.title || `${issue.category} ticket`}
                </div>
                <div className="text-sm text-slate-600">{issue.address || 'Address unspecified'}</div>
                <div className="text-xs text-slate-500 mt-1">Reporter: {issue.reporter_name || 'Anonymous Citizen'}</div>
              </div>
              <div className="flex items-center gap-3">
                <span className={getBadgeClass(issue.status)}>
                  {issue.status}
                </span>
                <ArrowRight size={18} className="text-slate-300" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
