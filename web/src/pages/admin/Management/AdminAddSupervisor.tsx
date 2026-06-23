import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../../../components/ui/select";
import { ArrowLeft, User, Mail, Phone, Lock, Eye, EyeOff, AlertCircle, CheckCircle, Shield, MapPin } from 'lucide-react';

export const AdminAddSupervisor: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();

  // Generate a random default employee ID to make input easier
  const defaultEmpId = `SUP-${Math.floor(100 + Math.random() * 900)}`;

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [employeeId, setEmployeeId] = useState(defaultEmpId);
  const [assignedArea, setAssignedArea] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [coverageRadius, setCoverageRadius] = useState('10');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [resolving, setResolving] = useState(false);

  const resolveCoordinates = async () => {
    if (!assignedArea) return;
    setResolving(true);
    setError(null);
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(assignedArea)}&limit=1`);
      const data = await res.json();
      if (data && data.length > 0) {
        setLatitude(parseFloat(data[0].lat).toFixed(6));
        setLongitude(parseFloat(data[0].lon).toFixed(6));
        if (data[0].display_name) {
          setAssignedArea(data[0].display_name.split(',').slice(0, 3).join(','));
        }
      } else {
        setError("Could not resolve location coordinates. Please enter manually.");
      }
    } catch (err) {
      console.error(err);
      setError("Geocoding service error. Please enter coordinates manually.");
    } finally {
      setResolving(false);
    }
  };

  const detectCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by your browser.");
      return;
    }
    setLoading(true);
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude: lat, longitude: lon } = position.coords;
        setLatitude(lat.toFixed(6));
        setLongitude(lon.toFixed(6));
        
        try {
          const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
          const data = await res.json();
          if (data && data.display_name) {
            setAssignedArea(data.display_name.split(',').slice(0, 3).join(','));
          }
        } catch (err) {
          setAssignedArea(`Coordinates (${lat.toFixed(4)}, ${lon.toFixed(4)})`);
        }
        setLoading(false);
      },
      (err) => {
        setError("Failed to get current location. Please type manually.");
        setLoading(false);
      }
    );
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!fullName || !email || !phone || !employeeId || !assignedArea || !latitude || !longitude || !coverageRadius || !password || !confirmPassword) {
      setError('Please fill in all required fields.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        full_name: fullName,
        employee_id: employeeId,
        email,
        phone,
        assigned_area: assignedArea,
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude),
        coverage_radius: parseFloat(coverageRadius),
        password
      };

      await apiCall('/auth/supervisor/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      setSuccess(`Supervisor account for ${fullName} created successfully!`);
      setTimeout(() => navigate('/admin/supervisors'), 1500);
    } catch (err: any) {
      setError(err.message || 'Failed to create supervisor account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-left">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate('/admin/supervisors')}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Management</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Supervisor Provisioning</h1>
        </div>
      </div>

      <form onSubmit={handleCreate} className="flex flex-col gap-6 w-full">
        {success && (
          <div className="flex items-center gap-3 p-4 text-sm rounded-lg text-left border bg-emerald-50 dark:bg-emerald-950/20 text-emerald-800 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/30">
            <CheckCircle size={18} className="shrink-0" />
            <span>{success}</span>
          </div>
        )}
        {error && (
          <div className="flex items-center gap-3 p-4 text-sm rounded-lg text-left border bg-red-50 dark:bg-red-950/20 text-red-800 dark:text-red-400 border-red-200 dark:border-red-900/30">
            <AlertCircle size={18} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch">
          {/* Left Panel: Personal Details */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col gap-4 h-full justify-between">
            <div className="flex flex-col gap-4">
              <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
                <User size={18} /> Personal Information
              </h3>
              
              <Input 
                label="Full Name *" 
                placeholder="Sarah Jenkins" 
                value={fullName} 
                onChange={e => setFullName(e.target.value)} 
                icon={<User size={20} className="text-muted-foreground" />} 
                required
                disabled={loading}
              />

              <Input 
                label="Email Address *" 
                placeholder="s.jenkins@city.gov" 
                type="email"
                value={email} 
                onChange={e => setEmail(e.target.value)} 
                icon={<Mail size={20} className="text-muted-foreground" />} 
                required
                disabled={loading}
              />

              <Input 
                label="Phone Number *" 
                placeholder="9876543210" 
                value={phone} 
                onChange={e => setPhone(e.target.value)} 
                icon={<Phone size={20} className="text-muted-foreground" />} 
                required
                disabled={loading}
              />
            </div>
          </div>

          {/* Right Panel: Credentials & Allocation */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)] flex flex-col gap-4 h-full justify-between">
            <div className="flex flex-col gap-4">
              <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
                <Shield size={18} /> Allocation & Security Credentials
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input 
                  label="Employee ID *" 
                  placeholder="e.g. SUP-402" 
                  value={employeeId} 
                  onChange={e => setEmployeeId(e.target.value)} 
                  icon={<Shield size={20} className="text-muted-foreground" />} 
                  required
                  disabled={loading}
                />
              </div>

              <div className="flex flex-col gap-1.5 w-full">
                <div className="flex flex-wrap items-center justify-between gap-x-2 gap-y-1 mb-0.5 w-full">
                  <label className="text-sm font-semibold text-foreground select-none">Assigned Locality Name *</label>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={resolveCoordinates}
                      disabled={resolving || !assignedArea}
                      className="text-[11px] font-bold px-2.5 py-1 rounded-md bg-indigo-50/50 dark:bg-indigo-950/20 text-indigo-600 dark:text-indigo-400 border border-indigo-100/50 dark:border-indigo-900/30 hover:bg-indigo-100/50 dark:hover:bg-indigo-900/35 disabled:opacity-40 cursor-pointer flex items-center gap-1 transition-all duration-150"
                    >
                      {resolving ? "Resolving..." : "Resolve GPS"}
                    </button>
                    <button
                      type="button"
                      onClick={detectCurrentLocation}
                      disabled={loading}
                      className="text-[11px] font-bold px-2.5 py-1 rounded-md bg-emerald-50/50 dark:bg-emerald-950/20 text-emerald-600 dark:text-emerald-400 border border-emerald-100/50 dark:border-emerald-900/30 hover:bg-emerald-100/50 dark:hover:bg-emerald-900/35 disabled:opacity-40 cursor-pointer flex items-center gap-1 transition-all duration-150"
                    >
                      <MapPin size={11} className="shrink-0" />
                      <span>Detect Location</span>
                    </button>
                  </div>
                </div>
                <Input 
                  placeholder="e.g. Connaught Place, Delhi" 
                  value={assignedArea} 
                  onChange={e => setAssignedArea(e.target.value)} 
                  icon={<MapPin size={20} className="text-muted-foreground" />} 
                  required
                  disabled={loading}
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Input 
                  label="Latitude *" 
                  placeholder="e.g. 28.6139" 
                  type="number"
                  step="any"
                  value={latitude} 
                  onChange={e => setLatitude(e.target.value)} 
                  required
                  disabled={loading}
                />

                <Input 
                  label="Longitude *" 
                  placeholder="e.g. 77.2090" 
                  type="number"
                  step="any"
                  value={longitude} 
                  onChange={e => setLongitude(e.target.value)} 
                  required
                  disabled={loading}
                />

                <Input 
                  label="Radius (KM) *" 
                  placeholder="e.g. 10" 
                  type="number"
                  step="any"
                  value={coverageRadius} 
                  onChange={e => setCoverageRadius(e.target.value)} 
                  required
                  disabled={loading}
                />
              </div>

              {latitude && longitude && (
                <div className="text-[11px] font-semibold text-emerald-600 flex items-center gap-1 -mt-2">
                  <CheckCircle size={12} />
                  <span>GPS Coordinates Mapped Successfully</span>
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input 
                  label="Temporary Password *" 
                  placeholder="••••••••" 
                  value={password} 
                  onChange={e => setPassword(e.target.value)} 
                  icon={<Lock size={20} className="text-muted-foreground" />} 
                  type={passwordVisible ? "text" : "password"}
                  rightIcon={
                    <button 
                      type="button" 
                      style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                      onClick={() => setPasswordVisible(!passwordVisible)}
                    >
                      {passwordVisible ? <EyeOff size={20} className="text-muted-foreground" /> : <Eye size={20} className="text-muted-foreground" />}
                    </button>
                  }
                  required
                  disabled={loading}
                />

                <Input 
                  label="Confirm Password *" 
                  placeholder="••••••••" 
                  value={confirmPassword} 
                  onChange={e => setConfirmPassword(e.target.value)} 
                  icon={<Lock size={20} className="text-muted-foreground" />} 
                  type={confirmPasswordVisible ? "text" : "password"}
                  rightIcon={
                    <button 
                      type="button" 
                      style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                      onClick={() => setConfirmPasswordVisible(!confirmPasswordVisible)}
                    >
                      {confirmPasswordVisible ? <EyeOff size={20} className="text-muted-foreground" /> : <Eye size={20} className="text-muted-foreground" />}
                    </button>
                  }
                  required
                  disabled={loading}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end mt-4">
          <Button 
            type="submit" 
            disabled={loading}
            className="bg-indigo-600 hover:bg-indigo-700 text-white shadow-md hover:shadow-lg font-bold rounded-xl text-base px-8 py-3.5 w-full sm:w-auto min-w-[260px] transition-all shrink-0 cursor-pointer"
          >
            {loading ? 'Registering Staff Account...' : 'Register Supervisor Profile'}
          </Button>
        </div>
      </form>
    </div>
  );
};
