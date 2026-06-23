import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../../components/Button';
import { useTheme } from 'next-themes';
import { 
  ArrowLeft, Calendar, User, MapPin, 
  CheckCircle, ClipboardList, ImageIcon, UserCheck, AlertTriangle
} from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Map, MapMarker, MarkerContent } from '@/components/ui/map';
import { DeliveryTracker } from '@/components/ui/delivery-tracker';
import { ImageGalleryCarousel } from '../../../components/ImageGalleryCarousel';
import { motion } from 'framer-motion';

interface Issue {
  id: number;
  category: string;
  title?: string;
  description?: string;
  address?: string;
  status: string;
  created_at: string;
  image_url?: string;
  resolved_at?: string;
  completion_image_url?: string;
  reporter_name?: string;
  assigned_supervisor_id?: number | null;
  latitude?: number | null;
  longitude?: number | null;
}

interface Supervisor {
  id: number;
  full_name: string;
  employee_id: string;
  assigned_area?: string;
  latitude?: number | null;
  longitude?: number | null;
  coverage_radius?: number | null;
  distance_km?: number | null;
}

interface AuditHistory {
  id: number;
  status: string;
  remarks?: string;
  created_at: string;
}

const TimelineNode: React.FC<{ status: string }> = ({ status }) => {
  const isCompleted = status.toLowerCase() === 'completed' || status.toLowerCase() === 'resolved';
  const isInProgress = status.toLowerCase() === 'in_progress' || status.toLowerCase() === 'in progress';
  const isPending = status.toLowerCase() === 'pending';

  return (
    <div className="absolute -left-[36px] top-1.5 w-6 h-6 flex items-center justify-center bg-card rounded-full z-10 select-none">
      {isCompleted ? (
        <svg className="w-5 h-5 text-emerald-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-emerald-500/10 dark:fill-emerald-500/20"
          />
          <motion.path 
            d="M6 10 L9 13 L14 7" 
            stroke="currentColor" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round"
            initial={{ pathLength: 0 }}
            animate={{ pathLength: 1 }}
            transition={{ delay: 0.2, duration: 0.3 }}
          />
        </svg>
      ) : isInProgress ? (
        <svg className="w-5 h-5 text-blue-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-blue-500/10 dark:fill-blue-500/20"
          />
          <motion.circle
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="1"
            initial={{ opacity: 0.6, scale: 1 }}
            animate={{ opacity: 0, scale: 1.4 }}
            transition={{ repeat: Infinity, duration: 1.5, ease: "easeOut" }}
          />
          <motion.circle 
            cx="10" 
            cy="10" 
            r="3.5" 
            fill="currentColor"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.1, duration: 0.2 }}
          />
        </svg>
      ) : isPending ? (
        <svg className="w-5 h-5 text-amber-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-amber-500/10 dark:fill-amber-500/20"
          />
          <motion.path
            d="M10 6 V11"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            initial={{ pathLength: 0 }}
            animate={{ pathLength: 1 }}
            transition={{ delay: 0.2, duration: 0.3 }}
          />
          <motion.circle
            cx="10"
            cy="14"
            r="1.2"
            fill="currentColor"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.4, duration: 0.2 }}
          />
        </svg>
      ) : (
        <svg className="w-5 h-5 text-indigo-500 animate-fade-in" viewBox="0 0 20 20" fill="none">
          <motion.circle 
            cx="10" 
            cy="10" 
            r="8" 
            stroke="currentColor" 
            strokeWidth="2"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.3 }}
            className="fill-indigo-500/10 dark:fill-indigo-500/20"
          />
          <motion.circle 
            cx="10" 
            cy="10" 
            r="3.5" 
            fill="currentColor"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.1, duration: 0.2 }}
          />
        </svg>
      )}
    </div>
  );
};

