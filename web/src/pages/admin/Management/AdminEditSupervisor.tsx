import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';
import { ArrowLeft, User, Mail, Phone, Map, Shield, MapPin, AlertCircle, CheckCircle } from 'lucide-react';

interface SupervisorRecord {
  id: number;
  full_name: string;
  email: string;
  employee_id: string;
  assigned_area?: string;
  assigned_reports: number;
  resolved_reports: number;
  latitude?: number | null;
  longitude?: number | null;
  coverage_radius?: number | null;
}

export const AdminEditSupervisor: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { apiCall } = useAuth();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [employeeId, setEmployeeId] = useState('');
  const [assignedArea, setAssignedArea] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [coverageRadius, setCoverageRadius] = useState('10');
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [resolving, setResolving] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    const fetchSupervisor = async () => {
      try {
        const list = await apiCall<SupervisorRecord[]>('/admin/supervisors');
        const found = list.find(s => s.id === Number(id));
        if (found) {
          setFullName(found.full_name);
          setEmail(found.email);
          setEmployeeId(found.employee_id);
          setAssignedArea(found.assigned_area || '');
          setLatitude(found.latitude !== null && found.latitude !== undefined ? found.latitude.toString() : '');
          setLongitude(found.longitude !== null && found.longitude !== undefined ? found.longitude.toString() : '');
          setCoverageRadius(found.coverage_radius !== null && found.coverage_radius !== undefined ? found.coverage_radius.toString() : '10');
        }
      } catch (err) {
        console.error('Failed to load supervisor details:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchSupervisor();
  }, [id, apiCall]);

  const resolveCoordinates = async () => {
    if (!assignedArea) return;
    setResolving(true);
    setErrorMsg(null);
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
        setErrorMsg("Could not resolve location coordinates. Please enter manually.");
      }
    } catch (err) {
      console.error(err);
      setErrorMsg("Geocoding service error. Please enter coordinates manually.");
    } finally {
      setResolving(false);
    }
  };

  const detectCurrentLocation = () => {
    if (!navigator.geolocation) {
      setErrorMsg("Geolocation is not supported by your browser.");
      return;
    }
    setSaving(true);
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
        setSaving(false);
      },
      (err) => {
        setErrorMsg("Failed to get current location. Please type manually.");
        setSaving(false);
      }
    );
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSuccessMsg(null);

    if (!fullName.trim()) { setErrorMsg('Full Name is required.'); return; }
    if (!email.trim()) { setErrorMsg('Email is required.'); return; }

    setSaving(true);
    try {
      await apiCall(`/admin/supervisors/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          full_name: fullName,
          email,
          phone,
          employee_id: employeeId,
          assigned_area: assignedArea,
          latitude: latitude ? parseFloat(latitude) : null,
          longitude: longitude ? parseFloat(longitude) : null,
          coverage_radius: coverageRadius ? parseFloat(coverageRadius) : null
        })
      });
      setSuccessMsg(`Supervisor ${fullName} updated successfully!`);
      setTimeout(() => navigate(`/admin/supervisors/${id}`), 1000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to update supervisor profile.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-border border-t-indigo-600 animate-spin" />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-left">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Management</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Edit Supervisor Profile</h1>
        </div>
      </div>

      <form onSubmit={handleSave} className="flex flex-col gap-6 w-full">
        {successMsg && (
          <div className="flex items-center gap-3 p-4 text-sm rounded-lg text-left border bg-emerald-50 dark:bg-emerald-950/20 text-emerald-800 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/30">
            <CheckCircle size={18} className="shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}
        {errorMsg && (
          <div className="flex items-center gap-3 p-4 text-sm rounded-lg text-left border bg-red-50 dark:bg-red-950/20 text-red-800 dark:text-red-400 border-red-200 dark:border-red-900/30">
            <AlertCircle size={18} className="shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch">
          {/* Left Panel: Personal Details */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex flex-col gap-4 h-full justify-between">
            <div className="flex flex-col gap-4">
              <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
                <User size={18} /> Personal Information
              </h3>
              
              <Input 
                label="Full Name *" 
                placeholder="Jane Doe" 
                value={fullName} 
                onChange={e => setFullName(e.target.value)} 
                icon={<User size={20} className="text-muted-foreground" />} 
                required
                disabled={saving}
              />

              <Input 
                label="Email Address *" 
                placeholder="jane.doe@city.gov" 
                type="email"
                value={email} 
                onChange={e => setEmail(e.target.value)} 
                icon={<Mail size={20} className="text-muted-foreground" />} 
                required
                disabled={saving}
              />

              <Input 
                label="Phone Number" 
                placeholder="9876543210" 
                value={phone} 
                onChange={e => setPhone(e.target.value)} 
                icon={<Phone size={20} className="text-muted-foreground" />} 
                disabled={saving}
              />
            </div>
          </div>

          {/* Right Panel: Credentials & Allocation */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex flex-col gap-4 h-full justify-between">
            <div className="flex flex-col gap-4">
              <h3 className="font-bold text-base text-indigo-600 dark:text-indigo-400 border-b border-border/60 pb-2.5 flex items-center gap-2">
                <Shield size={18} /> Allocation & Security Credentials
              </h3>

              <Input 
                label="Employee ID (Read Only)" 
                value={employeeId} 
                icon={<Shield size={20} className="text-muted-foreground" />} 
                readOnly
                className="cursor-not-allowed opacity-60 dark:opacity-40"
              />

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
                      disabled={saving}
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
                  disabled={saving}
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
                  disabled={saving}
                />

                <Input 
                  label="Longitude *" 
                  placeholder="e.g. 77.2090" 
                  type="number"
                  step="any"
                  value={longitude} 
                  onChange={e => setLongitude(e.target.value)} 
                  required
                  disabled={saving}
                />

                <Input 
                  label="Radius (KM) *" 
                  placeholder="e.g. 10" 
                  type="number"
                  step="any"
                  value={coverageRadius} 
                  onChange={e => setCoverageRadius(e.target.value)} 
                  required
                  disabled={saving}
                />
              </div>

              {latitude && longitude && (
                <div className="text-[11px] font-semibold text-emerald-600 flex items-center gap-1 -mt-2">
                  <CheckCircle size={12} />
                  <span>GPS Coordinates Mapped Successfully</span>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="flex justify-end mt-4">
          <Button 
            type="submit" 
            disabled={saving}
            className="bg-indigo-600 hover:bg-indigo-700 text-white shadow-md hover:shadow-lg font-bold rounded-xl text-base px-8 py-3.5 w-full sm:w-auto min-w-[260px] transition-all shrink-0 cursor-pointer"
          >
            {saving ? 'Saving changes...' : 'Save Changes'}
          </Button>
        </div>
      </form>
    </div>
  );
};
