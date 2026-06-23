import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { 
  MapPin, Clock, Award, ArrowRight, ClipboardList, 
  CheckCircle2, AlertTriangle, Route, Bell, Calendar,
  PlayCircle
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';

interface DashboardStats {
  assigned_reports: number;
  completed_reports: number;
  pending_reports: number;
  performance_score: number;
}

interface AssignedIssue {
  id: number;
  title: string;
  category: string;
  address?: string;
  status: string;
  created_at: string;
  image_url?: string;
  latitude?: number;
  longitude?: number;
}

interface HistoryLog {
  id: number;
  issue_id: number;
  status: string;
  remarks: string;
  created_at: string;
  category: string;
  title: string;
}

const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  const R = 6371; // Radius of the earth in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const d = R * c;
  return d; // Distance in km
};

export const SupervisorDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { supervisor, apiCall } = useAuth();

  const [stats, setStats] = useState<DashboardStats>({
    assigned_reports: 0,
    completed_reports: 0,
    pending_reports: 0,
    performance_score: 100
  });
  const [issues, setIssues] = useState<AssignedIssue[]>([]);
  const [historyLogs, setHistoryLogs] = useState<HistoryLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [coords, setCoords] = useState<{ latitude: number; longitude: number } | null>(null);

  const [activeTab, setActiveTab] = useState('all');

  useEffect(() => {
    const fetchSupervisorData = async () => {
      try {
        const statsData = await apiCall<DashboardStats>('/supervisor/dashboard');
        setStats(statsData);

        const issuesData = await apiCall<AssignedIssue[]>('/supervisor/issues');
        // Sort by newest assigned
        const sorted = issuesData.sort((a, b) => b.id - a.id);
        setIssues(sorted);

        const logsData = await apiCall<HistoryLog[]>('/supervisor/history');
        setHistoryLogs(logsData.slice(0, 5)); // show latest 5 log entries
      } catch (err) {
        console.error('Failed to load supervisor data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSupervisorData();
  }, [apiCall]);

  useEffect(() => {
    if (!navigator.geolocation) return;

    // Immediately sync once
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        setCoords({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        });
        try {
          await apiCall(`/supervisor/location?latitude=${position.coords.latitude}&longitude=${position.coords.longitude}`, {
            method: 'PUT'
          });
        } catch (err) {
          console.error('Failed to sync supervisor location:', err);
        }
      },
      (error) => console.warn('Sync location error:', error.message),
      { enableHighAccuracy: true }
    );

    // Watch and poll coordinates every 8 seconds
    const interval = setInterval(() => {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          setCoords({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          });
          try {
            await apiCall(`/supervisor/location?latitude=${position.coords.latitude}&longitude=${position.coords.longitude}`, {
              method: 'PUT'
            });
          } catch (err) {
            console.error('Failed to sync supervisor location:', err);
          }
        },
        (error) => console.warn('Sync location error:', error.message),
        { enableHighAccuracy: true }
      );
    }, 8000);

    return () => clearInterval(interval);
  }, [apiCall]);

  const formatTime = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHrs = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHrs / 24);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
      if (diffHrs < 24) return `${diffHrs} hour${diffHrs > 1 ? 's' : ''} ago`;
      return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    } catch (e) {
      return dateStr;
    }
  };

  const getBadgeClass = (status: string) => {
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return 'bg-amber-50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-400 border-amber-200 dark:border-amber-900/30 hover:bg-amber-50 dark:hover:bg-amber-950/25';
      case 'in_progress':
        return 'bg-blue-50 dark:bg-blue-950/20 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-900/30 hover:bg-blue-50 dark:hover:bg-blue-950/25';
      case 'completed':
      case 'resolved':
        return 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/30 hover:bg-emerald-50 dark:hover:bg-emerald-950/25';
      default:
        return 'bg-slate-50 dark:bg-slate-950/20 text-slate-700 dark:text-slate-400 border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-950/25';
    }
  };

  const progressPercentage = stats.assigned_reports > 0 
    ? Math.round((stats.completed_reports / stats.assigned_reports) * 100) 
    : 0;

  // Compute dynamic navigation stops sequence
  const getDynamicStops = () => {
    const activeTasks = issues.filter(i => ['pending', 'in_progress'].includes(i.status.toLowerCase().replace(' ', '_')));
    
    if (coords) {
      // Sort nearest first
      return activeTasks
        .map(task => {
          const distanceVal = task.latitude && task.longitude 
            ? calculateDistance(coords.latitude, coords.longitude, Number(task.latitude), Number(task.longitude))
            : 0;
          return { ...task, distanceVal };
        })
        .sort((a, b) => a.distanceVal - b.distanceVal);
    }
    
    return activeTasks.map(t => ({ ...t, distanceVal: 0 }));
  };

  const dynamicStops = getDynamicStops();

  const launchRoute = () => {
    if (dynamicStops.length === 0) return;
    navigate(`/supervisor/map/${dynamicStops[0].id}`);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 dark:border-slate-800 border-t-blue-600 dark:border-t-blue-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up pb-12 text-foreground">
      {/* Greeting Banner */}
      <Card className="bg-gradient-to-r from-blue-600 to-indigo-600 dark:from-blue-700 dark:to-indigo-700 border-none text-white shadow-lg relative overflow-hidden">
        <div className="absolute top-[-20%] right-[-10%] w-[300px] h-[300px] bg-white/10 rounded-full blur-[80px] pointer-events-none" />
        <CardContent className="p-6 md:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6 relative">
          <div className="text-left">
            <Badge className="bg-white/20 text-white border-none hover:bg-white/25 mb-3 px-3 py-1 text-[10px] font-bold uppercase tracking-wider">
              Field Operations Control
            </Badge>
            <h2 className="text-2xl md:text-3xl font-extrabold tracking-tight mb-2">Welcome Back, {supervisor?.name || 'Supervisor'}</h2>
            <p className="text-sm text-blue-100/90 font-medium">Employee ID: {supervisor?.employee_id || 'N/A'}</p>
          </div>
          <div className="flex items-center gap-4 bg-white/10 backdrop-blur-sm p-4 rounded-xl border border-white/10 shrink-0 self-stretch md:self-auto justify-between md:justify-start">
            <div className="text-left">
              <span className="text-[10px] text-blue-200 font-bold uppercase tracking-wider block mb-1">Performance Rating</span>
              <span className="text-2xl font-extrabold flex items-center gap-1.5 text-white">
                <Award className="text-amber-300 fill-amber-300" size={24} />
                {stats.performance_score}%
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Stats Cards Row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 md:gap-6">
        <Card className="bg-card border-border shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6 flex items-center gap-4 text-left">
            <div className="w-12 h-12 rounded-full flex items-center justify-center bg-blue-500/10 text-blue-600 dark:text-blue-400 shrink-0">
              <ClipboardList size={22} />
            </div>
            <div>
              <span className="text-2xl font-extrabold text-foreground leading-none">{stats.assigned_reports}</span>
              <span className="text-xs text-muted-foreground font-medium block mt-1">Assigned Workposts</span>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-card border-border shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6 flex items-center gap-4 text-left">
            <div className="w-12 h-12 rounded-full flex items-center justify-center bg-amber-500/10 text-amber-600 dark:text-amber-400 shrink-0">
              <Clock size={22} />
            </div>
            <div>
              <span className="text-2xl font-extrabold text-foreground leading-none">{stats.pending_reports}</span>
              <span className="text-xs text-muted-foreground font-medium block mt-1">Pending Inspections</span>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-card border-border shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6 flex items-center gap-4 text-left">
            <div className="w-12 h-12 rounded-full flex items-center justify-center bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 shrink-0">
              <CheckCircle2 size={22} />
            </div>
            <div>
              <span className="text-2xl font-extrabold text-foreground leading-none">{stats.completed_reports}</span>
              <span className="text-xs text-muted-foreground font-medium block mt-1">Inspections Completed</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Middle Grid Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Daily Target Progress */}
        <Card className="bg-card border-border shadow-sm h-full flex flex-col justify-between">
          <CardHeader className="pb-3 text-left">
            <CardTitle className="text-base font-bold flex items-center gap-2 text-foreground">
              <Award size={18} className="text-emerald-500 dark:text-emerald-400" /> Daily Target Progress
            </CardTitle>
            <CardDescription className="text-xs text-muted-foreground">
              Current shift performance goal and metrics.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-4 text-left flex-grow justify-center">
            <div className="flex justify-between items-baseline mb-[-8px]">
              <span className="text-sm font-semibold text-muted-foreground">Tasks Completed</span>
              <span className="text-lg font-extrabold text-foreground">{stats.completed_reports} / {stats.assigned_reports}</span>
            </div>
            <Progress value={progressPercentage} className="h-2.5 bg-muted" />
            <p className="text-xs text-muted-foreground leading-relaxed bg-muted/40 border border-border/50 p-3 rounded-lg">
              {progressPercentage >= 100 
                ? "🎉 Amazing work! You have completed all assigned inspections for today's shift."
                : `Keep going! Resolve ${stats.pending_reports} more pending reports to achieve 100% resolution score.`
              }
            </p>
          </CardContent>
        </Card>

        {/* Dynamic Route Planner */}
        <Card className="bg-card border-border shadow-sm h-full flex flex-col justify-between">
          <CardHeader className="pb-3 text-left">
            <CardTitle className="text-base font-bold flex items-center gap-2 text-foreground">
              <Route size={18} className="text-blue-500" /> Suggested Navigation Stop Sequence
            </CardTitle>
            <CardDescription className="text-xs text-muted-foreground">
              Proximity routing sequence calculated to minimize travel time.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3 text-left flex-grow justify-between">
            <div className="flex-grow flex flex-col justify-center gap-3">
              {dynamicStops.length === 0 ? (
                <div className="py-6 text-center text-xs text-muted-foreground italic bg-muted/45 border border-border/50 rounded-xl">
                  No pending tasks to route.
                </div>
              ) : (
                dynamicStops.slice(0, 3).map((stop, idx) => (
                  <div 
                     key={stop.id} 
                     className="flex items-center gap-3 p-2.5 rounded-lg bg-muted/40 border border-border/60 cursor-pointer hover:bg-muted/80"
                     onClick={() => navigate(`/supervisor/report/${stop.id}`)}
                  >
                    <div className="w-6 h-6 rounded-full bg-blue-500/10 text-blue-600 dark:text-blue-400 text-xs font-bold flex items-center justify-center shrink-0">
                      {idx + 1}
                    </div>
                    <div className="flex-grow min-w-0">
                      <div className="text-xs font-bold text-foreground truncate">{stop.title || stop.category}</div>
                      <span className="text-[10px] text-muted-foreground">
                        {stop.distanceVal > 0 ? `${stop.distanceVal.toFixed(1)} km away` : 'Calculating distance'} • {stop.address || 'Address unspecified'}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
            <Button 
              variant="outline" 
              size="sm" 
              className="w-full text-xs font-semibold text-blue-600 dark:text-blue-400 hover:bg-muted border-blue-200 dark:border-blue-900/30 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed mt-2"
              onClick={launchRoute}
              disabled={dynamicStops.length === 0}
            >
              Launch Route in Live Navigation Maps
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Allocated Waste Tasks Feed (Tabs filtering) */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full text-left">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-4">
          <span className="text-sm font-bold text-muted-foreground uppercase tracking-wider">
            Allocated Tasks Feed ({issues.length})
          </span>
          <TabsList className="bg-muted p-0.5 border border-border/60 rounded-lg">
            <TabsTrigger value="all" className="text-xs font-semibold px-3 py-1.5 rounded-md">All</TabsTrigger>
            <TabsTrigger value="pending" className="text-xs font-semibold px-3 py-1.5 rounded-md">Pending</TabsTrigger>
            <TabsTrigger value="in_progress" className="text-xs font-semibold px-3 py-1.5 rounded-md">In Progress</TabsTrigger>
            <TabsTrigger value="completed" className="text-xs font-semibold px-3 py-1.5 rounded-md">Completed</TabsTrigger>
          </TabsList>
        </div>

        <div className="mt-0">
          <TaskFeed issues={
            activeTab === 'all' 
              ? issues 
              : activeTab === 'pending'
              ? issues.filter(issue => issue.status.toLowerCase().replace(' ', '_') === 'pending')
              : activeTab === 'in_progress'
              ? issues.filter(issue => issue.status.toLowerCase().replace(' ', '_') === 'in_progress')
              : issues.filter(issue => ['completed', 'resolved'].includes(issue.status.toLowerCase().replace(' ', '_')))
          } getBadgeClass={getBadgeClass} navigate={navigate} />
        </div>
      </Tabs>

      {/* Shift Log Timeline Activity Panel */}
      <Card className="bg-card border-border shadow-sm text-left">
        <CardHeader className="pb-3 border-b border-border/60">
          <CardTitle className="text-base font-bold flex items-center gap-2 text-foreground">
            <Bell size={18} className="text-indigo-500" /> Recent Shift Timeline Activities
          </CardTitle>
          <CardDescription className="text-xs text-muted-foreground">
            Audit history of updates submitted during your active shift.
          </CardDescription>
        </CardHeader>
        <CardContent className="p-6">
          <div className="flex flex-col gap-6 relative before:absolute before:top-2 before:bottom-2 before:left-[11px] before:w-[2px] before:bg-border">
            {historyLogs.length === 0 ? (
              <p className="text-xs text-muted-foreground italic">No activity logged yet during your shifts.</p>
            ) : (
              historyLogs.map((log) => {
                const isCompleted = log.status.toLowerCase() === 'completed' || log.status.toLowerCase() === 'resolved';
                return (
                  <div key={log.id} className="flex items-start gap-4 relative" onClick={() => navigate(`/supervisor/report/${log.issue_id}`)}>
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 z-10 border-2 border-card cursor-pointer ${
                      isCompleted ? 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600' : 'bg-blue-50 dark:bg-blue-950/20 text-blue-600'
                    }`}>
                      {isCompleted ? (
                        <CheckCircle2 size={12} className="fill-current text-white dark:text-emerald-400 bg-emerald-600 dark:bg-transparent rounded-full" />
                      ) : (
                        <PlayCircle size={12} className="fill-current text-white dark:text-blue-400 bg-blue-600 dark:bg-transparent rounded-full" />
                      )}
                    </div>
                    <div className="flex-grow text-left cursor-pointer">
                      <div className="flex justify-between items-baseline gap-2 mb-0.5">
                        <span className="text-xs font-bold text-foreground">
                          {isCompleted ? 'Closed' : 'Updated'} waste ticket #ID-{log.issue_id}
                        </span>
                        <span className="text-[10px] text-muted-foreground">{formatTime(log.created_at)}</span>
                      </div>
                      <p className="text-[11px] text-muted-foreground">
                        Category: {log.category} • Status: {log.status}
                      </p>
                      {log.remarks && <p className="text-[11px] text-muted-foreground italic mt-0.5">"{log.remarks}"</p>}
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

// Task Feed Subcomponent
interface TaskFeedProps {
  issues: AssignedIssue[];
  getBadgeClass: (status: string) => string;
  navigate: (path: string) => void;
}

const TaskFeed: React.FC<TaskFeedProps> = ({ issues, getBadgeClass, navigate }) => {
  if (issues.length === 0) {
    return (
      <Card className="bg-card border-border shadow-sm p-12 flex flex-col items-center justify-center text-center gap-4 w-full">
        <CheckCircle2 size={36} className="text-muted-foreground/45" />
        <p className="text-sm text-muted-foreground font-medium">No active waste tasks match the selected filter.</p>
      </Card>
    );
  }

  const getBorderColorClass = (status: string) => {
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return 'border-l-amber-500 hover:border-l-amber-600';
      case 'in_progress':
        return 'border-l-blue-500 hover:border-l-blue-600';
      case 'completed':
      case 'resolved':
        return 'border-l-emerald-500 hover:border-l-emerald-600';
      default:
        return 'border-l-slate-400 hover:border-l-slate-500';
    }
  };

  return (
    <div className="flex flex-col gap-4">
      {issues.map((issue) => (
        <Card 
          key={issue.id} 
          className={`bg-card border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 cursor-pointer border-l-4 ${getBorderColorClass(issue.status)}`}
          onClick={() => navigate(`/supervisor/report/${issue.id}`)}
        >
          <CardContent className="p-4 sm:p-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div className="flex flex-col gap-1 text-left min-w-0 w-full sm:w-auto flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <Badge variant="secondary" className="text-[10px] font-bold bg-muted text-muted-foreground border-none">
                  {issue.category}
                </Badge>
                <span className="text-[10px] font-bold text-muted-foreground bg-muted/40 px-1.5 py-0.5 rounded border border-border/50 shrink-0">
                  #ID-{issue.id}
                </span>
              </div>
              <h3 className="text-sm sm:text-base font-bold text-foreground truncate mt-1">
                {issue.title || `${issue.category} report`}
              </h3>
              <div className="text-xs text-muted-foreground flex items-center mt-1 truncate">
                <MapPin size={13} className="mr-1 text-muted-foreground shrink-0" />
                {issue.address || 'Address unspecified'}
              </div>
              <div className="text-[10px] text-muted-foreground mt-1 font-medium">
                Assigned Date: {new Date(issue.created_at).toLocaleDateString(undefined, {
                  month: 'short',
                  day: 'numeric',
                  year: 'numeric'
                })}
              </div>
            </div>
            
            <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-border/50 gap-3 shrink-0">
              <Badge className={`${getBadgeClass(issue.status)} text-[10px] font-extrabold uppercase border shadow-none px-2.5 py-1`}>
                {issue.status}
              </Badge>
              <div className="flex items-center gap-1.5 text-xs text-blue-600 dark:text-blue-400 font-bold sm:hidden">
                View Ticket <ArrowRight size={14} />
              </div>
              <ArrowRight size={18} className="text-muted-foreground hidden sm:block" />
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};
