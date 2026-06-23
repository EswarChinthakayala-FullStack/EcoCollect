import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Camera, User, Eye, EyeOff, AlertCircle, Save, ArrowLeft, Shield, Lock, MapPin, Contact2, FileText, Calendar as CalendarIcon } from 'lucide-react';
import { format } from 'date-fns';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../../components/ui/select';
import { Popover, PopoverContent, PopoverTrigger } from '../../../components/ui/popover';
import { Calendar } from '../../../components/ui/calendar';

export const AdminEditProfile: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { user, apiCall, backendHost } = useAuth();

  // Profile fields state
  const [fullName, setFullName] = useState('');
  const [dob, setDob] = useState('');
  const [gender, setGender] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [country, setCountry] = useState('');
  const [avatar, setAvatar] = useState('');
  
  // Security settings state
  const [storedPassword, setStoredPassword] = useState('password123');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // UI state
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load from database on mount
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await apiCall<any>('/profile');
        setFullName(data.full_name || '');
        setDob(data.dob || '');
        setGender(data.gender || 'Male');
        setEmail(data.email || '');
        setPhone(data.phone || '');
        setAddress(data.address || '');
        setCity(data.city || '');
        setCountry(data.country || '');
        setAvatar(data.profile_image_url || '');
        setStoredPassword('password123');
      } catch (err) {
        console.error('Failed to load profile from database', err);
        // Fallback to local storage
        if (user) {
          setFullName(user.full_name || '');
          setEmail(user.email || '');
          setPhone(user.phone || '');
          setAvatar(user.profile_image_url || '');
        }
      }
    };
    fetchProfile();
  }, [apiCall, user]);

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatar(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  const handleNumericInput = (val: string, setter: (cleaned: string) => void) => {
    const numeric = val.replace(/\D/g, '');
    setter(numeric);
  };

  const handleSave = async () => {
    setErrorMsg(null);
    setSuccessMsg(null);

    // Required fields check
    if (!fullName.trim()) {
      setErrorMsg('Full Name is required.');
      return;
    }
    if (!phone.trim()) {
      setErrorMsg('Phone Number is required.');
      return;
    }

    // Security changes validation
    if (newPassword || currentPassword || confirmPassword) {
      if (!currentPassword) {
        setErrorMsg('Please enter your Current Password to make security changes.');
        return;
      }
      if (!newPassword) {
        setErrorMsg('Please enter a New Password.');
        return;
      }
      if (newPassword.length < 6) {
        setErrorMsg('New Password must be at least 6 characters.');
        return;
      }
      if (newPassword !== confirmPassword) {
        setErrorMsg('New Password and Confirm Password do not match.');
        return;
      }
    }

    setIsSubmitting(true);

    try {
      // 1. Upload Avatar if it is base64
      let uploadedImageUrl = avatar;
      if (avatar && avatar.startsWith('data:')) {
        const response = await fetch(avatar);
        const blob = await response.blob();
        const formData = new FormData();
        formData.append('file', blob, 'profile_avatar.jpg');
        
        const uploadRes = await fetch(`${backendHost}/api/upload`, {
          method: 'POST',
          body: formData,
        });
        if (uploadRes.ok) {
          const uploadData = await uploadRes.json();
          uploadedImageUrl = uploadData.file_url;
        }
      }

      // 2. Put profile data to database
      const profileToPut: any = {
        full_name: fullName,
        phone: phone,
        dob: dob || null,
        gender: gender || null,
        address: address || null,
        city: city || null,
        country: country || null,
        profile_image_url: uploadedImageUrl || null
      };

      const updatedUser = await apiCall<any>('/profile', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(profileToPut)
      });

      // 3. If password was edited, change password
      if (newPassword) {
        await apiCall('/change-password', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            current_password: currentPassword,
            new_password: newPassword
          })
        });
      }

      // Update local storage structures for context reload
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      setSuccessMsg('Profile updated successfully!');
      
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');

      setTimeout(() => {
        navigate('/admin/profile');
      }, 1500);
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.message || 'Failed to update profile. Please verify current password.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="w-full text-left text-foreground relative animate-slide-up pb-12 py-5">
      {/* Page Header */}
      <div className="flex items-center gap-4 mb-6">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Settings</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Edit Profile</h1>
        </div>
      </div>

      {/* Alerts */}
      {successMsg && (
        <div className="flex items-center p-3.5 mb-6 rounded-xl text-sm font-semibold border text-left bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 animate-fade-in">
          <span>{successMsg}</span>
        </div>
      )}
      {errorMsg && (
        <div className="flex items-center p-3.5 mb-6 rounded-xl text-sm font-semibold border text-left bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20 animate-fade-in">
          <AlertCircle size={18} className="mr-2 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Grid columns layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 w-full items-start">
        
        {/* Left Column */}
        <div className="flex flex-col gap-6 w-full">
          
          {/* Card 1: Avatar Editor & Basic Info */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-2xl p-6 flex flex-col gap-5 text-left">
            <h3 className="font-bold text-sm text-indigo-600 dark:text-indigo-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
              <FileText size={18} /> Basic Information
            </h3>

            {/* Profile Avatar Editor Component */}
            <div className="flex items-center gap-5 my-1">
              <div 
                className="relative cursor-pointer rounded-full transition-transform hover:scale-105" 
                onClick={triggerFileInput} 
                role="button" 
                tabIndex={0} 
                onKeyDown={(e) => e.key === 'Enter' && triggerFileInput()} 
                aria-label="Upload Profile Photo"
              >
                <div className="w-20 h-20 rounded-full bg-slate-100 dark:bg-slate-800 border-4 border-card shadow-md flex items-center justify-center overflow-hidden">
                  {avatar ? (
                    <img 
                      src={avatar.startsWith('data:') ? avatar : (avatar.startsWith('http') ? avatar : `${backendHost}${avatar.startsWith('/') ? '' : '/'}${avatar}`)} 
                      alt="Profile Avatar" 
                      className="w-full h-full object-cover" 
                    />
                  ) : (
                    <Shield size={36} className="text-indigo-650 dark:text-indigo-400" />
                  )}
                </div>
                <div className="absolute bottom-0 right-0 w-6.5 h-6.5 rounded-full bg-indigo-600 border-2 border-card flex items-center justify-center shadow-sm">
                  <Camera size={12} className="text-white" />
                </div>
              </div>
              <div className="flex flex-col text-left">
                <span className="text-sm font-bold text-foreground">Admin Image Profile</span>
                <span className="text-xs text-muted-foreground mt-0.5">Click to upload photo. PNG, JPG or GIF up to 5MB.</span>
              </div>
              <input 
                type="file" 
                accept="image/*" 
                className="hidden"
                ref={fileInputRef} 
                onChange={handleAvatarChange} 
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-muted-foreground">Full Name *</label>
              <input 
                className="font-sans bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10" 
                value={fullName} 
                onChange={e => setFullName(e.target.value)} 
                placeholder="Chief Administrator Name"
              />
            </div>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-muted-foreground">Date of Birth</label>
                <Popover>
                  <PopoverTrigger asChild>
                    <button 
                      type="button"
                      className="w-full bg-background border border-border rounded-xl !h-11 px-4 text-sm text-foreground outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10 cursor-pointer flex items-center justify-between text-left"
                    >
                      <span className="truncate">{dob ? format(new Date(dob + 'T00:00:00'), 'PPP') : 'Pick DOB Date'}</span>
                      <CalendarIcon size={16} className="text-muted-foreground shrink-0 ml-2" />
                    </button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0 bg-popover border border-border rounded-xl shadow-lg" align="start">
                    <Calendar
                      mode="single"
                      selected={dob ? new Date(dob + 'T00:00:00') : undefined}
                      onSelect={(date) => {
                        if (date) {
                          setDob(format(date, 'yyyy-MM-dd'));
                        } else {
                          setDob('');
                        }
                      }}
                      captionLayout="dropdown"
                      startMonth={new Date(1900, 0)}
                      endMonth={new Date()}
                      disabled={{ after: new Date() }}
                    />
                  </PopoverContent>
                </Popover>
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-muted-foreground">Gender</label>
                <Select value={gender} onValueChange={setGender}>
                  <SelectTrigger className="w-full bg-background border border-border rounded-xl !h-11 px-4 text-sm text-foreground outline-none focus:border-indigo-650 focus:ring-2 focus:ring-indigo-500/10 cursor-pointer flex justify-between items-center">
                    <SelectValue placeholder="Select gender" />
                  </SelectTrigger>
                  <SelectContent className="bg-popover border border-border rounded-xl shadow-lg text-foreground">
                    <SelectItem value="Male">Male</SelectItem>
                    <SelectItem value="Female">Female</SelectItem>
                    <SelectItem value="Other">Other</SelectItem>
                    <SelectItem value="Prefer not to say">Prefer not to say</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Card 2: Contact Information */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-2xl p-6 flex flex-col gap-4 text-left">
            <h3 className="font-bold text-sm text-indigo-600 dark:text-indigo-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
              <Contact2 size={18} /> Contact Information
            </h3>
            
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-muted-foreground">Email Address</label>
              <input 
                className="font-sans bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10 opacity-70" 
                type="email" 
                value={email} 
                placeholder="admin@ecocollect.city"
                disabled
              />
            </div>
            
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-muted-foreground">Contact Phone Number *</label>
              <input 
                className="font-sans bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10" 
                type="tel"
                value={phone} 
                onChange={e => handleNumericInput(e.target.value, setPhone)} 
                placeholder="e.g. 9876543210"
                maxLength={15}
              />
            </div>
          </div>
        </div>

        {/* Right Column */}
        <div className="flex flex-col gap-6 w-full">
          
          {/* Card 3: Address Information */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-2xl p-6 flex flex-col gap-4 text-left">
            <h3 className="font-bold text-sm text-indigo-600 dark:text-indigo-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
              <MapPin size={18} /> Address Location Info
            </h3>
            
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-muted-foreground">Street Address</label>
              <input 
                className="font-sans bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10" 
                value={address} 
                onChange={e => setAddress(e.target.value)} 
                placeholder="Command Center, Sector-5"
              />
            </div>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-muted-foreground">City</label>
                <input 
                  className="font-sans bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10" 
                  value={city} 
                  onChange={e => setCity(e.target.value)} 
                  placeholder="Metro City"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-muted-foreground">Country</label>
                <input 
                  className="font-sans bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-indigo-600 focus:ring-2 focus:ring-indigo-500/10" 
                  value={country} 
                  onChange={e => setCountry(e.target.value)} 
                  placeholder="Region"
                />
              </div>
            </div>
          </div>

          {/* Card 4: Security Credentials */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-2xl p-6 flex flex-col gap-4 text-left">
            <h3 className="font-bold text-sm text-indigo-600 dark:text-indigo-400 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-border/60">
              <Lock size={18} /> Update Security Password
            </h3>
            
            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-muted-foreground">Current Password</label>
                <div className="relative flex w-full">
                  <input 
                    className="font-sans bg-background border border-border rounded-xl h-11 pl-4 pr-12 text-sm text-foreground outline-none transition-all focus:border-indigo-650 focus:ring-2 focus:ring-indigo-500/10 w-full" 
                    type={showCurrentPassword ? 'text' : 'password'} 
                    value={currentPassword} 
                    onChange={e => setCurrentPassword(e.target.value)} 
                    placeholder="••••••••"
                  />
                  <button 
                    type="button" 
                    className="absolute right-3 top-1/2 -translate-y-1/2 bg-transparent border-none cursor-pointer flex items-center justify-center p-1.5 rounded text-muted-foreground hover:text-foreground hover:bg-muted/40 transition-colors" 
                    onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                    aria-label={showCurrentPassword ? "Hide current password" : "Show current password"}
                  >
                    {showCurrentPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-muted-foreground">New Password</label>
                  <div className="relative flex w-full">
                    <input 
                      className="font-sans bg-background border border-border rounded-xl h-11 pl-4 pr-12 text-sm text-foreground outline-none transition-all focus:border-indigo-650 focus:ring-2 focus:ring-indigo-500/10 w-full" 
                      type={showNewPassword ? 'text' : 'password'} 
                      value={newPassword} 
                      onChange={e => setNewPassword(e.target.value)} 
                      placeholder="••••••••"
                    />
                    <button 
                      type="button" 
                      className="absolute right-3 top-1/2 -translate-y-1/2 bg-transparent border-none cursor-pointer flex items-center justify-center p-1.5 rounded text-muted-foreground hover:text-foreground hover:bg-muted/40 transition-colors" 
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      aria-label={showNewPassword ? "Hide new password" : "Show new password"}
                    >
                      {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-muted-foreground">Confirm Password</label>
                  <div className="relative flex w-full">
                    <input 
                      className="font-sans bg-background border border-border rounded-xl h-11 pl-4 pr-12 text-sm text-foreground outline-none transition-all focus:border-indigo-650 focus:ring-2 focus:ring-indigo-500/10 w-full" 
                      type={showConfirmPassword ? 'text' : 'password'} 
                      value={confirmPassword} 
                      onChange={e => setConfirmPassword(e.target.value)} 
                      placeholder="••••••••"
                    />
                    <button 
                      type="button" 
                      className="absolute right-3 top-1/2 -translate-y-1/2 bg-transparent border-none cursor-pointer flex items-center justify-center p-1.5 rounded text-muted-foreground hover:text-foreground hover:bg-muted/40 transition-colors" 
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
                    >
                      {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Action Save Changes Button */}
          <button 
            className="w-full h-11 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold border-none cursor-pointer flex items-center justify-center shadow-md shadow-indigo-600/20 active:scale-[0.99] transition-all disabled:opacity-50 disabled:pointer-events-none" 
            onClick={handleSave}
            disabled={isSubmitting}
          >
            <Save size={16} className="mr-2" />
            {isSubmitting ? 'Saving Profile Changes...' : 'Save Settings Changes'}
          </button>
        </div>

      </div>
    </div>
  );
};