export const AdminReportDetails: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { apiCall, backendHost } = useAuth();
  const { theme } = useTheme();
  
  const [issue, setIssue] = useState<Issue | null>(null);
  const [history, setHistory] = useState<AuditHistory[]>([]);
  const [supervisors, setSupervisors] = useState<Supervisor[]>([]);
  const [selectedSupId, setSelectedSupId] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [assigning, setAssigning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mapViewport, setMapViewport] = useState<any>(null);

  // Animation variants for the audit timeline logs
  const listContainerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.12
      }
    }
  };

  const listItemVariants = {
    hidden: { opacity: 0, x: -12 },
    show: { 
      opacity: 1, 
      x: 0, 
      transition: { 
        type: "spring" as const, 
        stiffness: 90, 
        damping: 15 
      } 
    }
  };

  useEffect(() => {
    if (issue && issue.latitude !== null && issue.latitude !== undefined && issue.longitude !== null && issue.longitude !== undefined) {
      const lat = parseFloat(issue.latitude as any);
      const lng = parseFloat(issue.longitude as any);
      if (!isNaN(lat) && !isNaN(lng)) {
        setMapViewport({
          center: [lng, lat],
          zoom: 12
        });
      }
    }
  }, [issue]);

  useEffect(() => {
    if (!selectedSupId || supervisors.length === 0) return;
    const selectedSup = supervisors.find(s => s.id.toString() === selectedSupId);
    if (selectedSup && selectedSup.latitude !== null && selectedSup.latitude !== undefined && selectedSup.longitude !== null && selectedSup.longitude !== undefined) {
      const lat = parseFloat(selectedSup.latitude as any);
      const lng = parseFloat(selectedSup.longitude as any);
      if (!isNaN(lat) && !isNaN(lng)) {
        setMapViewport({
          center: [lng, lat],
          zoom: 12
        });
      }
    }
  }, [selectedSupId, supervisors]);

  const fetchIssueAndSupervisors = async () => {
    try {
      const issueResponse = await apiCall<{ issue: Issue; history: AuditHistory[] }>(`/issues/${id}`);
      setIssue(issueResponse.issue);
      setHistory(issueResponse.history.sort((a, b) => b.id - a.id));

      const supResponse = await apiCall<Supervisor[]>(`/admin/supervisors?issue_id=${id}`);
      setSupervisors(supResponse);
      if (issueResponse.issue.status === 'Pending' && supResponse.length > 0) {
        setSelectedSupId(supResponse[0].id.toString());
      }
    } catch (err) {
      console.error('Failed to load admin report details:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIssueAndSupervisors();
  }, [id, apiCall]);

  useEffect(() => {
    if (!issue || issue.status === 'Pending' || !issue.assigned_supervisor_id) return;

    const interval = setInterval(async () => {
      try {
        const supResponse = await apiCall<Supervisor[]>(`/admin/supervisors?issue_id=${id}`);
        setSupervisors(supResponse);
      } catch (err) {
        console.error('Failed to poll supervisor coordinates:', err);
      }
    }, 5000);

    return () => clearInterval(interval);
  }, [issue?.status, issue?.assigned_supervisor_id, id, apiCall]);

  const handleAssignSupervisor = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSupId) return;

    setAssigning(true);
    setError(null);
    try {
      await apiCall(`/admin/issues/${id}/assign?supervisor_id=${selectedSupId}`, {
        method: 'PUT'
      });
      // Refresh issue details
      await fetchIssueAndSupervisors();
    } catch (err: any) {
      setError(err.message || 'Failed to assign supervisor.');
    } finally {
      setAssigning(false);
    }
  };

  const getBadgeClass = (status: string) => {
    const base = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold capitalize w-fit border";
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return `${base} bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-900/30`;
      case 'in_progress':
        return `${base} bg-blue-50 text-blue-600 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-900/30`;
      case 'completed':
      case 'resolved':
        return `${base} bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-900/30`;
      default:
        return `${base} bg-muted text-muted-foreground border-border`;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-border border-t-indigo-600 dark:border-t-indigo-500 animate-spin" />
      </div>
    );
  }

  if (!issue) {
    return (
      <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
        <h3 className="text-lg font-bold text-foreground">Ticket Not Found</h3>
        <Button onClick={() => navigate('/admin/reports')}>Back to Tickets</Button>
      </div>
    );
  }

  // Find currently assigned supervisor name
  const assignedSupervisor = supervisors.find(s => s.id === issue.assigned_supervisor_id);
  const issueLat = issue.latitude !== null && issue.latitude !== undefined ? parseFloat(issue.latitude as any) : null;
  const issueLng = issue.longitude !== null && issue.longitude !== undefined ? parseFloat(issue.longitude as any) : null;

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up text-left">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Ticket #ID-{issue.id} Operations</h2>
      </div>

      {error && (
        <div className="flex items-center gap-3 p-4 mb-6 text-sm rounded-lg text-left border bg-red-50 dark:bg-red-950/20 text-red-800 dark:text-red-400 border-red-200 dark:border-red-900/30">
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-[1.2fr_1fr] gap-6">
        
        {/* Left Column: Photos & Citizen Details */}
        <div className="flex flex-col gap-5">
          
          {/* Photos Box */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] p-4 flex flex-col">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-2">Before & After Cleanup Status</span>
            <div className={`grid gap-4 ${issue.status === 'Completed' ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1'}`}>
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-2">Citizen Submission (Before)</span>
                {issue.image_url ? (
                  <ImageGalleryCarousel images={issue.image_url} />
                ) : (
                  <div className="h-[220px] rounded-xl bg-muted/40 border border-dashed border-border flex flex-col items-center justify-center gap-2 text-muted-foreground text-sm font-medium">
                    <ImageIcon size={24} className="text-muted-foreground/60" />
                    <span>No Before Photo</span>
                  </div>
                )}
              </div>

              {issue.status === 'Completed' && (
                <div>
                  <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-2">Supervisor Clean Up (After)</span>
                  {issue.completion_image_url ? (
                    <ImageGalleryCarousel images={issue.completion_image_url} />
                  ) : (
                    <div className="h-[220px] rounded-xl bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900/30 flex flex-col items-center justify-center gap-2 text-emerald-800 dark:text-emerald-400 text-sm font-medium">
                      <CheckCircle size={24} className="text-emerald-500 dark:text-emerald-400" />
                      <span>Resolved (No Photo)</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Description */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)]">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider">Citizen Description remarks</span>
            <div className="text-sm text-foreground leading-relaxed bg-muted/40 rounded-xl p-3.5 border border-border mt-2">
              {issue.description || 'No detailed description remarks filed.'}
            </div>
          </div>

          {/* Audit Logs Trail */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)]">
            <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-4">System Timeline Audit Logs</span>
            {history.length === 0 ? (
              <p className="text-xs text-muted-foreground italic">No history logs compiled.</p>
            ) : (
              <motion.div 
                variants={listContainerVariants}
                initial="hidden"
                animate="show"
                className="relative pl-6 ml-2 flex flex-col gap-6"
              >
                {/* Self-drawing vertical timeline track line */}
                <motion.div 
                  className="absolute left-0 top-2 bottom-2 w-0.5 bg-slate-200 dark:bg-slate-800 origin-top"
                  initial={{ scaleY: 0 }}
                  animate={{ scaleY: 1 }}
                  transition={{ duration: 0.8, ease: "easeInOut" }}
                />

                {history.map((h) => (
                  <motion.div 
                    key={h.id} 
                    variants={listItemVariants}
                    className="relative pl-2 group"
                  >
                    <TimelineNode status={h.status} />
                    <div className="flex flex-col gap-1.5 p-3 rounded-xl bg-slate-500/5 hover:bg-slate-500/10 border border-slate-200/40 dark:border-slate-800/40 hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full capitalize ${
                          h.status.toLowerCase() === 'pending' ? 'bg-amber-500/10 text-amber-600 dark:text-amber-400' :
                          h.status.toLowerCase().includes('progress') ? 'bg-blue-500/10 text-blue-600 dark:text-blue-400' :
                          'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                        }`}>
                          {h.status}
                        </span>
                        <div className="flex items-center gap-1 text-[10px] text-muted-foreground font-semibold">
                          <Calendar size={10} className="text-muted-foreground/60" />
                          <span>{new Date(h.created_at).toLocaleString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                      </div>
                      {h.remarks && (
                        <p className="text-xs text-foreground/80 leading-normal pl-0.5">
                          {h.remarks}
                        </p>
                      )}
                    </div>
                  </motion.div>
                ))}
              </motion.div>
            )}
          </div>

        </div>

        {/* Right Column: Ticket info & Allocations */}
        <div className="flex flex-col gap-5">
          
          {/* Metadata properties box */}
          <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.12)] flex flex-col gap-4">
            <div className="flex gap-2.5 text-muted-foreground">
              <ClipboardList size={18} className="shrink-0 text-indigo-500 dark:text-indigo-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Ticket Category</span>
                <span className="text-sm font-semibold text-foreground">{issue.category}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <Calendar size={18} className="shrink-0 text-indigo-500 dark:text-indigo-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Filing Timestamp</span>
                <span className="text-sm font-semibold text-foreground">{new Date(issue.created_at).toLocaleString()}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <MapPin size={18} className="shrink-0 text-indigo-500 dark:text-indigo-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Address Location</span>
                <span className="text-sm font-semibold text-foreground">{issue.address}</span>
              </div>
            </div>

            <div className="flex gap-2.5 text-muted-foreground">
              <User size={18} className="shrink-0 text-indigo-500 dark:text-indigo-400" />
              <div>
                <span className="block text-[11px] uppercase font-bold text-muted-foreground tracking-wider mb-0.5">Reporter Account</span>
                <span className="text-sm font-semibold text-foreground">{issue.reporter_name || 'Anonymous Citizen'}</span>
              </div>
            </div>

            <div className="flex items-center gap-3 pt-2 border-t border-border">
              <span className="text-[11px] uppercase font-bold text-muted-foreground tracking-wider">Operational Status:</span>
              <span className={getBadgeClass(issue.status)}>
                {issue.status}
              </span>
            </div>
          </div>

          {/* Allocation Actions / Supervisor Details */}
          {issue.status === 'Pending' ? (
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 flex flex-col gap-4">
              <h3 className="text-sm font-bold text-indigo-600 dark:text-indigo-400 flex items-center gap-2">
                <UserCheck size={18} /> Allocate Field Supervisor
              </h3>

              {issueLng !== null && issueLat !== null && !isNaN(issueLng) && !isNaN(issueLat) && (
                <div className="h-[200px] w-full rounded-xl overflow-hidden border border-border relative">
                  <Map
                    viewport={mapViewport || { center: [issueLng, issueLat], zoom: 12 }}
                    onViewportChange={(vp) => setMapViewport(vp)}
                    theme={theme === 'dark' ? 'dark' : 'light'}
                  >
                    {/* Marker for Issue location */}
                    <MapMarker longitude={issueLng} latitude={issueLat}>
                      <MarkerContent>
                        <div className="w-8 h-8 rounded-full border-2 border-white bg-rose-500 shadow-md flex items-center justify-center text-white animate-pulse">
                          <AlertTriangle size={14} className="text-white fill-current" />
                        </div>
                      </MarkerContent>
                    </MapMarker>

                    {/* Markers for each supervisor */}
                    {supervisors.map(s => {
                      if (s.latitude === null || s.latitude === undefined || s.longitude === null || s.longitude === undefined) return null;
                      const isSelected = s.id.toString() === selectedSupId;
                      return (
                        <MapMarker key={s.id} longitude={s.longitude} latitude={s.latitude}>
                          <MarkerContent>
                            <div 
                              onClick={() => setSelectedSupId(s.id.toString())}
                              className={`cursor-pointer flex flex-col items-center justify-center transition-all duration-200 ${isSelected ? 'scale-110' : 'scale-100 opacity-80'}`}
                            >
                              <div className={`w-7 h-7 rounded-full border-2 bg-indigo-600 shadow-md flex items-center justify-center text-white ${isSelected ? 'border-amber-400 ring-2 ring-indigo-400 bg-indigo-700' : 'border-white bg-indigo-500'}`}>
                                <User size={12} />
                              </div>
                              {isSelected && (
                                <div className="bg-popover text-popover-foreground text-[10px] font-bold px-1.5 py-0.5 rounded shadow mt-1 border border-border whitespace-nowrap">
                                  {s.full_name} ({s.distance_km ? `${s.distance_km.toFixed(1)} km` : ''})
                                </div>
                              )}
                            </div>
                          </MarkerContent>
                        </MapMarker>
                      );
                    })}
                  </Map>
                </div>
              )}
              
              {supervisors.length === 0 ? (
                <p className="text-xs text-muted-foreground">
                  No active supervisors found. Create supervisor profiles under the "Supervisors" tab.
                </p>
              ) : (
                <form onSubmit={handleAssignSupervisor} className="flex flex-col gap-4">
                  <div className="flex flex-col gap-1.5 text-left">
                    <label className="text-xs font-semibold text-muted-foreground">Select Supervisor Staff</label>
                    <Select 
                      value={selectedSupId} 
                      onValueChange={(value) => setSelectedSupId(value)}
                      disabled={assigning}
                    >
                      <SelectTrigger className="w-full !h-11 bg-card border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-indigo-700/10">
                        <SelectValue placeholder="Select Supervisor Staff" />
                      </SelectTrigger>
                      <SelectContent className="bg-card border border-border rounded-xl max-h-[300px] text-foreground">
                        {supervisors.map(s => (
                          <SelectItem key={s.id} value={s.id.toString()} className="text-foreground hover:bg-muted cursor-pointer">
                            {s.full_name} ({s.employee_id}) - Area: {s.assigned_area || 'None'} {s.distance_km !== null && s.distance_km !== undefined ? `[${s.distance_km.toFixed(1)} km away]` : ''}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <Button
                    type="submit"
                    fullWidth
                    theme="darkblue"
                    disabled={assigning}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm hover:shadow"
                  >
                    {assigning ? 'Allocating Staff...' : 'Confirm Allocation'}
                  </Button>
                </form>
              )}
            </div>
          ) : (
            <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.08)] rounded-xl p-6 flex flex-col gap-4 text-left">
              <div className="flex justify-between items-center border-b border-border pb-3">
                <h3 className="text-sm font-bold text-foreground flex items-center gap-2">
                  <UserCheck size={18} className="text-emerald-500 dark:text-emerald-400" /> Allocated Officer
                </h3>
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-400 border border-emerald-200/50 dark:border-emerald-900/30">
                  Supervisor Allocated
                </span>
              </div>

              <div className="flex flex-col gap-2.5">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground font-semibold">Supervisor Name</span>
                  <span className="text-foreground font-bold">
                    {assignedSupervisor ? assignedSupervisor.full_name : 'Supervisor Allocated'}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground font-semibold">Employee ID</span>
                  <span className="text-foreground/90 font-semibold font-sans">
                    {assignedSupervisor ? assignedSupervisor.employee_id : 'N/A'}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground font-semibold">Assigned Locality</span>
                  <span className="text-foreground/90 font-semibold font-sans">
                    {assignedSupervisor?.assigned_area || 'Downtown District'}
                  </span>
                </div>
              </div>
            </div>
          )}

        </div>

      </div>

      {issue.status !== 'Pending' && assignedSupervisor && assignedSupervisor.longitude !== null && assignedSupervisor.longitude !== undefined && assignedSupervisor.latitude !== null && assignedSupervisor.latitude !== undefined && issueLng !== null && issueLat !== null && (
        <div className="w-full mt-4">
          <DeliveryTracker
            supervisorName={assignedSupervisor.full_name}
            employeeId={assignedSupervisor.employee_id}
            assignedArea={assignedSupervisor.assigned_area || 'Downtown District'}
            supervisorCoords={{ lng: parseFloat(assignedSupervisor.longitude as any), lat: parseFloat(assignedSupervisor.latitude as any) }}
            incidentCoords={{ lng: issueLng, lat: issueLat }}
            theme={theme}
            reportId={issue.id}
            category={issue.category}
            reporterName={issue.reporter_name || 'Anonymous Citizen'}
            address={issue.address || 'No address specified'}
            status={issue.status}
            createdAt={issue.created_at}
            description={issue.description}
          />
        </div>
      )}
    </div>
  );
};
