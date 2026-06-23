import React from 'react';

interface LayoutProps {
  topBar?: React.ReactNode;
  bottomBar?: React.ReactNode;
  children: React.ReactNode;
  backgroundColor?: string;
}

export const Layout: React.FC<LayoutProps> = ({
  topBar,
  bottomBar,
  children,
  backgroundColor = 'bg-slate-50 dark:bg-slate-950',
}) => {
  // Check if backgroundColor is inline hex or var, otherwise use it as class name
  const isClass = backgroundColor.startsWith('bg-');
  const bgStyle = isClass ? {} : { backgroundColor };
  const bgClass = isClass ? backgroundColor : '';

  return (
    <div className={`flex flex-col min-h-screen w-full relative ${bgClass}`} style={bgStyle}>
      {topBar && <div className="sticky top-0 z-50 w-full">{topBar}</div>}
      
      <main className="flex-grow w-full overflow-y-auto">
        {children}
      </main>

      {bottomBar && <div className="fixed bottom-0 left-0 right-0 z-50 w-full">{bottomBar}</div>}
    </div>
  );
};
