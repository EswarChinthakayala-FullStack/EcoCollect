import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, Shield, CheckCircle, Award, Globe, ArrowLeft, Info } from 'lucide-react';
import { useTheme } from 'next-themes';

import appLogo from '../../../assets/logo.svg';

export const AboutAppScreen: React.FC = () => {
  const navigate = useNavigate();

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
          <h1 className="text-2xl font-bold tracking-tight text-foreground">About App</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Learn more about WasteReporting's mission, platform features, and ecosystem.</p>
        </div>
      </div>

      {/* Grid Layout */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full items-start">
        
        {/* Left Column: Branding & Mission */}
        <div className="flex flex-col gap-6">
          {/* App Branding Header Card */}
          <div className="bg-card border border-border rounded-2xl p-8 shadow-sm flex flex-col items-center text-center">
            <div className="w-20 h-20 rounded-2xl bg-emerald-500 flex items-center justify-center shadow-[0_8px_16px_-4px_rgba(22,163,74,0.3)] mb-4 overflow-hidden">
              <img src={appLogo} alt="Logo" className="w-full h-full object-contain" />
            </div>
            <h1 className="text-2xl font-extrabold text-foreground">WasteReporting</h1>
            <span className="px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-xs font-semibold mt-2 border border-emerald-500/20">
              Version v1.0.0
            </span>
          </div>

          {/* Description Section Card */}
          <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex flex-col gap-4">
            <h2 className="text-base font-bold text-emerald-600 dark:text-emerald-400 border-l-4 border-emerald-500 pl-2.5 flex items-center gap-2">
              <Info size={18} /> Our Mission
            </h2>
            <p className="text-sm text-muted-foreground leading-relaxed">
              WasteReporting is a community-driven platform built to make waste reporting, review, and collection transparent and collaborative. We empower citizens to actively report local waste issues, linking them directly to supervisors for immediate action.
            </p>
            <p className="text-sm text-muted-foreground leading-relaxed">
              By designing simplified digital workflows, we aim to speed up resolutions, clean up our neighborhoods, and nurture environmental responsibility across the community.
            </p>
          </div>
        </div>

        {/* Right Column: Key Portal Features Card */}
        <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex flex-col gap-4">
          <h2 className="text-base font-bold text-emerald-600 dark:text-emerald-400 border-l-4 border-emerald-500 pl-2.5 flex items-center gap-2">
            <Shield size={18} /> Platform Features
          </h2>
          
          <div className="flex flex-col gap-4 divide-y divide-border/40">
            <div className="flex gap-4 pt-1">
              <div className="w-10 h-10 bg-emerald-500/10 dark:bg-emerald-500/20 rounded-xl flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <CheckCircle size={20} />
              </div>
              <div className="flex flex-col gap-0.5 text-left">
                <span className="text-sm font-semibold text-foreground">Fast Photo Uploads</span>
                <span className="text-xs text-muted-foreground leading-normal">Instantly snap and upload waste photos to submit reports directly from the field.</span>
              </div>
            </div>

            <div className="flex gap-4 pt-4">
              <div className="w-10 h-10 bg-emerald-500/10 dark:bg-emerald-500/20 rounded-xl flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Globe size={20} />
              </div>
              <div className="flex flex-col gap-0.5 text-left">
                <span className="text-sm font-semibold text-foreground">Interactive Mapping</span>
                <span className="text-xs text-muted-foreground leading-normal"> pinpoint precisely where the waste issue is located using offline coordinates and map tracking.</span>
              </div>
            </div>

            <div className="flex gap-4 pt-4">
              <div className="w-10 h-10 bg-emerald-500/10 dark:bg-emerald-500/20 rounded-xl flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Award size={20} />
              </div>
              <div className="flex flex-col gap-0.5 text-left">
                <span className="text-sm font-semibold text-foreground">Eco Champion Rewards</span>
                <span className="text-xs text-muted-foreground leading-normal">Earn community reward points for every report successfully processed and resolved by supervisors.</span>
              </div>
            </div>

            <div className="flex gap-4 pt-4">
              <div className="w-10 h-10 bg-emerald-500/10 dark:bg-emerald-500/20 rounded-xl flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Shield size={20} />
              </div>
              <div className="flex flex-col gap-0.5 text-left">
                <span className="text-sm font-semibold text-foreground">Local-First Storage</span>
                <span className="text-xs text-muted-foreground leading-normal">Your draft reports and profiles are saved locally on your device for absolute privacy and offline use.</span>
              </div>
            </div>
          </div>
        </div>

      </div>

      {/* Footer Branding Links */}
      <div className="flex flex-col items-center text-center mt-12 gap-3 border-t border-border/60 pt-6">
        <span className="text-xs text-muted-foreground/60">© 2026 WasteReporting. All rights reserved.</span>
        <div className="flex gap-2.5 flex-wrap justify-center items-center">
          <span className="text-xs font-semibold text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 cursor-pointer transition-colors" onClick={() => {}}>Privacy Policy</span>
          <span className="text-[10px] text-muted-foreground/45">•</span>
          <span className="text-xs font-semibold text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 cursor-pointer transition-colors" onClick={() => {}}>Terms of Service</span>
          <span className="text-[10px] text-muted-foreground/45">•</span>
          <span className="text-xs font-semibold text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 cursor-pointer transition-colors" onClick={() => {}}>Licenses</span>
        </div>
      </div>

    </div>
  );
};
