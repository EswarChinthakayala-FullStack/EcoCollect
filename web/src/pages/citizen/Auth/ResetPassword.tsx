import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, AlertCircle, Lock, Eye, EyeOff } from 'lucide-react';
import appLogo from '../../../assets/logo.svg';
import { useAuth } from '../../../context/AuthContext';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';

export const ResetPassword: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { apiCall } = useAuth();

  const { email, otp } = location.state || {};

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Prevent illegal direct access without verified email/otp state context
  useEffect(() => {
    if (!email || !otp) {
      navigate('/citizen/forgot-password');
    }
  }, [email, otp, navigate]);

  const handleReset = async () => {
    if (!password || password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    if (password.length < 8) {
      setError("Password must be at least 8 characters long.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await apiCall('/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: email,
          otp: otp,
          new_password: password
        })
      });
      // Navigate to login on success
      navigate('/citizen/login');
    } catch (err: any) {
      setError(err.message || 'Failed to reset password. Please verify your OTP session.');
    } finally {
      setLoading(false);
    }
  };

  const isEnabled = password.trim() !== '' && password === confirmPassword && !loading;

  return (
    <div className="flex flex-col lg:flex-row h-screen w-full relative bg-slate-50 dark:bg-slate-950 font-sans overflow-hidden animate-fade-in transition-colors duration-300">
      {/* Left Panel */}
      <div className="hidden lg:flex flex-[1.2] bg-gradient-to-br from-emerald-600 via-static-emerald-700 to-emerald-900 items-center justify-center p-8 lg:p-16 relative text-white text-center">
        <div className="absolute top-[-10%] left-[-10%] w-[60%] h-[60%] rounded-full bg-white/5 blur-3xl pointer-events-none" />
        
        <div className="flex flex-col items-center max-w-md z-10">
          <div className="w-[110px] h-[110px] bg-white rounded-2xl flex items-center justify-center shadow-xl p-3 mb-6 transition-all hover:scale-[1.03]">
            <img src={appLogo} alt="Logo" className="w-full h-full object-contain" />
          </div>
          <h1 className="font-extrabold text-4xl lg:text-5xl tracking-tight mb-2 text-white">EcoCollect</h1>
          <p className="text-static-emerald-100 text-lg lg:text-xl font-medium mb-6">Citizen Portal</p>
          <div className="w-14 h-1 bg-white/20 rounded-full mb-6" />
          <p className="text-sm lg:text-base leading-relaxed text-static-emerald-50/90">
            Create a strong, secure new password for your account to ensure your environmental contributions remain safe.
          </p>
        </div>
      </div>

      {/* Right Panel */}
      <div className="flex-1 bg-white dark:bg-card border-l border-slate-100 dark:border-border/40 shadow-2xl relative h-full overflow-y-auto flex flex-col items-center justify-center">
        <div className="w-full max-w-sm flex flex-col text-left relative py-8 px-6 sm:px-0 my-auto">
          <button 
            className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground cursor-pointer bg-transparent border-none py-1.5 mb-4 transition-colors w-fit" 
            onClick={() => navigate('/citizen/forgot-password')}
            disabled={loading}
          >
            <ArrowLeft size={16} /> Back to OTP
          </button>
          
          {/* Mobile Header */}
          <div className="lg:hidden flex items-center gap-3 mb-5">
            <div className="w-10 h-10 bg-emerald-500/10 dark:bg-emerald-950/20 border border-emerald-500/20 dark:border-emerald-900/30 rounded-xl flex items-center justify-center p-1.5 shadow-sm">
              <img src={appLogo} alt="Logo" className="w-full h-full object-contain" />
            </div>
            <div>
              <h2 className="font-bold text-lg text-foreground leading-none">EcoCollect</h2>
              <p className="text-xs text-muted-foreground font-medium mt-1">Citizen Portal</p>
            </div>
          </div>
          
          <h1 className="font-extrabold text-3xl text-foreground mb-1">Create New Password</h1>
          <p className="text-sm text-muted-foreground mb-5">
            Your new password must be different from previously used passwords.
          </p>

          {error && (
            <div className="flex items-center gap-3 p-3.5 border border-red-500/20 bg-red-500/10 text-red-600 dark:text-red-400 rounded-lg text-sm mb-5">
              <AlertCircle size={18} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form className="flex flex-col gap-4" onSubmit={(e) => { e.preventDefault(); handleReset(); }}>
            <Input 
              label="New Password"
              placeholder="••••••••" 
              type={passwordVisible ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              icon={<Lock size={20} className="text-slate-400" />}
              rightIcon={
                <button 
                  type="button" 
                  style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                  onClick={() => setPasswordVisible(!passwordVisible)}
                >
                  {passwordVisible ? <EyeOff size={20} className="text-slate-400" /> : <Eye size={20} className="text-slate-400" />}
                </button>
              }
              required
              disabled={loading}
            />

            <Input 
              label="Confirm Password"
              placeholder="••••••••" 
              type={confirmPasswordVisible ? "text" : "password"}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              icon={<Lock size={20} className="text-slate-400" />}
              rightIcon={
                <button 
                  type="button" 
                  style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                  onClick={() => setConfirmPasswordVisible(!confirmPasswordVisible)}
                >
                  {confirmPasswordVisible ? <EyeOff size={20} className="text-slate-400" /> : <Eye size={20} className="text-slate-400" />}
                </button>
              }
              required
              disabled={loading}
            />

            <Button 
              type="submit"
              fullWidth 
              theme="green" 
              disabled={!isEnabled || loading}
            >
              {loading ? "Resetting..." : "Reset Password"}
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
};
