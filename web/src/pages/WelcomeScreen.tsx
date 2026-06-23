import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, User, Users, ArrowRight } from 'lucide-react';
import appLogo from '../assets/logo.svg';

export const WelcomeScreen: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col lg:flex-row min-h-screen w-full relative bg-slate-50 dark:bg-slate-950 font-sans overflow-hidden animate-fade-in transition-colors duration-300">
      {/* Left Panel - Branding & Slogan */}
      <div className="flex-[1.2] bg-gradient-to-br from-emerald-600 via-static-emerald-700 to-emerald-900 flex items-center justify-center p-8 lg:p-16 relative text-white text-center">
        {/* Decorative circle overlay */}
        <div className="absolute top-[-10%] left-[-10%] w-[60%] h-[60%] rounded-full bg-white/5 blur-3xl pointer-events-none" />

        <div className="flex flex-col items-center max-w-md z-10">
          <div className="w-[110px] h-[110px] bg-white rounded-2xl flex items-center justify-center shadow-xl p-3 mb-6 transition-all hover:scale-[1.03]">
            <img src={appLogo} alt="WasteReporting Logo" className="w-full h-full object-contain" />
          </div>
          <h1 className="font-extrabold text-4xl lg:text-5xl tracking-tight mb-2 text-white">EcoCollect</h1>
          <p className="text-static-emerald-100 text-lg lg:text-xl font-medium mb-6">Smart Waste Management System</p>
          <div className="w-14 h-1 bg-white/20 rounded-full mb-6" />
          <p className="text-sm lg:text-base leading-relaxed text-static-emerald-50/90">
            Report issues, coordinate resolutions, and track civic cleanliness in real-time. Join us in making our community cleaner and greener.
          </p>
        </div>
      </div>

      {/* Right Panel - Portal Selectors */}
      <div className="flex-1 bg-white dark:bg-card flex items-center justify-center p-8 lg:p-16 border-l border-slate-100 dark:border-border/40 shadow-2xl transition-colors duration-300">
        <div className="w-full max-w-sm flex flex-col text-left">
          <div className="mb-10">
            <h2 className="font-extrabold text-3xl text-slate-900 dark:text-foreground mb-2">Welcome Portal</h2>
            <p className="text-sm text-slate-500 dark:text-muted-foreground">Please select your access role to continue</p>
          </div>
          
          <div className="flex flex-col gap-4 mb-10">
            {/* Citizen Card */}
            <div 
              className="flex items-center gap-4 p-5 bg-slate-50 dark:bg-muted/20 border border-slate-200/80 dark:border-border/40 rounded-xl cursor-pointer transition-all hover:-translate-y-0.5 hover:bg-white dark:hover:bg-muted/40 hover:shadow-md hover:border-emerald-500 dark:hover:border-emerald-500 group" 
              onClick={() => navigate('/citizen/login')}
            >
              <div className="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 group-hover:scale-105 transition-transform">
                <User size={22} />
              </div>
              <div className="flex-1">
                <div className="font-bold text-sm text-slate-900 dark:text-foreground">Citizen Portal</div>
                <div className="text-xs text-slate-500 dark:text-muted-foreground mt-0.5">Report waste, track issues & earn points</div>
              </div>
              <ArrowRight className="text-slate-300 dark:text-muted-foreground/60 group-hover:translate-x-1 group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition-all" size={20} />
            </div>

            {/* Supervisor Card */}
            <div 
              className="flex items-center gap-4 p-5 bg-slate-50 dark:bg-muted/20 border border-slate-200/80 dark:border-border/40 rounded-xl cursor-pointer transition-all hover:-translate-y-0.5 hover:bg-white dark:hover:bg-muted/40 hover:shadow-md hover:border-blue-500 dark:hover:border-blue-500 group" 
              onClick={() => navigate('/supervisor/login')}
            >
              <div className="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 group-hover:scale-105 transition-transform">
                <Users size={22} />
              </div>
              <div className="flex-1">
                <div className="font-bold text-sm text-slate-900 dark:text-foreground">Supervisor Portal</div>
                <div className="text-xs text-slate-500 dark:text-muted-foreground mt-0.5">Claim assigned issues & report solutions</div>
              </div>
              <ArrowRight className="text-slate-300 dark:text-muted-foreground/60 group-hover:translate-x-1 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-all" size={20} />
            </div>

            {/* Admin Card */}
            <div 
              className="flex items-center gap-4 p-5 bg-slate-50 dark:bg-muted/20 border border-slate-200/80 dark:border-border/40 rounded-xl cursor-pointer transition-all hover:-translate-y-0.5 hover:bg-white dark:hover:bg-muted/40 hover:shadow-md hover:border-indigo-500 dark:hover:border-indigo-500 group" 
              onClick={() => navigate('/admin/login')}
            >
              <div className="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 group-hover:scale-105 transition-transform">
                <Shield size={22} />
              </div>
              <div className="flex-1">
                <div className="font-bold text-sm text-slate-900 dark:text-foreground">Admin Control Panel</div>
                <div className="text-xs text-slate-500 dark:text-muted-foreground mt-0.5">Monitor analytics, dispatch & manage supervisors</div>
              </div>
              <ArrowRight className="text-slate-300 dark:text-muted-foreground/60 group-hover:translate-x-1 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-all" size={20} />
            </div>
          </div>

          <div className="border-t border-slate-100 dark:border-border/40 pt-6 text-center">
            <button 
              className="text-xs font-semibold text-slate-400 dark:text-muted-foreground/60 hover:text-emerald-600 dark:hover:text-emerald-400 underline cursor-pointer bg-transparent border-none transition-colors" 
              onClick={() => navigate('/all-pages')}
            >
              Developer Page Directory
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
