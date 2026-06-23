import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Lock, Eye, EyeOff, CheckCircle } from 'lucide-react';
import { useAuth } from '../../../context/AuthContext';

export const AdminChangePassword: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();
  
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!currentPassword) {
      setError('Please enter your current password.');
      return;
    }
    
    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters long.');
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.');
      return;
    }

    setLoading(true);
    try {
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
      setSuccess(true);
      setTimeout(() => {
        navigate('/admin/profile');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Failed to update password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-left">
      {/* Header Row */}
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Security</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Change Password</h1>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch w-full">
        {/* Left Column: Security Information */}
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 md:p-8 flex flex-col items-center justify-center text-center h-full">
          <div className="w-14 h-14 rounded-full bg-blue-500/10 border border-blue-500/20 flex items-center justify-center mb-4">
            <Lock size={26} className="text-blue-600 dark:text-blue-400" />
          </div>
          <h2 className="text-xl font-extrabold text-foreground leading-tight">Update Security Credentials</h2>
          <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-sm">
            Ensure your admin account is secure by using a strong password. We recommend using a unique password that is at least 8 characters long and contains letters, numbers, and symbols.
          </p>
        </div>

        {/* Right Column: Password Fields Form */}
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-6 md:p-8 flex flex-col h-full justify-between">
          {error && (
            <div className="p-3 mb-4 text-xs font-semibold text-red-800 dark:text-red-400 bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/30 rounded-lg">
              {error}
            </div>
          )}
          
          {success ? (
            <div className="flex flex-col items-center justify-center py-12 text-center gap-3">
              <CheckCircle size={48} className="text-emerald-500 animate-bounce" />
              <h3 className="text-lg font-bold text-foreground">Password Updated Successfully!</h3>
              <p className="text-sm text-muted-foreground">Returning to your profile...</p>
            </div>
          ) : (
            <form className="flex flex-col gap-5 text-left h-full justify-between" onSubmit={handleSubmit}>
              <div className="flex flex-col gap-4">
                <div className="flex flex-col gap-1.5 text-left">
                  <label className="text-xs font-bold text-foreground">Current Password *</label>
                  <div className="relative">
                    <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
                    <input 
                      type={showCurrent ? "text" : "password"} 
                      className="font-sans w-full pl-12 pr-12 py-3 bg-muted/40 border border-border rounded-xl text-sm text-foreground outline-none transition-all focus:border-indigo-600 dark:focus:border-indigo-400 focus:ring-2 focus:ring-indigo-500/10 placeholder:text-muted-foreground"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      placeholder="Enter current password"
                      required
                      disabled={loading}
                    />
                    <button 
                      type="button" 
                      className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer flex items-center justify-center"
                      onClick={() => setShowCurrent(!showCurrent)}
                      disabled={loading}
                    >
                      {showCurrent ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                <div className="flex flex-col gap-1.5 text-left">
                  <label className="text-xs font-bold text-foreground">New Password *</label>
                  <div className="relative">
                    <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
                    <input 
                      type={showNew ? "text" : "password"} 
                      className="font-sans w-full pl-12 pr-12 py-3 bg-muted/40 border border-border rounded-xl text-sm text-foreground outline-none transition-all focus:border-indigo-600 dark:focus:border-indigo-400 focus:ring-2 focus:ring-indigo-500/10 placeholder:text-muted-foreground"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="Enter new password (min. 8 chars)"
                      required
                      disabled={loading}
                    />
                    <button 
                      type="button" 
                      className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer flex items-center justify-center"
                      onClick={() => setShowNew(!showNew)}
                      disabled={loading}
                    >
                      {showNew ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                <div className="flex flex-col gap-1.5 text-left">
                  <label className="text-xs font-bold text-foreground">Confirm New Password *</label>
                  <div className="relative">
                    <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
                    <input 
                      type={showConfirm ? "text" : "password"} 
                      className="font-sans w-full pl-12 pr-12 py-3 bg-muted/40 border border-border rounded-xl text-sm text-foreground outline-none transition-all focus:border-indigo-600 dark:focus:border-indigo-400 focus:ring-2 focus:ring-indigo-500/10 placeholder:text-muted-foreground"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Confirm new password"
                      required
                      disabled={loading}
                    />
                    <button 
                      type="button" 
                      className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer flex items-center justify-center"
                      onClick={() => setShowConfirm(!showConfirm)}
                      disabled={loading}
                    >
                      {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
              </div>

              <button 
                type="submit" 
                className="w-full mt-4 py-3.5 bg-indigo-600 hover:bg-indigo-750 text-white font-bold rounded-xl text-sm transition-all duration-150 cursor-pointer shadow-sm hover:shadow disabled:opacity-50"
                disabled={loading}
              >
                {loading ? 'Updating Password...' : 'Update Password'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
