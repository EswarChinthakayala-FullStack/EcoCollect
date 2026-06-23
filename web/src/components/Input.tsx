import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  fullWidth?: boolean;
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  icon,
  rightIcon,
  fullWidth = true,
  className = '',
  id,
  ...props
}) => {
  const inputId = id || `input-${Math.random().toString(36).substring(2, 9)}`;
  const containerClass = `flex flex-col gap-1.5 ${fullWidth ? 'w-full' : 'w-fit'}`;

  return (
    <div className={containerClass}>
      {label && (
        <label htmlFor={inputId} className="text-sm font-semibold text-foreground/90 select-none text-left">
          {label}
        </label>
      )}
      <div className={`relative flex items-center w-full border rounded-lg transition-all ${
        props.disabled 
          ? 'bg-slate-100 dark:bg-slate-800/40 border-slate-200 dark:border-slate-800/60' 
          : 'bg-card dark:bg-muted/30 border-border focus-within:border-indigo-600 dark:focus-within:border-indigo-400 focus-within:ring-2 focus-within:ring-indigo-500/10'
      } ${
        error ? 'border-red-500 focus-within:border-red-500 focus-within:ring-red-500/10' : ''
      }`}>
        {icon && (
          <span className="absolute left-3 flex items-center justify-center text-muted-foreground pointer-events-none">
            {icon}
          </span>
        )}
        <input
          id={inputId}
          className={`w-full bg-transparent border-none text-foreground text-sm font-normal outline-none py-2.5 px-3.5 placeholder:text-muted-foreground dark:placeholder:text-slate-500 ${
            icon ? 'pl-10' : ''
          } ${rightIcon ? 'pr-10' : ''} ${className}`}
          {...props}
        />
        {rightIcon && (
          <span className="absolute right-3 flex items-center justify-center text-muted-foreground">
            {rightIcon}
          </span>
        )}
      </div>
      {error && <span className="text-xs font-semibold text-red-500 text-left">{error}</span>}
    </div>
  );
};
