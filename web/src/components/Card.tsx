import React from 'react';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  elevation?: 'low' | 'medium' | 'high';
  padding?: 'none' | 'small' | 'medium' | 'large';
  onClick?: () => void;
}

export const Card: React.FC<CardProps> = ({
  children,
  elevation = 'low',
  padding = 'medium',
  onClick,
  className = '',
  ...props
}) => {
  const isClickable = onClick ? 'cursor-pointer hover:-translate-y-0.5 hover:shadow-md active:translate-y-0' : '';
  
  let elevationClass = 'shadow-sm';
  if (elevation === 'medium') elevationClass = 'shadow-md';
  else if (elevation === 'high') elevationClass = 'shadow-lg';

  let paddingClass = 'p-6';
  if (padding === 'none') paddingClass = 'p-0';
  else if (padding === 'small') paddingClass = 'p-4';
  else if (padding === 'large') paddingClass = 'p-8';

  return (
    <div
      className={`rounded-xl border border-border bg-card backdrop-blur-md transition-all ${elevationClass} ${paddingClass} ${isClickable} ${className}`}
      onClick={onClick}
      {...props}
    >
      {children}
    </div>
  );
};
