import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { ArrowLeft, Plus, Users, Award, Search, MapPin, Eye, RefreshCw } from 'lucide-react';

interface SupervisorStats {
  id: number;
  full_name: string;
  email: string;
  employee_id: string;
  assigned_area?: string;
  assigned_reports: number;
  resolved_reports: number;
  is_active?: boolean;
}

export const AdminSupervisors: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall } = useAuth();
  
  const [supervisors, setSupervisors] = useState<SupervisorStats[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  const fetchSupervisors = async () => {
    setLoading(true);
    try {
      const data = await apiCall<SupervisorStats[]>('/admin/supervisors');
      setSupervisors(data);
    } catch (err) {
      console.error('Failed to load supervisors:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSupervisors();
  }, [apiCall]);

  const filteredSupervisors = supervisors.filter(sup => {
    const text = `${sup.full_name} ${sup.employee_id} ${sup.assigned_area || ''} ${sup.email}`.toLowerCase();
    return text.includes(searchQuery.toLowerCase());
  });

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-left">
      {/* Header Row */}
      <div className="flex flex-wrap items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted" 
          onClick={() => navigate('/admin/dashboard')}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">Management</h2>
          <h1 className="text-2xl font-extrabold text-indigo-600 dark:text-indigo-400">Supervisor Registry</h1>
        </div>
        
        <div className="flex items-center gap-2 sm:ml-auto">
          <button 
            className="flex items-center gap-2 px-4 py-2 bg-card border border-indigo-600 dark:border-indigo-500 text-indigo-600 dark:text-indigo-400 font-semibold rounded-xl text-sm transition-all duration-150 hover:bg-muted cursor-pointer" 
            onClick={() => navigate('/admin/supervisors/add')}
          >
            <Plus size={16} />
            <span>Add Staff</span>
          </button>
          <button 
            className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-600 dark:hover:border-indigo-500 hover:bg-muted"
            onClick={fetchSupervisors}
            title="Refresh Registry"
          >
            <RefreshCw size={18} />
          </button>
        </div>
      </div>

      {/* KPI stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
        {/* Total Staff Card */}
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-5 flex items-center gap-4 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)]">
          <div className="w-11 h-11 rounded-full flex items-center justify-center bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 shrink-0">
            <Users size={20} />
          </div>
          <div>
            <span className="text-2xl font-extrabold text-foreground block leading-tight">{supervisors.length}</span>
            <span className="text-xs text-muted-foreground font-medium">Total Staff Members</span>
          </div>
        </div>

        {/* Resolved Collectively Card */}
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-5 flex items-center gap-4 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-[0_12px_40px_0_rgba(15,23,42,0.08)]">
          <div className="w-11 h-11 rounded-full flex items-center justify-center bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 shrink-0">
            <Award size={20} />
          </div>
          <div>
            <span className="text-2xl font-extrabold text-foreground block leading-tight">
              {supervisors.reduce((acc, curr) => acc + curr.resolved_reports, 0)}
            </span>
            <span className="text-xs text-muted-foreground font-medium">Resolved Collectively</span>
          </div>
        </div>
      </div>

      {/* Search Toolbar */}
      <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-4">
        <div className="relative">
          <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input 
            type="text" 
            className="font-sans w-full pl-12 pr-4 py-3 bg-muted/40 border border-border rounded-xl text-sm text-foreground outline-none transition-all focus:border-indigo-600 dark:focus:border-indigo-400 focus:ring-2 focus:ring-indigo-500/10 placeholder:text-muted-foreground"
            placeholder="Search supervisors by name, employee ID, email, or zone..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {/* List */}
      {loading ? (
        <div className="flex justify-center items-center py-12">
          <div className="w-10 h-10 border-4 border-indigo-600/20 border-t-indigo-600 dark:border-t-indigo-500 rounded-full animate-spin" />
        </div>
      ) : filteredSupervisors.length === 0 ? (
        <div className="bg-card border border-border shadow-[0_8px_32px_0_rgba(15,23,42,0.04)] rounded-xl p-12 flex flex-col items-center justify-center text-center gap-3">
          <Users size={36} className="text-muted-foreground/40" />
          <p className="text-sm font-medium text-muted-foreground">No supervisor staff registered in the registry.</p>
        </div>
      ) : (
        <div className="w-full overflow-hidden border border-border rounded-xl bg-card shadow-[0_8px_32px_0_rgba(15,23,42,0.04)]">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-border bg-muted/50">
                  <th className="text-muted-foreground font-semibold px-5 py-4">Officer Info</th>
                  <th className="text-muted-foreground font-semibold px-5 py-4">Employee ID</th>
                  <th className="text-muted-foreground font-semibold px-5 py-4">Assigned Zone</th>
                  <th className="text-muted-foreground font-semibold px-5 py-4">Status</th>
                  <th className="text-muted-foreground font-semibold px-5 py-4">Jobs Allocated</th>
                  <th className="text-muted-foreground font-semibold px-5 py-4">Jobs Resolved</th>
                  <th className="text-muted-foreground font-semibold px-5 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {filteredSupervisors.map(sup => (
                  <tr key={sup.id} className="hover:bg-muted/40 transition-colors duration-150">
                    <td className="px-5 py-4">
                      <div className="flex flex-col">
                        <span className="font-bold text-foreground">{sup.full_name}</span>
                        <span className="text-xs text-muted-foreground">{sup.email}</span>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <span className="font-semibold text-xs bg-muted text-muted-foreground border border-border/60 px-2.5 py-1 rounded-md">
                        {sup.employee_id}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <span className="font-semibold text-xs bg-muted text-muted-foreground border border-border/60 px-2.5 py-1.5 rounded-md inline-flex items-center gap-1.5">
                        <MapPin size={12} className="text-muted-foreground" />
                        <span>{sup.assigned_area || 'Not Assigned'}</span>
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      {sup.is_active === false ? (
                        <span className="font-extrabold text-[10px] uppercase tracking-wider bg-red-500/10 text-red-600 dark:text-red-400 border border-red-500/20 px-2.5 py-1 rounded-full">
                          Deactivated
                        </span>
                      ) : (
                        <span className="font-extrabold text-[10px] uppercase tracking-wider bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 px-2.5 py-1 rounded-full">
                          Active
                        </span>
                      )}
                    </td>
                    <td className="px-5 py-4 font-bold text-amber-600 dark:text-amber-400">
                      {sup.assigned_reports}
                    </td>
                    <td className="px-5 py-4 font-bold text-emerald-600 dark:text-emerald-400">
                      {sup.resolved_reports}
                    </td>
                    <td className="px-5 py-4 text-right">
                      <button 
                        onClick={() => navigate(`/admin/supervisors/${sup.id}`)}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold text-indigo-600 dark:text-indigo-400 hover:text-indigo-700 dark:hover:text-indigo-300 hover:bg-muted border border-border rounded-lg transition-all duration-150 cursor-pointer"
                      >
                        <Eye size={14} />
                        <span>View Details</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
