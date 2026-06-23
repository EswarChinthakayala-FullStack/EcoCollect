import React, { useState, useEffect, useRef, useCallback } from 'react';
import { createPortal } from 'react-dom';
import { ChevronLeft, ChevronRight, Maximize2, Download, X, Play, Pause, ZoomIn, ZoomOut, RotateCcw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface ImageGalleryProps {
  images: string | string[] | null | undefined;
  autoPlayInterval?: number; // ms, default 4000
}

/* ───────────────────────────────────────────
   Full-screen Lightbox (rendered via Portal)
   ─────────────────────────────────────────── */
interface LightboxProps {
  images: string[];
  startIndex: number;
  onClose: () => void;
}

const FullScreenLightbox: React.FC<LightboxProps> = ({ images, startIndex, onClose }) => {
  const [currentIndex, setCurrentIndex] = useState(startIndex);
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [isAnimatingIn, setIsAnimatingIn] = useState(true);
  const [isAnimatingOut, setIsAnimatingOut] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<HTMLImageElement>(null);
  const thumbnailContainerRef = useRef<HTMLDivElement>(null);
  const touchStartRef = useRef<{ x: number; y: number; dist: number; zoom: number }>({ x: 0, y: 0, dist: 0, zoom: 1 });

  // Entrance animation
  useEffect(() => {
    const raf = requestAnimationFrame(() => setIsAnimatingIn(false));
    return () => cancelAnimationFrame(raf);
  }, []);

  // Lock body scroll
  useEffect(() => {
    const original = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = original; };
  }, []);

  // Reset zoom/pan on image change
  useEffect(() => {
    setZoom(1);
    setPan({ x: 0, y: 0 });
    setImageLoaded(false);
  }, [currentIndex]);

  // Scroll active thumbnail into view
  useEffect(() => {
    if (thumbnailContainerRef.current) {
      const active = thumbnailContainerRef.current.children[currentIndex] as HTMLElement | undefined;
      active?.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
    }
  }, [currentIndex]);

  const handleClose = useCallback(() => {
    setIsAnimatingOut(true);
    setTimeout(() => onClose(), 250);
  }, [onClose]);

  const goTo = useCallback((idx: number) => {
    setCurrentIndex((idx + images.length) % images.length);
  }, [images.length]);

  const goPrev = useCallback(() => goTo(currentIndex - 1), [goTo, currentIndex]);
  const goNext = useCallback(() => goTo(currentIndex + 1), [goTo, currentIndex]);

  const zoomIn = useCallback(() => {
    setZoom(z => Math.min(z + 0.5, 5));
  }, []);

  const zoomOut = useCallback(() => {
    setZoom(z => {
      const next = Math.max(z - 0.5, 1);
      if (next === 1) setPan({ x: 0, y: 0 });
      return next;
    });
  }, []);

  const resetZoom = useCallback(() => {
    setZoom(1);
    setPan({ x: 0, y: 0 });
  }, []);

  // Keyboard navigation
  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') handleClose();
      else if (e.key === 'ArrowLeft') goPrev();
      else if (e.key === 'ArrowRight') goNext();
      else if (e.key === '+' || e.key === '=') zoomIn();
      else if (e.key === '-') zoomOut();
      else if (e.key === '0') resetZoom();
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [handleClose, goPrev, goNext, zoomIn, zoomOut, resetZoom]);

  // Mouse wheel zoom
  const handleWheel = useCallback((e: React.WheelEvent) => {
    e.preventDefault();
    if (e.deltaY < 0) zoomIn();
    else zoomOut();
  }, [zoomIn, zoomOut]);

  // Mouse drag for panning when zoomed
  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    if (zoom <= 1) return;
    e.preventDefault();
    setIsDragging(true);
    setDragStart({ x: e.clientX - pan.x, y: e.clientY - pan.y });
  }, [zoom, pan]);

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (!isDragging) return;
    setPan({ x: e.clientX - dragStart.x, y: e.clientY - dragStart.y });
  }, [isDragging, dragStart]);

  const handleMouseUp = useCallback(() => { setIsDragging(false); }, []);

  // Touch: swipe navigation + pinch-to-zoom
  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    if (e.touches.length === 2) {
      const dx = e.touches[0].clientX - e.touches[1].clientX;
      const dy = e.touches[0].clientY - e.touches[1].clientY;
      touchStartRef.current = {
        x: 0, y: 0,
        dist: Math.hypot(dx, dy),
        zoom
      };
    } else if (e.touches.length === 1) {
      touchStartRef.current = {
        x: e.touches[0].clientX,
        y: e.touches[0].clientY,
        dist: 0,
        zoom
      };
    }
  }, [zoom]);

  const handleTouchEnd = useCallback((e: React.TouchEvent) => {
    if (e.changedTouches.length === 1 && zoom <= 1) {
      const dx = e.changedTouches[0].clientX - touchStartRef.current.x;
      const dy = e.changedTouches[0].clientY - touchStartRef.current.y;
      if (Math.abs(dx) > 60 && Math.abs(dy) < 80) {
        if (dx > 0) goPrev();
        else goNext();
      }
    }
  }, [zoom, goPrev, goNext]);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (e.touches.length === 2) {
      const dx = e.touches[0].clientX - e.touches[1].clientX;
      const dy = e.touches[0].clientY - e.touches[1].clientY;
      const dist = Math.hypot(dx, dy);
      const scale = dist / (touchStartRef.current.dist || 1);
      setZoom(Math.min(Math.max(touchStartRef.current.zoom * scale, 1), 5));
    }
  }, []);

  const downloadImage = async (url: string) => {
    try {
      const filename = url.split('/').pop() || 'waste_report_image.jpg';
      const response = await fetch(url);
      const blob = await response.blob();
      const blobUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = blobUrl;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(blobUrl);
    } catch {
      window.open(url, '_blank');
    }
  };

  const overlayStyle: React.CSSProperties = {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100vw',
    height: '100vh',
    zIndex: 99999,
    display: 'flex',
    flexDirection: 'column',
    background: 'rgba(0,0,0,0.92)',
    backdropFilter: 'blur(24px)',
    WebkitBackdropFilter: 'blur(24px)',
    opacity: isAnimatingIn ? 0 : isAnimatingOut ? 0 : 1,
    transition: 'opacity 0.25s ease',
    userSelect: 'none',
  };

  const imageTransform: React.CSSProperties = {
    transform: `scale(${zoom}) translate(${pan.x / zoom}px, ${pan.y / zoom}px)`,
    transition: isDragging ? 'none' : 'transform 0.25s cubic-bezier(.4,0,.2,1)',
    cursor: zoom > 1 ? (isDragging ? 'grabbing' : 'grab') : 'default',
    maxWidth: '100%',
    maxHeight: '100%',
    objectFit: 'contain' as const,
    borderRadius: '8px',
    opacity: imageLoaded ? 1 : 0,
  };

  return createPortal(
    <div ref={containerRef} style={overlayStyle} role="dialog" aria-modal="true" aria-label="Image viewer">
      {/* ─── Top toolbar ─── */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '12px 16px', flexShrink: 0,
        background: 'linear-gradient(180deg, rgba(0,0,0,0.6) 0%, transparent 100%)',
      }}>
        <span style={{ color: 'rgba(255,255,255,0.7)', fontSize: '13px', fontWeight: 600, letterSpacing: '0.02em' }}>
          {currentIndex + 1} / {images.length}
        </span>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {/* Zoom controls */}
          <button onClick={zoomOut} title="Zoom out (−)" style={toolbarBtnStyle} aria-label="Zoom out">
            <ZoomOut size={18} />
          </button>
          <span style={{ color: 'rgba(255,255,255,0.6)', fontSize: '12px', fontWeight: 600, minWidth: '40px', textAlign: 'center' }}>
            {Math.round(zoom * 100)}%
          </span>
          <button onClick={zoomIn} title="Zoom in (+)" style={toolbarBtnStyle} aria-label="Zoom in">
            <ZoomIn size={18} />
          </button>
          {zoom > 1 && (
            <button onClick={resetZoom} title="Reset zoom (0)" style={toolbarBtnStyle} aria-label="Reset zoom">
              <RotateCcw size={16} />
            </button>
          )}

          <div style={{ width: '1px', height: '24px', background: 'rgba(255,255,255,0.15)', margin: '0 4px' }} />

          <button onClick={() => downloadImage(images[currentIndex])} title="Download" style={toolbarBtnStyle} aria-label="Download image">
            <Download size={18} />
          </button>
          <button onClick={handleClose} title="Close (Esc)" style={{ ...toolbarBtnStyle, background: 'rgba(255,255,255,0.15)' }} aria-label="Close viewer">
            <X size={20} />
          </button>
        </div>
      </div>

      {/* ─── Main image area ─── */}
      <div
        style={{
          flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
          position: 'relative', overflow: 'hidden', touchAction: 'none',
          minHeight: 0,
        }}
        onWheel={handleWheel}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        onClick={(e) => {
          if (e.target === e.currentTarget && zoom <= 1) handleClose();
        }}
      >
        {/* Loading spinner */}
        {!imageLoaded && (
          <div style={{
            position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <div style={{
              width: '40px', height: '40px', border: '3px solid rgba(255,255,255,0.15)',
              borderTopColor: 'rgba(255,255,255,0.7)', borderRadius: '50%',
              animation: 'lightbox-spin 0.8s linear infinite',
            }} />
          </div>
        )}

        <img
          ref={imageRef}
          key={currentIndex}
          src={images[currentIndex]}
          alt={`Image ${currentIndex + 1} of ${images.length}`}
          style={imageTransform}
          onLoad={() => setImageLoaded(true)}
          draggable={false}
          onDoubleClick={() => {
            if (zoom > 1) resetZoom();
            else zoomIn();
          }}
        />

        {/* Nav arrows */}
        {images.length > 1 && zoom <= 1 && (
          <>
            <button
              onClick={(e) => { e.stopPropagation(); goPrev(); }}
              style={{ ...navArrowStyle, left: '12px' }}
              aria-label="Previous image"
            >
              <ChevronLeft size={28} />
            </button>
            <button
              onClick={(e) => { e.stopPropagation(); goNext(); }}
              style={{ ...navArrowStyle, right: '12px' }}
              aria-label="Next image"
            >
              <ChevronRight size={28} />
            </button>
          </>
        )}
      </div>

      {/* ─── Thumbnails strip ─── */}
      {images.length > 1 && (
        <div style={{
          flexShrink: 0, padding: '10px 16px 14px',
          background: 'linear-gradient(0deg, rgba(0,0,0,0.6) 0%, transparent 100%)',
        }}>
          <div
            ref={thumbnailContainerRef}
            style={{
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              gap: '8px', overflowX: 'auto', padding: '4px 0',
              scrollbarWidth: 'none',
            }}
          >
            {images.map((img, idx) => (
              <button
                key={idx}
                onClick={() => goTo(idx)}
                style={{
                  flex: '0 0 auto',
                  width: idx === currentIndex ? '56px' : '48px',
                  height: idx === currentIndex ? '56px' : '48px',
                  borderRadius: '10px',
                  overflow: 'hidden',
                  border: idx === currentIndex ? '2px solid rgba(255,255,255,0.9)' : '2px solid rgba(255,255,255,0.15)',
                  opacity: idx === currentIndex ? 1 : 0.5,
                  transition: 'all 0.25s ease',
                  cursor: 'pointer',
                  padding: 0,
                  background: 'transparent',
                  boxShadow: idx === currentIndex ? '0 0 0 3px rgba(255,255,255,0.2)' : 'none',
                }}
                aria-label={`Go to image ${idx + 1}`}
              >
                <img
                  src={img}
                  alt={`Thumbnail ${idx + 1}`}
                  style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
                  draggable={false}
                />
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Inline keyframe for the spinner */}
      <style>{`
        @keyframes lightbox-spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>,
    document.body
  );
};

/* ─── Shared styles ─── */
const toolbarBtnStyle: React.CSSProperties = {
  width: '36px', height: '36px', borderRadius: '50%', border: 'none',
  background: 'rgba(255,255,255,0.1)', color: '#fff', cursor: 'pointer',
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  transition: 'background 0.2s ease',
};

const navArrowStyle: React.CSSProperties = {
  position: 'absolute', top: '50%', transform: 'translateY(-50%)',
  width: '48px', height: '48px', borderRadius: '50%', border: 'none',
  background: 'rgba(255,255,255,0.1)', color: '#fff', cursor: 'pointer',
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  transition: 'background 0.2s ease, transform 0.15s ease',
  backdropFilter: 'blur(8px)',
};


/* ─────────────────────────────────────────────────────
   Carousel Gallery (inline – the visible card preview)
   ───────────────────────────────────────────────────── */
export const ImageGalleryCarousel: React.FC<ImageGalleryProps> = ({
  images,
  autoPlayInterval = 4000
}) => {
  const { backendHost } = useAuth();
  const [imageList, setImageList] = useState<string[]>([]);
  const [activeIndex, setActiveIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const [lightboxStartIndex, setLightboxStartIndex] = useState(0);

  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Parse images prop into list of complete URLs
  useEffect(() => {
    if (!images) {
      setImageList([]);
      return;
    }
    let parsed: string[] = [];
    if (Array.isArray(images)) {
      parsed = images;
    } else if (typeof images === 'string') {
      if (images.trim().startsWith('[')) {
        try {
          parsed = JSON.parse(images) as string[];
        } catch {
          parsed = images.split(',').map(s => s.trim()).filter(Boolean);
        }
      } else {
        parsed = images.split(',').map(s => s.trim()).filter(Boolean);
      }
    }

    const urls = parsed.map(img => {
      if (img.startsWith('http') || img.startsWith('data:')) return img;
      return `${backendHost}${img}`;
    });
    setImageList(urls);
    setActiveIndex(0);
  }, [images, backendHost]);

  // Autoplay
  useEffect(() => {
    if (!isPlaying || imageList.length <= 1 || lightboxOpen) {
      if (timerRef.current) { clearInterval(timerRef.current); timerRef.current = null; }
      return;
    }
    timerRef.current = setInterval(() => {
      setActiveIndex(prev => (prev + 1) % imageList.length);
    }, autoPlayInterval);
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [isPlaying, imageList.length, autoPlayInterval, lightboxOpen]);

  if (imageList.length === 0) return null;

  const handlePrev = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    setActiveIndex(prev => (prev - 1 + imageList.length) % imageList.length);
  };

  const handleNext = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    setActiveIndex(prev => (prev + 1) % imageList.length);
  };

  const openLightbox = (index: number) => {
    setLightboxStartIndex(index);
    setLightboxOpen(true);
  };

  return (
    <div className="relative w-full rounded-2xl overflow-hidden bg-muted group shadow-sm border border-border">
      {/* Slides wrapper */}
      <div className="relative h-[280px] md:h-[320px] w-full flex items-center justify-center overflow-hidden">
        {imageList.map((img, idx) => (
          <div
            key={idx}
            className={`absolute inset-0 transition-all duration-700 ease-in-out flex items-center justify-center ${
              idx === activeIndex ? 'opacity-100 scale-100 z-10' : 'opacity-0 scale-95 z-0 pointer-events-none'
            }`}
          >
            <img
              src={img}
              alt={`Report Slide ${idx + 1}`}
              className="w-full h-full object-cover cursor-pointer select-none"
              onClick={() => openLightbox(idx)}
            />
          </div>
        ))}

        {/* Prev/Next controls */}
        {imageList.length > 1 && (
          <>
            <button
              onClick={handlePrev}
              className="absolute left-3 w-9 h-9 rounded-full flex items-center justify-center bg-black/35 hover:bg-black/60 text-white transition-all opacity-0 group-hover:opacity-100 z-20 cursor-pointer border-none"
              aria-label="Previous image"
            >
              <ChevronLeft size={20} />
            </button>
            <button
              onClick={handleNext}
              className="absolute right-3 w-9 h-9 rounded-full flex items-center justify-center bg-black/35 hover:bg-black/60 text-white transition-all opacity-0 group-hover:opacity-100 z-20 cursor-pointer border-none"
              aria-label="Next image"
            >
              <ChevronRight size={20} />
            </button>
          </>
        )}

        {/* Maximize */}
        <button
          onClick={() => openLightbox(activeIndex)}
          className="absolute top-3 right-3 w-8 h-8 rounded-lg bg-black/35 hover:bg-black/60 text-white flex items-center justify-center transition-all opacity-0 group-hover:opacity-100 z-20 cursor-pointer border-none"
          title="View fullscreen"
        >
          <Maximize2 size={16} />
        </button>

        {/* Play/Pause */}
        {imageList.length > 1 && (
          <button
            onClick={() => setIsPlaying(!isPlaying)}
            className="absolute top-3 left-3 w-8 h-8 rounded-lg bg-black/35 hover:bg-black/60 text-white flex items-center justify-center transition-all opacity-0 group-hover:opacity-100 z-20 cursor-pointer border-none"
            title={isPlaying ? 'Pause Auto Scroll' : 'Start Auto Scroll'}
          >
            {isPlaying ? <Pause size={14} /> : <Play size={14} />}
          </button>
        )}

        {/* Counter badge */}
        <div className="absolute bottom-3 right-3 px-2 py-0.5 rounded bg-black/50 text-[10px] font-bold text-white z-20 select-none">
          {activeIndex + 1} / {imageList.length}
        </div>
      </div>

      {/* Dots */}
      {imageList.length > 1 && (
        <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex items-center gap-1.5 z-20">
          {imageList.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setActiveIndex(idx)}
              className={`h-1.5 rounded-full transition-all duration-300 cursor-pointer border-none p-0 ${
                idx === activeIndex ? 'w-4 bg-white' : 'w-1.5 bg-white/50 hover:bg-white/85'
              }`}
              aria-label={`Go to slide ${idx + 1}`}
            />
          ))}
        </div>
      )}

      {/* Lightbox via Portal */}
      {lightboxOpen && (
        <FullScreenLightbox
          images={imageList}
          startIndex={lightboxStartIndex}
          onClose={() => setLightboxOpen(false)}
        />
      )}
    </div>
  );
};
