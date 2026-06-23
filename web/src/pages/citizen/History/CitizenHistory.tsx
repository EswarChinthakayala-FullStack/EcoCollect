import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { 
  ArrowLeft, RefreshCw, AlertTriangle, Eye, Search, SlidersHorizontal,
  X, Calendar, MapPin, Check, Trash2, Recycle, Leaf, Package, PlusSquare
} from 'lucide-react';

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
}

export const CitizenHistory: React.FC = () => {
  const navigate = useNavigate();
  const { apiCall, backendHost } = useAuth();
  
  const [issues, setIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);

  // Search & Filter States
  const [searchQuery, setSearchQuery] = useState('');
  const [isFilterSheetOpen, setIsFilterSheetOpen] = useState(false);

  // Active filter parameters
  const [activeStatuses, setActiveStatuses] = useState<string[]>([]);
  const [activeCategories, setActiveCategories] = useState<string[]>([]);
  const [sortBy, setSortBy] = useState<string>('newest'); // 'newest' | 'oldest' | 'category'
  const [dateRange, setDateRange] = useState<string>('all'); // 'all' | 'week' | 'month'

  // Draft filter parameters for sheet
  const [draftStatuses, setDraftStatuses] = useState<string[]>([]);
  const [draftCategories, setDraftCategories] = useState<string[]>([]);
  const [draftSortBy, setDraftSortBy] = useState<string>('newest');
  const [draftDateRange, setDraftDateRange] = useState<string>('all');

  const fetchIssues = async () => {
    setLoading(true);
    try {
      const data = await apiCall<Issue[]>('/citizen/issues');
      // Sort newest first initially
      const sorted = data.sort((a, b) => b.id - a.id);
      setIssues(sorted);
    } catch (err) {
      console.error('Failed to load history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIssues();
  }, [apiCall]);

  const handleSelectIssue = (issue: Issue) => {
    navigate(`/citizen/history/${issue.id}`);
  };

  const openFilterSheet = () => {
    setDraftStatuses([...activeStatuses]);
    setDraftCategories([...activeCategories]);
    setDraftSortBy(sortBy);
    setDraftDateRange(dateRange);
    setIsFilterSheetOpen(true);
  };

  const applyFilters = () => {
    setActiveStatuses([...draftStatuses]);
    setActiveCategories([...draftCategories]);
    setSortBy(draftSortBy);
    setDateRange(draftDateRange);
    setIsFilterSheetOpen(false);
  };

  const clearFilters = () => {
    setDraftStatuses([]);
    setDraftCategories([]);
    setDraftSortBy('newest');
    setDraftDateRange('all');
  };

  const resetAllFilters = () => {
    setActiveStatuses([]);
    setActiveCategories([]);
    setSortBy('newest');
    setDateRange('all');
    setSearchQuery('');
  };

  const getFirstImageUrl = (imageUrl: string | string[] | null | undefined) => {
    if (!imageUrl) return null;
    let parsed: string[] = [];
    if (Array.isArray(imageUrl)) {
      parsed = imageUrl;
    } else if (typeof imageUrl === 'string') {
      const trimmed = imageUrl.trim();
      if (trimmed.startsWith('[')) {
        try {
          parsed = JSON.parse(trimmed) as string[];
        } catch (e) {
          parsed = trimmed.split(',').map(s => s.trim()).filter(Boolean);
        }
      } else {
        parsed = trimmed.split(',').map(s => s.trim()).filter(Boolean);
      }
    }
    
    if (parsed.length === 0) return null;
    const first = parsed[0];
    if (first.startsWith('http') || first.startsWith('data:')) {
      return first;
    }
    return `${backendHost}${first}`;
  };

  const getBadgeClass = (status: string) => {
    const base = "inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider w-fit border";
    switch (status.toLowerCase().replace(' ', '_')) {
      case 'pending':
        return `${base} bg-amber-50 dark:bg-amber-950/20 text-amber-600 dark:text-amber-400 border-amber-200 dark:border-amber-900/30`;
      case 'in_progress':
        return `${base} bg-blue-50 dark:bg-blue-950/20 text-blue-600 dark:text-blue-400 border-blue-200 dark:border-blue-900/30`;
      case 'completed':
      case 'resolved':
        return `${base} bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/30`;
      default:
        return `${base} bg-slate-50 dark:bg-slate-950/20 text-slate-600 dark:text-slate-400 border-slate-200 dark:border-slate-800/40`;
    }
  };

  const getCategoryIcon = (category: string) => {
    const size = 30;
    switch (category) {
      case 'Overflowing Bin':
        return <Trash2 size={size} className="text-amber-600 dark:text-amber-500" />;
      case 'Illegal Dumping':
        return <AlertTriangle size={size} className="text-rose-600 dark:text-rose-500" />;
      case 'Recycling Issue':
        return <Recycle size={size} className="text-blue-600 dark:text-blue-500" />;
      case 'Green Waste':
        return <Leaf size={size} className="text-emerald-600 dark:text-emerald-500" />;
      case 'Bulky Items':
        return <Package size={size} className="text-purple-600 dark:text-purple-500" />;
      default:
        return <PlusSquare size={size} className="text-slate-600 dark:text-slate-500" />;
    }
  };

  // Perform Client-side search and filtering
  const filteredIssues = issues.filter(issue => {
    // 1. Search Query
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      const matchesId = `#id-${issue.id}`.includes(query) || `id-${issue.id}`.includes(query) || issue.id.toString().includes(query);
      const matchesCategory = issue.category.toLowerCase().includes(query);
      const matchesTitle = issue.title?.toLowerCase().includes(query) || false;
      const matchesAddress = issue.address?.toLowerCase().includes(query) || false;
      const matchesDesc = issue.description?.toLowerCase().includes(query) || false;
      
      if (!matchesId && !matchesCategory && !matchesTitle && !matchesAddress && !matchesDesc) {
        return false;
      }
    }

    // 2. Status
    if (activeStatuses.length > 0) {
      if (!activeStatuses.includes(issue.status)) {
        return false;
      }
    }

    // 3. Category
    if (activeCategories.length > 0) {
      if (!activeCategories.includes(issue.category)) {
        return false;
      }
    }

    // 4. Date Range
    if (dateRange !== 'all') {
      const issueDate = new Date(issue.created_at);
      const now = new Date();
      if (dateRange === 'week') {
        const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        if (issueDate < oneWeekAgo) return false;
      } else if (dateRange === 'month') {
        const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        if (issueDate < oneMonthAgo) return false;
      }
    }

    return true;
  }).sort((a, b) => {
    if (sortBy === 'newest') {
      return new Date(b.created_at).getTime() - new Date(a.created_at).getTime();
    } else if (sortBy === 'oldest') {
      return new Date(a.created_at).getTime() - new Date(b.created_at).getTime();
    } else if (sortBy === 'category') {
      return a.category.localeCompare(b.category);
    }
    return 0;
  });

  const hasActiveFilters = activeStatuses.length > 0 || activeCategories.length > 0 || dateRange !== 'all' || sortBy !== 'newest';

  return (
    <div className="w-full text-foreground relative">
      <div className="flex flex-col gap-6 w-full animate-slide-up">
      
      {/* Header Panel */}
      <div className="flex items-center gap-4 mb-2">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none" 
          onClick={() => navigate('/citizen/dashboard')}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-xl font-bold text-foreground">My Reported Issues</h2>
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none ml-auto" 
          onClick={fetchIssues}
          aria-label="Refresh issues"
        >
          <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
        </button>
      </div>

      {/* Search and Filters Toolbar */}
      <div className="bg-card border border-border shadow-sm rounded-xl p-4 flex gap-3 items-center">
        <div className="relative flex items-center w-full">
          <Search size={18} className="absolute left-4 text-muted-foreground" />
          <input 
            type="text" 
            className="w-full h-11 bg-muted/40 border border-border rounded-xl pl-12 pr-4 text-sm text-foreground outline-none transition-all focus:bg-muted/80 focus:border-emerald-600 focus:ring-2 focus:ring-emerald-600/10 placeholder:text-muted-foreground"
            placeholder="Search by ticket ID, category, title, or address..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <button
          onClick={openFilterSheet}
          className={`h-11 px-4 rounded-xl flex items-center justify-center gap-2 border text-sm font-semibold cursor-pointer transition-all relative shrink-0 ${
            hasActiveFilters
              ? 'bg-emerald-500/10 border-emerald-500 text-emerald-600 dark:text-emerald-400'
              : 'bg-card border-border text-muted-foreground hover:bg-muted'
          }`}
        >
          <SlidersHorizontal size={18} />
          <span className="hidden sm:inline font-sans">Filters</span>
          {hasActiveFilters && (
            <span className="absolute -top-1 -right-1 w-2.5 h-2.5 bg-emerald-600 rounded-full border border-card" />
          )}
        </button>
      </div>

      {/* List / Grid Display */}
      {loading ? (
        <div className="flex items-center justify-center min-h-[400px] w-full">
          <div className="w-12 h-12 rounded-full border-4 border-slate-200 dark:border-slate-800 border-t-emerald-600 dark:border-t-emerald-500 animate-spin" />
        </div>
      ) : issues.length === 0 ? (
        <div className="bg-card text-card-foreground border border-border shadow-sm rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
          <AlertTriangle size={36} className="text-muted-foreground" />
          <p className="text-sm text-muted-foreground">You have not submitted any reports yet.</p>
          <button 
            className="bg-emerald-600 hover:bg-emerald-700 text-white px-5 py-2.5 rounded-xl border-none font-semibold text-sm cursor-pointer transition-all duration-150 ease-out hover:-translate-y-0.5 dark:bg-emerald-600 dark:hover:bg-emerald-700" 
            onClick={() => navigate('/citizen/report')}
          >
            File a Report
          </button>
        </div>
      ) : filteredIssues.length === 0 ? (
        <div className="bg-card text-card-foreground border border-border shadow-sm rounded-xl p-12 flex flex-col items-center justify-center text-center gap-4">
          <AlertTriangle size={36} className="text-muted-foreground" />
          <p className="text-sm text-muted-foreground">No reports match your selected search query or filters.</p>
          <button 
            className="bg-emerald-600 hover:bg-emerald-700 text-white px-5 py-2.5 rounded-xl border-none font-semibold text-sm cursor-pointer transition-all duration-150 ease-out hover:-translate-y-0.5 dark:bg-emerald-600 dark:hover:bg-emerald-700" 
            onClick={resetAllFilters}
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredIssues.map((issue) => (
            <div 
              key={issue.id} 
              className="relative bg-card border border-border shadow-sm rounded-2xl overflow-hidden cursor-pointer hover:border-emerald-500/50 hover:shadow-md transition-all duration-300 group flex flex-col h-full text-left"
              onClick={() => handleSelectIssue(issue)}
            >
              {/* Card Image Header */}
              <div className="relative w-full h-44 overflow-hidden bg-muted/40 border-b border-border/40 shrink-0">
                {getFirstImageUrl(issue.image_url) ? (
                  <img 
                    src={getFirstImageUrl(issue.image_url) || ''} 
                    alt={issue.category} 
                    className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                ) : (
                  <div className="w-full h-full bg-gradient-to-br from-emerald-500/10 via-teal-500/5 to-transparent flex items-center justify-center">
                    {getCategoryIcon(issue.category)}
                  </div>
                )}
                
                {/* Absolute status badge on header */}
                <div className="absolute top-3 left-3 z-10">
                  <span className={getBadgeClass(issue.status)}>
                    {issue.status}
                  </span>
                </div>

                {/* Absolute Ticket ID on header */}
                <div className="absolute top-3 right-3 z-10 bg-black/60 backdrop-blur-md text-white border border-white/20 text-[10px] font-bold px-2 py-0.5 rounded-lg font-sans">
                  #ID-{issue.id}
                </div>
              </div>

              {/* Card Content Body */}
              <div className="p-5 flex flex-col justify-between flex-grow">
                <div className="flex flex-col">
                  <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400 uppercase tracking-wider mb-1 font-sans">{issue.category}</span>
                  <h3 className="text-base font-extrabold text-foreground line-clamp-1 group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition-colors">
                    {issue.title || `${issue.category} Issue`}
                  </h3>
                  <p className="text-xs text-muted-foreground flex items-start gap-1.5 mt-2.5 line-clamp-2 min-h-[32px] leading-relaxed">
                    <MapPin size={13} className="shrink-0 mt-0.5 text-muted-foreground/80" />
                    <span>{issue.address || 'Location unspecified'}</span>
                  </p>
                </div>

                <div className="flex justify-between items-center mt-5 pt-3.5 border-t border-border/60">
                  <span className="text-[11px] text-muted-foreground/80 flex items-center gap-1 font-sans font-medium">
                    <Calendar size={12} />
                    {new Date(issue.created_at).toLocaleDateString(undefined, {
                      month: 'short',
                      day: 'numeric',
                      year: 'numeric'
                    })}
                  </span>
                  
                  <span className="flex items-center gap-1 text-[11px] font-bold text-emerald-600 dark:text-emerald-400 group-hover:underline transition-all">
                    <Eye size={13} />
                    <span>View Audit</span>
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
      </div>

      {/* Filters Sheet Drawer */}
      {isFilterSheetOpen && (
        <div className="fixed inset-0 z-[300] overflow-hidden" aria-labelledby="slide-over-title" role="dialog" aria-modal="true">
          {/* Backdrop overlay */}
          <div 
            className="fixed inset-0 bg-black/60 backdrop-blur-sm transition-opacity duration-300 animate-fade-in" 
            onClick={() => setIsFilterSheetOpen(false)}
          />

          {/* Drawer container directly fixed */}
          <div className="fixed inset-y-0 right-0 w-full sm:max-w-md bg-card text-card-foreground shadow-2xl border-l border-border flex flex-col h-screen z-[310] animate-slide-left">
              
              {/* Drawer Header */}
              <div className="px-6 py-5 border-b border-border/80 flex items-center justify-between">
                <div className="flex flex-col text-left">
                  <h2 id="slide-over-title" className="text-base font-bold text-foreground">Filter & Sort</h2>
                  <span className="text-xs text-muted-foreground">Refine your reported issues view</span>
                </div>
                <div className="flex items-center gap-3">
                  <button 
                    onClick={clearFilters}
                    className="bg-transparent border-none text-xs font-bold text-emerald-600 dark:text-emerald-400 hover:underline cursor-pointer"
                  >
                    Clear All
                  </button>
                  <button 
                    onClick={() => setIsFilterSheetOpen(false)}
                    className="w-8 h-8 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none"
                    aria-label="Close filters sheet"
                  >
                    <X size={16} />
                  </button>
                </div>
              </div>

              {/* Scrollable Drawer Body */}
              <div className="flex-grow overflow-y-auto p-6 flex flex-col gap-6 text-left">
                {/* Status Selection */}
                <div className="flex flex-col gap-2.5">
                  <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Filing Status</span>
                  <div className="flex flex-wrap gap-2">
                    {['Pending', 'In Progress', 'Completed'].map((status) => {
                      const isSelected = draftStatuses.includes(status);
                      return (
                        <button
                          key={status}
                          onClick={() => {
                            if (isSelected) {
                              setDraftStatuses(draftStatuses.filter(s => s !== status));
                            } else {
                              setDraftStatuses([...draftStatuses, status]);
                            }
                          }}
                          className={`px-3 py-1.5 rounded-full text-xs font-semibold border cursor-pointer transition-all ${
                            isSelected 
                              ? 'bg-emerald-600 border-emerald-600 text-white dark:bg-emerald-600 dark:border-emerald-500' 
                              : 'bg-muted/50 border-border text-muted-foreground hover:bg-muted'
                          }`}
                        >
                          {status}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Category Selection */}
                <div className="flex flex-col gap-2.5">
                  <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Report Category</span>
                  <div className="grid grid-cols-2 gap-2">
                    {['Overflowing Bin', 'Illegal Dumping', 'Recycling Issue', 'Green Waste', 'Bulky Items', 'Other'].map((cat) => {
                      const isSelected = draftCategories.includes(cat);
                      return (
                        <button
                          key={cat}
                          onClick={() => {
                            if (isSelected) {
                              setDraftCategories(draftCategories.filter(c => c !== cat));
                            } else {
                              setDraftCategories([...draftCategories, cat]);
                            }
                          }}
                          className={`px-3 py-2.5 rounded-xl text-xs font-bold border cursor-pointer text-left flex items-center justify-between transition-all ${
                            isSelected 
                              ? 'bg-emerald-500/10 border-emerald-500 text-emerald-600 dark:text-emerald-400' 
                              : 'bg-muted/30 border-border text-muted-foreground hover:bg-muted/70'
                          }`}
                        >
                          <span>{cat}</span>
                          {isSelected && <Check size={14} className="text-emerald-600 dark:text-emerald-400 shrink-0" />}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Sort Order Selector */}
                <div className="flex flex-col gap-2.5">
                  <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Sort Order</span>
                  <div className="flex flex-col gap-2">
                    {[
                      { id: 'newest', label: 'Newest First' },
                      { id: 'oldest', label: 'Oldest First' },
                      { id: 'category', label: 'Category Name (A-Z)' }
                    ].map((opt) => {
                      const isSelected = draftSortBy === opt.id;
                      return (
                        <button
                          key={opt.id}
                          onClick={() => setDraftSortBy(opt.id)}
                          className={`px-4 py-3 rounded-xl text-xs font-bold border cursor-pointer text-left flex items-center justify-between transition-all ${
                            isSelected 
                              ? 'bg-emerald-500/10 border-emerald-500 text-emerald-600 dark:text-emerald-400' 
                              : 'bg-muted/30 border-border text-muted-foreground hover:bg-muted/70'
                          }`}
                        >
                          <span>{opt.label}</span>
                          {isSelected && <Check size={14} className="text-emerald-600 dark:text-emerald-400 shrink-0" />}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Date Selection */}
                <div className="flex flex-col gap-2.5">
                  <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Filing Date</span>
                  <div className="flex flex-col gap-2">
                    {[
                      { id: 'all', label: 'All Time' },
                      { id: 'week', label: 'Past 7 Days' },
                      { id: 'month', label: 'Past 30 Days' }
                    ].map((opt) => {
                      const isSelected = draftDateRange === opt.id;
                      return (
                        <button
                          key={opt.id}
                          onClick={() => setDraftDateRange(opt.id)}
                          className={`px-4 py-3 rounded-xl text-xs font-bold border cursor-pointer text-left flex items-center justify-between transition-all ${
                            isSelected 
                              ? 'bg-emerald-500/10 border-emerald-500 text-emerald-600 dark:text-emerald-400' 
                              : 'bg-muted/30 border-border text-muted-foreground hover:bg-muted/70'
                          }`}
                        >
                          <span>{opt.label}</span>
                          {isSelected && <Check size={14} className="text-emerald-600 dark:text-emerald-400 shrink-0" />}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* Drawer Footer Actions */}
              <div className="px-6 py-5 border-t border-border/80 bg-muted/20 flex gap-3">
                <button 
                  onClick={() => setIsFilterSheetOpen(false)}
                  className="flex-1 h-11 bg-card hover:bg-muted border border-border rounded-xl text-xs font-bold text-muted-foreground cursor-pointer transition-colors"
                >
                  Cancel
                </button>
                <button 
                  onClick={applyFilters}
                  className="flex-1 h-11 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold border-none cursor-pointer transition-colors shadow-sm"
                >
                  Apply Filters
                </button>
              </div>

            </div>

        </div>
      )}

    </div>
  );
};
