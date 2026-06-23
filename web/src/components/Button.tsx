import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'text';
  theme?: 'green' | 'blue' | 'darkblue';
  fullWidth?: boolean;
  icon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  theme = 'green',
  fullWidth = false,
  icon,
  className = '',
  ...props
}) => {
  const baseClass = 'inline-flex items-center justify-center gap-2 rounded-lg font-semibold text-sm transition-all focus-visible:outline-none select-none active:translate-y-[1px] disabled:pointer-events-none disabled:opacity-50 h-10 px-4 py-2';
  
  let variantClass = '';
  
  if (variant === 'primary') {
    if (theme === 'green') variantClass = 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm hover:shadow';
    else if (theme === 'blue') variantClass = 'bg-blue-600 hover:bg-blue-700 text-white shadow-sm hover:shadow';
    else if (theme === 'darkblue') variantClass = 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm hover:shadow';
  } else if (variant === 'secondary') {
    if (theme === 'green') variantClass = 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:bg-emerald-950/30 dark:text-emerald-400 dark:hover:bg-emerald-950/50';
    else if (theme === 'blue') variantClass = 'bg-blue-50 text-blue-700 hover:bg-blue-100 dark:bg-blue-950/30 dark:text-blue-400 dark:hover:bg-blue-950/50';
    else if (theme === 'darkblue') variantClass = 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100 dark:bg-indigo-950/30 dark:text-indigo-400 dark:hover:bg-indigo-950/50';
  } else if (variant === 'outline') {
    variantClass = 'border border-border bg-card text-foreground hover:bg-muted';
  } else if (variant === 'text') {
    variantClass = 'text-muted-foreground hover:text-foreground hover:bg-muted/50';
  }

  const widthClass = fullWidth ? 'w-full' : 'w-fit';

  return (
    <button
      className={`${baseClass} ${variantClass} ${widthClass} ${className}`}
      {...props}
    >
      {icon && <span className="inline-flex items-center shrink-0">{icon}</span>}
      <span className="truncate">{children}</span>
    </button>
  );
};
