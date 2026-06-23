import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';
import { Mail, Lock, Eye, EyeOff, AlertCircle, ArrowLeft } from 'lucide-react';
import appLogo from '../../../assets/logo.svg';

export const AdminLogin: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await login(email, password, 'admin');
      navigate('/admin/dashboard');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please verify admin credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col lg:flex-row h-screen w-full relative bg-slate-50 dark:bg-slate-950 font-sans overflow-hidden animate-fade-in">
      {/* Left Panel */}
      <div className="hidden lg:flex flex-[1.2] bg-gradient-to-br from-static-slate-800 via-static-slate-900 to-static-slate-950 items-center justify-center p-8 lg:p-16 relative text-white text-center">
        <div className="absolute top-[-10%] left-[-10%] w-[60%] h-[60%] rounded-full bg-white/5 blur-3xl pointer-events-none" />
        
        <div className="flex flex-col items-center max-w-md z-10">
          <div className="w-[110px] h-[110px] bg-white rounded-2xl flex items-center justify-center shadow-xl p-3 mb-6 transition-all hover:scale-[1.03]">
            <img src={appLogo} alt="Logo" className="w-full h-full object-contain" />
          </div>
          <h1 className="font-extrabold text-4xl lg:text-5xl tracking-tight mb-2 text-white">EcoCollect</h1>
          <p className="text-static-slate-300 text-lg lg:text-xl font-medium mb-6">Admin Control Center</p>
          <div className="w-14 h-1 bg-white/20 rounded-full mb-6" />
          <p className="text-sm lg:text-base leading-relaxed text-static-slate-300/90">
            Sign in with authorization clearance to oversee municipal cleanliness metrics, review audit logs, and allocate field supervisors.
          </p>
        </div>
      </div>

      {/* Right Panel */}
      <div className="flex-1 bg-white dark:bg-card border-l border-slate-100 dark:border-border/40 shadow-2xl relative h-full overflow-y-auto flex flex-col items-center">
        <div className="w-full max-w-sm flex flex-col text-left relative py-8 px-6 sm:px-0 my-auto">
          <button 
            className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground cursor-pointer bg-transparent border-none py-1.5 mb-4 transition-colors w-fit" 
            onClick={() => navigate('/')}
          >
            <ArrowLeft size={16} /> Back
          </button>
          
          {/* Mobile Header */}
          <div className="lg:hidden flex items-center gap-3 mb-5">
            <div className="w-10 h-10 bg-slate-500/10 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl flex items-center justify-center p-1.5 shadow-sm">
              <img src={appLogo} alt="Logo" className="w-full h-full object-contain" />
            </div>
            <div>
              <h2 className="font-bold text-lg text-foreground leading-none">EcoCollect</h2>
              <p className="text-xs text-muted-foreground font-medium mt-1">Admin Control Center</p>
            </div>
          </div>
          
          <h1 className="font-extrabold text-3xl text-foreground mb-1">Admin Access</h1>
          <p className="text-sm text-muted-foreground mb-5">Authorized Control Room Login</p>

          {error && (
            <div className="flex items-center gap-3 p-3.5 border border-red-500/20 bg-red-500/10 text-red-600 dark:text-red-400 rounded-lg text-sm mb-5">
              <AlertCircle size={18} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form className="flex flex-col" onSubmit={handleLogin}>
            <Input 
              label="Admin Email"
              placeholder="admin@ecocollect.city" 
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              icon={<Mail size={20} className="text-slate-400" />}
              required
            />
            <div style={{ height: 12 }} />
            
            <Input 
              label="Password"
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
            />

            <div style={{ height: 16 }} />

            <Button 
              type="submit"
              fullWidth 
              theme="darkblue" 
              disabled={loading}
            >
              {loading ? 'Entering Room...' : 'Access Command Center'}
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
};
