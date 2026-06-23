import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useTheme } from 'next-themes';
import { ArrowLeft } from 'lucide-react';
import { DeliveryTracker } from '@/components/ui/delivery-tracker';

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
  reporter_name?: string;
  latitude?: number | null;
  longitude?: number | null;
}

export const SupervisorMap: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { supervisor, apiCall } = useAuth();
  const { theme } = useTheme();

  const [issue, setIssue] = useState<Issue | null>(null);
  const [loading, setLoading] = useState(true);
  const [coords, setCoords] = useState<{ latitude: number; longitude: number } | null>(null);

  useEffect(() => {
    const fetchIssue = async () => {
      try {
        const response = await apiCall<{ issue: Issue }>(`/issues/${id}`);
        setIssue(response.issue);
      } catch (err) {
        console.error('Failed to load issue for map routing:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchIssue();
  }, [id, apiCall]);

  useEffect(() => {
    if (!navigator.geolocation) return;

    // Sync current location immediately
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
          console.error('Failed to sync location:', err);
        }
      },
      (error) => console.warn('Location tracking error:', error.message),
      { enableHighAccuracy: true }
    );

    // Poll location every 8 seconds
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
            console.error('Failed to sync location:', err);
          }
        },
        (error) => console.warn('Location tracking error:', error.message),
        { enableHighAccuracy: true }
      );
    }, 8000);

    return () => clearInterval(interval);
  }, [apiCall]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px] w-full">
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 border-t-blue-600 dark:border-t-blue-500 animate-spin" />
      </div>
    );
  }

  if (!issue || issue.latitude === null || issue.latitude === undefined || issue.longitude === null || issue.longitude === undefined) {
    return (
      <div className="bg-card border border-border shadow-sm rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4 text-foreground w-full">
        <h3 className="text-lg font-bold">Location Coordinates Not Available</h3>
        <button 
          onClick={() => navigate('/supervisor/dashboard')}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-bold text-sm transition-colors cursor-pointer"
        >
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up pb-12 text-foreground text-left">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 hover:border-blue-600 dark:hover:border-blue-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider font-sans">Live Navigation Route</h2>
      </div>

      <div className="w-full">
        <DeliveryTracker
          supervisorName={supervisor?.name || 'Supervisor'}
          employeeId={supervisor?.employee_id || 'N/A'}
          assignedArea={supervisor?.assigned_area || 'Assigned Area'}
          supervisorCoords={{ 
            lng: coords?.longitude ?? parseFloat(issue.longitude as any), 
            lat: coords?.latitude ?? parseFloat(issue.latitude as any) 
          }}
          incidentCoords={{ 
            lng: parseFloat(issue.longitude as any), 
            lat: parseFloat(issue.latitude as any) 
          }}
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
    </div>
  );
};
