import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, AlertCircle, Mail, KeyRound } from 'lucide-react';
import appLogo from '../../../assets/logo.svg';
import { useAuth } from '../../../context/AuthContext';
import { Input } from '../../../components/Input';
import { Button } from '../../../components/Button';

export const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();

  const [isOtpSent, setIsOtpSent] = useState(false);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [timer, setTimer] = useState(30);
  const [isTimerActive, setIsTimerActive] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    if (isTimerActive && timer > 0) {
      interval = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
    } else if (timer === 0) {
      setIsTimerActive(false);
    }
    return () => clearInterval(interval);
  }, [isTimerActive, timer]);

  const handleSendOtp = async () => {
    if (!email.trim()) return;
    setLoading(true);
    setError(null);
    try {
      await apiCall('/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim() })
      });
      setIsOtpSent(true);
      setTimer(30);
      setIsTimerActive(true);
    } catch (err: any) {
      setError(err.message || 'Failed to send OTP code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    if (otp.length !== 6) return;
    setLoading(true);
    setError(null);
    try {
      await apiCall('/auth/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), otp: otp.trim() })
      });
      navigate('/citizen/reset-password', { state: { email: email.trim(), otp: otp.trim() } });
    } catch (err: any) {
      setError(err.message || 'Invalid or expired verification code.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setLoading(true);
    setError(null);
    try {
      await apiCall('/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim() })
      });
      setTimer(30);
      setIsTimerActive(true);
    } catch (err: any) {
      setError(err.message || 'Failed to resend OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

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
            Recover your account credentials to continue reporting, tracking local environmental progress, and earning green reward points.
          </p>
        </div>
      </div>

      {/* Right Panel */}
      <div className="flex-1 bg-white dark:bg-card border-l border-slate-100 dark:border-border/40 shadow-2xl relative h-full overflow-y-auto flex flex-col items-center justify-center">
        <div className="w-full max-w-sm flex flex-col text-left relative py-8 px-6 sm:px-0 my-auto">
          <button 
            className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground cursor-pointer bg-transparent border-none py-1.5 mb-4 transition-colors w-fit" 
            onClick={() => {
              if (isOtpSent) {
                setIsOtpSent(false);
              } else {
                navigate('/citizen/login');
              }
            }}
            disabled={loading}
          >
            <ArrowLeft size={16} /> {isOtpSent ? "Back to Email" : "Back to Login"}
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
          
          <h1 className="font-extrabold text-3xl text-foreground mb-1">
            {!isOtpSent ? "Forgot Password" : "Verify OTP"}
          </h1>
          <p className="text-sm text-muted-foreground mb-5">
            {!isOtpSent 
              ? "Enter your registered email address to receive a verification code."
              : `We have sent a verification code to ${email}`}
          </p>

          {error && (
            <div className="flex items-center gap-3 p-3.5 border border-red-500/20 bg-red-500/10 text-red-600 dark:text-red-400 rounded-lg text-sm mb-5">
              <AlertCircle size={18} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {!isOtpSent ? (
            <div className="flex flex-col gap-4">
              <Input 
                label="Email Address"
                placeholder="name@example.com" 
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                icon={<Mail size={20} className="text-slate-400" />}
                required
                disabled={loading}
              />

              <Button 
                onClick={handleSendOtp}
                fullWidth
                theme="green"
                disabled={!email.trim() || loading}
              >
                {loading ? "Sending..." : "Send OTP"}
              </Button>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              <Input 
                label="Verification Code"
                placeholder="Enter 6-digit code" 
                type="text"
                value={otp}
                onChange={(e) => {
                  const val = e.target.value;
                  if (/^\d*$/.test(val) && val.length <= 6) setOtp(val);
                }}
                icon={<KeyRound size={20} className="text-slate-400" />}
                required
                disabled={loading}
              />

              <Button 
                onClick={handleVerifyOtp}
                fullWidth
                theme="green"
                disabled={otp.length !== 6 || loading}
              >
                {loading ? "Verifying..." : "Verify OTP"}
              </Button>

              <div className="flex items-center justify-center gap-2 mt-2 text-xs font-semibold">
                <span className="text-muted-foreground">Didn't receive the code?</span>
                {timer > 0 ? (
                  <span className="text-muted-foreground font-bold">Resend in {timer}s</span>
                ) : (
                  <button 
                    type="button"
                    className="text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline cursor-pointer bg-transparent border-none font-bold" 
                    onClick={handleResend}
                    disabled={loading}
                  >
                    Resend OTP
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
