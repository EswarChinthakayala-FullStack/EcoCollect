import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Search, MapPin, AlertTriangle, RefreshCw, Eye } from 'lucide-react';

interface Issue {
  id: number;
  category: string;
  title?: string;
  address?: string;
  status: string;
  created_at: string;
  reporter_name?: string;
}

export const AdminReports: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();
  
  const [issues, setIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('All');

  const filters = ['All', 'Pending', 'In Progress', 'Completed'];

  const fetchIssues = async () => {
    setLoading(true);
    try {
      const data = await apiCall<Issue[]>('/admin/issues');
      setIssues(data.sort((a, b) => b.id - a.id));
    } catch (err) {
      console.error('Failed to load tickets:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIssues();
  }, [apiCall]);

  const filteredReports = issues.filter(issue => {
    // Role status match
    const statusMatch = selectedFilter === 'All' || issue.status === selectedFilter;
    
    // Search query match
    const text = `${issue.id} ${issue.category} ${issue.address} ${issue.title} ${issue.reporter_name}`.toLowerCase();
    const queryMatch = text.includes(searchQuery.toLowerCase());

    return statusMatch && queryMatch;
  });

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
        return `${base} bg-muted text-muted-foreground border-border`;
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up text-left">
      <div className="flex items-center gap-4">
        <div className="flex flex-col">
          <h2 className="text-xl font-bold text-indigo-600 dark:text-indigo-400">Ticket Operations Center</h2>
          <span className="text-xs text-muted-foreground mt-1">Allocating municipal staff, auditing statuses, and resolving tickets</span>
        </div>
        <div className="flex gap-2 items-center ml-auto">
          <div className="bg-indigo-50 dark:bg-indigo-950/20 border border-indigo-100 dark:border-indigo-900/30 text-indigo-700 dark:text-indigo-400 px-3 py-1.5 rounded-xl text-xs font-bold shrink-0">
            Total: {issues.length}
          </div>
          <button 
            className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted shrink-0" 
            onClick={fetchIssues}
            aria-label="Refresh tickets"
          >
            <RefreshCw size={18} />
          </button>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="bg-card/70 backdrop-blur-md border border-border/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-4 flex flex-col gap-4">
        <div className="relative flex items-center w-full">
          <Search size={18} className="absolute left-4 text-muted-foreground" />
          <input 
            type="text" 
            className="w-full h-12 bg-muted/50 border border-border rounded-lg pl-12 pr-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 dark:focus:border-indigo-500 focus:bg-card focus:ring-2 focus:ring-indigo-700/10 placeholder:text-muted-foreground"
            placeholder="Search by ticket ID, category, reporter, or address..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="flex gap-2 flex-wrap">
          {filters.map(f => (
            <button 
              key={f}
              className={`px-4 py-2 rounded-full text-xs font-semibold border cursor-pointer whitespace-nowrap shadow-sm transition-all ${
                selectedFilter === f 
                  ? 'bg-indigo-600 border-indigo-600 text-white hover:bg-indigo-700 dark:bg-indigo-500 dark:border-indigo-500 dark:hover:bg-indigo-600' 
                  : 'bg-card border-border text-muted-foreground hover:bg-muted/50'
              }`}
              onClick={() => setSelectedFilter(f)}
            >
              {f}
            </button>
          ))}
        </div>
      </div>

      {/* Issues List */}
      {loading ? (
        <div className="flex items-center justify-center min-h-[400px] w-full">
          <div className="w-12 h-12 rounded-full border-4 border-border border-t-indigo-600 dark:border-t-indigo-500 animate-spin" />
        </div>
      ) : filteredReports.length === 0 ? (
        <div className="bg-card/70 backdrop-blur-md border border-border/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
          <AlertTriangle size={36} className="text-muted-foreground" />
          <p className="text-sm text-muted-foreground">No report tickets found matching parameters.</p>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {filteredReports.map(report => (
            <div 
              key={report.id} 
              className="bg-card/70 backdrop-blur-md border border-border/60 shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex justify-between items-center cursor-pointer border-l-4 border-l-indigo-700 dark:border-l-indigo-500"
              onClick={() => navigate(`/admin/report/${report.id}`)}
            >
              <div className="flex flex-col gap-1.5 text-left">
                <div className="flex items-center gap-2.5">
                  <span className="text-sm font-bold text-indigo-600 dark:text-indigo-400">{report.category}</span>
                  <span className="text-[11px] font-semibold text-muted-foreground bg-muted px-1.5 py-0.5 rounded">#ID-{report.id}</span>
                </div>
                <div className="text-base font-bold text-foreground">
                  {report.title || `${report.category} ticket`}
                </div>
                <div className="text-sm text-muted-foreground flex items-center">
                  <MapPin size={13} className="mr-1 text-muted-foreground/80" />
                  {report.address || 'Address unspecified'}
                </div>
                <div className="text-xs text-muted-foreground/75 mt-1">Reporter: {report.reporter_name || 'Anonymous Citizen'} • filed {new Date(report.created_at).toLocaleDateString()}</div>
              </div>
              
              <div className="flex items-center gap-3">
                <span className={getBadgeClass(report.status)}>
                  {report.status}
                </span>
                <button 
                  className="bg-transparent border-none text-indigo-600 dark:text-indigo-400 cursor-pointer p-1.5 rounded-full hover:bg-muted transition-colors flex items-center justify-center"
                  onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/admin/report/${report.id}`);
                  }}
                  aria-label="View ticket details"
                >
                  <Eye size={18} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
