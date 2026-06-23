import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTheme } from 'next-themes';
import { 
  ArrowRight, ShieldCheck, MapPin, Users, Award, 
  Leaf, Trophy, CheckCircle2, ChevronRight, LayoutGrid,
  Star, Lock, Building2, Sparkles, Check, Sun, Moon
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import appLogo from '../assets/logo.svg';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Mock Leaderboard Data
  const leaderboardUsers = [
    { rank: 1, name: 'Aarav Sharma', initials: 'AS', points: 2850 },
    { rank: 2, name: 'Neha Goel', initials: 'NG', points: 2420 },
    { rank: 3, name: 'Vikram Singh', initials: 'VS', points: 2190 },
    { rank: 4, name: 'Priya Patel', initials: 'PP', points: 1980 },
    { rank: 5, name: 'Rahul Verma', initials: 'RV', points: 1850 },
  ];

  return (
    <div className="flex flex-col min-h-screen w-full relative bg-slate-50 dark:bg-slate-950 text-foreground font-sans">
      {/* Decorative Blurs */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none -z-10">
        <div className="absolute top-[-10%] right-[-10%] w-[250px] h-[250px] bg-blue-500/10 rounded-full blur-[80px] pointer-events-none" />
        <div className="absolute bottom-[-10%] left-[-5%] w-[300px] h-[300px] bg-emerald-500/10 rounded-full blur-[90px] pointer-events-none" />
      </div>

      {/* Navigation */}
      <header className="sticky top-0 z-50 flex items-center justify-between px-6 py-4 md:px-[8%] bg-white/70 dark:bg-card/70 backdrop-blur-md border-b border-slate-100/60 dark:border-border/40 shadow-sm">
        <div className="flex items-center gap-3 cursor-pointer hover:scale-[1.02] transition-transform" onClick={() => navigate('/')}>
          <img src={appLogo} alt="EcoCollect Logo" className="w-10 h-10 object-contain" />
          <span className="font-extrabold text-2xl bg-gradient-to-r from-emerald-600 to-blue-600 bg-clip-text text-transparent tracking-tight">
            EcoCollect
          </span>
        </div>
        <nav className="flex items-center gap-4 sm:gap-6 md:gap-8">
          <a href="#about" className="hidden md:inline text-sm font-medium text-slate-600 dark:text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors">About</a>
          <a href="#portals" className="hidden md:inline text-sm font-medium text-slate-600 dark:text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors">Portals</a>
          <a href="#leaderboard" className="hidden md:inline text-sm font-medium text-slate-600 dark:text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors">Eco-Rewards</a>
          
          {/* Theme Toggle Button */}
          {mounted && (
            <button
              onClick={() => setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')}
              className="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors border-none bg-transparent cursor-pointer shrink-0"
              title="Toggle Theme"
            >
              {resolvedTheme === 'dark' ? (
                <Sun size={18} className="text-amber-400 animate-fade-in" />
              ) : (
                <Moon size={18} className="text-slate-600 dark:text-slate-400 animate-fade-in" />
              )}
            </button>
          )}

          <Button className="bg-emerald-600 hover:bg-emerald-700 text-white shadow-md hover:shadow-lg transition-all" onClick={() => navigate('/welcome')}>
            Access Portals
          </Button>
        </nav>
      </header>

      {/* Hero Section Container with Grid Background */}
      <div className="w-full relative overflow-hidden bg-slate-50 dark:bg-slate-950 border-b border-slate-200/40 dark:border-border/30">
        {/* Modern Grid Background overlay */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#cbd5e1_1px,transparent_1px),linear-gradient(to_bottom,#cbd5e1_1px,transparent_1px)] dark:bg-[linear-gradient(to_right,#334155_1px,transparent_1px),linear-gradient(to_bottom,#334155_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_50%,#000_70%,transparent_100%)] opacity-20 pointer-events-none" />
        
        {/* Soft glowing background gradients */}
        <div className="absolute top-[20%] left-[50%] -translate-x-[50%] -translate-y-[50%] w-[380px] h-[380px] bg-blue-500/10 rounded-full blur-[100px] pointer-events-none" />
        <div className="absolute bottom-[20%] left-[50%] -translate-x-[50%] -translate-y-[50%] w-[380px] h-[380px] bg-emerald-500/10 rounded-full blur-[100px] pointer-events-none" />

        <section className="flex flex-col items-center justify-center text-center px-6 py-20 md:py-28 max-w-4xl mx-auto relative z-10" id="about">
          <div className="flex flex-col items-center max-w-3xl">
            <Badge className="bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-900/30 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 rounded-full px-3.5 py-1.5 text-xs font-semibold mb-6 gap-2 mx-auto">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              Empowering Smart Communities
            </Badge>
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 dark:text-foreground leading-[1.1] mb-6 text-center">
              Smart Waste Management For a <span className="bg-gradient-to-r from-emerald-600 to-blue-600 dark:from-emerald-400 dark:to-blue-400 bg-clip-text text-transparent">Cleaner Tomorrow</span>
            </h1>
            <p className="text-base md:text-lg text-slate-600 dark:text-muted-foreground leading-relaxed mb-8 text-center max-w-2xl">
              EcoCollect connects citizens, supervisors, and administrators in real-time. Report civic issues, coordinate efficient dispatches, track actions on live maps, and earn rewards for a green city.
            </p>
            <div className="flex flex-wrap items-center justify-center gap-4">
              <Button className="bg-gradient-to-r from-emerald-600 to-emerald-500 hover:from-emerald-700 hover:to-emerald-600 text-white px-7 py-6 text-base font-semibold shadow-lg hover:-translate-y-0.5 transition-all gap-2" onClick={() => navigate('/citizen/login')}>
                Join as Citizen <ArrowRight size={18} />
              </Button>
              <Button variant="outline" className="border-slate-200 dark:border-border bg-white dark:bg-muted/30 hover:bg-slate-50 dark:hover:bg-muted/50 text-slate-700 dark:text-muted-foreground px-7 py-6 text-base font-semibold shadow-sm hover:-translate-y-0.5 transition-all gap-2" onClick={() => navigate('/all-pages')}>
                <LayoutGrid size={18} /> All Pages Directory
              </Button>
            </div>
          </div>
        </section>
      </div>

      {/* Trust Badges / Partners Banner */}
      <section className="py-8 px-6 bg-slate-100/50 dark:bg-muted/10 border-t border-b border-slate-200/50 dark:border-border/30 w-full">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6 md:gap-12">
          <span className="text-xs font-bold text-slate-400 dark:text-muted-foreground/60 uppercase tracking-widest">Endorsed & Trusted By</span>
          <div className="flex flex-wrap items-center justify-center gap-8 md:gap-12 lg:gap-16 opacity-60 dark:opacity-85">
            <div className="flex items-center gap-2 text-slate-500 dark:text-muted-foreground font-semibold tracking-tight">
              <Building2 size={18} className="text-slate-400 dark:text-muted-foreground/50" />
              <span className="text-sm">Municipal Environmental Board</span>
            </div>
            <div className="flex items-center gap-2 text-slate-500 dark:text-muted-foreground font-semibold tracking-tight">
              <Sparkles size={18} className="text-slate-400 dark:text-muted-foreground/50" />
              <span className="text-sm">Smart City Initiative</span>
            </div>
            <div className="flex items-center gap-2 text-slate-500 dark:text-muted-foreground font-semibold tracking-tight">
              <Leaf size={18} className="text-slate-400 dark:text-muted-foreground/50" />
              <span className="text-sm">Green Earth Alliance</span>
            </div>
            <div className="flex items-center gap-2 text-slate-500 dark:text-muted-foreground font-semibold tracking-tight">
              <ShieldCheck size={18} className="text-slate-400 dark:text-muted-foreground/50" />
              <span className="text-sm">Civic Trust Association</span>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Bar */}
      <section className="py-10 px-6 md:px-[8%] bg-white/50 dark:bg-card/30 border-t border-b border-slate-100/80 dark:border-border/20">
        <div className="flex flex-wrap justify-around items-center max-w-6xl mx-auto gap-8">
          <div className="text-center flex-1 min-w-[200px]">
            <div className="text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-emerald-600 to-blue-600 dark:from-emerald-400 dark:to-blue-400 bg-clip-text text-transparent mb-2">12,450+</div>
            <div className="text-xs font-semibold text-slate-500 dark:text-muted-foreground uppercase tracking-wider">Issues Resolved</div>
          </div>
          <div className="text-center flex-1 min-w-[200px]">
            <div className="text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-emerald-600 to-blue-600 dark:from-emerald-400 dark:to-blue-400 bg-clip-text text-transparent mb-2">45+ Tons</div>
            <div className="text-xs font-semibold text-slate-500 dark:text-muted-foreground uppercase tracking-wider">Waste Diverted</div>
          </div>
          <div className="text-center flex-1 min-w-[200px]">
            <div className="text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-emerald-600 to-blue-600 dark:from-emerald-400 dark:to-blue-400 bg-clip-text text-transparent mb-2">98.2%</div>
            <div className="text-xs font-semibold text-slate-500 dark:text-muted-foreground uppercase tracking-wider">Cleanliness Rating</div>
          </div>
          <div className="text-center flex-1 min-w-[200px]">
            <div className="text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-emerald-600 to-blue-600 dark:from-emerald-400 dark:to-blue-400 bg-clip-text text-transparent mb-2">15 Mins</div>
            <div className="text-xs font-semibold text-slate-500 dark:text-muted-foreground uppercase tracking-wider">Average Dispatch Time</div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-16 md:py-24 px-6 md:px-[8%] bg-white/40 dark:bg-background/20 border-b border-slate-100/50 dark:border-border/20" id="how-it-works">
        <div className="max-w-7xl mx-auto text-center">
          <span className="text-emerald-600 dark:text-emerald-400 text-xs font-bold uppercase tracking-widest mb-3 block">Transparent Process</span>
          <h2 className="text-3xl md:text-4xl font-extrabold text-slate-900 dark:text-foreground mb-4">How EcoCollect Works</h2>
          <p className="text-sm md:text-base text-slate-500 dark:text-muted-foreground leading-relaxed max-w-2xl mx-auto mb-16">
            We've built an open, accountable feedback loop that bridges citizens and municipal cleaning crews with transparent milestones.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 relative">
            {/* Step 1 */}
            <div className="flex flex-col items-center text-center p-6 bg-white/80 dark:bg-card/50 border border-slate-100 dark:border-border/40 rounded-2xl shadow-sm relative group hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-lg mb-6 shadow-inner">1</div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-foreground mb-2">Citizen Reports</h3>
              <p className="text-xs text-slate-500 dark:text-muted-foreground leading-relaxed">
                Snap a photo, choose the waste category, pin the live GPS coordinates, and submit in seconds.
              </p>
            </div>

            {/* Step 2 */}
            <div className="flex flex-col items-center text-center p-6 bg-white/80 dark:bg-card/50 border border-slate-100 dark:border-border/40 rounded-2xl shadow-sm relative group hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-lg mb-6 shadow-inner">2</div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-foreground mb-2">Smart Dispatch</h3>
              <p className="text-xs text-slate-500 dark:text-muted-foreground leading-relaxed">
                Our platform automatically processes and dispatches the task to the nearest supervisor in charge.
              </p>
            </div>

            {/* Step 3 */}
            <div className="flex flex-col items-center text-center p-6 bg-white/80 dark:bg-card/50 border border-slate-100 dark:border-border/40 rounded-2xl shadow-sm relative group hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-lg mb-6 shadow-inner">3</div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-foreground mb-2">Field Resolution</h3>
              <p className="text-xs text-slate-500 dark:text-muted-foreground leading-relaxed">
                The supervisor navigates to the GPS location, coordinates the cleanup, and uploads evidence.
              </p>
            </div>

            {/* Step 4 */}
            <div className="flex flex-col items-center text-center p-6 bg-white/80 dark:bg-card/50 border border-slate-100 dark:border-border/40 rounded-2xl shadow-sm relative group hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-lg mb-6 shadow-inner">4</div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-foreground mb-2">Verification & Rewards</h3>
              <p className="text-xs text-slate-500 dark:text-muted-foreground leading-relaxed">
                Admin verifies closure. The reporter receives Eco-points redeemable for local sustainability coupons.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Trust & Integrity Verification Section */}
      <section className="py-16 md:py-24 px-6 md:px-[8%] max-w-7xl mx-auto" id="trust-integrity">
        <div className="flex flex-col lg:flex-row items-center justify-between gap-12 lg:gap-16">
          <div className="flex-1 text-left max-w-xl">
            <span className="text-blue-600 dark:text-blue-400 text-xs font-bold uppercase tracking-widest mb-3 block">System Integrity</span>
            <h2 className="text-3xl md:text-4xl font-extrabold text-slate-900 dark:text-foreground mb-6">Why You Can Trust EcoCollect</h2>
            <p className="text-sm md:text-base text-slate-500 dark:text-muted-foreground leading-relaxed mb-8">
              We understand that accountability is the cornerstone of public trust. EcoCollect uses high-precision verification systems to ensure that waste is physically cleaned up and citizen actions are genuine.
            </p>

            <div className="flex flex-col gap-6">
              <div className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0 mt-1">
                  <MapPin size={20} />
                </div>
                <div className="flex flex-col">
                  <span className="font-bold text-slate-900 dark:text-foreground text-base">GPS-Fenced Verifications</span>
                  <span className="text-xs md:text-sm text-slate-500 dark:text-muted-foreground leading-relaxed">Supervisors must be physically within 10 meters of coordinates to close tickets, verified by hardware sensors.</span>
                </div>
              </div>

              <div className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0 mt-1">
                  <ShieldCheck size={20} />
                </div>
                <div className="flex flex-col">
                  <span className="font-bold text-slate-900 dark:text-foreground text-base">Photo Evidence Audit</span>
                  <span className="text-xs md:text-sm text-slate-500 dark:text-muted-foreground leading-relaxed">Every closure requires photo proof matching the reported issue category, double-checked by administrators.</span>
                </div>
              </div>

              <div className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0 mt-1">
                  <Lock size={20} />
                </div>
                <div className="flex flex-col">
                  <span className="font-bold text-slate-900 dark:text-foreground text-base">Privacy-First Data Protection</span>
                  <span className="text-xs md:text-sm text-slate-500 dark:text-muted-foreground leading-relaxed">Citizen identities are protected. Reports can be submitted completely anonymously with secure data encryption.</span>
                </div>
              </div>
            </div>
          </div>

          <Card className="flex-1 w-full max-w-[520px] bg-gradient-to-br from-static-slate-900 to-static-slate-950 text-white rounded-2xl shadow-xl p-8 relative overflow-hidden border border-white/10">
            <div className="absolute top-[-20%] right-[-10%] w-[200px] h-[200px] bg-blue-500/10 rounded-full blur-[50px] pointer-events-none" />
            <h3 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <ShieldCheck className="text-emerald-500" size={24} /> Civic Integrity Dashboard
            </h3>
            <p className="text-xs text-static-slate-300 leading-relaxed mb-6">
              Our system logs every milestone on a tamper-proof dashboard visible to municipal inspectors and admins.
            </p>
            <div className="flex flex-col gap-4">
              <div className="flex items-center justify-between border-b border-white/10 pb-3 text-xs">
                <span className="text-static-slate-400">Total System Audited Tasks</span>
                <span className="font-bold text-white">12,456</span>
              </div>
              <div className="flex items-center justify-between border-b border-white/10 pb-3 text-xs">
                <span className="text-static-slate-400">GPS Match Rate</span>
                <span className="font-bold text-emerald-400">99.8% Verified</span>
              </div>
              <div className="flex items-center justify-between border-b border-white/10 pb-3 text-xs">
                <span className="text-static-slate-400">Avg. Citizen Satisfaction</span>
                <span className="font-bold text-amber-400 flex items-center gap-1">
                  <Star size={12} className="fill-amber-400 text-amber-400" /> 4.9 / 5.0
                </span>
              </div>
              <div className="flex items-center justify-between pb-3 text-xs">
                <span className="text-static-slate-400">Public Audit Access</span>
                <span className="font-bold text-blue-400">Full Open-Data API</span>
              </div>
            </div>
            <div className="mt-6 p-4 rounded-xl bg-white/5 border border-white/10 flex items-center gap-3">
              <Building2 className="text-emerald-500 shrink-0" size={20} />
              <p className="text-[11px] text-static-slate-300 text-left leading-relaxed">
                Directly connected to city operations via official API integrations. Guaranteed transparency.
              </p>
            </div>
          </Card>
        </div>
      </section>

      {/* Role Gateways Section */}
      <section className="py-16 md:py-24 px-6 md:px-[8%] max-w-7xl mx-auto" id="portals">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <span className="text-emerald-600 dark:text-emerald-400 text-xs font-bold uppercase tracking-widest mb-3 block">Portal Access Gateways</span>
          <h2 className="text-3xl md:text-4xl font-extrabold text-slate-900 dark:text-foreground mb-4">One Platform, Three Tailored Panels</h2>
          <p className="text-sm md:text-base text-slate-500 dark:text-muted-foreground leading-relaxed">
            EcoCollect provides dedicated, premium modules optimized for every stakeholder role. Select your portal to log in.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {/* Citizen Card */}
          <Card className="bg-white/80 dark:bg-card/50 backdrop-blur border-slate-100 dark:border-border/40 shadow-md hover:shadow-xl hover:-translate-y-1.5 transition-all relative overflow-hidden flex flex-col p-8 rounded-2xl group">
            <div className="absolute top-0 left-0 w-full h-[4px] bg-gradient-to-r from-emerald-500 to-emerald-200" />
            <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-6 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400">
              <Users size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-foreground mb-3">Citizen Portal</h3>
            <p className="text-sm text-slate-500 dark:text-muted-foreground leading-relaxed mb-6 flex-grow">
              Be the eyes of your neighborhood. Take a photo, select category, pinpoint coordinates, and submit reports in under 30 seconds.
            </p>
            <ul className="list-none flex flex-col gap-3 mb-8 p-0 text-left">
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" /> Instant photo-based waste reporting
              </li>
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" /> Live ticket tracking & history logs
              </li>
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" /> Gamified Eco-points & rewards
              </li>
            </ul>
            <Button className="w-full bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm hover:shadow-md transition-all gap-1.5" onClick={() => navigate('/citizen/login')}>
              Enter Citizen Portal <ChevronRight size={16} />
            </Button>
          </Card>

          {/* Supervisor Card */}
          <Card className="bg-white/80 dark:bg-card/50 backdrop-blur border-slate-100 dark:border-border/40 shadow-md hover:shadow-xl hover:-translate-y-1.5 transition-all relative overflow-hidden flex flex-col p-8 rounded-2xl group">
            <div className="absolute top-0 left-0 w-full h-[4px] bg-gradient-to-r from-blue-500 to-blue-200" />
            <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-6 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400">
              <MapPin size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-foreground mb-3">Supervisor Portal</h3>
            <p className="text-sm text-slate-500 dark:text-muted-foreground leading-relaxed mb-6 flex-grow">
              Optimize field operations. Inspect assigned waste hotspots, navigate using smart map routes, and submit verified photo closures.
            </p>
            <ul className="list-none flex flex-col gap-3 mb-8 p-0 text-left">
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-500" /> Real-time issue assignment alerts
              </li>
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-500" /> High-precision location navigation
              </li>
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-500" /> One-click resolved status submissions
              </li>
            </ul>
            <Button className="w-full bg-blue-600 hover:bg-blue-700 text-white shadow-sm hover:shadow-md transition-all gap-1.5" onClick={() => navigate('/supervisor/login')}>
              Enter Staff Portal <ChevronRight size={16} />
            </Button>
          </Card>

          {/* Admin Control Panel */}
          <Card className="bg-white/80 dark:bg-card/50 backdrop-blur border-slate-100 dark:border-border/40 shadow-md hover:shadow-xl hover:-translate-y-1.5 transition-all relative overflow-hidden flex flex-col p-8 rounded-2xl group">
            <div className="absolute top-0 left-0 w-full h-[4px] bg-gradient-to-r from-indigo-500 to-indigo-200" />
            <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-6 bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
              <ShieldCheck size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-foreground mb-3">Admin Dashboard</h3>
            <p className="text-sm text-slate-500 dark:text-muted-foreground leading-relaxed mb-6 flex-grow">
              Harness centralized coordination. Review analytics, oversee municipal metrics, register employees, and coordinate smart dispatch loops.
            </p>
            <ul className="list-none flex flex-col gap-3 mb-8 p-0 text-left">
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" /> Advanced dashboard analytics maps
              </li>
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" /> Supervisor registration & routing control
              </li>
              <li className="flex items-center gap-3 text-xs md:text-sm text-slate-600 dark:text-muted-foreground">
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" /> City-wide status audit logs
              </li>
            </ul>
            <Button className="w-full bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm hover:shadow-md transition-all gap-1.5" onClick={() => navigate('/admin/login')}>
              Enter Admin Control <ChevronRight size={16} />
            </Button>
          </Card>
        </div>
      </section>

      {/* Rewards Leaderboard Section */}
      <section className="py-16 md:py-20 px-6 md:px-[8%] bg-white/30 dark:bg-background/20 border-t border-slate-100 dark:border-border/20" id="leaderboard">
        <div className="flex flex-col lg:flex-row items-center justify-between max-w-6xl mx-auto gap-12 lg:gap-20">
          <div className="flex-1 max-w-xl text-left">
            <Badge className="bg-amber-50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-400 border border-amber-100 dark:border-amber-900/30 hover:bg-amber-50 rounded-full px-3 py-1 text-xs font-semibold mb-5 gap-2">
              <Trophy size={16} />
              <span>Community Hero Leaderboard</span>
            </Badge>
            <h2 className="text-3xl md:text-4xl font-extrabold text-slate-900 dark:text-foreground leading-tight mb-5">
              Get Rewarded For Cleaning Your Community
            </h2>
            <p className="text-sm md:text-base text-slate-500 dark:text-muted-foreground leading-relaxed mb-6">
              EcoCollect values civic responsibility. Submit validated waste reports to earn Eco-points. Compare rankings on local boards and redeem points for sustainability awards, coupons, and local merchant benefits.
            </p>
            <div className="flex flex-col gap-4">
              <div className="flex items-center gap-3">
                <CheckCircle2 size={20} className="text-emerald-500" />
                <span className="text-[15px] font-medium text-slate-700 dark:text-muted-foreground">100 points for reporting an approved issue</span>
              </div>
              <div className="flex items-center gap-3">
                <CheckCircle2 size={20} className="text-emerald-500" />
                <span className="text-[15px] font-medium text-slate-700 dark:text-muted-foreground">Streak bonuses for consecutive weekly cleanups</span>
              </div>
              <div className="flex items-center gap-3">
                <CheckCircle2 size={20} className="text-emerald-500" />
                <span className="text-[15px] font-medium text-slate-700 dark:text-muted-foreground">Leaderboard badges shown on citizen profile</span>
              </div>
            </div>
          </div>

          <Card className="flex-1 w-full max-w-[500px] bg-white dark:bg-card border border-slate-200 dark:border-border/40 rounded-2xl shadow-lg p-6 md:p-8">
            <div className="flex justify-between items-center mb-6 border-b border-slate-100 dark:border-border/40 pb-4">
              <span className="text-lg font-bold text-slate-900 dark:text-foreground">Top Contributors</span>
              <span className="text-xs font-bold px-2 py-1 bg-amber-100 dark:bg-amber-950/40 text-amber-700 dark:text-amber-400 rounded">June 2026</span>
            </div>
            <div className="flex flex-col gap-3">
              {leaderboardUsers.map((item) => (
                <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-muted/20 border border-slate-100/50 dark:border-border/20 hover:translate-x-1 hover:bg-white dark:hover:bg-muted/40 hover:border-emerald-500 hover:shadow-sm transition-all" key={item.rank}>
                  <div className="flex items-center gap-3.5">
                    <span className={`font-extrabold text-sm w-5 text-center ${
                      item.rank === 1 ? 'text-amber-500' : 
                      item.rank === 2 ? 'text-slate-400' : 
                      item.rank === 3 ? 'text-amber-800' : 'text-slate-300'
                    }`}>{item.rank}</span>
                    <div className="w-9 h-9 rounded-full bg-slate-200 dark:bg-slate-800 flex items-center justify-center font-bold text-xs text-slate-600 dark:text-muted-foreground">{item.initials}</div>
                    <span className="text-sm font-semibold text-slate-900 dark:text-foreground">{item.name}</span>
                  </div>
                  <span className="font-bold text-sm text-emerald-600 dark:text-emerald-400">{item.points} pts</span>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="py-16 md:py-24 px-6 md:px-[8%] bg-slate-50 dark:bg-slate-950 border-t border-b border-slate-100 dark:border-border/20" id="testimonials">
        <div className="max-w-7xl mx-auto text-center">
          <span className="text-emerald-600 dark:text-emerald-400 text-xs font-bold uppercase tracking-widest mb-3 block">Citizen & Staff Feedback</span>
          <h2 className="text-3xl md:text-4xl font-extrabold text-slate-900 dark:text-foreground mb-4">What Our Community Says</h2>
          <p className="text-sm md:text-base text-slate-500 dark:text-muted-foreground leading-relaxed max-w-2xl mx-auto mb-16">
            EcoCollect is built for the people of our city. Here are stories from citizens, supervisors, and administrative officers.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Testimonial 1 */}
            <Card className="bg-white dark:bg-card border-slate-100 dark:border-border/40 shadow-sm p-8 rounded-2xl flex flex-col justify-between text-left hover:shadow-md transition-all">
              <div>
                <div className="flex gap-1 mb-4 text-amber-500">
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                </div>
                <p className="text-sm text-slate-600 dark:text-muted-foreground leading-relaxed italic mb-6">
                  "EcoCollect made it so easy to get that overflowing dumpster on my corner cleared. Plus, getting points for helping my city feels amazing!"
                </p>
              </div>
              <div className="flex items-center gap-3.5 pt-4 border-t border-slate-100 dark:border-border/40">
                <div className="w-10 h-10 rounded-full bg-emerald-100 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400 flex items-center justify-center font-bold text-sm">AS</div>
                <div>
                  <h4 className="text-sm font-bold text-slate-900 dark:text-foreground">Aarav Sharma</h4>
                  <span className="text-xs text-slate-400 dark:text-muted-foreground font-medium">Citizen Hero (2,850 pts)</span>
                </div>
              </div>
            </Card>

            {/* Testimonial 2 */}
            <Card className="bg-white dark:bg-card border-slate-100 dark:border-border/40 shadow-sm p-8 rounded-2xl flex flex-col justify-between text-left hover:shadow-md transition-all">
              <div>
                <div className="flex gap-1 mb-4 text-amber-500">
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                </div>
                <p className="text-sm text-slate-600 dark:text-muted-foreground leading-relaxed italic mb-6">
                  "No more searching for coordinates. The route routing brings me straight to the issue, and uploading resolution proof is instant. Real productivity booster."
                </p>
              </div>
              <div className="flex items-center gap-3.5 pt-4 border-t border-slate-100 dark:border-border/40">
                <div className="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-950/40 text-blue-700 dark:text-blue-400 flex items-center justify-center font-bold text-sm">VS</div>
                <div>
                  <h4 className="text-sm font-bold text-slate-900 dark:text-foreground">Vikram Singh</h4>
                  <span className="text-xs text-slate-400 dark:text-muted-foreground font-medium">Field Supervisor</span>
                </div>
              </div>
            </Card>

            {/* Testimonial 3 */}
            <Card className="bg-white dark:bg-card border-slate-100 dark:border-border/40 shadow-sm p-8 rounded-2xl flex flex-col justify-between text-left hover:shadow-md transition-all">
              <div>
                <div className="flex gap-1 mb-4 text-amber-500">
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                  <Star size={16} className="fill-current" />
                </div>
                <p className="text-sm text-slate-600 dark:text-muted-foreground leading-relaxed italic mb-6">
                  "Centralized analytics has given us complete oversight of municipal waste targets. Dispatch and resolution response times dropped by 65% in two weeks."
                </p>
              </div>
              <div className="flex items-center gap-3.5 pt-4 border-t border-slate-100 dark:border-border/40">
                <div className="w-10 h-10 rounded-full bg-indigo-100 dark:bg-indigo-950/40 text-indigo-700 dark:text-indigo-400 flex items-center justify-center font-bold text-sm">AP</div>
                <div>
                  <h4 className="text-sm font-bold text-slate-900 dark:text-foreground">Dr. Ananya Patel</h4>
                  <span className="text-xs text-slate-400 dark:text-muted-foreground font-medium">Municipal Commissioner</span>
                </div>
              </div>
            </Card>
          </div>
        </div>
      </section>

      {/* Call to Action Banner */}
      <section className="py-12 md:py-16 px-6 md:px-[8%] max-w-7xl mx-auto">
        <div className="bg-gradient-to-r from-static-slate-900 to-static-slate-950 rounded-2xl p-8 md:p-16 text-center text-white shadow-2xl relative overflow-hidden">
          <div className="absolute top-[-50%] right-[-20%] w-[400px] h-[400px] bg-emerald-500/10 rounded-full blur-[80px] pointer-events-none" />
          <h2 className="text-3xl md:text-4xl font-extrabold mb-4 text-white">Ready to Make an Impact?</h2>
          <p className="text-static-slate-300 text-sm md:text-lg max-w-xl mx-auto mb-8">
            Sign up today as a citizen to report community waste problems or connect with municipal admins to request supervisor credentials.
          </p>
          <div className="flex justify-center gap-4 flex-wrap">
            <Button className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold px-6 py-5 rounded-lg shadow-md hover:-translate-y-0.5 transition-all" onClick={() => navigate('/citizen/signup')}>
              Sign Up As Citizen
            </Button>
            <Button className="bg-white hover:bg-static-slate-100 text-static-slate-950 font-semibold px-6 py-5 rounded-lg shadow-md hover:-translate-y-0.5 transition-all" onClick={() => navigate('/citizen/login')}>
              Access Portal Gateways
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-static-slate-950 py-12 md:py-16 px-6 md:px-[8%] text-static-slate-400 border-t border-white/10">
        <div className="flex flex-col md:flex-row justify-between items-start gap-12 max-w-6xl mx-auto mb-10 text-left">
          <div className="max-w-xs">
            <div className="flex items-center gap-3 mb-4">
              <img src={appLogo} alt="EcoCollect Logo" className="w-9 h-9" />
              <span className="text-white font-bold text-lg">EcoCollect</span>
            </div>
            <p className="text-sm leading-relaxed">
              EcoCollect is a state-of-the-art smart waste management system designed to connect communities and municipal authorities for optimized cleaner environments.
            </p>
          </div>
          
          <div className="flex flex-col gap-3.5">
            <span className="text-white font-semibold text-xs uppercase tracking-wider">Portals</span>
            <span className="text-sm hover:text-white cursor-pointer transition-colors" onClick={() => navigate('/citizen/login')}>Citizen Login</span>
            <span className="text-sm hover:text-white cursor-pointer transition-colors" onClick={() => navigate('/supervisor/login')}>Supervisor Login</span>
            <span className="text-sm hover:text-white cursor-pointer transition-colors" onClick={() => navigate('/admin/login')}>Admin Dashboard</span>
          </div>

          <div className="flex flex-col gap-3.5">
            <span className="text-white font-semibold text-xs uppercase tracking-wider">System</span>
            <span className="text-sm hover:text-white cursor-pointer transition-colors" onClick={() => navigate('/onboarding')}>Tutorial Onboarding</span>
            <span className="text-sm hover:text-white cursor-pointer transition-colors" onClick={() => navigate('/all-pages')}>Developer Directory</span>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row justify-between items-center max-w-6xl mx-auto border-t border-white/10 pt-8 gap-4 text-xs">
          <span>&copy; {new Date().getFullYear()} EcoCollect. All rights reserved.</span>
          <span className="flex items-center gap-1.5">
            Made with <Leaf size={14} className="text-emerald-500" /> for Greener Cities
          </span>
        </div>
      </footer>
    </div>
  );
};
