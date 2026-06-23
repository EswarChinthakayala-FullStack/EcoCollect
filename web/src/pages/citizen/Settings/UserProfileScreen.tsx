import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { 
  User, Edit, Settings as SettingsIcon, HelpCircle, 
  Info, LogOut, ChevronRight, MapPin, Phone, Mail, Award, Calendar
} from 'lucide-react';

interface ProfileData {
  fullName: string;
  email: string;
  phone: string;
  dob: string;
  gender: string;
  address: string;
  city: string;
  country: string;
  alternatePhone?: string;
  emergencyPhone?: string;
  avatar?: string;
}

export const UserProfileScreen: React.FC = () => {
  const navigate = useNavigate();
  const { logout, apiCall, backendHost } = useAuth();
  
  const [profile, setProfile] = useState<ProfileData>({
    fullName: '',
    email: '',
    phone: '',
    dob: '',
    gender: '',
    address: '',
    city: '',
    country: '',
    alternatePhone: '',
    emergencyPhone: '',
    avatar: ''
  });

  const [stats, setStats] = useState({
    total_reports: 0,
    resolved_reports: 0,
    pending_reports: 0,
    eco_points: 0
  });
  
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 1. Fetch Profile from Database
    const fetchProfile = async () => {
      try {
        const data = await apiCall('/profile');
        setProfile({
          fullName: data.full_name || 'Citizen User',
          email: data.email || 'citizen@example.com',
          phone: data.phone || 'Not provided',
          dob: data.dob || 'Not provided',
          gender: data.gender || 'Not provided',
          address: data.address || '',
          city: data.city || '',
          country: data.country || '',
          avatar: data.profile_image_url || ''
        });
      } catch (err) {
        console.error('Failed to load profile from database, loading fallback', err);
        // Fallback to local storage
        const stored = localStorage.getItem('citizenProfile');
        if (stored) {
          try {
            const localData = JSON.parse(stored);
            setProfile({
              fullName: localData.fullName || 'Citizen User',
              email: localData.email || 'citizen@example.com',
              phone: localData.phone || 'Not provided',
              dob: localData.dob || 'Not provided',
              gender: localData.gender || 'Not provided',
              address: localData.address || '',
              city: localData.city || '',
              country: localData.country || '',
              avatar: localData.avatar || ''
            });
          } catch (e) {
            console.error('Failed to parse fallback profile', e);
          }
        }
      }
    };
    
    // 2. Fetch dashboard stats for account activity
    const fetchDashboardStats = async () => {
      try {
        const statsData = await apiCall<{
          total_reports: number;
          resolved_reports: number;
          pending_reports: number;
          eco_points: number;
        }>('/citizen/dashboard');
        setStats(statsData);
      } catch (err) {
        console.error('Failed to load stats:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
    fetchDashboardStats();
  }, [apiCall]);

  const handleLogout = () => {
    logout();
    navigate('/citizen/login');
  };

  return (
    <div className="w-full text-foreground relative animate-slide-up pb-12">
      
      {/* 2-Column Options Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start w-full">
        
        {/* Profile Card Header Column */}
        <div className="lg:col-span-1 flex flex-col gap-6">
          <div className="flex flex-col items-center text-center p-6 bg-card text-foreground rounded-2xl relative overflow-hidden shadow-sm border border-border">
            {/* Decorative gradients */}
            <div className="absolute -right-10 -top-10 w-40 h-40 bg-emerald-500/5 dark:bg-emerald-500/10 rounded-full blur-2xl" />
            <div className="absolute -left-10 -bottom-10 w-40 h-40 bg-emerald-600/5 dark:bg-emerald-600/10 rounded-full blur-2xl" />
            
            <div className="relative w-24 h-24 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center border border-border mb-4 z-10 overflow-hidden">
              {profile.avatar ? (
                <img 
                  src={profile.avatar.startsWith('data:') ? profile.avatar : (profile.avatar.startsWith('http') ? profile.avatar : `${backendHost}${profile.avatar.startsWith('/') ? '' : '/'}${profile.avatar}`)} 
                  alt="Avatar" 
                  className="w-full h-full object-cover" 
                />
              ) : (
                <User size={40} className="text-muted-foreground" />
              )}
              <div className="absolute bottom-0.5 right-0.5 w-4 h-4 bg-emerald-500 rounded-full border-2 border-card" />
            </div>
            
            <span className="text-xl font-extrabold z-10 leading-tight">{profile.fullName}</span>
            <span className="text-xs text-muted-foreground font-semibold z-10 mt-1">{profile.email}</span>
            
            <div className="inline-flex mt-4 px-3.5 py-1.5 text-[10px] font-extrabold uppercase tracking-wider bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 rounded-full z-10">
              Eco Champion
            </div>
          </div>

          {/* Account Statistics card */}
          <div className="bg-card border border-border shadow-sm rounded-2xl p-6 flex flex-col gap-4">
            <h3 className="text-sm font-bold text-emerald-600 dark:text-emerald-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
              <Award size={18} /> Points & Activity
            </h3>
            <div className="grid grid-cols-3 gap-3">
              <div className="bg-muted border border-border shadow-sm rounded-xl p-3 flex flex-col items-center justify-center transition-all duration-200 hover:-translate-y-0.5">
                <span className="text-lg font-extrabold text-foreground">{loading ? '...' : stats.total_reports}</span>
                <span className="text-[9px] uppercase font-bold tracking-wider text-muted-foreground mt-1 text-center leading-tight">Total Filed</span>
              </div>
              <div className="bg-muted border border-border shadow-sm rounded-xl p-3 flex flex-col items-center justify-center transition-all duration-200 hover:-translate-y-0.5">
                <span className="text-lg font-extrabold text-foreground">{loading ? '...' : stats.resolved_reports}</span>
                <span className="text-[9px] uppercase font-bold tracking-wider text-muted-foreground mt-1 text-center leading-tight">Resolved</span>
              </div>
              <div className="bg-muted border border-border shadow-sm rounded-xl p-3 flex flex-col items-center justify-center transition-all duration-200 hover:-translate-y-0.5">
                <span className="text-lg font-extrabold text-foreground text-amber-500">{loading ? '...' : stats.eco_points}</span>
                <span className="text-[9px] uppercase font-bold tracking-wider text-muted-foreground mt-1 text-center leading-tight">Eco Points</span>
              </div>
            </div>
          </div>

          {/* Logout Button */}
          <button 
            className="w-full py-3 flex items-center justify-center gap-2 bg-red-500/10 hover:bg-red-600 hover:text-white text-red-600 dark:text-red-400 border border-red-500/20 font-bold rounded-xl text-sm transition-all duration-150 cursor-pointer shadow-sm hover:shadow"
            onClick={handleLogout}
          >
            <LogOut size={16} />
            <span>Logout Session</span>
          </button>
        </div>

        {/* Right Columns (spanning 2 columns on lg screen) */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          
          {/* Quick Actions List Card */}
          <div className="bg-card border border-border shadow-sm rounded-2xl p-6 flex flex-col gap-2">
            <h3 className="text-sm font-bold text-emerald-600 dark:text-emerald-400 border-b border-border/60 pb-2.5 flex items-center gap-2 mb-2">
              <SettingsIcon size={18} /> Account Preferences
            </h3>
            
            <div className="flex flex-col gap-1.5 divide-y divide-border/40">
              <div 
                className="flex items-center gap-3.5 p-2 py-3 rounded-xl transition-all duration-150 hover:bg-muted/45 cursor-pointer" 
                onClick={() => navigate('/citizen/profile/edit')}
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                  <Edit size={20} />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">Edit Profile Info</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Upload photo, change phone number and personal info</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>

              <div 
                className="flex items-center gap-3.5 p-2 py-3 rounded-xl transition-all duration-150 hover:bg-muted/45 cursor-pointer" 
                onClick={() => navigate('/citizen/settings')}
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                  <SettingsIcon size={20} />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">App Preferences</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Notifications, geolocation updates, dark mode</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>

              <div 
                className="flex items-center gap-3.5 p-2 py-3 rounded-xl transition-all duration-150 hover:bg-muted/45 cursor-pointer" 
                onClick={() => navigate('/citizen/help')}
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                  <HelpCircle size={20} />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">Help & Support Center</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Browse FAQs and contact customer support desk</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>

              <div 
                className="flex items-center gap-3.5 p-2 pt-3 rounded-xl transition-all duration-150 hover:bg-muted/45 cursor-pointer" 
                onClick={() => navigate('/citizen/about')}
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                  <Info size={20} />
                </div>
                <div className="flex-1 flex flex-col text-left">
                  <span className="text-sm font-bold text-foreground">About Application</span>
                  <span className="text-xs text-muted-foreground mt-0.5">Read terms of service, platform version and licenses</span>
                </div>
                <ChevronRight size={18} className="text-muted-foreground" />
              </div>
            </div>
          </div>

          {/* Citizen Detailed Information Card */}
          <div className="bg-card border border-border shadow-sm rounded-2xl p-6 flex flex-col gap-1.5 divide-y divide-border/40">
            <h3 className="text-sm font-bold text-emerald-600 dark:text-emerald-400 pb-2.5 flex items-center gap-2">
              <User size={18} /> Profile Details
            </h3>
            
            <div className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl">
              <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <MapPin size={20} />
              </div>
              <div className="flex-1 flex flex-col text-left">
                <span className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Residential Address</span>
                <span className="text-sm font-semibold text-foreground">
                  {profile.address ? `${profile.address}, ${profile.city}, ${profile.country}` : 'Not provided'}
                </span>
              </div>
            </div>
            
            <div className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl">
              <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Phone size={20} />
              </div>
              <div className="flex-1 flex flex-col text-left">
                <span className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Primary Contact</span>
                <span className="text-sm font-semibold text-foreground">{profile.phone || 'Not provided'}</span>
              </div>
            </div>

            <div className="flex items-center gap-3.5 p-2 py-3.5 rounded-xl">
              <div className="w-10 h-10 rounded-xl bg-muted border border-border flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Calendar size={20} />
              </div>
              <div className="flex-1 flex flex-col text-left">
                <span className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Personal Details</span>
                <span className="text-sm font-semibold text-foreground">
                  DOB: {profile.dob || 'N/A'} • Gender: {profile.gender || 'N/A'}
                </span>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
};
