import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Home, PlusCircle, MapPin, ClipboardList, 
  Settings, Users, LogOut, Menu, X, User, Award,
  PanelLeftClose, PanelLeftOpen, Sun, Moon, Shield, History,
  Bell, Check, Inbox
} from 'lucide-react';
import appLogo from '../assets/logo.svg';
import { Sheet, SheetContent, SheetTrigger } from './ui/sheet';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from './ui/tooltip';
import { useTheme } from 'next-themes';
import { Avatar, AvatarImage, AvatarFallback } from './ui/avatar';
import { Popover, PopoverContent, PopoverTrigger } from './ui/popover';
import { 
  Breadcrumb, 
  BreadcrumbList, 
  BreadcrumbItem, 
  BreadcrumbLink, 
  BreadcrumbPage, 
  BreadcrumbSeparator 
} from './ui/breadcrumb';

interface AppLayoutProps {
  children: React.ReactNode;
}

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
}

export const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const { user, role, logout, apiCall } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const [notifications, setNotifications] = useState<any[]>([]);

  const fetchNotifications = async () => {
    try {
      const data = await apiCall<any[]>('/notifications');
      // Filter out read notifications (read_status === 0 means unread)
      const unread = data.filter((n: any) => n.read_status === 0);
      setNotifications(unread);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    }
  };

  const handleMarkAsRead = async (id: number) => {
    // Instant update local UI state first
    setNotifications((prev) => prev.filter((n) => n.id !== id));
    try {
      await apiCall(`/notifications/${id}/read`, { method: 'PUT' });
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
      fetchNotifications();
    }
  };

  const handleMarkAllAsRead = async () => {
    const ids = notifications.map((n) => n.id);
    // Instant update local UI state first
    setNotifications([]);
    try {
      await Promise.all(
        ids.map((id) => apiCall(`/notifications/${id}/read`, { method: 'PUT' }))
      );
    } catch (err) {
      console.error('Failed to mark all notifications as read:', err);
      fetchNotifications();
    }
  };

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (user) {
      fetchNotifications();
      const interval = setInterval(fetchNotifications, 10000); // Poll every 10 seconds
      return () => clearInterval(interval);
    }
  }, [user]);

  const activePath = location.pathname;

  // Define navigation items based on role
  const getNavItems = (): NavItem[] => {
    switch (role) {
      case 'citizen':
        return [
          { label: 'Dashboard', path: '/citizen/dashboard', icon: <Home size={20} /> },
          { label: 'Report Waste', path: '/citizen/report', icon: <PlusCircle size={20} /> },
          { label: 'City Map', path: '/citizen/map', icon: <MapPin size={20} /> },
          { label: 'My History', path: '/citizen/history', icon: <ClipboardList size={20} /> },
          { label: 'Profile', path: '/citizen/profile', icon: <User size={20} /> },
          { label: 'Settings', path: '/citizen/settings', icon: <Settings size={20} /> },
        ];
      case 'supervisor':
        return [
          { label: 'Dashboard', path: '/supervisor/dashboard', icon: <Home size={20} /> },
          { label: 'City Map', path: '/supervisor/map', icon: <MapPin size={20} /> },
          { label: 'Resolved History', path: '/supervisor/history', icon: <History size={20} /> },
          { label: 'Profile', path: '/supervisor/profile', icon: <User size={20} /> },
          { label: 'Settings', path: '/supervisor/settings', icon: <Settings size={20} /> },
        ];
      case 'admin':
        return [
          { label: 'Dashboard', path: '/admin/dashboard', icon: <Home size={20} /> },
          { label: 'City Map', path: '/admin/map', icon: <MapPin size={20} /> },
          { label: 'Waste Reports', path: '/admin/reports', icon: <ClipboardList size={20} /> },
          { label: 'Supervisors', path: '/admin/supervisors', icon: <Users size={20} /> },
          { label: 'Permissions', path: '/admin/permissions', icon: <Shield size={20} /> },
          { label: 'Activity Logs', path: '/admin/logs', icon: <History size={20} /> },
          { label: 'Profile', path: '/admin/profile', icon: <User size={20} /> },
          { label: 'Settings', path: '/admin/settings', icon: <Settings size={20} /> },
        ];
      default:
        return [];
    }
  };

  const navItems = getNavItems();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Close mobile sidebar on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location]);

  // Generate dynamic breadcrumbs based on pathname
  const renderBreadcrumbs = () => {
    const segments = location.pathname.split('/').filter(Boolean);
    
    const routeLabels: Record<string, string> = {
      citizen: 'Citizen Hub',
      supervisor: 'Supervisor Portal',
      admin: 'Admin Control Center',
      dashboard: 'Dashboard',
      report: 'Report Waste',
      category: 'Select Category',
      location: 'Confirm Location',
      success: 'Success',
      map: 'City Map',
      nearby: 'Nearby Issues',
      settings: 'Settings',
      profile: 'Profile',
      edit: 'Edit Profile',
      help: 'Help & Support',
      about: 'About App',
      history: 'My History',
      reports: 'Waste Reports',
      supervisors: 'Supervisors',
      add: 'Add Supervisor',
      password: 'Change Password',
      permissions: 'Permissions',
      logs: 'Activity Logs',
    };

    const items: { label: string; path: string }[] = [];
    let currentPath = '';

    // Always start with the top-level "EcoCollect" breadcrumb pointing to the dashboard
    items.push({
      label: 'EcoCollect',
      path: `/${role}/dashboard`,
    });

    for (let i = 0; i < segments.length; i++) {
      const segment = segments[i];
      currentPath += `/${segment}`;

      let label = routeLabels[segment] || segment;

      // Custom formatting for IDs
      const isId = /^\d+$/.test(segment) || (segment.length > 8 && !routeLabels[segment]);
      if (isId) {
        const prevSegment = segments[i - 1];
        if (prevSegment === 'report') {
          label = 'Report Details';
        } else if (prevSegment === 'supervisors') {
          label = 'Supervisor Profile';
        } else {
          label = 'Details';
        }
      } else if (!routeLabels[segment]) {
        // Fallback title casing
        label = segment.charAt(0).toUpperCase() + segment.slice(1);
      }

      // Link mapping logic
      let linkPath = currentPath;
      if (segment === role) {
        linkPath = `/${role}/dashboard`;
      } else if (segment === 'report' && role === 'supervisor') {
        linkPath = '/supervisor/history';
      } else if (segment === 'report' && role === 'admin') {
        linkPath = '/admin/reports';
      }

      items.push({
        label,
        path: linkPath,
      });
    }

    return (
      <Breadcrumb>
        <BreadcrumbList className="flex items-center gap-1.5 text-xs md:text-sm font-semibold select-none text-muted-foreground">
          {items.map((item, index) => {
            const isLast = index === items.length - 1;
            
            // Skip the second item if it represents the role portal root dashboard path
            if (index === 1 && item.path === items[0].path) {
              return null;
            }

            return (
              <React.Fragment key={index}>
                {index > 0 && <BreadcrumbSeparator className="text-muted-foreground/30" />}
                <BreadcrumbItem>
                  {isLast ? (
                    <BreadcrumbPage className="text-foreground font-bold">{item.label}</BreadcrumbPage>
                  ) : (
                    <BreadcrumbLink 
                      className="text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
                      onClick={() => navigate(item.path)}
                    >
                      {item.label}
                    </BreadcrumbLink>
                  )}
                </BreadcrumbItem>
              </React.Fragment>
            );
          })}
        </BreadcrumbList>
      </Breadcrumb>
    );
  };

  // Theme accent active styles (pill shape or border tab)
  const getActiveStyle = (isActive: boolean) => {
    if (!isActive) return 'text-muted-foreground hover:bg-muted hover:text-foreground pl-4 rounded-xl';
    
    if (role === 'citizen') {
      return isCollapsed
        ? 'bg-emerald-500/10 text-emerald-400'
        : 'bg-emerald-500/10 text-emerald-400 font-semibold border-l-4 border-emerald-500 pl-3 rounded-r-xl rounded-l-none';
    }
    if (role === 'supervisor') {
      return isCollapsed
        ? 'bg-blue-500/10 text-blue-400'
        : 'bg-blue-500/10 text-blue-400 font-semibold border-l-4 border-blue-500 pl-3 rounded-r-xl rounded-l-none';
    }
    if (role === 'admin') {
      return isCollapsed
        ? 'bg-indigo-500/10 text-indigo-400'
        : 'bg-indigo-500/10 text-indigo-400 font-semibold border-l-4 border-indigo-500 pl-3 rounded-r-xl rounded-l-none';
    }
    return 'text-white bg-slate-700 pl-4 rounded-xl';
  };

  const getPointsBg = () => {
    if (role === 'citizen') return 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 dark:text-emerald-400';
    if (role === 'supervisor') return 'bg-blue-500/10 text-blue-500 border border-blue-500/20 dark:text-blue-400';
    return 'bg-muted text-muted-foreground border border-border';
  };

  const getRoleBadgeClass = () => {
    if (role === 'citizen') return 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 dark:text-emerald-400';
    if (role === 'supervisor') return 'bg-blue-500/10 text-blue-500 border border-blue-500/20 dark:text-blue-400';
    if (role === 'admin') return 'bg-indigo-500/10 text-indigo-500 border border-indigo-500/20 dark:text-indigo-400';
    return 'bg-muted text-muted-foreground';
  };

  return (
    <TooltipProvider>
      <div className="flex flex-row h-screen w-full bg-background relative font-sans overflow-hidden">
        {/* Desktop Collapsible Sidebar */}
        <aside className={`hidden md:flex flex-col bg-card text-foreground border-r border-border shrink-0 transition-all duration-300 ${
          isCollapsed ? 'w-[76px]' : 'w-[260px]'
        }`}>
          {/* Sidebar Header with Gemini-style Expand Trigger on logo hover */}
          <div className="p-4 flex items-center justify-between border-b border-slate-200/80 h-16 shrink-0">
            {isCollapsed ? (
              <button
                onClick={() => setIsCollapsed(false)}
                className="relative w-11 h-11 mx-auto rounded-xl flex items-center justify-center hover:bg-muted transition-all border-none bg-transparent cursor-pointer text-muted-foreground hover:text-foreground group"
                title="Expand Sidebar"
              >
                <img src={appLogo} alt="Logo" className="h-7 w-7 object-contain group-hover:opacity-0 group-hover:scale-0 transition-all duration-200" />
                <PanelLeftOpen size={22} className="absolute opacity-0 scale-50 group-hover:opacity-100 group-hover:scale-100 transition-all duration-200 text-slate-500" />
              </button>
            ) : (
              <>
                <div className="flex items-center gap-3">
                  <img src={appLogo} alt="Logo" className="h-8 w-8 animate-fade-in" />
                  <div className="text-left animate-fade-in">
                    <div className="font-bold text-sm tracking-tight text-foreground leading-none">EcoCollect</div>
                    <div className="text-[9px] text-muted-foreground font-bold uppercase tracking-wider mt-1">Smart Waste Panel</div>
                  </div>
                </div>
                <button
                  onClick={() => setIsCollapsed(true)}
                  className="w-8 h-8 rounded-lg flex items-center justify-center hover:bg-muted text-muted-foreground hover:text-foreground transition-colors border-none bg-transparent cursor-pointer"
                  title="Collapse Sidebar"
                >
                  <PanelLeftClose size={18} />
                </button>
              </>
            )}
          </div>

          {/* Navigation links */}
          <nav className="flex-grow px-3 py-5 flex flex-col gap-2 overflow-y-auto">
            {navItems.map((item) => {
              const isActive = activePath === item.path || activePath.startsWith(item.path + '/');
              
              const btn = (
                <button
                  onClick={() => navigate(item.path)}
                  className={`flex items-center transition-all border-none cursor-pointer ${
                    isCollapsed 
                      ? 'justify-center p-3 h-11 w-11 mx-auto rounded-xl' 
                      : 'gap-3.5 px-4 py-3 rounded-xl w-full text-left'
                  } ${getActiveStyle(isActive)}`}
                >
                  <span className="shrink-0">{item.icon}</span>
                  {!isCollapsed && <span className="font-medium text-sm truncate animate-fade-in">{item.label}</span>}
                </button>
              );

              if (isCollapsed) {
                return (
                  <Tooltip key={item.path}>
                    <TooltipTrigger asChild>
                      {btn}
                    </TooltipTrigger>
                    <TooltipContent side="right" sideOffset={12}>
                      {item.label}
                    </TooltipContent>
                  </Tooltip>
                );
              }

              return <React.Fragment key={item.path}>{btn}</React.Fragment>;
            })}
          </nav>

          {/* Collapsed/Expanded Account section at the bottom */}
          {isCollapsed ? (
            <div className="p-4 border-t border-slate-200/80 flex flex-col items-center gap-4 shrink-0">
              <Tooltip>
                <TooltipTrigger asChild>
                  <Avatar 
                    size="lg"
                    className="cursor-pointer hover:ring-2 hover:ring-indigo-600/20 transition-all animate-fade-in"
                    onClick={() => navigate(`/${role}/settings`)}
                  >
                    <AvatarImage src={user?.profile_image_url} alt={user?.full_name || 'User'} />
                    <AvatarFallback className="font-semibold select-none">
                      {user?.full_name ? user.full_name.charAt(0).toUpperCase() : <User size={16} />}
                    </AvatarFallback>
                  </Avatar>
                </TooltipTrigger>
                <TooltipContent side="right" sideOffset={12}>
                  <div className="text-xs font-bold text-slate-900">{user?.full_name || 'System Account'}</div>
                  <div className="text-[10px] text-slate-500 truncate">{user?.email || ''}</div>
                </TooltipContent>
              </Tooltip>

              <Tooltip>
                <TooltipTrigger asChild>
                  <button 
                    className="w-10 h-10 rounded-xl flex items-center justify-center bg-red-500/10 hover:bg-red-600 text-red-500 hover:text-white transition-all border-none cursor-pointer"
                    onClick={handleLogout}
                  >
                    <LogOut size={16} />
                  </button>
                </TooltipTrigger>
                <TooltipContent side="right" sideOffset={12}>
                  Sign Out
                </TooltipContent>
              </Tooltip>
            </div>
          ) : (
            <div className="p-4 border-t border-slate-200/80 flex flex-col gap-3 shrink-0">
              <div className="flex items-center gap-3 p-2.5 rounded-xl bg-muted/40 border border-border text-left animate-fade-in shadow-sm">
                <Avatar className="h-9 w-9 shrink-0">
                  <AvatarImage src={user?.profile_image_url} alt={user?.full_name || 'User'} />
                  <AvatarFallback className="font-semibold select-none">
                    {user?.full_name ? user.full_name.charAt(0).toUpperCase() : <User size={16} />}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <div className="text-xs font-bold text-foreground truncate">{user?.full_name || 'System Account'}</div>
                  <div className="text-[10px] text-muted-foreground truncate">{user?.email || ''}</div>
                </div>
              </div>
              
              <button 
                className="flex items-center justify-center gap-2 p-2.5 rounded-lg border border-red-500/20 bg-red-500/5 hover:bg-red-600 hover:text-white text-red-500 text-xs font-semibold transition-all border-none cursor-pointer" 
                onClick={handleLogout}
              >
                <LogOut size={14} />
                <span>Sign Out</span>
              </button>
            </div>
          )}
        </aside>

        {/* Right content panel (Navbar + Main Content) */}
        <div className="flex-1 flex flex-col min-w-0 h-full overflow-hidden">
          {/* Top Navbar */}
          <header className="h-16 bg-card/85 backdrop-blur-md border-b border-border/60 flex items-center justify-between px-4 sm:px-6 sticky top-0 z-50 shadow-sm shrink-0">
            <div className="flex items-center gap-2 sm:gap-4">
              {/* Mobile Navigation Menu Trigger */}
              <Sheet open={isMobileMenuOpen} onOpenChange={setIsMobileMenuOpen}>
                <SheetTrigger asChild>
                  <button 
                    className="md:hidden p-2 rounded-lg text-muted-foreground hover:bg-muted transition-colors border-none cursor-pointer flex items-center justify-center bg-transparent" 
                    aria-label="Toggle Navigation"
                  >
                    <Menu size={24} />
                  </button>
                </SheetTrigger>
                <SheetContent side="left" className="w-[270px] bg-white dark:bg-slate-950 text-slate-900 p-0 border-none flex flex-col h-full z-[100]" showCloseButton={false}>
                  <div className="p-4 flex items-center justify-between border-b border-slate-200/80 h-16 shrink-0">
                    <div className="flex items-center gap-3">
                      <img src={appLogo} alt="Logo" className="h-8 w-8" />
                      <div className="text-left">
                        <div className="font-bold text-sm tracking-tight text-slate-900 leading-none">EcoCollect</div>
                        <div className="text-[9px] text-slate-500 font-bold uppercase tracking-wider mt-1">Smart Waste Panel</div>
                      </div>
                    </div>
                    <button 
                      className="w-8 h-8 rounded-lg flex items-center justify-center hover:bg-slate-100 dark:hover:bg-white/10 text-slate-500 hover:text-slate-900 transition-colors border-none cursor-pointer bg-transparent"
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <X size={18} />
                    </button>
                  </div>
 
                  <nav className="flex-1 px-4 py-5 flex flex-col gap-2 text-left">
                    {navItems.map((item) => {
                      const isActive = activePath === item.path || activePath.startsWith(item.path + '/');
                      return (
                        <button
                          key={item.path}
                          className={`flex items-center gap-3.5 px-4 py-3 rounded-xl font-medium text-sm transition-all text-left border-none cursor-pointer ${getActiveStyle(isActive)}`}
                          onClick={() => {
                            navigate(item.path);
                            setIsMobileMenuOpen(false);
                          }}
                        >
                          <span className="shrink-0">{item.icon}</span>
                          <span>{item.label}</span>
                        </button>
                      );
                    })}
                  </nav>
 
                  <div className="p-4 border-t border-slate-200/80 flex flex-col gap-3">
                    <div className="flex items-center gap-3 p-2 rounded-lg bg-muted text-left">
                      <Avatar className="h-9 w-9 shrink-0">
                        <AvatarImage src={user?.profile_image_url} alt={user?.full_name || 'User'} />
                        <AvatarFallback className="font-semibold select-none">
                          {user?.full_name ? user.full_name.charAt(0).toUpperCase() : <User size={16} />}
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <div className="text-xs font-bold text-foreground truncate">{user?.full_name || 'System Account'}</div>
                        <div className="text-[10px] text-muted-foreground truncate">{user?.email || ''}</div>
                      </div>
                    </div>
                    
                    <button 
                      className="flex items-center justify-center gap-2 p-2.5 rounded-lg border border-red-500/20 bg-red-500/5 hover:bg-red-600 hover:text-white text-red-500 text-xs font-semibold transition-all border-none cursor-pointer" 
                      onClick={() => {
                        handleLogout();
                        setIsMobileMenuOpen(false);
                      }}
                    >
                      <LogOut size={14} />
                      <span>Sign Out</span>
                    </button>
                  </div>
                </SheetContent>
              </Sheet>
              
              {/* Mobile logo and header branding (Hidden on Desktop to avoid redundant logo branding) */}
              <div className="md:hidden flex items-center gap-2 cursor-pointer" onClick={() => navigate(`/${role}/dashboard`)}>
                <img src={appLogo} alt="Logo" className="h-8 w-8 object-contain" />
                <span className="hidden sm:inline font-bold text-lg tracking-tight text-slate-900 dark:text-white bg-gradient-to-r from-emerald-600 to-blue-600 bg-clip-text text-transparent">
                  EcoCollect
                </span>
              </div>
 
              {/* Desktop Breadcrumbs trail using ShadCN UI component */}
              <div className="hidden md:flex items-center select-none">
                {renderBreadcrumbs()}
              </div>
            </div>
 
            <div className="flex items-center gap-2.5 sm:gap-5">
              {role === 'citizen' && user && (
                <div className={`flex items-center gap-1 px-2 py-1 sm:px-3 sm:py-1.5 rounded-full text-[11px] sm:text-xs font-semibold ${getPointsBg()}`}>
                  <Award size={14} className="text-emerald-500 sm:w-4 sm:h-4" />
                  <span>{user.eco_points} pts</span>
                </div>
              )}
 
              <div className="hidden sm:flex">
                <span className={`text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-full ${getRoleBadgeClass()}`}>
                  {role}
                </span>
              </div>
 
              {/* Notification Bell */}
              {mounted && user && (
                <Popover>
                  <PopoverTrigger asChild>
                    <button
                      className="relative w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 transition-colors border-none bg-transparent cursor-pointer"
                      title="Notifications"
                    >
                      <Bell size={18} />
                      {notifications.length > 0 && (
                        <span className="absolute -top-0.5 -right-0.5 min-w-[15px] h-[15px] px-1 rounded-full bg-red-500 text-white text-[9px] font-bold flex items-center justify-center border border-card shadow-sm animate-pulse">
                          {notifications.length}
                        </span>
                      )}
                    </button>
                  </PopoverTrigger>
                  <PopoverContent align="end" className="w-80 p-0 overflow-hidden bg-card border border-border shadow-lg rounded-xl flex flex-col z-[100]">
                    <div className="flex items-center justify-between p-3.5 border-b border-border bg-muted/20">
                      <span className="font-bold text-sm text-foreground">Notifications</span>
                      {notifications.length > 0 && (
                        <button
                          onClick={handleMarkAllAsRead}
                          className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 hover:underline cursor-pointer bg-transparent border-none p-0"
                        >
                          Clear all
                        </button>
                      )}
                    </div>
                    <div className="max-h-72 overflow-y-auto divide-y divide-border">
                      {notifications.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-10 px-4 text-center gap-2">
                          <div className="w-10 h-10 rounded-full bg-muted/40 flex items-center justify-center text-muted-foreground/60">
                            <Inbox size={20} />
                          </div>
                          <span className="text-xs font-medium text-muted-foreground">You are all caught up!</span>
                        </div>
                      ) : (
                        notifications.map((n) => (
                          <div key={n.id} className="flex items-start gap-2.5 p-3 hover:bg-muted/30 transition-colors group">
                            <div className="flex-1 text-left min-w-0">
                              <p className="text-xs font-semibold text-foreground leading-normal break-words">{n.message}</p>
                              <span className="text-[10px] text-muted-foreground mt-1 block">
                                {new Date(n.created_at).toLocaleString()}
                              </span>
                            </div>
                            <button
                              onClick={() => handleMarkAsRead(n.id)}
                              className="opacity-0 group-hover:opacity-100 focus:opacity-100 w-6 h-6 rounded-md hover:bg-muted flex items-center justify-center text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 transition-all border-none cursor-pointer bg-transparent"
                              title="Mark as read"
                            >
                              <Check size={14} />
                            </button>
                          </div>
                        ))
                      )}
                    </div>
                  </PopoverContent>
                </Popover>
              )}

              {/* Theme Toggle Button */}
              {mounted && (
                <button
                  onClick={() => setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')}
                  className="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 transition-colors border-none bg-transparent cursor-pointer"
                  title="Toggle Theme"
                >
                  {resolvedTheme === 'dark' ? (
                    <Sun size={18} className="text-amber-400 animate-fade-in" />
                  ) : (
                    <Moon size={18} className="text-slate-600 animate-fade-in" />
                  )}
                </button>
              )}
 
              <div className="flex items-center gap-1.5 sm:gap-2.5 cursor-pointer p-0.5 sm:p-1 rounded-lg hover:bg-muted transition-colors" onClick={() => navigate(`/${role}/settings`)}>
                <Avatar className="h-7 w-7 sm:h-8 sm:w-8">
                  <AvatarImage src={user?.profile_image_url} alt={user?.full_name || 'User'} />
                  <AvatarFallback className="font-semibold select-none text-xs sm:text-sm">
                    {user?.full_name ? user.full_name.charAt(0).toUpperCase() : <User size={14} />}
                  </AvatarFallback>
                </Avatar>
                <span className="hidden sm:inline text-sm font-medium text-muted-foreground max-w-[100px] truncate">
                  {user?.full_name?.split(' ')[0] || 'User'}
                </span>
              </div>
            </div>
          </header>

          <main className={`flex-grow min-w-0 bg-background ${
            ['/citizen/map', '/supervisor/map', '/admin/map'].includes(location.pathname) 
              ? 'overflow-hidden h-full w-full' 
              : 'overflow-y-auto px-4 py-8 md:p-8 flex flex-col items-center'
          }`}>
            <div className={['/citizen/map', '/supervisor/map', '/admin/map'].includes(location.pathname) ? 'w-full h-full' : 'w-full max-w-7xl'}>
              {children}
            </div>
          </main>
        </div>
      </div>
    </TooltipProvider>
  );
};
