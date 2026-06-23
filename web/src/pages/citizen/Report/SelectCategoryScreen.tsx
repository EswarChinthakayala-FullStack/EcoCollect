import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Button } from '../../../components/Button';
import { Trash2, AlertTriangle, Recycle, Leaf, Package, PlusSquare, ArrowLeft } from 'lucide-react';

interface CategoryItem {
  id: string;
  title: string;
  icon: React.ReactNode;
  iconColor: string;
}

export const SelectCategoryScreen: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // Extract previous step state
  const { image_url } = (location.state as { image_url?: string | null }) || { image_url: null };
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  const categories: CategoryItem[] = [
    { id: 'Overflowing Bin', title: 'Overflowing Bin', icon: <Trash2 size={24} />, iconColor: '#F59E0B' },
    { id: 'Illegal Dumping', title: 'Illegal Dumping', icon: <AlertTriangle size={24} />, iconColor: '#EF4444' },
    { id: 'Recycling Issue', title: 'Recycling Issue', icon: <Recycle size={24} />, iconColor: '#3B82F6' },
    { id: 'Green Waste', title: 'Green Waste', icon: <Leaf size={24} />, iconColor: '#10B981' },
    { id: 'Bulky Items', title: 'Bulky Items', icon: <Package size={24} />, iconColor: '#8B5CF6' },
    { id: 'Other', title: 'Other', icon: <PlusSquare size={24} />, iconColor: '#64748B' },
  ];

  const handleContinue = () => {
    if (selectedCategory) {
      navigate('/citizen/report/location', { 
        state: { 
          image_url, 
          category: selectedCategory 
        } 
      });
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
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">2 of 3: Select Category</h2>
      </div>

      <div className="bg-card text-card-foreground border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col">
        <div className="mb-6 text-left">
          <h2 className="text-2xl lg:text-3xl font-extrabold text-foreground mb-2">What type of waste?</h2>
          <p className="text-sm text-muted-foreground leading-relaxed">
            Categorizing helps us route your report to the appropriate cleanup team and municipal service.
          </p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
          {categories.map((cat) => (
            <div 
              key={cat.id}
              className={`flex flex-col items-center justify-center p-6 border rounded-xl bg-card text-foreground cursor-pointer transition-all duration-250 hover:-translate-y-0.5 hover:shadow-[0_4px_12px_rgba(15,23,42,0.05)] hover:border-slate-300 dark:hover:border-emerald-500/40 group ${
                selectedCategory === cat.id 
                  ? 'border-emerald-500 dark:border-emerald-400 bg-emerald-500/5 dark:bg-emerald-500/10 shadow-[0_4px_12px_rgba(16,185,129,0.1)]' 
                  : 'border-border'
              }`}
              onClick={() => setSelectedCategory(cat.id)}
            >
              <div className="w-12 h-12 rounded-full bg-muted/65 flex items-center justify-center mb-3 transition-transform duration-150 group-hover:scale-110" style={{ color: cat.iconColor }}>
                {cat.icon}
              </div>
              <span className="text-sm font-bold text-foreground text-center">{cat.title}</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-3 mt-6">
          <Button 
            fullWidth 
            theme="green" 
            onClick={handleContinue}
            disabled={!selectedCategory}
          >
            Continue
          </Button>
        </div>
      </div>
    </div>
  );
};
