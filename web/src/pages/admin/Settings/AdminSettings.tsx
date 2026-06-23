import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Bell, Moon, Globe, Trash2, Info, ShieldAlert, ChevronRight } from 'lucide-react';
import { useTheme } from 'next-themes';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "../../../components/ui/alert-dialog";

export const AdminSettings: React.FC = () => {
  const navigate = useNavigate();
  const { theme, setTheme, resolvedTheme } = useTheme();

  const [notifications, setNotifications] = useState(() => {
    return localStorage.getItem('adminNotifications') !== 'false';
  });
  const [isResetConfirmOpen, setIsResetConfirmOpen] = useState(false);
  
  const darkMode = resolvedTheme === 'dark';
  const setDarkMode = (val: boolean) => {
    setTheme(val ? 'dark' : 'light');
  };

  useEffect(() => {
    localStorage.setItem('adminNotifications', notifications.toString());
  }, [notifications]);

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
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">System Configuration</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Settings</h1>
        </div>
      </div>

      {/* Settings Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start w-full">
        
        {/* Left Column: General Preferences */}
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col gap-4">
          <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
            <Globe size={18} /> General Preferences
          </h3>
          <div className="flex flex-col gap-1.5 divide-y divide-border/40">
            <ToggleItem 
              icon={<Bell size={20} />}
              title="Push Notifications"
              subtitle="Receive alerts for high-priority reports"
              checked={notifications}
              onChange={() => setNotifications(!notifications)}
            />
            <ToggleItem 
              icon={<Moon size={20} />}
              title="Dark Mode"
              subtitle="Adjust screen brightness theme settings"
              checked={darkMode}
              onChange={() => setDarkMode(!darkMode)}
            />
            <ActionItem 
              icon={<Globe size={20} />}
              title="Language"
              subtitle="English (US)"
            />
          </div>
        </div>

        {/* Right Column: Data Management & About System */}
        <div className="flex flex-col gap-6">
          
          {/* Data Management */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)]">
            <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2 mb-4">
              <Trash2 size={18} /> Data Management
            </h3>
            <ActionItem 
              icon={<Trash2 size={20} className="text-red-500" />}
              title="Reset Application Data"
              subtitle="Clear all local storage data"
              onClick={() => setIsResetConfirmOpen(true)}
              danger
            />
          </div>

          {/* About System */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col gap-4">
            <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
              <Info size={18} /> About System
            </h3>
            <div className="flex flex-col gap-1.5 divide-y divide-border/40">
              <ActionItem 
                icon={<Info size={20} />}
                title="System Version"
                subtitle="WasteReporting Admin v2.4.1"
              />
              <ActionItem 
                icon={<ShieldAlert size={20} />}
                title="Privacy Policy"
                subtitle="Read our data handling guidelines"
              />
            </div>
          </div>
        </div>
      </div>

      {/* ShadCN Alert Dialog for Reset Application Data */}
      <AlertDialog open={isResetConfirmOpen} onOpenChange={setIsResetConfirmOpen}>
        <AlertDialogContent className="bg-popover text-popover-foreground border border-border rounded-xl shadow-lg">
          <AlertDialogHeader>
            <AlertDialogTitle className="text-foreground font-extrabold text-lg">Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription className="text-muted-foreground text-sm leading-relaxed">
              This action cannot be undone. This will permanently delete all locally stored supervisor accounts, citizen reports, history logs, and system configuration data.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter className="flex flex-col-reverse sm:flex-row gap-2 mt-4">
            <AlertDialogCancel className="cursor-pointer font-bold border border-border hover:bg-muted text-foreground">Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={() => {
                localStorage.clear();
                window.location.href = '/';
              }}
              className="bg-red-600 hover:bg-red-700 text-white font-bold cursor-pointer"
            >
              Reset Application Data
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

const ToggleItem: React.FC<{
  icon: React.ReactNode;
  title: string;
  subtitle: string;
  checked: boolean;
  onChange: () => void;
}> = ({ icon, title, subtitle, checked, onChange }) => (
  <div className="flex items-center gap-4 py-3 first:pt-1 last:pb-1">
    <div className="w-10 h-10 rounded-xl bg-muted flex items-center justify-center shrink-0 text-muted-foreground">
      {icon}
    </div>
    <div className="flex-1 flex flex-col">
      <span className="text-sm font-bold text-foreground">{title}</span>
      <span className="text-xs text-muted-foreground mt-0.5">{subtitle}</span>
    </div>
    <div 
      className={`w-11 h-6 rounded-full p-0.5 transition-colors duration-200 cursor-pointer relative shrink-0 ${checked ? 'bg-indigo-600' : 'bg-muted'}`}
      onClick={onChange}
    >
      <div className={`w-5 h-5 bg-white dark:bg-zinc-50 rounded-full shadow-sm transform transition-transform duration-200 ${checked ? 'translate-x-5' : 'translate-x-0'}`} />
    </div>
  </div>
);

const ActionItem: React.FC<{
  icon: React.ReactNode;
  title: string;
  subtitle: string;
  onClick?: () => void;
  danger?: boolean;
}> = ({ icon, title, subtitle, onClick, danger = false }) => (
  <div 
    className={`flex items-center gap-4 py-3 first:pt-1 last:pb-1 ${onClick ? 'cursor-pointer hover:bg-muted/40 rounded-lg px-2 -mx-2' : ''}`} 
    onClick={onClick}
  >
    <div className="w-10 h-10 rounded-xl bg-muted flex items-center justify-center shrink-0 text-muted-foreground">
      {icon}
    </div>
    <div className="flex-1 flex flex-col">
      <span className={`text-sm font-bold ${danger ? 'text-red-500 dark:text-red-400' : 'text-foreground'}`}>{title}</span>
      <span className="text-xs text-muted-foreground mt-0.5">{subtitle}</span>
    </div>
    {onClick && <ChevronRight size={18} className="text-muted-foreground shrink-0" />}
  </div>
);
