import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';
import { ChevronLeft, User, BadgeAlert, Mail, Phone, MapPin, Camera } from 'lucide-react';

export const SupervisorEditProfile: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();

  const [fullName, setFullName] = useState('');
  const [employeeId, setEmployeeId] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [assignedArea, setAssignedArea] = useState('');
  
  const [isSaving, setIsSaving] = useState(false);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await apiCall('/profile');
        setFullName(data.full_name || '');
        setEmployeeId(data.employee_id || '');
        setEmail(data.email || '');
        setPhone(data.phone || '');
        setAssignedArea(data.assigned_area || '');
      } catch (err: any) {
        console.error('Failed to load profile:', err);
        setErrorMessage(err.message || 'Failed to load profile details.');
      }
    };
    fetchProfile();
  }, [apiCall]);

  const handleSave = async () => {
    setIsSaving(true);
    setErrorMessage(null);
    try {
      const updatedUser = await apiCall('/profile', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          full_name: fullName,
          phone: phone,
        }),
      });

      // Update local storage context for consistency
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      // Update supervisor structure in local storage if exists
      const storedSupervisor = localStorage.getItem('supervisor');
      if (storedSupervisor) {
        const parsed = JSON.parse(storedSupervisor);
        parsed.name = fullName;
        parsed.phone = phone;
        localStorage.setItem('supervisor', JSON.stringify(parsed));
      }

      setShowSuccessMessage(true);
      setTimeout(() => {
        navigate(-1);
      }, 1500);
    } catch (err: any) {
      console.error(err);
      setErrorMessage(err.message || 'Failed to update profile.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="flex flex-col flex-1 bg-slate-50 min-h-screen text-left">
      <div className="flex items-center bg-white p-4 relative shadow-sm border-b border-slate-100">
        <button className="bg-transparent border-none cursor-pointer p-2 text-slate-900 absolute left-2" onClick={() => navigate(-1)} aria-label="Go back">
          <ChevronLeft size={24} />
        </button>
        <div className="flex flex-col items-center flex-1">
          <span className="text-lg font-bold text-slate-900">Edit Profile</span>
          <span className="text-xs text-slate-500">Update your supervisor information</span>
        </div>
      </div>

      <div className="p-6 flex flex-col items-center max-w-[400px] mx-auto w-full">
        <div className="relative w-24 h-24 mb-3">
          <div className="w-full h-full rounded-full bg-blue-100 flex items-center justify-center">
            <User size={60} className="text-blue-500" />
          </div>
          <button className="absolute bottom-1 right-1 w-8 h-8 rounded-full bg-blue-600 border-2 border-white flex items-center justify-center cursor-pointer shadow-sm hover:bg-blue-700" aria-label="Upload photo">
            <Camera size={16} className="text-white" />
          </button>
        </div>

        <button className="text-sm font-semibold text-blue-600 bg-transparent border-none cursor-pointer mb-8">Change Photo</button>

        {errorMessage && (
          <div className="w-full bg-red-50 text-red-800 border border-red-200 rounded-xl p-3.5 text-center mb-4 font-semibold text-sm">
            {errorMessage}
          </div>
        )}

        <Input 
          label="Full Name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          icon={<User size={20} className="text-slate-400" />}
        />
        <div style={{ height: 16 }} />
        
        <Input 
          label="Employee ID"
          value={employeeId}
          disabled
          icon={<BadgeAlert size={20} className="text-slate-400" />}
        />
        <div style={{ height: 16 }} />

        <Input 
          label="Email Address"
          value={email}
          disabled
          icon={<Mail size={20} className="text-slate-400" />}
        />
        <div style={{ height: 16 }} />

        <Input 
          label="Phone Number"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          icon={<Phone size={20} className="text-slate-400" />}
        />
        <div style={{ height: 16 }} />

        <Input 
          label="Assigned Area"
          value={assignedArea}
          disabled
          icon={<MapPin size={20} className="text-slate-400" />}
        />
        <div style={{ height: 32 }} />

        {showSuccessMessage && (
          <div className="w-full bg-emerald-50 text-emerald-800 border border-emerald-200 rounded-xl p-3.5 text-center mb-4 font-semibold text-sm">
            Profile Updated Successfully
          </div>
        )}

        <Button 
          fullWidth 
          theme="blue" 
          onClick={handleSave}
          disabled={isSaving}
          className="mb-4 bg-blue-600 hover:bg-blue-700 text-white shadow-sm hover:shadow"
        >
          {isSaving ? "Saving..." : "Save Changes"}
        </Button>

        <button className="w-full bg-transparent border-none text-slate-500 font-semibold text-sm cursor-pointer p-3 hover:text-slate-700 transition-colors" onClick={() => navigate(-1)}>
          Cancel
        </button>
      </div>
    </div>
  );
};
