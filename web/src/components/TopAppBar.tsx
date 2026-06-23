import React from 'react';
import { ArrowLeft } from 'lucide-react';

interface TopAppBarProps {
  title: string;
  onBack?: () => void;
  actions?: React.ReactNode;
  theme?: 'green' | 'blue' | 'darkblue';
  elevation?: boolean;
}

export const TopAppBar: React.FC<TopAppBarProps> = ({
  title,
  onBack,
  actions,
  theme = 'green',
  elevation = false,
}) => {
  const borderThemeClass = 
    theme === 'green' ? 'border-b-emerald-100 dark:border-b-emerald-900/30' :
    theme === 'blue' ? 'border-b-blue-100 dark:border-b-blue-900/30' : 'border-b-indigo-100 dark:border-b-indigo-900/30';

  return (
    <div className={`h-16 flex items-center justify-between px-6 border-b bg-white/90 dark:bg-card/90 backdrop-blur-md border-slate-200/80 dark:border-border/60 ${borderThemeClass} ${elevation ? 'shadow-sm' : ''}`}>
      <div className="flex items-center">
        {onBack && (
          <button onClick={onBack} className="p-2 rounded-lg text-slate-600 dark:text-muted-foreground hover:text-slate-950 dark:hover:text-foreground hover:bg-slate-100 dark:hover:bg-muted transition-all mr-2">
            <ArrowLeft size={20} />
          </button>
        )}
        <h1 className="text-lg font-bold tracking-tight text-slate-900 dark:text-foreground">{title}</h1>
      </div>
      <div className="flex items-center gap-2">
        {actions}
      </div>
    </div>
  );
};
