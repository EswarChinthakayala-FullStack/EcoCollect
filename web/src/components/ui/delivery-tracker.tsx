"use client";

import { useEffect, useState, useMemo, useRef } from "react";
import { Truck, MapPin, AlertTriangle, ClipboardList, User } from "lucide-react";
import { Map, MapMarker, MapRoute, MarkerContent, MarkerTooltip, MapControls } from "@/components/ui/map";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface OsrmRouteData {
  coordinates: [number, number][];
  duration: number;
  distance: number;
}

interface DeliveryTrackerProps {
  supervisorName: string;
  employeeId: string;
  assignedArea: string;
  supervisorCoords: { lng: number; lat: number };
  incidentCoords: { lng: number; lat: number };
  theme?: string;
  reportId?: number;
  category?: string;
  reporterName?: string;
  address?: string;
  status?: string;
  createdAt?: string;
  description?: string;
}

function formatDistance(meters?: number) {
  if (!meters) return "--";
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(2)} km`;
}

function formatDuration(seconds?: number) {
  if (!seconds) return "--";
  const minutes = Math.round(seconds / 60);
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  return `${hours}h ${remainingMinutes}m`;
}

function estimateDistanceAndDuration(
  start: { lng: number; lat: number },
  end: { lng: number; lat: number }
) {
  const R = 6371000; // Earth radius in meters
  const dLat = ((end.lat - start.lat) * Math.PI) / 180;
  const dLng = ((end.lng - start.lng) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((start.lat * Math.PI) / 180) *
      Math.cos((end.lat * Math.PI) / 180) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c; // meters

  // Assume average speed of 30 km/h (8.33 m/s) in traffic
  const duration = distance / 8.33; // seconds

  return { distance, duration };
}

export function DeliveryTracker({
  supervisorName,
  employeeId,
  assignedArea,
  supervisorCoords,
  incidentCoords,
  theme,
  reportId,
  category,
  reporterName,
  address,
  status,
  createdAt,
  description,
}: DeliveryTrackerProps) {
  const [routeData, setRouteData] = useState<OsrmRouteData | null>(null);
  const [loading, setLoading] = useState(true);
  const lastFetchedCoords = useRef<{ lng: number; lat: number } | null>(null);

  useEffect(() => {
    async function fetchRoute() {
      if (
        !supervisorCoords.lng ||
        !supervisorCoords.lat ||
        !incidentCoords.lng ||
        !incidentCoords.lat
      ) {
        setLoading(false);
        return;
      }

      // Check if location change is significant enough (> 10-15 meters) to justify OSRM query.
      // 0.0001 degrees latitude/longitude is approximately 11 meters at the equator.
      const lastCoords = lastFetchedCoords.current;
      const hasMovedSignificantly =
        !lastCoords ||
        Math.abs(supervisorCoords.lng - lastCoords.lng) > 0.0001 ||
        Math.abs(supervisorCoords.lat - lastCoords.lat) > 0.0001;

      if (!hasMovedSignificantly && routeData) {
        return;
      }

      // Only show full loading spinner on the first fetch
      if (!routeData) {
        setLoading(true);
      }

      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 3500); // 3.5s timeout for public routing engine

      try {
        const response = await fetch(
          `https://router.project-osrm.org/route/v1/driving/${supervisorCoords.lng},${supervisorCoords.lat};${incidentCoords.lng},${incidentCoords.lat}?overview=full&geometries=geojson`,
          { signal: controller.signal }
        );
        clearTimeout(timeoutId);

        if (!response.ok) {
          throw new Error(`OSRM returned status ${response.status}`);
        }
        const data = await response.json();
        const route = data?.routes?.[0];
        if (!route?.geometry?.coordinates) {
          throw new Error("No route geometry found in OSRM response");
        }

        setRouteData({
          coordinates: route.geometry.coordinates as [number, number][],
          duration: route.duration as number,
          distance: route.distance as number,
        });
        lastFetchedCoords.current = { lng: supervisorCoords.lng, lat: supervisorCoords.lat };
      } catch (error) {
        console.error("Failed to fetch route, using straight-line fallback:", error);
        
        // Fallback: direct straight line between supervisor and incident
        const estimation = estimateDistanceAndDuration(supervisorCoords, incidentCoords);
        setRouteData({
          coordinates: [
            [supervisorCoords.lng, supervisorCoords.lat],
            [incidentCoords.lng, incidentCoords.lat]
          ],
          duration: estimation.duration,
          distance: estimation.distance,
        });

        // Set last fetched coordinates to avoid endless loops on fallback
        lastFetchedCoords.current = { lng: supervisorCoords.lng, lat: supervisorCoords.lat };
      } finally {
        setLoading(false);
      }
    }

    fetchRoute();
  }, [supervisorCoords.lng, supervisorCoords.lat, incidentCoords.lng, incidentCoords.lat]);

  // Simulate progress (e.g. 64% of the way along the route)
  const progressCoordinates = useMemo(() => {
    if (!routeData?.coordinates) return [];
    const count = Math.max(
      2,
      Math.floor(routeData.coordinates.length * 0.64)
    );
    return routeData.coordinates.slice(0, count);
  }, [routeData]);

  const courierPosition = useMemo(() => {
    if (progressCoordinates.length === 0) return null;
    return progressCoordinates[progressCoordinates.length - 1];
  }, [progressCoordinates]);

  const mapCenter: [number, number] = useMemo(() => {
    if (courierPosition) {
      return [
        (courierPosition[0] + incidentCoords.lng) / 2,
        (courierPosition[1] + incidentCoords.lat) / 2,
      ];
    }

    // Fallback logic when route fails, doesn't load yet, or start and end are across ocean (New York vs India).
    // If the distance is too large (> 10 degrees, approx 1100km), center on the incident location directly.
    const latDiff = Math.abs(supervisorCoords.lat - incidentCoords.lat);
    const lngDiff = Math.abs(supervisorCoords.lng - incidentCoords.lng);
    if (latDiff > 10 || lngDiff > 10) {
      return [incidentCoords.lng, incidentCoords.lat];
    }

    return [
      (supervisorCoords.lng + incidentCoords.lng) / 2,
      (supervisorCoords.lat + incidentCoords.lat) / 2,
    ];
  }, [supervisorCoords.lng, supervisorCoords.lat, incidentCoords.lng, incidentCoords.lat, courierPosition]);

  return (
    <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.06)] rounded-2xl overflow-hidden grid grid-cols-1 md:grid-cols-[1.15fr_1fr] md:h-[530px] text-left">
      {/* Left Pane - Details & Actions */}
      <div className="flex flex-col p-5 md:p-6 justify-between h-full bg-card gap-5 overflow-y-auto">
        <div className="space-y-1 shrink-0">
          <div className="flex items-center justify-between gap-2">
            <h3 className="text-lg font-bold tracking-tight text-foreground flex items-center gap-2">
              <Truck className="text-indigo-600 dark:text-indigo-400 size-5" /> Live Track Dispatch
            </h3>
            <Badge variant="secondary" className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 animate-pulse flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              Active Route
            </Badge>
          </div>
          <p className="text-muted-foreground text-xs font-semibold">
            Reported: {createdAt ? new Date(createdAt).toLocaleString() : new Date().toLocaleString()}
          </p>
        </div>

        <Card className="border border-border/60 bg-muted/30 dark:bg-muted/10 shrink-0">
          <CardHeader className="py-2.5 px-3 border-b border-border/50">
            <CardTitle className="font-bold text-[10px] uppercase tracking-wider text-muted-foreground">
              Incident Details (#ID-{reportId || "N/A"})
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3.5 py-3.5 px-3">
            <div className="flex items-start gap-2">
              <div className="bg-muted grid size-7 place-items-center rounded-full text-xs font-medium shrink-0">
                <ClipboardList className="text-indigo-500 dark:text-indigo-400 size-3.5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wide">Category</p>
                <p className="truncate text-xs font-semibold text-foreground">
                  {category || "Uncategorized"}
                </p>
              </div>
              <Badge variant="outline" className="h-5 rounded-full px-2 text-[9px] font-bold capitalize bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border border-indigo-500/20">
                {status || "Assigned"}
              </Badge>
            </div>

            <div className="flex items-start gap-2">
              <div className="bg-muted grid size-7 place-items-center rounded-full text-xs font-medium shrink-0">
                <MapPin className="text-indigo-500 dark:text-indigo-400 size-3.5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wide">Incident Address</p>
                <p className="text-xs font-semibold text-foreground/90 line-clamp-2 leading-relaxed">
                  {address || "No address specified"}
                </p>
              </div>
            </div>

            <div className="flex items-start gap-2">
              <div className="bg-muted grid size-7 place-items-center rounded-full text-xs font-medium shrink-0">
                <User className="text-indigo-500 dark:text-indigo-400 size-3.5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wide">Citizen Reporter</p>
                <p className="truncate text-xs font-semibold text-foreground/90">
                  {reporterName || "Anonymous Citizen"}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Dispatch Metrics */}
        <div className="grid gap-3 sm:grid-cols-2 shrink-0">
          <Card className="border border-border/60 bg-muted/30 dark:bg-muted/10">
            <CardContent className="space-y-1.5 p-3">
              <p className="text-muted-foreground text-[9px] uppercase font-bold tracking-wider">
                Assigned Supervisor
              </p>
              <div className="space-y-0.5">
                <p className="text-xs font-bold text-foreground truncate">
                  {supervisorName}
                </p>
                <p className="text-[9px] text-muted-foreground font-sans">
                  ID: {employeeId} | {assignedArea}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card className="border border-border/60 bg-muted/30 dark:bg-muted/10">
            <CardContent className="space-y-1.5 p-3">
              <p className="text-muted-foreground text-[9px] uppercase font-bold tracking-wider">
                Remaining Travel
              </p>
              <div className="space-y-0.5">
                <p className="text-xs font-extrabold text-indigo-600 dark:text-indigo-400">
                  {routeData ? formatDuration(routeData.duration * 0.36) : "--"}
                </p>
                <p className="text-[9px] text-muted-foreground">
                  Distance: {routeData ? formatDistance(routeData.distance * 0.36) : "--"}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>

      </div>

      {/* Right Pane - Themed Route Map */}
      <div className="relative h-[300px] md:h-full w-full overflow-hidden border-t md:border-t-0 md:border-l border-border shadow-inner">
        <Map
          loading={loading}
          viewport={{ center: mapCenter, zoom: 12.5 }}
          theme={theme === "dark" ? "dark" : "light"}
        >
          <MapControls showFullscreen={true} />

          {/* Faded Traveled Route (Behind the supervisor) */}
          {progressCoordinates.length > 0 && (
            <MapRoute
              id="delivery-full-route"
              coordinates={progressCoordinates}
              color={theme === "dark" ? "#475569" : "#94a3b8"}
              width={5}
              opacity={0.35}
              interactive={false}
            />
          )}

          {/* Active Highlighted Route (In front of the supervisor, attached to incident) */}
          {routeData && routeData.coordinates.length > 0 && (
            <MapRoute
              id="delivery-progress-route"
              coordinates={routeData.coordinates.slice(progressCoordinates.length - 1)}
              color="#6366f1"
              width={5.5}
              opacity={0.9}
              interactive={false}
            />
          )}

          {/* Supervisor Live Position Marker */}
          {courierPosition && (
            <MapMarker
              longitude={courierPosition[0]}
              latitude={courierPosition[1]}
            >
              <MarkerContent>
                <div className="relative flex items-center justify-center scale-110">
                  <div className="w-8 h-8 rounded-full border-2 border-amber-400 bg-indigo-700 shadow-md flex items-center justify-center text-white ring-2 ring-indigo-400 animate-pulse">
                    <Truck size={14} className="text-white" />
                  </div>
                </div>
              </MarkerContent>
              <MarkerTooltip>
                <div className="space-y-0.5 text-xs text-foreground p-1 font-sans">
                  <p className="font-bold text-[10px] text-indigo-600 dark:text-indigo-400">Supervisor Dispatching</p>
                  <p className="font-semibold">{routeData ? formatDuration(routeData.duration * 0.36) : "--"} away</p>
                  <p className="text-muted-foreground text-[9px]">{routeData ? formatDistance(routeData.distance * 0.36) : "--"} remaining</p>
                </div>
              </MarkerTooltip>
            </MapMarker>
          )}

          {/* Supervisor Start Marker */}
          <MapMarker longitude={supervisorCoords.lng} latitude={supervisorCoords.lat}>
            <MarkerContent>
              <div className="w-4 h-4 rounded-full border-2 border-white bg-indigo-500 shadow-sm" />
            </MarkerContent>
            <MarkerTooltip>
              <div className="text-xs p-1 font-bold text-foreground">Supervisor Origin</div>
            </MarkerTooltip>
          </MapMarker>

          {/* Incident Destination Marker */}
          <MapMarker longitude={incidentCoords.lng} latitude={incidentCoords.lat}>
            <MarkerContent>
              <div className="flex flex-col items-center justify-center scale-110">
                <div className="w-8 h-8 rounded-full border-2 border-white bg-rose-500 shadow-md flex items-center justify-center text-white ring-2 ring-rose-400/50">
                  <AlertTriangle size={14} className="text-white fill-current" />
                </div>
              </div>
            </MarkerContent>
            <MarkerTooltip>
              <div className="text-xs p-1 font-bold text-foreground">Incident Location</div>
            </MarkerTooltip>
          </MapMarker>
        </Map>
      </div>
    </div>
  );
}
