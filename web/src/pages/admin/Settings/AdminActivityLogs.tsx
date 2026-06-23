import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, History, LogIn, UserPlus, Edit, Trash2, ClipboardCheck, Lock } from 'lucide-react';
import { useAuth } from '../../../context/AuthContext';

interface ActivityLog {
  id: number;
  action: string;
  type: 'login' | 'create' | 'update' | 'delete' | 'report' | 'security';
  details: string;
  created_at: string;
}

export const AdminActivityLogs: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();
  const [logs, setLogs] = useState<ActivityLog[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const data = await apiCall<ActivityLog[]>('/admin/logs');
        setLogs(data);
      } catch (err) {
        console.error('Failed to load activity logs:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchLogs();
  }, [apiCall]);

  const formatTime = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHrs = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHrs / 24);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
      if (diffHrs < 24) return `${diffHrs} hour${diffHrs > 1 ? 's' : ''} ago`;
      if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
      return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch (e) {
      return dateStr;
    }
  };

  const getLogStyles = (type: ActivityLog['type']) => {
    switch (type) {
      case 'login': 
        return {
          icon: <LogIn size={18} className="text-blue-600 dark:text-blue-400" />,
          bg: 'bg-blue-50 dark:bg-blue-950/20 border border-blue-100/50 dark:border-blue-900/30'
        };
      case 'security': 
        return {
          icon: <Lock size={18} className="text-purple-600 dark:text-purple-400" />,
          bg: 'bg-purple-50 dark:bg-purple-950/20 border border-purple-100/50 dark:border-purple-900/30'
        };
      case 'create': 
        return {
          icon: <UserPlus size={18} className="text-emerald-600 dark:text-emerald-400" />,
          bg: 'bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-100/50 dark:border-emerald-900/30'
        };
      case 'update': 
        return {
          icon: <Edit size={18} className="text-amber-600 dark:text-amber-400" />,
          bg: 'bg-amber-50 dark:bg-amber-950/20 border border-amber-100/50 dark:border-amber-900/30'
        };
      case 'delete': 
        return {
          icon: <Trash2 size={18} className="text-red-600 dark:text-red-400" />,
          bg: 'bg-red-50 dark:bg-red-950/20 border border-red-100/50 dark:border-red-900/30'
        };
      case 'report': 
        return {
          icon: <ClipboardCheck size={18} className="text-sky-600 dark:text-sky-400" />,
          bg: 'bg-sky-50 dark:bg-sky-950/20 border border-sky-100/50 dark:border-sky-900/30'
        };
      default: 
        return {
          icon: <History size={18} className="text-slate-600" />,
          bg: 'bg-slate-50 dark:bg-slate-950/20 border border-slate-100/50'
        };
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-left">
      {/* Top Header */}
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">System Audit</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Activity Logs</h1>
        </div>
      </div>

      {/* Main Content */}
      <div className="w-full">
        {loading ? (
          <div className="flex justify-center items-center py-12 bg-card border border-border rounded-xl shadow-[0_8px_32px_0_rgba(15,23,42,0.04)]">
            <div className="w-10 h-10 border-4 border-indigo-600/20 border-t-indigo-600 dark:border-t-indigo-500 rounded-full animate-spin" />
          </div>
        ) : logs.length === 0 ? (
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-12 flex flex-col items-center justify-center text-center gap-3">
            <History size={36} className="text-muted-foreground/40" />
            <p className="text-sm font-medium text-muted-foreground">No activity logs recorded in the system.</p>
          </div>
        ) : (
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 md:p-8">
            <div className="flex items-center gap-2 text-foreground font-bold mb-6 border-b border-border pb-3">
              <History size={20} className="text-muted-foreground" />
              <span>Audit Trail Ledger</span>
            </div>

            <div className="flex flex-col">
              {logs.map((log, index) => {
                const styles = getLogStyles(log.type);
                return (
                  <div key={log.id} className="flex relative pb-8 last:pb-0 gap-4">
                    {/* Timeline vertical connection line */}
                    {index !== logs.length - 1 && (
                      <div className="absolute left-5 top-10 bottom-0 w-[2px] bg-border" />
                    )}
                    
                    {/* Icon Circle */}
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 z-10 ${styles.bg}`}>
                      {styles.icon}
                    </div>

                    {/* Details */}
                    <div className="flex-1 flex flex-col sm:flex-row sm:justify-between sm:items-start gap-1">
                      <div className="flex flex-col text-left">
                        <span className="font-bold text-foreground text-[15px]">{log.action}</span>
                        <span className="text-sm text-muted-foreground mt-1 leading-relaxed">{log.details}</span>
                      </div>
                      <span className="text-xs text-muted-foreground/80 font-semibold sm:mt-1 shrink-0">
                        {formatTime(log.created_at)}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
