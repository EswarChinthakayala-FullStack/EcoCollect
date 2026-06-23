import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Lock, Settings, ShieldCheck, History, ChevronRight, LogOut, LayoutDashboard, ClipboardList, Users, User } from 'lucide-react';
import { useAuth } from '../../../context/AuthContext';

export const AdminProfile: React.FC = () => {
  const navigate = useNavigate();
  const { user, apiCall, backendHost } = useAuth();
  const [actionsCount, setActionsCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const logs = await apiCall<any[]>('/admin/logs');
        setActionsCount(logs.length);
      } catch (err) {
        console.error('Failed to load admin profile stats:', err);
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

  const daysActive = getDaysActive(user?.created_at);

  return (
    <div className="flex flex-col min-h-screen text-left w-full pb-5">
      <div className="flex flex-col gap-6 w-full animate-slide-up py-5">
        
        {/* Profile Card Header */}
        <div className="flex flex-col items-center text-center p-6 bg-card text-foreground rounded-2xl relative overflow-hidden shadow-lg border border-border">
          {/* Decorative gradients */}
          <div className="absolute -right-10 -top-10 w-40 h-40 bg-indigo-500/5 dark:bg-indigo-500/10 rounded-full blur-2xl" />
          <div className="absolute -left-10 -bottom-10 w-40 h-40 bg-indigo-600/5 dark:bg-indigo-600/10 rounded-full blur-2xl" />
          
          <div className="relative w-20 h-20 rounded-full bg-muted flex items-center justify-center border border-border mb-4 z-10 overflow-hidden">
            {user?.profile_image_url ? (
              <img 
                src={user.profile_image_url.startsWith('data:') ? user.profile_image_url : (user.profile_image_url.startsWith('http') ? user.profile_image_url : `${backendHost}${user.profile_image_url.startsWith('/') ? '' : '/'}${user.profile_image_url}`)} 
                alt="Admin Avatar" 
                className="w-full h-full object-cover" 
              />
            ) : (
              <Shield size={40} className="text-indigo-600 dark:text-indigo-400" />
            )}
            <div className="absolute bottom-0.5 right-0.5 w-4 h-4 bg-emerald-500 rounded-full border-2 border-card" />
          </div>
          
          <span className="text-xl font-extrabold z-10 leading-tight">{user?.full_name || 'Chief Administrator'}</span>
          <span className="text-xs text-muted-foreground font-semibold z-10 mt-1">ADM-{user?.id || '001'} • {user?.email || 'admin@wastereporting.local'}</span>
          
          <div className="inline-flex mt-4 px-3.5 py-1.5 text-[10px] font-extrabold uppercase tracking-wider bg-indigo-500/10 border border-indigo-500/20 text-indigo-600 dark:text-indigo-400 rounded-full z-10">
            Super Administrator
          </div>
        </div>

        {/* 2-Column Options Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start w-full">
          
          {/* Left Column: Quick Actions & Statistics */}
          <div className="flex flex-col gap-6">
            {/* Quick Actions */}
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 flex flex-col gap-2">
              <h3 className="text-sm font-bold text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2 mb-2">
                <Shield size={18} /> Quick Actions
              </h3>
              
              <div 
                className="flex items-center gap-3.5 p-2 rounded-xl transition-all duration-150 hover:bg-muted/40 cursor-pointer" 
                onClick={() => navigate('/admin/profile/edit')}
              >
                <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                  <User size={20} className="text-indigo-600 dark:text-indigo-400" />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">Edit Profile Info</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Update name, avatar, and contact details</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>

              <div 
                className="flex items-center gap-3.5 p-2 rounded-xl transition-all duration-150 hover:bg-muted/40 cursor-pointer" 
                onClick={() => navigate('/admin/password')}
              >
                <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                  <Lock size={20} className="text-indigo-600 dark:text-indigo-400" />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">Change Password</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Update your security credentials</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>
            </div>

            {/* Statistics */}
            <div className="flex flex-col gap-2">
              <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 flex flex-col">
                <h3 className="text-sm font-bold text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2 mb-4">
                  <History size={18} /> Account Statistics
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-muted/40 border border-border shadow-sm rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:-translate-y-0.5 hover:shadow">
                    <span className="text-2xl font-extrabold text-foreground">{loading ? '...' : actionsCount.toLocaleString()}</span>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1">Actions Taken</span>
                  </div>
                  <div className="bg-muted/40 border border-border shadow-sm rounded-xl p-4 flex flex-col items-center justify-center transition-all duration-250 hover:-translate-y-0.5 hover:shadow">
                    <span className="text-2xl font-extrabold text-foreground">{daysActive.toLocaleString()}</span>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mt-1">Days Active</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Logout Button */}
            <button 
              className="w-full py-3.5 flex items-center justify-center gap-2 bg-red-500/10 hover:bg-red-600 hover:text-white text-red-600 dark:text-red-400 border border-red-500/20 font-bold rounded-xl text-sm transition-all duration-150 cursor-pointer shadow-sm hover:shadow"
              onClick={() => navigate('/admin/login')}
            >
              <LogOut size={18} />
              <span>Logout Session</span>
            </button>
          </div>

          {/* Right Column: System Options */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 flex flex-col gap-1.5 divide-y divide-border/40">
            <h3 className="text-sm font-bold text-indigo-600 dark:text-indigo-400 pb-2.5 flex items-center gap-2">
              <Settings size={18} /> System Settings
            </h3>
            
            <div 
              className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl transition-all duration-150 hover:bg-muted/40 cursor-pointer"
              onClick={() => navigate('/admin/settings')}
            >
              <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                <Settings size={20} className="text-indigo-600 dark:text-indigo-400" />
              </div>
              <div className="flex-1 flex flex-col text-left">
                <span className="text-sm font-bold text-foreground">System Configuration</span>
                <span className="text-xs text-muted-foreground mt-0.5">Preferences, Security, Data</span>
              </div>
              <ChevronRight size={18} className="text-muted-foreground" />
            </div>
            
            <div 
              className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl transition-all duration-150 hover:bg-muted/40 cursor-pointer"
              onClick={() => navigate('/admin/permissions')}
            >
              <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                <ShieldCheck size={20} className="text-purple-650 dark:text-purple-400" />
              </div>
              <div className="flex-1 flex flex-col text-left">
                <span className="text-sm font-bold text-foreground">Permissions</span>
                <span className="text-xs text-muted-foreground mt-0.5">Manage roles and access</span>
              </div>
              <ChevronRight size={18} className="text-muted-foreground" />
            </div>

            <div 
              className="flex items-center gap-3.5 p-2 pt-3.5 rounded-xl transition-all duration-150 hover:bg-muted/40 cursor-pointer"
              onClick={() => navigate('/admin/logs')}
            >
              <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0">
                <History size={20} className="text-amber-600 dark:text-amber-400" />
              </div>
              <div className="flex-1 flex flex-col text-left">
                <span className="text-sm font-bold text-foreground">Activity Logs</span>
                <span className="text-xs text-muted-foreground mt-0.5">Audit trails and login history</span>
              </div>
              <ChevronRight size={18} className="text-muted-foreground" />
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};
