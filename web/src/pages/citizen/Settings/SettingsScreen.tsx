import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, MapPin as MapPinIcon, Moon, Shield, ChevronRight, Trash2, ArrowLeft } from 'lucide-react';
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
} from '../../../components/ui/alert-dialog';

export const SettingsScreen: React.FC = () => {
  const navigate = useNavigate();
  const { theme, setTheme, resolvedTheme } = useTheme();
  
  // Load settings from localStorage or fallback to defaults
  const [pushEnabled, setPushEnabled] = useState(() => {
    const stored = localStorage.getItem('settings_push');
    return stored !== null ? JSON.parse(stored) : true;
  });

  const [locationEnabled, setLocationEnabled] = useState(() => {
    const stored = localStorage.getItem('settings_location');
    return stored !== null ? JSON.parse(stored) : true;
  });

  const darkModeEnabled = resolvedTheme === 'dark';
  const setDarkModeEnabled = (val: boolean) => {
    setTheme(val ? 'dark' : 'light');
  };

  const [isResetOpen, setIsResetOpen] = useState(false);

  // Sync settings with localStorage
  useEffect(() => {
    localStorage.setItem('settings_push', JSON.stringify(pushEnabled));
  }, [pushEnabled]);

  useEffect(() => {
    localStorage.setItem('settings_location', JSON.stringify(locationEnabled));
  }, [locationEnabled]);

  const handleResetData = () => {
    setIsResetOpen(true);
  };

  const confirmResetData = () => {
    localStorage.clear();
    setPushEnabled(true);
    setLocationEnabled(true);
    setDarkModeEnabled(false);
    setIsResetOpen(false);
    navigate('/');
  };

  return (
    <div className="w-full text-foreground relative animate-slide-up pb-12">
      {/* Page Header */}
      <div className="flex items-center gap-4 mb-6">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none animate-fade-in" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">App Settings</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Configure your notifications, app layout preferences, and theme options.</p>
        </div>
      </div>

      {/* Settings Grid Layout */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full items-start">
        
        {/* Card 1: Notifications Settings */}
        <div className="bg-card border border-border shadow-sm rounded-2xl p-5 flex flex-col gap-4">
          <h3 className="font-bold text-sm text-emerald-600 dark:text-emerald-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
            <Bell size={18} /> Notifications
          </h3>
          
          <div className="flex flex-col gap-4 divide-y divide-border/40">
            <div className="flex justify-between items-center pt-1">
              <div className="flex items-center">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center mr-3 text-emerald-600 dark:text-emerald-400">
                  <Bell size={18} />
                </div>
                <div className="flex flex-col">
                  <span className="font-semibold text-sm text-foreground">Push Notifications</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Report updates & alerts</span>
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input 
                  id="push-notifications-toggle"
                  type="checkbox" 
                  checked={pushEnabled} 
                  onChange={(e) => setPushEnabled(e.target.checked)} 
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-200 dark:bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 dark:after:border-slate-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
              </label>
            </div>

            <div className="flex justify-between items-center pt-4">
              <div className="flex items-center">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center mr-3 text-emerald-600 dark:text-emerald-400">
                  <MapPinIcon size={18} />
                </div>
                <div className="flex flex-col">
                  <span className="font-semibold text-sm text-foreground">Location Alerts</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Nearby collection trucks</span>
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input 
                  id="location-alerts-toggle"
                  type="checkbox" 
                  checked={locationEnabled} 
                  onChange={(e) => setLocationEnabled(e.target.checked)} 
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-200 dark:bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 dark:after:border-slate-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
              </label>
            </div>
          </div>
        </div>

        {/* Card 2: App Preferences Settings */}
        <div className="bg-card border border-border shadow-sm rounded-2xl p-5 flex flex-col gap-4">
          <h3 className="font-bold text-sm text-emerald-600 dark:text-emerald-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
            <Moon size={18} /> Theme & Style
          </h3>

          <div className="flex flex-col gap-4">
            <div className="flex justify-between items-center pt-1">
              <div className="flex items-center">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center mr-3 text-emerald-600 dark:text-emerald-400">
                  <Moon size={18} />
                </div>
                <div className="flex flex-col">
                  <span className="font-semibold text-sm text-foreground">Dark Mode</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Toggle light & dark themes</span>
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input 
                  id="dark-mode-toggle"
                  type="checkbox" 
                  checked={darkModeEnabled} 
                  onChange={(e) => setDarkModeEnabled(e.target.checked)} 
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-200 dark:bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 dark:after:border-slate-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
              </label>
            </div>
          </div>
        </div>

        {/* Card 3: Profile & Security */}
        <div className="bg-card border border-border shadow-sm rounded-2xl p-5 flex flex-col gap-4">
          <h3 className="font-bold text-sm text-emerald-600 dark:text-emerald-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
            <Shield size={18} /> Profile & Privacy
          </h3>
          
          <div 
            className="flex justify-between items-center p-3 rounded-xl bg-muted/50 dark:bg-muted/30 border border-border/60 cursor-pointer hover:bg-muted/80 dark:hover:bg-muted/50 transition-all duration-200" 
            onClick={() => navigate('/citizen/profile')}
          >
            <div className="flex items-center">
              <div className="w-9 h-9 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center mr-3 text-emerald-600 dark:text-emerald-400">
                <Shield size={18} />
              </div>
              <div className="flex flex-col">
                <span className="font-semibold text-sm text-foreground">My Profile & Privacy</span>
                <span className="text-xs text-muted-foreground mt-0.5">Edit personal info & address</span>
              </div>
            </div>
            <div className="flex items-center gap-1">
              <span className="text-xs text-muted-foreground font-semibold">Manage</span>
              <ChevronRight size={16} className="text-muted-foreground" />
            </div>
          </div>
        </div>

        {/* Card 4: Danger Zone */}
        <div className="bg-card border border-destructive/20 shadow-sm rounded-2xl p-5 flex flex-col gap-4">
          <h3 className="font-bold text-sm text-red-600 dark:text-red-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-destructive/10">
            <Trash2 size={18} /> System Actions
          </h3>
          
          <div 
            className="flex justify-between items-center p-3 rounded-xl bg-red-500/5 dark:bg-red-500/10 border border-red-500/20 dark:border-red-500/30 cursor-pointer hover:bg-red-500/10 dark:hover:bg-red-500/20 transition-all duration-200" 
            onClick={handleResetData}
          >
            <div className="flex items-center">
              <div className="w-9 h-9 rounded-xl bg-red-500/10 dark:bg-red-500/20 flex items-center justify-center mr-3 text-red-600 dark:text-red-400">
                <Trash2 size={18} />
              </div>
              <div className="flex flex-col">
                <span className="font-semibold text-sm text-red-600 dark:text-red-400">Reset App Data</span>
                <span className="text-xs text-red-400/80 dark:text-red-400/60 mt-0.5">Wipe all local storage</span>
              </div>
            </div>
            <ChevronRight size={16} className="text-red-400/80 dark:text-red-400/60" />
          </div>
        </div>

      </div>
      
      <div className="mt-8 text-center text-xs text-muted-foreground/60 select-none pb-8">
        WasteReporting Citizen App v2.4.1
      </div>

      <AlertDialog open={isResetOpen} onOpenChange={setIsResetOpen}>
        <AlertDialogContent className="bg-popover border border-border rounded-xl">
          <AlertDialogHeader>
            <AlertDialogTitle className="text-foreground font-bold">Reset App Data?</AlertDialogTitle>
            <AlertDialogDescription className="text-muted-foreground text-sm">
              Warning: Are you sure you want to reset all app data? This will clear your custom profile settings, report drafts, and preferences. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel className="border border-border text-foreground hover:bg-muted rounded-xl px-4 py-2 cursor-pointer font-semibold bg-transparent">
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction 
              onClick={confirmResetData} 
              className="bg-red-600 hover:bg-red-700 text-white rounded-xl px-4 py-2 cursor-pointer font-bold border-none"
            >
              Reset Data
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};


