import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ShieldCheck, Shield, Users, User } from 'lucide-react';

export const AdminPermissions: React.FC = () => {
  const navigate = useNavigate();

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
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Access Control</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">System Permissions</h1>
        </div>
      </div>

      {/* Main Content */}
      <div className="w-full flex flex-col gap-6">
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 md:p-8 flex flex-col items-center text-center">
          <ShieldCheck size={48} className="text-purple-500 dark:text-purple-400 mb-4 animate-pulse" />
          <h2 className="text-xl font-extrabold text-foreground leading-tight">Role & Access Management</h2>
          <p className="text-xs text-muted-foreground mt-1.5 leading-relaxed max-w-xl">
            Review the access levels and capabilities for all system roles. This is a read-only overview.
          </p>
        </div>

        {/* Roles 3-Column Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch w-full">
          {/* Administrator Role Card */}
          <div className="bg-card border-l-4 border-l-indigo-600 dark:border-l-indigo-500 border-y border-r border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col h-full">
            <div className="flex items-center gap-4 border-b border-border pb-3 mb-4 shrink-0">
              <div className="w-10 h-10 rounded-xl bg-indigo-500/10 flex items-center justify-center text-indigo-600 dark:text-indigo-400 shrink-0">
                <Shield size={20} />
              </div>
              <div>
                <h3 className="font-extrabold text-foreground text-lg">Administrator</h3>
                <span className="inline-block mt-0.5 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border border-indigo-500/20 rounded">
                  Full Access
                </span>
              </div>
            </div>
            <ul className="list-disc pl-5 text-sm text-muted-foreground flex flex-col gap-2 grow">
              <li>Create, edit, and delete Supervisor accounts</li>
              <li>View all system reports and statuses globally</li>
              <li>Access advanced analytics and system settings</li>
              <li>Modify system configuration and permissions</li>
              <li>View complete audit and activity logs</li>
            </ul>
          </div>

          {/* Supervisor Role Card */}
          <div className="bg-card border-l-4 border-l-blue-500 dark:border-l-blue-400 border-y border-r border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col h-full">
            <div className="flex items-center gap-4 border-b border-border pb-3 mb-4 shrink-0">
              <div className="w-10 h-10 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-600 dark:text-blue-400 shrink-0">
                <Users size={20} />
              </div>
              <div>
                <h3 className="font-extrabold text-foreground text-lg">Supervisor</h3>
                <span className="inline-block mt-0.5 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider bg-blue-500/10 text-blue-500 dark:text-blue-400 border border-blue-500/20 rounded">
                  Restricted
                </span>
              </div>
            </div>
            <ul className="list-disc pl-5 text-sm text-muted-foreground flex flex-col gap-2 grow">
              <li>View reports assigned to their specific zone</li>
              <li>Update report statuses (In Progress, Resolved)</li>
              <li>Add resolution images and notes to reports</li>
              <li>View personal performance history</li>
              <li>Cannot modify other users or system settings</li>
            </ul>
          </div>

          {/* Citizen Role Card */}
          <div className="bg-card border-l-4 border-l-emerald-500 dark:border-l-emerald-400 border-y border-r border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col h-full">
            <div className="flex items-center gap-4 border-b border-border pb-3 mb-4 shrink-0">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 flex items-center justify-center text-emerald-600 dark:text-emerald-400 shrink-0">
                <User size={20} />
              </div>
              <div>
                <h3 className="font-extrabold text-foreground text-lg">Citizen</h3>
                <span className="inline-block mt-0.5 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-500 dark:text-emerald-400 border border-emerald-500/20 rounded">
                  Basic
                </span>
              </div>
            </div>
            <ul className="list-disc pl-5 text-sm text-muted-foreground flex flex-col gap-2 grow">
              <li>Submit new waste reports with images and location</li>
              <li>Track status of personally submitted reports</li>
              <li>View public reports on the community map</li>
              <li>Manage personal profile details</li>
              <li>Cannot access internal management portals</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
