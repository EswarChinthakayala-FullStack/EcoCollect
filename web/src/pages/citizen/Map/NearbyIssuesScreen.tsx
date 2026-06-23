import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useTheme } from 'next-themes';
import { 
  ArrowLeft, RefreshCw, AlertTriangle, MapPin, 
  Trash2, Recycle, Leaf, Package, PlusSquare 
} from 'lucide-react';
import { Map, MapMarker, MarkerContent } from '@/components/ui/map';

interface Issue {
  id: number;
  category: string;
  title?: string;
  address?: string;
  latitude: number;
  longitude: number;
  created_at: string;
  status: string;
  image_url?: string;
}

const DEFAULT_CENTER = { lat: 15.73, lng: 79.88 }; // Sensible default area center (India region)

const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  const R = 3958.8; // Radius of the Earth in miles
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const d = R * c;
  return d.toFixed(1) + ' mi';
};

const formatRelativeTime = (dateStr: string) => {
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHrs = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHrs / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHrs < 24) return `${diffHrs}h ago`;
    return `${diffDays}d ago`;
  } catch (e) {
    return '';
  }
};

export const NearbyIssuesScreen: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall, backendHost } = useAuth();
  const { theme } = useTheme();
  
  const [issues, setIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);
  const [coords, setCoords] = useState<{ latitude: number; longitude: number } | null>(null);

  const fetchIssues = async () => {
    setLoading(true);
    try {
      const data = await apiCall<Issue[]>('/citizen/issues/public');
      setIssues(data);
    } catch (err) {
      console.error('Failed to load nearby issues:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // Attempt to obtain geolocation
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setCoords({
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude
          });
        },
        () => {
          // Fallback to default center if blocked
          setCoords({
            latitude: DEFAULT_CENTER.lat,
            longitude: DEFAULT_CENTER.lng
          });
        }
      );
    } else {
      setCoords({
        latitude: DEFAULT_CENTER.lat,
        longitude: DEFAULT_CENTER.lng
      });
    }
    
    fetchIssues();
  }, [apiCall]);

  const sortedIssues = [...issues]
    .map((issue) => {
      const distVal = coords 
        ? calculateDistance(coords.latitude, coords.longitude, Number(issue.latitude), Number(issue.longitude))
        : '0.0 mi';
      return {
        ...issue,
        distanceStr: distVal,
        timeStr: formatRelativeTime(issue.created_at)
      };
    });

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

  const getCategoryIcon = (category: string) => {
    const size = 20;
    switch (category) {
      case 'Overflowing Bin':
        return <Trash2 size={size} className="text-amber-600 dark:text-amber-500" />;
      case 'Illegal Dumping':
        return <AlertTriangle size={size} className="text-rose-600 dark:text-rose-500" />;
      case 'Recycling Issue':
        return <Recycle size={size} className="text-blue-600 dark:text-blue-500" />;
      case 'Green Waste':
        return <Leaf size={size} className="text-emerald-600 dark:text-emerald-400" />;
      case 'Bulky Items':
        return <Package size={size} className="text-purple-600 dark:text-purple-400" />;
      default:
        return <PlusSquare size={size} className="text-slate-600 dark:text-slate-400" />;
    }
  };

  const getStatusStyle = (status: string) => {
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending': return 'bg-amber-50 dark:bg-amber-950/20 text-amber-600 dark:text-amber-400 border border-amber-200 dark:border-amber-900/30';
      case 'in_progress': return 'bg-blue-50 dark:bg-blue-950/20 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-900/30';
      case 'completed': case 'resolved': return 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-900/30';
      default: return 'bg-slate-50 dark:bg-slate-900/20 text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-700';
    }
  };

  const getMapCenter = (): [number, number] => {
    if (coords && coords.latitude !== DEFAULT_CENTER.lat && coords.longitude !== DEFAULT_CENTER.lng) {
      return [coords.longitude, coords.latitude];
    }
    if (issues.length > 0) {
      const firstWithCoords = issues.find(i => i.latitude && i.longitude);
      if (firstWithCoords) {
        return [Number(firstWithCoords.longitude), Number(firstWithCoords.latitude)];
      }
    }
    return [DEFAULT_CENTER.lng, DEFAULT_CENTER.lat];
  };

  return (
    <div className="w-full text-foreground relative">
      <div className="flex flex-col gap-6 w-full animate-slide-up text-left">
        
        {/* Page Header */}
        <div className="flex items-center gap-4 mb-2">
          <button 
            className="w-10 h-10 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none shrink-0" 
            onClick={() => navigate('/citizen/dashboard')}
            aria-label="Go back"
          >
            <ArrowLeft size={18} />
          </button>
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold tracking-tight text-foreground truncate">Nearby Issues</h1>
            <p className="text-sm text-muted-foreground mt-0.5 truncate hidden sm:block">Explore waste reports submitted near your current location.</p>
          </div>
          <button 
            className="w-10 h-10 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none shrink-0" 
            onClick={fetchIssues}
            aria-label="Refresh issues"
          >
            <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>

        {/* Map Preview Banner */}
        <div 
          className="h-[240px] relative rounded-2xl border border-border overflow-hidden cursor-pointer shadow-sm hover:shadow-md transition-shadow group" 
          onClick={() => navigate('/citizen/map')}
        >
          <Map
            viewport={{ center: getMapCenter(), zoom: 13 }}
            interactive={false}
            theme={theme === 'dark' ? 'dark' : 'light'}
          >
            {issues.map((issue) => (
              <MapMarker key={issue.id} longitude={Number(issue.longitude)} latitude={Number(issue.latitude)}>
                <MarkerContent>
                  <div className="w-4.5 h-4.5 rounded-full border-2 border-white dark:border-slate-900 bg-amber-500 dark:bg-amber-400 shadow-lg animate-pulse" />
                </MarkerContent>
              </MapMarker>
            ))}
          </Map>
          <div className="absolute inset-0 bg-slate-900/5 group-hover:bg-slate-900/10 transition-colors pointer-events-none flex flex-col items-center justify-center z-10">
            <div className="bg-card/95 border border-border/80 text-foreground text-xs font-bold px-3.5 py-2 rounded-xl backdrop-blur-md flex items-center gap-1.5 shadow-lg group-hover:scale-105 transition-transform duration-200">
              <MapPin size={14} className="text-emerald-600 dark:text-emerald-400" />
              <span>Tap to view full map</span>
            </div>
          </div>
        </div>

        {/* List Section */}
        <div>
          <h3 className="text-lg font-bold text-foreground mb-4">Issues near you</h3>

          {loading ? (
            <div className="flex justify-center items-center py-16">
              <div className="w-10 h-10 border-3 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin" />
            </div>
          ) : sortedIssues.length === 0 ? (
            <div className="bg-card border border-border shadow-sm rounded-2xl p-12 flex flex-col items-center justify-center text-center gap-3">
              <AlertTriangle size={32} className="text-muted-foreground" />
              <p className="text-sm font-semibold text-muted-foreground">No nearby issues found.</p>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {sortedIssues.map((issue) => {
                const firstImg = getFirstImageUrl(issue.image_url);
                return (
                  <div 
                    key={issue.id} 
                    className="flex items-start p-4 bg-card rounded-2xl shadow-sm cursor-pointer transition-all hover:-translate-y-0.5 hover:shadow-md border border-border" 
                    onClick={() => navigate(`/citizen/history/${issue.id}`)}
                  >
                    {/* Image or Category Icon */}
                    {firstImg ? (
                      <img 
                        src={firstImg} 
                        alt={issue.category} 
                        className="w-14 h-14 rounded-xl object-cover mr-4 border border-border shrink-0" 
                      />
                    ) : (
                      <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-emerald-500/10 to-teal-500/5 dark:from-emerald-500/20 dark:to-teal-500/10 border border-emerald-500/10 dark:border-emerald-500/20 flex items-center justify-center mr-4 shrink-0">
                        {getCategoryIcon(issue.category)}
                      </div>
                    )}
                    
                    <div className="flex-1 flex flex-col min-w-0">
                      <div className="flex justify-between items-start mb-1 gap-2">
                        <span className="font-bold text-base text-foreground truncate">{issue.title || issue.category}</span>
                        <span className="text-xs text-muted-foreground font-semibold shrink-0 mt-0.5">{issue.distanceStr}</span>
                      </div>
                      <span className="text-xs text-muted-foreground line-clamp-1 mb-2.5 flex items-center gap-1">
                        <MapPin size={11} className="shrink-0 animate-pulse text-emerald-600 dark:text-emerald-400" />
                        <span>{issue.address || 'Address unspecified'}</span>
                      </span>
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-extrabold uppercase tracking-wider ${getStatusStyle(issue.status)}`}>
                          {issue.status}
                        </span>
                        <span className="text-[11px] text-muted-foreground/80 font-medium">{issue.timeStr}</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

      </div>
    </div>
  );
};
