import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Truck, Star } from 'lucide-react';
import appLogo from '../assets/logo.svg';

export const Onboarding: React.FC = () => {
  const navigate = useNavigate();
  const [showSplash, setShowSplash] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);

  const pages = [
    {
      icon: <MapPin size={48} className="text-emerald-600 dark:text-emerald-400" />,
      iconBg: 'bg-emerald-50 dark:bg-emerald-950/40',
      title: "Report Waste Easily",
      desc: "Spot an overflowing bin or illegal dumping? Snap a photo and report it instantly to keep our city clean."
    },
    {
      icon: <Truck size={48} className="text-blue-600 dark:text-blue-400" />,
      iconBg: 'bg-blue-50 dark:bg-blue-950/40',
      title: "Smart Collection",
      desc: "Our AI-optimized routes ensure timely pickups. Track collection vehicles and know exactly when your bins will be emptied."
    },
    {
      icon: <Star size={48} className="text-amber-600 dark:text-amber-400" />,
      iconBg: 'bg-amber-50 dark:bg-amber-950/40',
      title: "Earn Eco Points",
      desc: "Get rewarded for your contributions. Report issues, recycle properly, and climb the community leaderboard!"
    }
  ];

  if (showSplash) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-slate-50 dark:bg-slate-950 font-sans p-6 text-center overflow-hidden animate-fade-in">
        <div className="flex flex-col items-center justify-center flex-grow w-full max-w-sm pt-12">
          <div className="w-20 h-20 bg-white dark:bg-card border border-slate-200/80 dark:border-border/40 shadow-md rounded-2xl flex items-center justify-center p-3.5 mb-6 transition-all hover:scale-[1.02]">
            <img src={appLogo} alt="Logo" className="w-full h-full object-contain" />
          </div>
          <span className="text-2xl font-extrabold text-slate-900 dark:text-foreground tracking-tight mb-2">WasteReporting</span>
          <span className="text-sm text-slate-500 dark:text-muted-foreground leading-relaxed">
            Smart Waste Management for a<br />Cleaner Tomorrow
          </span>
        </div>

        <div className="w-full max-w-sm flex flex-col gap-3 mt-auto pb-10">
          <button 
            className="w-full h-11 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm shadow-md transition-all active:translate-y-px cursor-pointer" 
            onClick={() => setShowSplash(false)}
          >
            Get Started
          </button>
          <span 
            className="text-xs font-semibold text-slate-400 dark:text-muted-foreground/60 hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer transition-colors mt-2" 
            onClick={() => navigate('/supervisor/login')}
          >
            Staff Portal · Supervisor Login
          </span>
          <span 
            className="text-xs font-semibold text-slate-400 dark:text-muted-foreground/60 hover:text-indigo-600 dark:hover:text-indigo-400 cursor-pointer transition-colors" 
            onClick={() => navigate('/admin/login')}
          >
            Admin Control Center
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen w-full bg-slate-50 dark:bg-slate-950 font-sans p-6 text-center overflow-hidden animate-fade-in">
      <div className="w-full max-w-sm flex flex-col justify-between min-h-[450px] py-10 relative">
        <div className="relative w-full h-[280px] overflow-hidden">
          {pages.map((page, index) => {
            const offset = (index - currentPage) * 100;
            return (
              <div 
                key={index}
                className="w-full h-full flex flex-col items-center justify-center transition-transform duration-500 ease-out text-center absolute top-0 left-0" 
                style={{ transform: `translateX(${offset}%)` }}
              >
                <div className={`w-24 h-24 rounded-2xl flex items-center justify-center mb-6 ${page.iconBg}`}>{page.icon}</div>
                <span className="text-xl font-bold text-slate-900 dark:text-foreground mb-3">{page.title}</span>
                <span className="text-sm text-slate-500 dark:text-muted-foreground leading-relaxed px-4">{page.desc}</span>
              </div>
            );
          })}
        </div>

        <div className="flex flex-col gap-4 mt-auto">
          <div className="flex justify-center gap-2 mb-2">
            {[0, 1, 2].map(index => (
              <div 
                key={index} 
                className={`transition-all duration-300 ${
                  currentPage === index ? 'w-4 h-1.5 bg-emerald-600 rounded-full' : 'w-1.5 h-1.5 bg-slate-300 dark:bg-slate-800 rounded-full'
                }`} 
              />
            ))}
          </div>

          <button 
            className="w-full h-11 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm rounded-lg shadow-md transition-all active:translate-y-px cursor-pointer" 
            onClick={() => {
              if (currentPage < 2) setCurrentPage(currentPage + 1);
              else navigate('/citizen/login');
            }}
          >
            {currentPage === 2 ? 'Continue' : 'Next'}
          </button>

          <button 
            className="text-sm font-semibold text-slate-400 dark:text-muted-foreground/60 hover:text-slate-600 dark:hover:text-slate-300 transition-colors py-1 bg-transparent border-none cursor-pointer" 
            onClick={() => {
              if (currentPage > 0) setCurrentPage(currentPage - 1);
              else navigate('/citizen/login');
            }}
          >
            {currentPage === 0 ? 'Skip' : 'Back'}
          </button>
        </div>
      </div>
    </div>
  );
};
