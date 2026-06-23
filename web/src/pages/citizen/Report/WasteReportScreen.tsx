import React, { useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../../components/Button';
import { Image as ImageIcon, ArrowLeft, Loader2, X, PlusCircle } from 'lucide-react';

export const WasteReportScreen: React.FC = () => {
  const navigate = useNavigate();
  const { token, backendHost } = useAuth();
  
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState('');
  const [error, setError] = useState<string | null>(null);

  // Clean up object URLs on unmount to prevent leaks
  useEffect(() => {
    return () => {
      previews.forEach(url => URL.revokeObjectURL(url));
    };
  }, [previews]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    setError(null);
    const newFiles = [...selectedFiles, ...files];
    setSelectedFiles(newFiles);

    const newPreviews = files.map(file => URL.createObjectURL(file));
    setPreviews([...previews, ...newPreviews]);
    
    // Reset file input value so same files can be chosen again
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const removeFile = (idx: number) => {
    URL.revokeObjectURL(previews[idx]);
    setSelectedFiles(selectedFiles.filter((_, i) => i !== idx));
    setPreviews(previews.filter((_, i) => i !== idx));
  };

  const handleUploadAndProceed = async () => {
    if (selectedFiles.length === 0) return;

    setUploading(true);
    setError(null);

    const uploadedUrls: string[] = [];

    try {
      for (let i = 0; i < selectedFiles.length; i++) {
        setUploadProgress(`Uploading photo ${i + 1} of ${selectedFiles.length}...`);
        
        const file = selectedFiles[i];
        const formData = new FormData();
        formData.append('file', file);

        const res = await fetch(`${backendHost}/api/upload`, {
          method: 'POST',
          headers: token ? { 'Authorization': `Bearer ${token}` } : {},
          body: formData,
        });

        if (!res.ok) {
          throw new Error(`Failed to upload photo ${i + 1}.`);
        }

        const data = await res.json();
        uploadedUrls.push(data.file_url);
      }

      // Concatenate file urls with comma separation
      const imageUrlsStr = uploadedUrls.join(',');
      navigate('/citizen/report/category', { state: { image_url: imageUrlsStr } });
    } catch (err: any) {
      setError(err.message || 'Failed to upload photo. Please try again.');
    } finally {
      setUploading(false);
      setUploadProgress('');
    }
  };

  const handleGalleryClick = () => {
    fileInputRef.current?.click();
  };

  const handleSkipPhoto = () => {
    navigate('/citizen/report/category', { state: { image_url: null } });
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up text-foreground">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-background border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-emerald-600 hover:border-emerald-600 dark:hover:text-emerald-400 hover:bg-emerald-500/5" 
          onClick={() => navigate('/citizen/dashboard')}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider">1 of 3: Upload Photos</h2>
      </div>

      <div className="bg-card text-card-foreground border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col">
        <div className="mb-6 text-left">
          <h2 className="text-2xl lg:text-3xl font-extrabold text-foreground mb-2">Let's capture the issue</h2>
          <p className="text-sm text-muted-foreground leading-relaxed">
            Uploading photos helps municipal workers identify the right equipment and tools required to resolve the issue quickly.
          </p>
        </div>

        {error && (
          <div className="flex items-center p-4 mb-6 text-sm text-red-600 dark:text-red-400 rounded-lg bg-red-50 dark:bg-red-950/20 border border-red-100 dark:border-red-900/30 text-left">
            <span>{error}</span>
          </div>
        )}

        {/* Selected files preview panel */}
        {previews.length > 0 && (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 mb-6 text-left">
            {previews.map((src, idx) => (
              <div key={idx} className="relative aspect-square rounded-xl overflow-hidden border border-border bg-muted/30 group">
                <img src={src} alt={`Selected waste ${idx + 1}`} className="w-full h-full object-cover" />
                <button 
                  type="button" 
                  onClick={() => removeFile(idx)}
                  className="absolute top-2 right-2 w-7 h-7 rounded-full bg-slate-950/80 hover:bg-slate-950 dark:bg-slate-900/80 dark:hover:bg-slate-800 text-white flex items-center justify-center cursor-pointer border-none shadow-md"
                  title="Remove image"
                  disabled={uploading}
                >
                  <X size={14} />
                </button>
              </div>
            ))}
            
            {/* Add more files box */}
            <button 
              type="button" 
              onClick={handleGalleryClick}
              disabled={uploading}
              className="aspect-square rounded-xl border-2 border-dashed border-border hover:border-emerald-500 dark:hover:border-emerald-400 hover:bg-emerald-500/5 flex flex-col items-center justify-center gap-1 text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors cursor-pointer bg-transparent"
              title="Add more photos"
            >
              <PlusCircle size={24} />
              <span className="text-[10px] font-bold uppercase">Add Photo</span>
            </button>
          </div>
        )}

        {previews.length === 0 && (
          <div 
            className="min-h-[240px] flex flex-col items-center justify-center border-2 border-dashed border-border rounded-2xl cursor-pointer mb-6 bg-muted/20 transition-all duration-250 p-8 text-center hover:border-emerald-500 dark:hover:border-emerald-400 hover:bg-emerald-500/5 group" 
            onClick={uploading ? undefined : handleGalleryClick}
          >
            {uploading ? (
              <div className="flex flex-col items-center justify-center gap-3">
                <Loader2 className="text-emerald-600 animate-spin" size={48} />
                <span className="text-base font-bold text-foreground mb-1.5">{uploadProgress || 'Uploading Photos...'}</span>
                <span className="text-xs text-muted-foreground">Connecting to media server</span>
              </div>
            ) : (
              <>
                <ImageIcon size={48} className="text-muted-foreground mb-4 transition-colors duration-150 group-hover:text-emerald-500 dark:group-hover:text-emerald-400" />
                <span className="text-base font-bold text-foreground mb-1.5">Upload Waste Photos</span>
                <span className="text-xs text-muted-foreground">Select one or multiple photos from your device</span>
              </>
            )}
          </div>
        )}

        {uploading && previews.length > 0 && (
          <div className="flex items-center justify-center gap-3 py-6 mb-4 bg-muted/30 border border-border rounded-xl">
            <Loader2 className="text-emerald-600 animate-spin" size={24} />
            <span className="text-sm font-bold text-foreground">{uploadProgress}</span>
          </div>
        )}

        <input 
          type="file" 
          accept="image/*" 
          multiple
          className="hidden" 
          ref={fileInputRef}
          onChange={handleFileChange}
          disabled={uploading}
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {previews.length > 0 ? (
            <Button 
              fullWidth 
              theme="green" 
              onClick={handleUploadAndProceed}
              disabled={uploading}
            >
              Upload & Continue
            </Button>
          ) : (
            <button 
              className="flex items-center justify-center gap-2 p-3.5 border border-emerald-500 dark:border-emerald-600 rounded-xl cursor-pointer bg-card hover:bg-emerald-500/5 text-emerald-600 dark:text-emerald-400 font-bold text-sm transition-all duration-150" 
              onClick={handleGalleryClick} 
              disabled={uploading}
            >
              <ImageIcon size={20} />
              <span>Choose Photos</span>
            </button>
          )}

          <Button 
            fullWidth 
            theme="green" 
            variant="outline" 
            onClick={handleSkipPhoto}
            disabled={uploading}
          >
            Continue Without Photo
          </Button>
        </div>
      </div>
    </div>
  );
};
