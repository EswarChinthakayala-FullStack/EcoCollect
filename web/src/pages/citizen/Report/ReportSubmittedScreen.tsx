import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Check, Award, FileText } from 'lucide-react';
import { Button } from '../../../components/Button';

export const ReportSubmittedScreen: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // Extract created report data from redirect state
  const { report } = (location.state as { report?: any }) || { report: null };

  return (
    <div className="py-5 max-w-[480px] mx-auto w-full animate-slide-up text-foreground">
      <div className="bg-card text-card-foreground border border-border shadow-sm rounded-xl p-10 flex flex-col items-center text-center transition-all duration-250 hover:-translate-y-0.5 hover:shadow-md">
        
        <div className="w-20 h-20 rounded-full bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center mb-6">
          <div className="w-14 h-14 rounded-full border-2 border-emerald-600 dark:border-emerald-500 flex items-center justify-center">
            <Check size={32} className="text-emerald-600 dark:text-emerald-500" strokeWidth={3} />
          </div>
        </div>

        <h1 className="text-2xl font-extrabold text-foreground mb-3">Report Submitted!</h1>
        <p className="text-sm text-muted-foreground leading-relaxed mb-6">
          Thank you for helping keep our city clean. Your report has been dispatched to field supervisors.
        </p>

        {report && (
          <div className="w-full bg-muted/35 border border-border rounded-xl p-4 mb-6 flex flex-col items-center text-sm text-muted-foreground">
            <div className="flex items-center gap-2">
              <FileText size={18} className="text-muted-foreground" />
              <span>Report Ticket: <strong className="font-bold text-foreground">#ID-{report.id}</strong></span>
            </div>
            <div className="flex items-center gap-2 mt-2">
              <span className="w-2 h-2 rounded-full bg-amber-500" />
              <span>Status: <strong className="font-bold text-amber-600 dark:text-amber-400">{report.status}</strong></span>
            </div>
          </div>
        )}

        <div className="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4 flex items-center gap-4 w-full box-border">
          <div className="w-10 h-10 rounded-full bg-amber-500 flex items-center justify-center shrink-0">
            <Award size={22} className="text-white" />
          </div>
          <div className="flex flex-col text-left">
            <span className="text-amber-600 dark:text-amber-400 text-[10px] font-bold uppercase tracking-wider font-sans">You earned</span>
            <span className="text-amber-700 dark:text-amber-300 text-lg font-extrabold font-sans">+10 Eco Points</span>
          </div>
        </div>

        <Button 
          fullWidth
          theme="green"
          onClick={() => navigate('/citizen/dashboard')}
          className="mt-8"
        >
          Back to Dashboard
        </Button>
      </div>
    </div>
  );
};
