import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import {
  Search, X, AlertTriangle, MapPin, Navigation, ChevronRight, Phone,
  Mail, Shield, Star, Locate, Eye, EyeOff, Info, Layers
} from 'lucide-react';
import { Map, MapMarker, MarkerContent, MarkerPopup, MapControls } from '@/components/ui/map';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';

interface Issue {
  id: number;
  title: string;
  category: string;
  description: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  status: string;
  image_url?: string;
}

interface SupervisorZone {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  profile_image_url?: string;
  assigned_area?: string;
  department?: string;
  latitude?: number;
  longitude?: number;
  coverage_radius?: number;
  performance_score: number;
}

const CATEGORY_FILTERS = [
  { key: 'all', label: 'All', icon: <Layers size={14} /> },
  { key: 'issues', label: 'Issues', icon: <AlertTriangle size={14} /> },
  { key: 'supervisors', label: 'Supervisors', icon: <Shield size={14} /> },
];

export const MapScreen: React.FC = () => {
  const navigate = useNavigate();
  const { role, apiCall, backendHost } = useAuth();
  const [activeFilter, setActiveFilter] = useState('all');
  const [issues, setIssues] = useState<Issue[]>([]);
  const [supervisors, setSupervisors] = useState<SupervisorZone[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [userCoords, setUserCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [selectedSupervisor, setSelectedSupervisor] = useState<SupervisorZone | null>(null);
  const [showSupervisorZones, setShowSupervisorZones] = useState(true);
  const [mapCenter, setMapCenter] = useState<[number, number]>([-74.0060, 40.7128]);
  const [mapZoom, setMapZoom] = useState(14);
  const [isLocating, setIsLocating] = useState(false);
  const [loading, setLoading] = useState(true);
  const searchInputRef = useRef<HTMLInputElement>(null);

  // Get user's current location on mount
  useEffect(() => {
    if (navigator.geolocation) {
      setIsLocating(true);
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const coords = { lat: pos.coords.latitude, lng: pos.coords.longitude };
          setUserCoords(coords);
          setMapCenter([coords.lng, coords.lat]);
          setMapZoom(15);
          setIsLocating(false);
        },
        () => {
          setIsLocating(false);
        },
        { enableHighAccuracy: true, timeout: 10000 }
      );
    }
  }, []);

  // Fetch issues and supervisors
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [issueData, supervisorData] = await Promise.all([
          apiCall<Issue[]>('/citizen/issues/public'),
          apiCall<SupervisorZone[]>('/citizen/supervisors/zones'),
        ]);
        setIssues(issueData);
        setSupervisors(supervisorData);
      } catch (err) {
        console.error('Failed to load map data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [apiCall]);

  const getFirstImageUrl = (imageUrl: string | null | undefined) => {
    if (!imageUrl) return null;
    let parsed: string[] = [];
    if (typeof imageUrl === 'string') {
      const trimmed = imageUrl.trim();
      if (trimmed.startsWith('[')) {
        try { parsed = JSON.parse(trimmed) as string[]; } catch { parsed = trimmed.split(',').map(s => s.trim()).filter(Boolean); }
      } else {
        parsed = trimmed.split(',').map(s => s.trim()).filter(Boolean);
      }
    }
    if (parsed.length === 0) return null;
    const first = parsed[0];
    if (first.startsWith('http') || first.startsWith('data:')) return first;
    return `${backendHost}${first}`;
  };

  const getStatusStyle = (status: string) => {
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending': return { bg: 'bg-amber-500', text: 'text-amber-700 dark:text-amber-300', badge: 'bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border-amber-200 dark:border-amber-800/40' };
      case 'in_progress': return { bg: 'bg-blue-500', text: 'text-blue-700 dark:text-blue-300', badge: 'bg-blue-50 dark:bg-blue-950/30 text-blue-700 dark:text-blue-300 border-blue-200 dark:border-blue-800/40' };
      case 'completed': case 'resolved': return { bg: 'bg-emerald-500', text: 'text-emerald-700 dark:text-emerald-300', badge: 'bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800/40' };
      default: return { bg: 'bg-slate-500', text: 'text-slate-700 dark:text-slate-300', badge: 'bg-slate-50 dark:bg-slate-900/30 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700' };
    }
  };

  const getPerformanceColor = (score: number) => {
    if (score >= 80) return 'text-emerald-500';
    if (score >= 50) return 'text-amber-500';
    return 'text-red-500';
  };

  const handleLocateMe = useCallback(() => {
    if (!navigator.geolocation) return;
    setIsLocating(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const coords = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        setUserCoords(coords);
        setMapCenter([coords.lng, coords.lat]);
        setMapZoom(16);
        setIsLocating(false);
      },
      () => setIsLocating(false),
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }, []);

  // Filter logic
  const filteredIssues = (activeFilter === 'all' || activeFilter === 'issues')
    ? issues.filter(i =>
      !searchQuery ||
      i.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (i.address && i.address.toLowerCase().includes(searchQuery.toLowerCase())) ||
      i.category.toLowerCase().includes(searchQuery.toLowerCase())
    ) : [];

  const filteredSupervisors = (activeFilter === 'all' || activeFilter === 'supervisors')
    ? supervisors.filter(s =>
      !searchQuery ||
      s.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (s.assigned_area && s.assigned_area.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (s.department && s.department.toLowerCase().includes(searchQuery.toLowerCase()))
    ) : [];

  const totalIssuesCount = filteredIssues.length;
  const totalSupervisorsCount = filteredSupervisors.length;

  return (
    <div className="relative w-full h-full bg-background overflow-hidden">
      {/* Full-screen Map */}
      <div className="absolute inset-0">
        <Map
          viewport={{ center: mapCenter, zoom: mapZoom }}
          onViewportChange={(vp) => {
            setMapCenter(vp.center);
            setMapZoom(vp.zoom);
          }}
        >
          {/* User location marker */}
          {userCoords && (
            <MapMarker longitude={userCoords.lng} latitude={userCoords.lat}>
              <MarkerContent>
                <div className="relative flex items-center justify-center">
                  <div className="absolute w-10 h-10 rounded-full bg-blue-500/20 dark:bg-blue-400/20 animate-ping" />
                  <div className="absolute w-7 h-7 rounded-full bg-blue-500/15 dark:bg-blue-400/15" />
                  <div className="w-4 h-4 rounded-full bg-blue-600 dark:bg-blue-400 border-[3px] border-white dark:border-slate-900 shadow-lg shadow-blue-500/30 z-10" />
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Supervisor zone markers */}
          {showSupervisorZones && filteredSupervisors.map((sup) => (
            sup.latitude && sup.longitude && (
              <MapMarker
                key={`sup-${sup.id}`}
                longitude={sup.longitude}
                latitude={sup.latitude}
              >
                <MarkerContent>
                  <button
                    onClick={() => setSelectedSupervisor(selectedSupervisor?.id === sup.id ? null : sup)}
                    className="relative group cursor-pointer bg-transparent border-none p-0"
                  >
                    {/* Coverage radius circle */}
                    <div
                      className="absolute rounded-full border-2 border-emerald-500/30 dark:border-emerald-400/25 bg-emerald-500/8 dark:bg-emerald-400/6 pointer-events-none"
                      style={{
                        width: `${Math.max(40, (sup.coverage_radius || 5) * 6)}px`,
                        height: `${Math.max(40, (sup.coverage_radius || 5) * 6)}px`,
                        top: `50%`,
                        left: `50%`,
                        transform: 'translate(-50%, -50%)',
                      }}
                    />
                    {/* Supervisor pin */}
                    <div className={`relative z-10 flex items-center gap-1 px-2 py-1.5 rounded-full shadow-lg border transition-all duration-200
                      ${selectedSupervisor?.id === sup.id
                        ? 'bg-emerald-600 dark:bg-emerald-500 border-emerald-500 dark:border-emerald-400 text-white scale-110'
                        : 'bg-card/95 dark:bg-card/90 border-emerald-500/40 dark:border-emerald-400/30 text-emerald-700 dark:text-emerald-400 group-hover:border-emerald-500 dark:group-hover:border-emerald-400 group-hover:scale-105'
                      }`}
                    >
                      <Shield size={13} className={selectedSupervisor?.id === sup.id ? 'text-white' : ''} />
                      <span className={`text-[10px] font-bold max-w-[70px] truncate ${selectedSupervisor?.id === sup.id ? 'text-white' : ''}`}>
                        {sup.name.split(' ')[0]}
                      </span>
                    </div>
                  </button>
                </MarkerContent>
              </MapMarker>
            )
          ))}

          {/* Issue markers */}
          {filteredIssues.map((issue) => {
            const statusStyle = getStatusStyle(issue.status);
            return (
              <MapMarker
                key={`issue-${issue.id}`}
                longitude={issue.longitude || -74.0060}
                latitude={issue.latitude || 40.7128}
              >
                <MarkerContent>
                  <div className={`p-1.5 rounded-full text-white ${statusStyle.bg} shadow-lg border-2 border-white dark:border-slate-800 transition-transform hover:scale-110 cursor-pointer`}>
                    <AlertTriangle size={13} />
                  </div>
                </MarkerContent>
                <MarkerPopup>
                  <div className="flex flex-col text-left max-w-[220px] p-1">
                    <div className="flex items-start gap-2 mb-1.5">
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-bold text-foreground leading-tight line-clamp-1">{issue.title}</p>
                        <p className="text-[10px] text-muted-foreground leading-normal mt-0.5 flex items-center gap-1">
                          <MapPin size={9} className="shrink-0" />
                          <span className="truncate">{issue.address || 'Location not specified'}</span>
                        </p>
                      </div>
                    </div>

                    {issue.description && (
                      <p className="text-[10px] text-muted-foreground/80 leading-relaxed line-clamp-2 mb-2">{issue.description}</p>
                    )}

                    <div className="flex justify-between items-center pt-2 border-t border-border gap-2">
                      <Badge className={`${statusStyle.badge} text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 shadow-none border`}>
                        {issue.status}
                      </Badge>
                      <button
                        onClick={() => {
                          if (role === 'admin') navigate(`/admin/report/${issue.id}`);
                          else if (role === 'supervisor') navigate(`/supervisor/report/${issue.id}`);
                          else navigate(`/citizen/history/${issue.id}`);
                        }}
                        className="flex items-center gap-0.5 text-[10px] font-bold text-emerald-600 dark:text-emerald-400 hover:underline border-none bg-transparent cursor-pointer"
                      >
                        View Details <ChevronRight size={10} />
                      </button>
                      {getFirstImageUrl(issue.image_url) && (
                        <img src={getFirstImageUrl(issue.image_url) || ''} alt="Issue" className="w-8 h-8 rounded object-cover border border-border shrink-0" />
                      )}
                    </div>
                  </div>
                </MarkerPopup>
              </MapMarker>
            );
          })}

          <MapControls showZoom={true} showLocate={true} onLocate={(coords) => {
            setUserCoords({ lat: coords.latitude, lng: coords.longitude });
            setMapCenter([coords.longitude, coords.latitude]);
            setMapZoom(16);
          }} />
        </Map>
      </div>

      {/* Google Maps-style search bar overlay */}
      <div className="absolute top-4 left-4 right-4 z-10 flex flex-col gap-3 pointer-events-none">
        <div className="flex items-center gap-2 pointer-events-auto">
          <div className="flex-1 bg-card/95 dark:bg-card/90 backdrop-blur-xl rounded-2xl flex items-center px-4 h-12 shadow-lg border border-border/60 dark:border-border/40 transition-all focus-within:shadow-xl focus-within:border-border">
            <Search size={18} className="text-muted-foreground/70 shrink-0" />
            <input
              ref={searchInputRef}
              type="text"
              className="border-none outline-none flex-1 ml-3 text-sm bg-transparent text-foreground placeholder:text-muted-foreground/50 font-medium"
              placeholder="Search issues, supervisors, areas..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            {searchQuery && (
              <button
                className="p-1 rounded-full hover:bg-muted transition-colors border-none bg-transparent cursor-pointer text-muted-foreground"
                onClick={() => setSearchQuery('')}
              >
                <X size={16} />
              </button>
            )}
          </div>
        </div>

        {/* Category filter chips */}
        <div className="flex gap-2 overflow-x-auto scrollbar-none pointer-events-auto pl-0.5">
          {CATEGORY_FILTERS.map(f => (
            <button
              key={f.key}
              className={`flex items-center gap-1.5 px-3.5 py-2 rounded-full text-xs font-semibold border cursor-pointer whitespace-nowrap shadow-md transition-all duration-200 ${
                activeFilter === f.key
                  ? 'bg-emerald-600 dark:bg-emerald-500 border-emerald-600 dark:border-emerald-500 text-white shadow-emerald-600/20'
                  : 'bg-card/95 dark:bg-card/90 backdrop-blur-xl border-border/60 dark:border-border/40 text-foreground hover:bg-muted dark:hover:bg-muted/50'
              }`}
              onClick={() => setActiveFilter(f.key)}
            >
              {f.icon}
              <span>{f.label}</span>
              {f.key === 'issues' && totalIssuesCount > 0 && (
                <span className={`ml-0.5 text-[10px] px-1.5 py-0.5 rounded-full font-bold ${
                  activeFilter === f.key ? 'bg-white/20 text-white' : 'bg-muted dark:bg-muted/60 text-muted-foreground'
                }`}>{totalIssuesCount}</span>
              )}
              {f.key === 'supervisors' && totalSupervisorsCount > 0 && (
                <span className={`ml-0.5 text-[10px] px-1.5 py-0.5 rounded-full font-bold ${
                  activeFilter === f.key ? 'bg-white/20 text-white' : 'bg-muted dark:bg-muted/60 text-muted-foreground'
                }`}>{totalSupervisorsCount}</span>
              )}
            </button>
          ))}

          {/* Toggle supervisor zones visibility */}
          <button
            className={`flex items-center gap-1.5 px-3 py-2 rounded-full text-xs font-semibold border cursor-pointer whitespace-nowrap shadow-md transition-all duration-200 ${
              showSupervisorZones
                ? 'bg-card/95 dark:bg-card/90 backdrop-blur-xl border-emerald-500/40 dark:border-emerald-400/30 text-emerald-700 dark:text-emerald-400'
                : 'bg-card/95 dark:bg-card/90 backdrop-blur-xl border-border/60 dark:border-border/40 text-muted-foreground'
            }`}
            onClick={() => setShowSupervisorZones(!showSupervisorZones)}
          >
            {showSupervisorZones ? <Eye size={14} /> : <EyeOff size={14} />}
            <span>Zones</span>
          </button>
        </div>
      </div>

      {/* Locate me FAB (mobile-friendly) */}
      <button
        onClick={handleLocateMe}
        disabled={isLocating}
        className={`absolute bottom-6 right-4 z-10 w-12 h-12 rounded-full shadow-lg border flex items-center justify-center transition-all duration-200 cursor-pointer
          ${isLocating
            ? 'bg-blue-500 dark:bg-blue-600 border-blue-400 text-white animate-pulse'
            : 'bg-card/95 dark:bg-card/90 backdrop-blur-xl border-border/60 dark:border-border/40 text-foreground hover:bg-muted dark:hover:bg-muted/50 hover:shadow-xl'
          }`}
        title="Center on my location"
      >
        {isLocating ? (
          <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
        ) : (
          <Navigation size={20} className="text-blue-600 dark:text-blue-400" />
        )}
      </button>

      {/* Stats footer bar */}
      <div className="absolute bottom-6 left-4 z-10 pointer-events-auto">
        <div
          className={`flex items-center gap-3 px-4 py-3 bg-card/95 dark:bg-card/90 backdrop-blur-xl rounded-2xl shadow-lg border border-border/60 dark:border-border/40 transition-all ${
            role === 'citizen' ? 'cursor-pointer hover:shadow-xl' : 'cursor-default'
          }`}
          onClick={() => role === 'citizen' && navigate('/citizen/map/nearby')}
        >
          <div className="flex items-center gap-3">
            <div className="flex flex-col text-left">
              <span className="text-xs font-bold text-foreground">
                {totalIssuesCount} issue{totalIssuesCount !== 1 ? 's' : ''} nearby
              </span>
              <span className="text-[10px] text-muted-foreground">
                {totalSupervisorsCount} supervisor{totalSupervisorsCount !== 1 ? 's' : ''} active
              </span>
            </div>
          </div>
          {role === 'citizen' && <ChevronRight size={16} className="text-muted-foreground" />}
        </div>
      </div>

      {/* Selected Supervisor detail panel */}
      {selectedSupervisor && (
        <div className="absolute bottom-20 left-4 right-4 md:left-auto md:right-4 md:w-[360px] z-20 animate-in slide-in-from-bottom-4 duration-300">
          <div className="bg-card/98 dark:bg-card/95 backdrop-blur-2xl rounded-2xl shadow-2xl border border-border/60 dark:border-border/40 overflow-hidden">
            {/* Header with gradient */}
            <div className="relative bg-gradient-to-r from-emerald-600 to-teal-600 dark:from-emerald-700 dark:to-teal-700 px-5 pt-5 pb-12">
              <button
                onClick={() => setSelectedSupervisor(null)}
                className="absolute top-3 right-3 w-7 h-7 rounded-full bg-white/15 hover:bg-white/25 flex items-center justify-center text-white transition-colors border-none cursor-pointer backdrop-blur-sm"
              >
                <X size={14} />
              </button>
              <div className="flex items-center gap-1.5 text-emerald-100/70 text-[10px] font-bold uppercase tracking-widest mb-1">
                <Shield size={11} />
                <span>Municipal Supervisor</span>
              </div>
              <h3 className="text-white font-bold text-base leading-tight">{selectedSupervisor.name}</h3>
            </div>

            {/* Avatar overlap */}
            <div className="relative px-5 -mt-8">
              <Avatar className="h-14 w-14 border-4 border-card dark:border-card shadow-lg">
                <AvatarImage src={selectedSupervisor.profile_image_url ? `${backendHost}${selectedSupervisor.profile_image_url}` : undefined} alt={selectedSupervisor.name} />
                <AvatarFallback className="bg-emerald-100 dark:bg-emerald-900/50 text-emerald-700 dark:text-emerald-300 font-bold text-lg">
                  {selectedSupervisor.name.charAt(0).toUpperCase()}
                </AvatarFallback>
              </Avatar>
            </div>

            {/* Content */}
            <div className="px-5 pt-3 pb-5">
              <div className="grid grid-cols-2 gap-3 mb-4">
                {selectedSupervisor.assigned_area && (
                  <div className="flex items-start gap-2 col-span-2">
                    <MapPin size={14} className="text-emerald-500 dark:text-emerald-400 mt-0.5 shrink-0" />
                    <div>
                      <p className="text-[10px] text-muted-foreground/70 font-semibold uppercase tracking-wider">Assigned Area</p>
                      <p className="text-xs text-foreground font-medium">{selectedSupervisor.assigned_area}</p>
                    </div>
                  </div>
                )}
                {selectedSupervisor.department && (
                  <div className="flex items-start gap-2">
                    <Info size={14} className="text-blue-500 dark:text-blue-400 mt-0.5 shrink-0" />
                    <div>
                      <p className="text-[10px] text-muted-foreground/70 font-semibold uppercase tracking-wider">Department</p>
                      <p className="text-xs text-foreground font-medium">{selectedSupervisor.department}</p>
                    </div>
                  </div>
                )}
                <div className="flex items-start gap-2">
                  <Star size={14} className={`${getPerformanceColor(selectedSupervisor.performance_score)} mt-0.5 shrink-0`} />
                  <div>
                    <p className="text-[10px] text-muted-foreground/70 font-semibold uppercase tracking-wider">Performance</p>
                    <p className={`text-xs font-bold ${getPerformanceColor(selectedSupervisor.performance_score)}`}>
                      {selectedSupervisor.performance_score}/100
                    </p>
                  </div>
                </div>
                {selectedSupervisor.coverage_radius && (
                  <div className="flex items-start gap-2">
                    <Locate size={14} className="text-purple-500 dark:text-purple-400 mt-0.5 shrink-0" />
                    <div>
                      <p className="text-[10px] text-muted-foreground/70 font-semibold uppercase tracking-wider">Coverage</p>
                      <p className="text-xs text-foreground font-medium">{selectedSupervisor.coverage_radius} km radius</p>
                    </div>
                  </div>
                )}
              </div>

              {/* Contact actions */}
              <div className="flex gap-2 pt-3 border-t border-border/60 dark:border-border/40">
                {selectedSupervisor.phone && (
                  <a
                    href={`tel:${selectedSupervisor.phone}`}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800/40 text-emerald-700 dark:text-emerald-400 text-xs font-semibold hover:bg-emerald-100 dark:hover:bg-emerald-950/50 transition-colors no-underline"
                  >
                    <Phone size={13} />
                    <span>Call</span>
                  </a>
                )}
                {selectedSupervisor.email && (
                  <a
                    href={`mailto:${selectedSupervisor.email}`}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-800/40 text-blue-700 dark:text-blue-400 text-xs font-semibold hover:bg-blue-100 dark:hover:bg-blue-950/50 transition-colors no-underline"
                  >
                    <Mail size={13} />
                    <span>Email</span>
                  </a>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Loading overlay */}
      {loading && (
        <div className="absolute inset-0 z-30 bg-background/40 dark:bg-background/50 backdrop-blur-sm flex items-center justify-center pointer-events-none">
          <div className="flex flex-col items-center gap-3 p-6 bg-card/95 dark:bg-card/90 backdrop-blur-xl rounded-2xl shadow-2xl border border-border/60">
            <div className="w-10 h-10 border-[3px] border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin" />
            <p className="text-xs text-muted-foreground font-medium">Loading map data...</p>
          </div>
        </div>
      )}
    </div>
  );
};
