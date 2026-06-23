import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { CheckCircle, Clock, MapPin, RefreshCw, AlertTriangle, Search, ArrowLeft } from 'lucide-react';

interface Issue {
  id: number;
  issue_id: number;
  title: string;
  category: string;
  address?: string;
  created_at: string;
  status: string;
  resolved_at?: string;
}

export const SupervisorHistory: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();

  const [resolvedIssues, setResolvedIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const data = await apiCall<Issue[]>('/supervisor/history');
      setResolvedIssues(data);
    } catch (err) {
      console.error('Failed to load history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [apiCall]);

  const filteredIssues = resolvedIssues.filter(issue => {
    const text = `${issue.issue_id} ${issue.category} ${issue.address || ''} ${issue.title || ''}`.toLowerCase();
    return text.includes(searchQuery.toLowerCase());
  });

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up pb-12 text-foreground text-left">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button 
            className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 hover:border-blue-600 dark:hover:border-blue-500 hover:bg-muted" 
            onClick={() => navigate(-1)}
            aria-label="Go back"
          >
            <ArrowLeft size={18} />
          </button>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider font-sans">Resolved History</h2>
        </div>
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 hover:border-blue-600 dark:hover:border-blue-500 hover:bg-muted disabled:opacity-50"
          onClick={fetchHistory}
          disabled={loading}
          aria-label="Refresh"
        >
          <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
        </button>
      </div>
      
      <div className="flex flex-col gap-4">
          
          <div className="bg-card rounded-2xl p-5 flex justify-between items-center shadow-sm border border-border mb-2">
            <div>
              <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">All-Time Resolved</div>
              <div className="flex items-baseline gap-1 mt-1">
                <span className="font-extrabold text-3xl text-emerald-600 dark:text-emerald-500">{resolvedIssues.length}</span>
                <span className="font-bold text-lg text-emerald-600 dark:text-emerald-500">resolved</span>
              </div>
            </div>
            <CheckCircle size={32} className="text-emerald-600 dark:text-emerald-400" />
          </div>

          {/* Search Toolbar */}
          <div className="bg-card border border-border shadow-sm rounded-xl p-4 mb-2 flex items-center gap-3">
            <div className="relative flex items-center w-full">
              <Search size={18} className="absolute left-4 text-muted-foreground" />
              <input 
                type="text" 
                className="w-full h-11 bg-muted/40 border border-border rounded-xl pl-12 pr-4 text-sm text-foreground outline-none transition-all focus:bg-muted/80 focus:border-blue-600 focus:ring-2 focus:ring-blue-600/10 placeholder:text-muted-foreground"
                placeholder="Search by ticket ID, category, title, or address..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {loading ? (
            <div className="flex justify-center items-center py-12">
              <div className="w-10 h-10 border-4 border-blue-600/20 border-t-blue-600 rounded-full animate-spin" />
            </div>
          ) : filteredIssues.length === 0 ? (
            <div className="bg-card rounded-2xl p-8 flex flex-col items-center justify-center text-center gap-3 border border-border shadow-sm">
              <AlertTriangle size={32} className="text-muted-foreground" />
              <p className="text-sm font-medium text-muted-foreground">
                {searchQuery ? "No matching resolved tasks found." : "No completed tasks in your registry."}
              </p>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {filteredIssues.map(issue => (
                <div 
                  key={issue.id} 
                  className="bg-card rounded-2xl p-4 flex gap-4 shadow-sm border border-border hover:border-blue-500 dark:hover:border-blue-500 hover:-translate-y-0.5 transition-all duration-200 cursor-pointer"
                  onClick={() => navigate(`/supervisor/report/${issue.issue_id}`)}
                >
                  <div className="w-16 h-16 rounded-xl bg-muted flex flex-col overflow-hidden shrink-0 border border-border justify-center items-center">
                    <CheckCircle size={28} className="text-emerald-600 dark:text-emerald-400" />
                  </div>
                  
                  <div className="flex-grow flex flex-col gap-1.5 min-w-0">
                    <div className="flex justify-between items-center gap-2">
                      <span className="font-bold text-foreground text-sm md:text-base truncate">{issue.title || `${issue.category} report`}</span>
                      <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-900/30 shrink-0">
                        {issue.status}
                      </span>
                    </div>
                    <div className="text-xs font-bold text-muted-foreground">#ID-{issue.issue_id} • {issue.category}</div>
                    
                    <div className="text-xs text-muted-foreground flex items-center truncate">
                      <MapPin size={12} className="mr-1 shrink-0" />
                      {issue.address || 'Address unspecified'}
                    </div>
                    
                    <div className="flex items-center mt-0.5">
                      <div className="text-xs text-muted-foreground flex items-center">
                        <Clock size={12} className="mr-1" />
                        {issue.resolved_at ? new Date(issue.resolved_at).toLocaleDateString() : new Date(issue.created_at).toLocaleDateString()}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
      </div>
    </div>
  );
};
