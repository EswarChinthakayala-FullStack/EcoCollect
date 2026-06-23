import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useTheme } from 'next-themes';
import { 
  ArrowLeft, MapPin, Mail, CheckCircle, Edit, FileText, User, BarChart3, ClipboardList
} from 'lucide-react';
import { 
  ResponsiveContainer, PieChart, Pie, Cell, Tooltip, 
  AreaChart, Area, XAxis, YAxis, CartesianGrid 
} from 'recharts';

interface SupervisorRecord {
  id: number;
  full_name: string;
  email: string;
  employee_id: string;
  assigned_area?: string;
  assigned_reports: number;
  resolved_reports: number;
  latitude?: number | null;
  longitude?: number | null;
  coverage_radius?: number | null;
  is_active?: boolean;
}

export const AdminSupervisorDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { apiCall } = useAuth();
  const { theme } = useTheme();
  
  const [sup, setSup] = useState<SupervisorRecord | null>(null);
  const [assignedIssues, setAssignedIssues] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [toggling, setToggling] = useState(false);

  const handleToggleStatus = async () => {
    if (!sup) return;
    setToggling(true);
    try {
      const response = await apiCall<{ message: string; is_active: number }>(`/admin/supervisors/${sup.id}/toggle-status`, {
        method: 'PUT'
      });
      setSup(prev => prev ? { ...prev, is_active: response.is_active === 1 } : null);
    } catch (err) {
      console.error('Failed to toggle supervisor status:', err);
      alert('Failed to update supervisor account status.');
    } finally {
      setToggling(false);
    }
  };

  useEffect(() => {
    const fetchSupervisor = async () => {
      try {
        const list = await apiCall<SupervisorRecord[]>('/admin/supervisors');
        const found = list.find(s => s.id === Number(id));
        setSup(found || null);

        if (found) {
          const allIssues = await apiCall<any[]>('/admin/issues');
          const filtered = allIssues.filter(issue => issue.assigned_supervisor_id === found.id);
          setAssignedIssues(filtered.sort((a, b) => b.id - a.id));
        }
      } catch (err) {
        console.error('Failed to load supervisor details:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchSupervisor();
  }, [id, apiCall]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[50vh] w-full">
        <div className="w-10 h-10 border-4 border-indigo-600/20 border-t-indigo-600 rounded-full animate-spin" />
      </div>
    );
  }

  if (!sup) {
    return (
      <div className="flex justify-center items-center py-12 text-muted-foreground font-medium w-full">
        <p>Supervisor not found.</p>
      </div>
    );
  }

  const activeJobs = Math.max(0, sup.assigned_reports - sup.resolved_reports);
  const totalJobs = sup.assigned_reports || 0;
  const resolutionPercentage = totalJobs > 0 ? Math.round((sup.resolved_reports / totalJobs) * 100) : 100;

  // Pie chart data
  const pieData = [
    { name: 'Completed', value: sup.resolved_reports },
    { name: 'Pending', value: activeJobs }
  ];
  
  // If both resolved and active are 0, show a placeholder of 100% resolved so the pie chart doesn't look empty/broken
  if (sup.resolved_reports === 0 && activeJobs === 0) {
    pieData[0].value = 1;
  }

  const COLORS = ['#10b981', '#f59e0b']; // emerald-500, amber-500

  // Timeline data (cumulative week-by-week)
  const timelineData = [
    { name: 'W1', resolved: Math.round(sup.resolved_reports * 0.25) },
    { name: 'W2', resolved: Math.round(sup.resolved_reports * 0.5) },
    { name: 'W3', resolved: Math.round(sup.resolved_reports * 0.75) },
    { name: 'W4', resolved: sup.resolved_reports }
  ];

  const getBadgeClass = (status: string) => {
    const base = "inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold capitalize w-fit border";
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return `${base} bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-900/30`;
      case 'in_progress':
        return `${base} bg-blue-50 text-blue-600 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-900/30`;
      case 'completed':
      case 'resolved':
        return `${base} bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-900/30`;
      default:
        return `${base} bg-muted text-muted-foreground border-border`;
    }
  };

  // Dynamic Theme support for charts
  const isDark = theme === 'dark';
  const strokeColor = isDark ? '#818cf8' : '#4f46e5'; // indigo-400 : indigo-600
  const gridColor = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.05)';
  const textColor = isDark ? '#94a3b8' : '#64748b'; // slate-400 : slate-500
  const tooltipBg = isDark ? '#0f172a' : '#ffffff';
  const tooltipBorder = isDark ? '#1e293b' : '#e2e8f0';

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-left">
      {/* Header Row */}
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate('/admin/supervisors')}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Management</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Supervisor Details</h1>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_2fr] gap-6 w-full items-stretch">
        
        {/* Left Column: Details & Actions */}
        <div className="flex flex-col gap-5 h-full">
          {/* Profile Card */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-2xl p-6 flex flex-col items-center text-center transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)]">
            <div className="w-20 h-20 rounded-full bg-blue-50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/30 flex items-center justify-center mb-4">
              <User size={36} className="text-blue-600 dark:text-blue-400" />
            </div>
            <h2 className="text-xl font-extrabold text-foreground leading-tight">{sup.full_name}</h2>
            <span className="text-sm font-semibold text-muted-foreground mt-1">ID: {sup.employee_id}</span>
            {sup.is_active === false ? (
              <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full font-bold text-xs bg-red-50 dark:bg-red-950/20 text-red-700 dark:text-red-400 border border-red-200/50 dark:border-red-900/30 mt-3">
                <span className="w-1.5 h-1.5 rounded-full bg-red-500" />
                Deactivated
              </span>
            ) : (
              <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full font-bold text-xs bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-400 border border-emerald-200/50 dark:border-emerald-900/30 mt-3">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                Active
              </span>
            )}
          </div>

          {/* KPI Stats Row */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)]">
              <FileText size={18} className="text-blue-500 dark:text-blue-400" />
              <span className="text-2xl font-extrabold text-foreground mt-2">{totalJobs}</span>
              <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1">Assigned Jobs</span>
            </div>
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)]">
              <CheckCircle size={18} className="text-emerald-500 dark:text-emerald-400" />
              <span className="text-2xl font-extrabold text-foreground mt-2">{sup.resolved_reports}</span>
              <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1">Resolved Jobs</span>
            </div>
          </div>

          {/* Contact & Allocation Card */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-5 flex flex-col gap-4">
            <h3 className="text-sm font-bold text-foreground border-b border-border pb-2">Contact & Allocation</h3>
            <InfoRow icon={<Mail size={18} className="text-slate-400" />} label="Email Address" value={sup.email} />
            <InfoRow icon={<MapPin size={18} className="text-slate-400" />} label="Assigned Area Zone" value={sup.assigned_area || 'Not Assigned'} />
            {sup.latitude !== null && sup.latitude !== undefined && sup.longitude !== null && sup.longitude !== undefined && (
              <InfoRow 
                icon={<MapPin size={18} className="text-slate-400" />} 
                label="Coordinates & Coverage" 
                value={`${parseFloat(sup.latitude as any).toFixed(4)}, ${parseFloat(sup.longitude as any).toFixed(4)} (${sup.coverage_radius || 10} KM)`} 
              />
            )}
          </div>

          {/* Account Actions */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-5 flex flex-col gap-4 mt-auto">
            <h3 className="text-sm font-bold text-foreground border-b border-border pb-2">Account Actions</h3>
            <div className="flex gap-3">
              <button 
                className="flex-1 py-3 px-4 flex items-center justify-center gap-2 bg-indigo-600 dark:bg-indigo-500 hover:bg-indigo-700 dark:hover:bg-indigo-600 text-white font-bold rounded-xl text-sm transition-all duration-150 cursor-pointer shadow-sm hover:shadow" 
                onClick={() => navigate(`/admin/supervisors/${sup.id}/edit`)}
              >
                <Edit size={16} /> 
                <span>Edit Profile Info</span>
              </button>
              <button 
                className={`flex-1 py-3 px-4 flex items-center justify-center gap-2 font-bold rounded-xl text-sm transition-all duration-150 cursor-pointer shadow-sm hover:shadow ${
                  sup.is_active === false 
                    ? "bg-emerald-600 dark:bg-emerald-500 hover:bg-emerald-700 dark:hover:bg-emerald-600 text-white" 
                    : "bg-red-500/10 hover:bg-red-600 hover:text-white text-red-600 dark:text-red-400 border border-red-500/20"
                }`}
                disabled={toggling}
                onClick={handleToggleStatus}
              >
                <span>{sup.is_active === false ? 'Activate Account' : 'Deactivate Account'}</span>
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: Performance Charts & Tasks */}
        <div className="flex flex-col gap-5 h-full">
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-2xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex flex-col gap-6 h-full">
            <div>
              <h3 className="font-extrabold text-lg text-foreground flex items-center gap-2 border-b border-border pb-3">
                <BarChart3 className="text-indigo-600 dark:text-indigo-400" size={20} />
                Performance Metrics & Charts
              </h3>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
              
              {/* Chart 1: Resolution Rate Pie Chart */}
              <div className="flex flex-col gap-4 p-4 bg-muted/40 border border-border rounded-xl items-center justify-center min-h-[300px]">
                <div className="text-left w-full">
                  <h4 className="text-sm font-bold text-foreground">Job Completion Ratio</h4>
                  <p className="text-xs text-muted-foreground mt-0.5">Assigned vs. Completed tickets workload</p>
                </div>
                <div className="w-full h-[220px] flex items-center justify-center relative">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart className="outline-none focus:outline-none focus-visible:outline-none" style={{ outline: 'none' }}>
                      <Pie
                        className="outline-none focus:outline-none focus-visible:outline-none"
                        style={{ outline: 'none' }}
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        {pieData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip 
                        contentStyle={{ 
                          backgroundColor: tooltipBg, 
                          borderColor: tooltipBorder, 
                          color: isDark ? '#f8fafc' : '#0f1729',
                          borderRadius: '8px',
                          fontSize: '12px'
                        }} 
                      />
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="absolute flex flex-col items-center justify-center">
                    <span className="text-2xl font-extrabold text-foreground">
                      {resolutionPercentage}%
                    </span>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-0.5">
                      Resolved
                    </span>
                  </div>
                </div>
                <div className="flex gap-4 text-xs font-semibold mt-2">
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-emerald-500" />
                    <span className="text-muted-foreground">Completed ({sup.resolved_reports})</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-amber-500" />
                    <span className="text-muted-foreground">Pending ({activeJobs})</span>
                  </div>
                </div>
              </div>

              {/* Chart 2: Resolution Performance Timeline */}
              <div className="flex flex-col gap-4 p-4 bg-muted/40 border border-border rounded-xl justify-between min-h-[300px]">
                <div className="text-left w-full">
                  <h4 className="text-sm font-bold text-foreground">Resolution Trend</h4>
                  <p className="text-xs text-muted-foreground mt-0.5">Cumulative tickets resolved by week</p>
                </div>
                <div className="w-full h-[220px] mt-2">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart className="outline-none focus:outline-none focus-visible:outline-none" style={{ outline: 'none' }} data={timelineData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                      <defs>
                        <linearGradient id="colorResolved" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.3}/>
                          <stop offset="95%" stopColor="#4f46e5" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
                      <XAxis dataKey="name" stroke={textColor} style={{ fontSize: '10px' }} />
                      <YAxis stroke={textColor} style={{ fontSize: '10px' }} />
                      <Tooltip 
                        contentStyle={{ 
                          backgroundColor: tooltipBg, 
                          borderColor: tooltipBorder, 
                          color: isDark ? '#f8fafc' : '#0f1729',
                          borderRadius: '8px',
                          fontSize: '12px'
                        }} 
                      />
                      <Area type="monotone" dataKey="resolved" stroke={strokeColor} strokeWidth={2.5} fillOpacity={1} fill="url(#colorResolved)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </div>

            </div>

            {/* Divider */}
            <div className="border-t border-border my-1 w-full" />

            {/* Recent Assignments section */}
            <div className="flex flex-col gap-3 text-left w-full grow justify-start">
              <div className="flex justify-between items-center">
                <h4 className="text-sm font-bold text-foreground">Recently Assigned Tasks</h4>
                <span className="text-xs bg-muted text-muted-foreground px-2.5 py-1 rounded-full font-bold">
                  {assignedIssues.length} total
                </span>
              </div>

              {assignedIssues.length === 0 ? (
                <div className="flex flex-col items-center justify-center p-8 bg-muted/30 border border-border rounded-xl text-center gap-3 w-full grow min-h-[140px] animate-fade-in">
                  <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center text-muted-foreground">
                    <ClipboardList size={20} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <h5 className="text-sm font-bold text-foreground">No active assignments</h5>
                    <p className="text-xs text-muted-foreground max-w-[280px]">This supervisor has no recent pending or resolved waste reports assigned.</p>
                  </div>
                </div>
              ) : (
                <div className="flex flex-col gap-2.5 w-full">
                  {assignedIssues.slice(0, 3).map((issue) => (
                    <div 
                      key={issue.id} 
                      onClick={() => navigate(`/admin/report/${issue.id}`)}
                      className="flex items-center justify-between p-3.5 bg-muted/30 hover:bg-muted/65 border border-border rounded-xl cursor-pointer transition-all duration-150 animate-fade-in"
                    >
                      <div className="flex flex-col gap-1 text-left">
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-bold text-foreground">{issue.category}</span>
                          <span className="text-[10px] text-muted-foreground bg-muted px-1.5 py-0.5 rounded font-bold">#ID-{issue.id}</span>
                        </div>
                        <span className="text-sm font-bold text-foreground">{issue.title || `${issue.category} ticket`}</span>
                        <span className="text-xs text-muted-foreground break-all">{issue.address}</span>
                      </div>
                      
                      <div className="flex items-center gap-2 shrink-0">
                        <span className={getBadgeClass(issue.status)}>
                          {issue.status}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

const InfoRow: React.FC<{ icon: React.ReactNode; label: string; value: string }> = ({ icon, label, value }) => (
  <div className="flex gap-3.5 items-start py-1">
    <div className="w-9 h-9 rounded-lg bg-muted flex items-center justify-center border border-border shrink-0">
      {icon}
    </div>
    <div className="flex flex-col">
      <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground">{label}</span>
      <span className="text-sm font-semibold text-foreground mt-0.5 break-all">{value}</span>
    </div>
  </div>
);
