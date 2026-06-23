import React from 'react';

export interface BottomNavItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  activeIcon?: React.ReactNode;
  path: string;
}

interface BottomNavProps {
  items: BottomNavItem[];
  activeId: string;
  onItemClick: (item: BottomNavItem) => void;
  theme?: 'green' | 'blue' | 'darkblue';
}

export const BottomNav: React.FC<BottomNavProps> = ({
  items,
  activeId,
  onItemClick,
  theme = 'green',
}) => {
  return (
    <div className="fixed bottom-0 left-0 right-0 h-16 bg-white/95 dark:bg-slate-950/95 backdrop-blur-lg border-t border-slate-200/80 flex justify-around items-center z-50 shadow-lg pb-safe">
      {items.map((item) => {
        const isActive = item.id === activeId;
        
        let activeThemeClass = 'text-emerald-600 dark:text-emerald-400';
        if (theme === 'blue') activeThemeClass = 'text-blue-600 dark:text-blue-400';
        else if (theme === 'darkblue') activeThemeClass = 'text-indigo-600 dark:text-indigo-400';

        return (
          <div
            key={item.id}
            className={`flex flex-col items-center justify-center gap-1 cursor-pointer w-1/4 h-full select-none transition-colors ${
              isActive ? `${activeThemeClass} font-semibold` : 'text-slate-500 hover:text-slate-900 dark:hover:text-slate-200'
            }`}
            onClick={() => onItemClick(item)}
          >
            <div className="flex items-center justify-center shrink-0">
              {isActive && item.activeIcon ? item.activeIcon : item.icon}
            </div>
            <span className="text-[10px] uppercase tracking-wider font-semibold">{item.label}</span>
          </div>
        );
      })}
    </div>
  );
};
