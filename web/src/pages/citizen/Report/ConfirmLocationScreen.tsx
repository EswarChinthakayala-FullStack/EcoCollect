import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../../components/Button';
import { Input } from '../../../components/Input';
import { MapPin, Search, ArrowLeft, AlertCircle, Loader2 } from 'lucide-react';
import { Map, MapMarker, MarkerContent, MapControls } from '@/components/ui/map';

export const ConfirmLocationScreen: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { apiCall } = useAuth();

  // Extract wizard state from router
  const { image_url, category } = (location.state as { image_url?: string | null; category: string }) || {
    image_url: null,
    category: 'Other'
  };

  const [address, setAddress] = useState('100 Park Lane, Downtown District, Cityville');
  const [locationName, setLocationName] = useState('Downtown District');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const [loadingAddress, setLoadingAddress] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Stateful coordinates (Default: Downtown District)
  const [coordinates, setCoordinates] = useState({
    latitude: 40.7128,
    longitude: -74.0060
  });

  // Stateful controlled viewport
  const [viewport, setViewport] = useState({
    center: [-74.0060, 40.7128] as [number, number],
    zoom: 15,
    bearing: 0,
    pitch: 0
  });

  const fetchAddress = async (lat: number, lon: number) => {
    try {
      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
      const data = await response.json();
      if (data) {
        if (data.display_name) {
          setAddress(data.display_name);
        } else {
          setAddress(`${lat.toFixed(6)}, ${lon.toFixed(6)}`);
        }
        const addr = data.address || {};
        const area = addr.suburb || addr.neighbourhood || addr.city_district || addr.quarter || addr.municipality || addr.city || addr.town || addr.village || 'Unknown Area';
        setLocationName(area);
      } else {
        setAddress(`${lat.toFixed(6)}, ${lon.toFixed(6)}`);
        setLocationName('Unknown Area');
      }
    } catch (e) {
      setAddress(`${lat.toFixed(6)}, ${lon.toFixed(6)}`);
      setLocationName('Unknown Area');
    }
  };

  const getLiveLocation = () => {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by your browser.");
      return;
    }

    setLocating(true);
    setError(null);

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        setCoordinates({ latitude: lat, longitude: lon });
        setViewport({
          center: [lon, lat],
          zoom: 16,
          bearing: 0,
          pitch: 0
        });
        await fetchAddress(lat, lon);
        setLocating(false);
      },
      (err) => {
        setError("Unable to retrieve your location. Using default location instead.");
        setLocating(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  };

  const handleSearchAddress = async () => {
    if (!address.trim()) return;
    setLoadingAddress(true);
    setError(null);
    try {
      const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=1`);
      const data = await response.json();
      if (data && data.length > 0) {
        const lat = parseFloat(data[0].lat);
        const lon = parseFloat(data[0].lon);
        setCoordinates({ latitude: lat, longitude: lon });
        setViewport({
          center: [lon, lat],
          zoom: 16,
          bearing: 0,
          pitch: 0
        });
        await fetchAddress(lat, lon);
      } else {
        setError("Address not found. Please drag the pin manually or try a different search.");
      }
    } catch (err) {
      setError("Failed to look up address. Please drag the pin on the map instead.");
    } finally {
      setLoadingAddress(false);
    }
  };

  // Fetch user's live location on component mount
  useEffect(() => {
    getLiveLocation();
  }, []);

  const handleDragEnd = async (lngLat: { lng: number; lat: number }) => {
    setCoordinates({ latitude: lngLat.lat, longitude: lngLat.lng });
    setViewport(prev => ({
      ...prev,
      center: [lngLat.lng, lngLat.lat]
    }));
    await fetchAddress(lngLat.lat, lngLat.lng);
  };

  const handleConfirm = async () => {
    setLoading(true);
    setError(null);

    try {
      const payload = {
        title: `${category} at ${address.split(',')[0]}`,
        category,
        description: description || `Reported ${category} issue.`,
        latitude: coordinates.latitude,
        longitude: coordinates.longitude,
        address,
        location: locationName,
        image_url
      };

      const result = await apiCall('/citizen/issues', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      // Pass the success report details to the success screen
      navigate('/citizen/report/success', { state: { report: result } });
    } catch (err: any) {
      setError(err.message || 'Failed to submit report. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up text-foreground">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-background border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-emerald-600 hover:border-emerald-600 dark:hover:text-emerald-400 hover:bg-emerald-500/5" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">3 of 3: Confirm Details</h2>
      </div>

      <div className="bg-card text-card-foreground border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col">
        <div className="mb-6 text-left">
          <h2 className="text-2xl lg:text-3xl font-extrabold text-foreground mb-2">Where is the issue?</h2>
          <p className="text-sm text-muted-foreground leading-relaxed">
            Provide the location and some descriptive details so our supervisors can locate it accurately.
          </p>
        </div>

        {error && (
          <div className="flex items-center gap-3 p-4 mb-6 text-sm text-red-600 dark:text-red-400 rounded-lg bg-red-50 dark:bg-red-950/20 border border-red-100 dark:border-red-900/30 text-left">
            <AlertCircle size={18} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="flex flex-wrap gap-2 mb-5">
          <span className="text-xs bg-muted text-muted-foreground px-3 py-1.5 rounded-lg border border-border">Category: <strong className="text-foreground font-bold">{category}</strong></span>
          {image_url && <span className="text-xs bg-muted text-muted-foreground px-3 py-1.5 rounded-lg border border-border font-medium">Photo attached</span>}
        </div>

        <div className="mb-4 text-left flex flex-col gap-1.5">
          <div className="flex justify-between items-center mb-1">
            <label className="text-sm font-semibold text-foreground/90 select-none">Location Address</label>
            <button
              type="button"
              onClick={getLiveLocation}
              disabled={locating}
              className="text-xs font-semibold text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300 cursor-pointer flex items-center gap-1 hover:underline bg-transparent border-none py-0.5 animate-fade-in"
            >
              <MapPin size={14} className={locating ? "animate-bounce" : ""} />
              {locating ? "Locating..." : "Use Current Location"}
            </button>
          </div>
          <Input 
            placeholder="Type address and press Enter to locate..." 
            icon={<Search size={20} className="text-muted-foreground" />}
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                handleSearchAddress();
              }
            }}
            rightIcon={
              <button 
                type="button"
                className="p-1 hover:bg-muted rounded-md cursor-pointer transition-colors text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 flex items-center justify-center border-none"
                onClick={handleSearchAddress}
                title="Search address on map"
                disabled={loadingAddress}
              >
                {loadingAddress ? <Loader2 size={18} className="animate-spin text-emerald-600" /> : <Search size={18} />}
              </button>
            }
          />
        </div>

        <div className="mb-4 text-left flex flex-col gap-1.5 animate-fade-in">
          <label className="text-sm font-semibold text-foreground/90 select-none">Detected Locality / Area</label>
          <div className="flex items-center gap-2.5 p-3.5 bg-muted/30 border border-border rounded-xl">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0 border border-emerald-500/15">
              <MapPin size={16} className="fill-current text-emerald-600 dark:text-emerald-400" />
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground">Assigned Municipal Zone</span>
              <span className="text-sm font-bold text-foreground">{locationName}</span>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-2 mb-6 text-left">
          <label className="text-sm font-semibold text-foreground/90 select-none">Description / Remarks (Optional)</label>
          <textarea
            className="font-sans w-full p-3 bg-card border border-border rounded-xl text-sm text-foreground outline-none transition-all focus:border-emerald-600 dark:focus:border-emerald-400 focus:ring-2 focus:ring-emerald-600/10 placeholder:text-muted-foreground"
            rows={3}
            placeholder="Describe the issue (e.g. Blocked walkway, smelly odor, hazardous items...)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            style={{ resize: 'vertical' }}
          />
        </div>

        <div className="h-[360px] rounded-xl relative border border-border overflow-hidden mb-6 shadow-sm">
          <Map
            viewport={viewport}
            onViewportChange={setViewport}
          >
            <MapMarker 
              longitude={coordinates.longitude} 
              latitude={coordinates.latitude}
              draggable={true}
              onDragEnd={handleDragEnd}
            >
              <MarkerContent>
                <div className="relative flex items-center justify-center cursor-grab active:cursor-grabbing">
                  {/* Pulsing ring animation under pin */}
                  <div className="absolute w-8 h-8 rounded-full bg-emerald-500/30 dark:bg-emerald-400/30 animate-ping pointer-events-none" />
                  <div className="relative p-2.5 bg-gradient-to-br from-emerald-500 to-teal-600 dark:from-emerald-400 dark:to-teal-500 text-white rounded-full shadow-[0_8px_16px_rgba(16,185,129,0.3)] border-2 border-white dark:border-slate-900 flex items-center justify-center transition-transform hover:scale-105">
                    <MapPin size={18} className="fill-current text-white" />
                  </div>
                </div>
              </MarkerContent>
            </MapMarker>
            <MapControls showZoom={true} />
          </Map>
          <span className="absolute bottom-3 left-3 bg-slate-950/85 dark:bg-card/90 border border-border/10 text-white dark:text-foreground text-[9px] font-bold uppercase tracking-wider px-2.5 py-1.5 rounded-md backdrop-blur-sm z-10 select-none shadow-md">
            EcoCollect GPS Pin Secured
          </span>
        </div>

        <div className="flex flex-col gap-3 mt-6">
          <Button 
            fullWidth 
            theme="green" 
            onClick={handleConfirm}
            disabled={loading || !address}
          >
            {loading ? 'Submitting Report...' : 'Submit Report'}
          </Button>
        </div>
      </div>
    </div>
  );
};
