import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, Bell, MapPin, Moon, Shield } from 'lucide-react';
import { useTheme } from 'next-themes';

export const SupervisorSettings: React.FC = () => {
  const navigate = useNavigate();
  const { theme, setTheme, resolvedTheme } = useTheme();

  const [pushEnabled, setPushEnabled] = useState(true);
  const [locationEnabled, setLocationEnabled] = useState(true);
  
  const darkModeEnabled = resolvedTheme === 'dark';
  const setDarkModeEnabled = (val: boolean) => {
    setTheme(val ? 'dark' : 'light');
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up pb-12 text-foreground text-left">
      <div className="flex items-center gap-4 mb-2">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 hover:border-blue-600 dark:hover:border-blue-500 hover:bg-muted" 
          onClick={() => navigate(-1)} 
          aria-label="Go back"
        >
          <ChevronLeft size={24} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider font-sans">Settings</h2>
      </div>

      <div className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2 ml-2 mt-4">Notifications</div>
      <div className="bg-card rounded-2xl p-4 shadow-sm border border-border mb-6 flex flex-col">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center mr-4 text-muted-foreground">
              <Bell size={20} className="text-muted-foreground" />
            </div>
            <div className="flex flex-col">
              <span className="text-sm font-semibold text-foreground">Push Notifications</span>
              <span className="text-xs text-muted-foreground mt-0.5">Report updates & alerts</span>
            </div>
          </div>
          <label className="relative inline-flex items-center cursor-pointer">
            <input 
              type="checkbox" 
              checked={pushEnabled} 
              onChange={(e) => setPushEnabled(e.target.checked)} 
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-slate-300 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 dark:after:border-slate-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
          </label>
        </div>
        
        <div className="h-[1px] bg-border my-3" />
        
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center mr-4 text-muted-foreground">
              <MapPin size={20} className="text-muted-foreground" />
            </div>
            <div className="flex flex-col">
              <span className="text-sm font-semibold text-foreground">Location Alerts</span>
              <span className="text-xs text-muted-foreground mt-0.5">Nearby collection trucks</span>
            </div>
          </div>
          <label className="relative inline-flex items-center cursor-pointer">
            <input 
              type="checkbox" 
              checked={locationEnabled} 
              onChange={(e) => setLocationEnabled(e.target.checked)} 
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-slate-300 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 dark:after:border-slate-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
          </label>
        </div>
      </div>

      <div className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2 ml-2 mt-4">App Preferences</div>
      <div className="bg-card rounded-2xl p-4 shadow-sm border border-border mb-6 flex flex-col">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center mr-4 text-muted-foreground">
              <Moon size={20} className="text-muted-foreground" />
            </div>
            <div className="flex flex-col">
              <span className="text-sm font-semibold text-foreground">Dark Mode</span>
            </div>
          </div>
          <label className="relative inline-flex items-center cursor-pointer">
            <input 
              type="checkbox" 
              checked={darkModeEnabled} 
              onChange={(e) => setDarkModeEnabled(e.target.checked)} 
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-slate-300 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 dark:after:border-slate-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
          </label>
        </div>
        
        <div className="h-[1px] bg-border my-3" />
        
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center mr-4 text-muted-foreground">
              <Shield size={20} className="text-muted-foreground" />
            </div>
            <div className="flex flex-col">
              <span className="text-sm font-semibold text-foreground">Privacy Settings</span>
            </div>
          </div>
          <button className="bg-transparent border-none text-muted-foreground hover:text-foreground text-sm font-medium cursor-pointer transition-colors">
            Manage
          </button>
        </div>
      </div>

    </div>
  );
};
