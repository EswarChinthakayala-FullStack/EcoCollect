import React from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, LogIn, UserPlus, Key, KeyRound, BookOpen, 
  LayoutDashboard, FileText, MapPin, Navigation, Settings, 
  User, Edit, History, ClipboardList, Shield, Users, UserPlus2 
} from 'lucide-react';

const sections = [
  {
    title: 'Citizen — Auth',
    color: '#10B981',
    links: [
      { name: 'Citizen Login', path: '/citizen/login', icon: LogIn },
      { name: 'Create Account', path: '/citizen/signup', icon: UserPlus },
      { name: 'Forgot Password', path: '/citizen/forgot-password', icon: Key },
      { name: 'Reset Password', path: '/citizen/reset-password', icon: KeyRound },
      { name: 'Onboarding', path: '/onboarding', icon: BookOpen },
    ],
  },
  {
    title: 'Citizen — App',
    color: '#10B981',
    links: [
      { name: 'Dashboard', path: '/citizen/dashboard', icon: LayoutDashboard },
      { name: 'Report Waste', path: '/citizen/report', icon: FileText },
      { name: 'Select Category', path: '/citizen/report/category', icon: ClipboardList },
      { name: 'Confirm Location', path: '/citizen/report/location', icon: MapPin },
      { name: 'Report Submitted', path: '/citizen/report/success', icon: FileText },
      { name: 'Map', path: '/citizen/map', icon: Navigation },
      { name: 'Nearby Issues', path: '/citizen/map/nearby', icon: MapPin },
      { name: 'Settings', path: '/citizen/settings', icon: Settings },
      { name: 'Profile', path: '/citizen/profile', icon: User },
      { name: 'Edit Profile', path: '/citizen/profile/edit', icon: Edit },
    ],
  },
  {
    title: 'Supervisor — Auth',
    color: '#3B82F6',
    links: [
      { name: 'Supervisor Login', path: '/supervisor/login', icon: LogIn },
    ],
  },
  {
    title: 'Supervisor — App',
    color: '#3B82F6',
    links: [
      { name: 'Dashboard', path: '/supervisor/dashboard', icon: LayoutDashboard },
      { name: 'Report Details', path: '/supervisor/report/1', icon: FileText },
      { name: 'Report Status', path: '/supervisor/report/1/status', icon: ClipboardList },
      { name: 'History', path: '/supervisor/history', icon: History },
      { name: 'Profile', path: '/supervisor/profile', icon: User },
      { name: 'Settings', path: '/supervisor/settings', icon: Settings },
    ],
  },
  {
    title: 'Admin — Auth',
    color: '#4F46E5',
    links: [
      { name: 'Admin Login', path: '/admin/login', icon: Shield },
    ],
  },
  {
    title: 'Admin — App',
    color: '#4F46E5',
    links: [
      { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
      { name: 'Reports', path: '/admin/reports', icon: ClipboardList },
      { name: 'Report Details', path: '/admin/report/8942', icon: FileText },
      { name: 'Supervisors', path: '/admin/supervisors', icon: Users },
      { name: 'Add Supervisor', path: '/admin/supervisors/add', icon: UserPlus2 },
      { name: 'Profile', path: '/admin/profile', icon: User },
      { name: 'Edit Profile', path: '/admin/profile/edit', icon: Edit },
      { name: 'Settings', path: '/admin/settings', icon: Settings },
    ],
  },
];

export const AllPagesScreen: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen w-full bg-slate-50 dark:bg-slate-950 font-sans transition-colors duration-300">
      <div className="max-w-6xl mx-auto p-6 md:p-12 text-left">
        <button 
          className="inline-flex items-center gap-2 text-sm font-semibold text-slate-500 hover:text-slate-900 dark:text-muted-foreground dark:hover:text-foreground transition-colors mb-6 cursor-pointer bg-transparent border-none" 
          onClick={() => navigate('/')}
        >
          <ArrowLeft size={16} /> Back to Welcome
        </button>
        <h1 className="text-3xl font-extrabold text-slate-900 dark:text-foreground mb-2">All Pages</h1>
        <p className="text-sm text-slate-500 dark:text-muted-foreground mb-10">
          Quick links to every screen in WasteReporting ({sections.reduce((sum, s) => sum + s.links.length, 0)} pages)
        </p>

        {sections.map((section) => (
          <div className="mb-10" key={section.title}>
            <h2 className="text-lg font-bold text-slate-900 dark:text-foreground mb-4 border-b border-slate-200 dark:border-border/60 pb-2">{section.title}</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
              {section.links.map((link) => {
                const Icon = link.icon;
                return (
                  <div
                    key={link.path}
                    className="flex items-center gap-3.5 p-4 bg-white dark:bg-card border border-slate-200/80 dark:border-border/40 rounded-xl cursor-pointer hover:shadow-md hover:border-slate-300 dark:hover:border-border/80 hover:-translate-y-0.5 transition-all"
                    onClick={() => navigate(link.path)}
                  >
                    <div className="w-10 h-10 rounded-lg flex items-center justify-center shrink-0" style={{ backgroundColor: section.color + '15' }}>
                      <Icon size={18} color={section.color} />
                    </div>
                    <div className="min-w-0">
                      <div className="text-sm font-semibold text-slate-900 dark:text-foreground truncate">{link.name}</div>
                      <div className="text-[10px] font-mono text-slate-400 dark:text-muted-foreground mt-0.5 truncate">{link.path}</div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
