import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { 
  Shield, Lock, Settings, History, 
  ChevronRight, LogOut, MapPin, Phone, Mail, Info
} from 'lucide-react';

export const SupervisorProfile: React.FC = () => {
  const navigate = useNavigate();
  const { supervisor, logout, apiCall } = useAuth();

  const [stats, setStats] = useState<{
    assigned_reports: number;
    completed_reports: number;
    pending_reports: number;
    performance_score: number;
  } | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const statsData = await apiCall('/supervisor/dashboard');
        setStats(statsData);
      } catch (err) {
        console.error('Failed to load supervisor profile stats:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, [apiCall]);

  const getDaysActive = (createdAtStr?: string) => {
    if (!createdAtStr) return 1;
    try {
      const created = new Date(createdAtStr);
      const now = new Date();
      const diffMs = now.getTime() - created.getTime();
      const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
      return diffDays <= 0 ? 1 : diffDays;
    } catch (e) {
      return 1;
    }
  };

  const daysActive = getDaysActive(supervisor?.created_at);

  const handleLogout = () => {
    logout();
    navigate('/supervisor/login');
  };

  return (
    <div className="flex flex-col min-h-screen text-left w-full pb-5">
      <div className="flex flex-col gap-6 w-full animate-slide-up py-5">
        
        {/* Profile Card Header */}
        <div className="flex flex-col items-center text-center p-6 bg-card text-foreground rounded-2xl relative overflow-hidden shadow-lg border border-border">
          {/* Decorative gradients */}
          <div className="absolute -right-10 -top-10 w-40 h-40 bg-blue-500/5 dark:bg-blue-500/10 rounded-full blur-2xl" />
          <div className="absolute -left-10 -bottom-10 w-40 h-40 bg-blue-600/5 dark:bg-blue-600/10 rounded-full blur-2xl" />
          
          <div className="relative w-20 h-20 rounded-full bg-muted flex items-center justify-center border border-border mb-4 z-10">
            <Shield size={40} className="text-blue-600 dark:text-blue-400" />
            <div className="absolute bottom-0.5 right-0.5 w-4 h-4 bg-emerald-500 rounded-full border-2 border-card" />
          </div>
          
          <span className="text-xl font-extrabold z-10 leading-tight">{supervisor?.name || 'Supervisor'}</span>
          <span className="text-xs text-muted-foreground font-semibold z-10 mt-1">{supervisor?.employee_id || 'SUP-125'} • {supervisor?.email || 'N/A'}</span>
          
          <div className="flex flex-wrap gap-2 justify-center items-center mt-4 z-10">
            <div className="inline-flex px-3.5 py-1.5 text-[10px] font-extrabold uppercase tracking-wider bg-blue-500/10 border border-blue-500/20 text-blue-600 dark:text-blue-400 rounded-full">
              Field Supervisor
            </div>
            {supervisor?.assigned_area && (
              <div className="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-semibold bg-muted text-muted-foreground rounded-full border border-border">
                <MapPin size={12} className="text-blue-600 dark:text-blue-400" />
                <span>Area: {supervisor.assigned_area}</span>
              </div>
            )}
          </div>
        </div>

        {/* 2-Column Options Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start w-full">
          
          {/* Left Column: Quick Actions & Statistics */}
          <div className="flex flex-col gap-6">
            {/* Quick Actions */}
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 flex flex-col gap-2">
              <h3 className="text-sm font-bold text-blue-600 dark:text-blue-400 border-b border-border/60 pb-2.5 flex items-center gap-2 mb-2">
                <Shield size={18} /> Quick Actions
              </h3>

              <div 
                className="flex items-center gap-3.5 p-2 rounded-xl transition-all duration-150 hover:bg-muted/40 cursor-pointer" 
                onClick={() => navigate('/supervisor/about')}
              >
                <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                  <Info size={20} className="text-blue-600 dark:text-blue-400" />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">About Application</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Read terms of service, platform version and licenses</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>
            </div>

            {/* Statistics */}
            <div className="flex flex-col gap-2">
              <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 flex flex-col">
                <h3 className="text-sm font-bold text-blue-600 dark:text-blue-400 border-b border-border/60 pb-2.5 flex items-center gap-2 mb-4">
                  <History size={18} /> Account Statistics
                </h3>
                <div className="grid grid-cols-3 gap-4">
                  <div className="bg-muted/40 border border-border shadow-sm rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:-translate-y-0.5 hover:shadow">
                    <span className="text-xl md:text-2xl font-extrabold text-foreground">{loading ? '...' : stats?.completed_reports.toLocaleString()}</span>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1 text-center leading-normal">Total Resolved</span>
                  </div>
                  <div className="bg-muted/40 border border-border shadow-sm rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:-translate-y-0.5 hover:shadow">
                    <span className="text-xl md:text-2xl font-extrabold text-foreground">{loading ? '...' : stats?.pending_reports.toLocaleString()}</span>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1 text-center leading-normal">Pending Tasks</span>
                  </div>
                  <div className="bg-muted/40 border border-border shadow-sm rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:-translate-y-0.5 hover:shadow">
                    <span className="text-xl md:text-2xl font-extrabold text-foreground">{daysActive.toLocaleString()}</span>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1 text-center leading-normal">Days Active</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Logout Button */}
            <button 
              className="w-full py-3.5 flex items-center justify-center gap-2 bg-red-500/10 hover:bg-red-600 hover:text-white text-red-600 dark:text-red-400 border border-red-500/20 font-bold rounded-xl text-sm transition-all duration-150 cursor-pointer shadow-sm hover:shadow"
              onClick={handleLogout}
            >
              <LogOut size={18} />
              <span>Logout Session</span>
            </button>
          </div>

          {/* Right Column: Supervisor Details */}
          <div className="flex flex-col gap-6">
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 flex flex-col gap-1.5 divide-y divide-border/40">
              <h3 className="text-sm font-bold text-blue-600 dark:text-blue-400 pb-2.5 flex items-center gap-2">
                <Shield size={18} /> Supervisor Information
              </h3>
              
              <div className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl">
                <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                  <MapPin size={20} className="text-blue-600 dark:text-blue-400" />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-[10px] uppercase font-bold text-muted-foreground/80 tracking-wider mb-0.5">Assigned Area</span>
                  <span className="text-sm font-semibold text-foreground">{supervisor?.assigned_area || 'Downtown District (Zone A)'}</span>
                </div>
              </div>
              
              <div className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl">
                <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                  <Phone size={20} className="text-blue-600 dark:text-blue-400" />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-[10px] uppercase font-bold text-muted-foreground/80 tracking-wider mb-0.5">Phone Contact</span>
                  <span className="text-sm font-semibold text-foreground">{supervisor?.phone || 'N/A'}</span>
                </div>
              </div>

              <div className="flex items-center gap-3.5 p-2 pt-3.5 rounded-xl">
                <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                  <Mail size={20} className="text-blue-600 dark:text-blue-400" />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-[10px] uppercase font-bold text-muted-foreground/80 tracking-wider mb-0.5">Email Address</span>
                  <span className="text-sm font-semibold text-foreground">{supervisor?.email || 'N/A'}</span>
                </div>
              </div>
            </div>

            {/* Read-only notice */}
            <div className="bg-blue-500/10 border border-blue-500/20 text-blue-800 dark:text-blue-300 rounded-xl p-4 flex items-start text-xs font-semibold leading-relaxed text-left">
              <Shield size={18} className="text-blue-500 dark:text-blue-400 mr-2.5 shrink-0" />
              <span>Your profile is managed by the Admin. Contact your administrator for any changes.</span>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};
