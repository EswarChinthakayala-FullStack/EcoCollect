import React, { useRef, useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../../components/Button';
import { ArrowLeft, Image as ImageIcon, Loader2, AlertCircle, CheckCircle, X, PlusCircle } from 'lucide-react';

export const SupervisorReportStatus: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { apiCall, token, backendHost } = useAuth();
  
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [remarks, setRemarks] = useState('');
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    return () => {
      previews.forEach(url => URL.revokeObjectURL(url));
    };
  }, [previews]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    setError(null);
    setSelectedFiles([...selectedFiles, ...files]);
    const newPreviews = files.map(file => URL.createObjectURL(file));
    setPreviews([...previews, ...newPreviews]);

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const removeFile = (idx: number) => {
    URL.revokeObjectURL(previews[idx]);
    setSelectedFiles(selectedFiles.filter((_, i) => i !== idx));
    setPreviews(previews.filter((_, i) => i !== idx));
  };

  const handleGalleryClick = () => {
    fileInputRef.current?.click();
  };

  const handleSubmitResolution = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    const uploadedUrls: string[] = [];

    try {
      // 1. Upload photos first if there are any selected
      if (selectedFiles.length > 0) {
        setUploading(true);
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
            throw new Error(`Failed to upload resolution photo ${i + 1}.`);
          }

          const data = await res.json();
          uploadedUrls.push(data.file_url);
        }
        setUploading(false);
        setUploadProgress('');
      }

      // 2. Submit resolution details
      const query = new URLSearchParams();
      if (uploadedUrls.length > 0) {
        query.append('completion_image_url', uploadedUrls.join(','));
      }
      if (remarks) query.append('remarks', remarks);

      await apiCall(`/supervisor/issues/${id}/complete?${query.toString()}`, {
        method: 'PUT'
      });

      navigate('/supervisor/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to submit resolution. Please try again.');
      setSubmitting(false);
      setUploading(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full animate-slide-up py-5 text-foreground">
      <div className="flex items-center gap-4">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-card border border-border text-muted-foreground transition-all duration-150 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 hover:border-blue-600 dark:hover:border-blue-500 hover:bg-muted" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <h2 className="text-sm font-bold text-muted-foreground uppercase tracking-wider font-sans">Task Resolution</h2>
      </div>

      <div className="bg-card border border-border shadow-sm rounded-xl p-6 transition-all duration-250 ease-in-out hover:-translate-y-0.5 hover:shadow-md flex flex-col">
        <div className="mb-6 text-left">
          <h2 className="text-2xl lg:text-3xl font-extrabold text-foreground mb-2">Submit Clean Up Resolution</h2>
          <p className="text-sm text-muted-foreground leading-relaxed">
            Upload photos demonstrating the resolved issue and describe the cleanup actions taken for municipal records.
          </p>
        </div>

        {error && (
          <div className="flex items-center gap-3 p-4 mb-6 text-sm text-red-600 dark:text-red-400 rounded-lg bg-red-500/10 border border-red-500/20 text-left">
            <AlertCircle size={18} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmitResolution}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-2">
            {/* Left Column - Photos Upload */}
            <div className="flex flex-col gap-2 text-left">
              <label className="text-sm font-semibold text-foreground">Resolution Photos</label>
              
              {previews.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 mb-4">
                  {previews.map((src, idx) => (
                    <div key={idx} className="relative aspect-video rounded-xl overflow-hidden border border-border bg-muted group">
                      <img src={src} alt={`Resolution proof ${idx + 1}`} className="w-full h-full object-cover" />
                      <button 
                        type="button" 
                        onClick={() => removeFile(idx)}
                        className="absolute top-2 right-2 w-7 h-7 rounded-full bg-slate-950/70 hover:bg-slate-950 text-white flex items-center justify-center cursor-pointer border-none shadow-md"
                        title="Remove image"
                        disabled={submitting || uploading}
                      >
                        <X size={14} />
                      </button>
                    </div>
                  ))}
                  
                  {/* Add more box */}
                  <button 
                    type="button" 
                    onClick={handleGalleryClick}
                    disabled={submitting || uploading}
                    className="aspect-video rounded-xl border-2 border-dashed border-border hover:border-blue-500 hover:bg-blue-500/5 flex flex-col items-center justify-center gap-1 text-muted-foreground hover:text-blue-600 transition-colors cursor-pointer bg-transparent"
                    title="Add more photos"
                  >
                    <PlusCircle size={20} />
                    <span className="text-[10px] font-bold uppercase">Add Photo</span>
                  </button>
                </div>
              ) : (
                <div 
                  className="min-h-[180px] h-[180px] flex flex-col items-center justify-center border-2 border-dashed border-border rounded-2xl cursor-pointer mb-4 bg-muted/40 transition-all duration-250 p-6 text-center hover:border-blue-500 hover:bg-blue-500/5 dark:hover:bg-blue-500/10 group relative overflow-hidden" 
                  onClick={submitting || uploading ? undefined : handleGalleryClick}
                >
                  {uploading ? (
                    <div className="flex flex-col items-center justify-center gap-2">
                      <Loader2 className="text-blue-600 animate-spin" size={32} />
                      <span className="text-sm font-bold text-foreground mb-1">{uploadProgress || 'Uploading...'}</span>
                    </div>
                  ) : (
                    <>
                      <ImageIcon size={32} className="text-muted-foreground/60 mb-2 transition-colors duration-150 group-hover:text-blue-500" />
                      <span className="text-sm font-bold text-foreground mb-1">Upload Cleaned Photos</span>
                      <span className="text-xs text-muted-foreground/80">Show the resolved state (single or multiple)</span>
                    </>
                  )}
                </div>
              )}
              
              <input 
                type="file" 
                accept="image/*" 
                multiple
                className="hidden" 
                ref={fileInputRef}
                onChange={handleFileChange}
                disabled={uploading || submitting}
              />
              
              {previews.length === 0 && !uploading && (
                <button 
                  type="button" 
                  className="flex items-center justify-center gap-2 p-2.5 border border-blue-500 dark:border-blue-400 rounded-xl cursor-pointer bg-card text-blue-600 dark:text-blue-400 font-bold text-xs transition-all duration-150 hover:bg-blue-500/5 w-full" 
                  onClick={handleGalleryClick}
                >
                  Choose Image Files
                </button>
              )}
            </div>

            {/* Right Column - Remarks & Submission */}
            <div className="flex flex-col gap-4 text-left justify-between h-full">
              <div className="flex flex-col gap-2 flex-grow">
                <label className="text-sm font-semibold text-foreground">Cleanup Action Remarks *</label>
                <textarea
                  className="font-sans w-full p-3 bg-muted/40 border border-border rounded-xl text-sm text-foreground outline-none transition-all focus:bg-muted/80 focus:border-blue-600 focus:ring-2 focus:ring-blue-600/10 placeholder:text-muted-foreground flex-grow min-h-[140px] md:min-h-[180px]"
                  placeholder="e.g. Cleared overflowing waste, swept the sidewalk, and locked the bin correctly."
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  required
                  disabled={submitting}
                  style={{ resize: 'vertical' }}
                />
              </div>

              <div className="pt-2">
                <Button 
                  type="submit" 
                  fullWidth 
                  theme="blue" 
                  disabled={submitting || uploading}
                >
                  <div className="flex items-center justify-center gap-2">
                    {submitting ? (
                      <>
                        <Loader2 className="text-white animate-spin" size={18} />
                        {uploading ? uploadProgress : 'Submitting Resolution Logs...'}
                      </>
                    ) : (
                      <>
                        <CheckCircle size={18} />
                        Submit Resolution Log
                      </>
                    )}
                  </div>
                </Button>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
