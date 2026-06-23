import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, Search, Mail, Phone, ChevronDown, ChevronUp, Send, CheckCircle, ArrowLeft, MessageSquare } from 'lucide-react';

interface FAQItem {
  question: string;
  answer: string;
}

export const HelpSupportScreen: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [openIndex, setOpenIndex] = useState<number | null>(null);
  
  // Support Form state
  const [subject, setSubject] = useState('');
  const [message, setMessage] = useState('');
  const [formSubmitted, setFormSubmitted] = useState(false);

  const faqs: FAQItem[] = [
    {
      question: "How to report waste?",
      answer: "Navigate to the 'Report' tab from the bottom navigation bar. Upload a photo of the waste issue using the 'Upload Waste Photo' button, confirm your location on the map, select the appropriate category (e.g., Plastic, Bio-waste, E-waste), add any optional description, and click submit. Your report will be instantly registered."
    },
    {
      question: "How to track report status?",
      answer: "You can track your reports on your dashboard. They will appear under your recent activity feed. There are two primary statuses: 'Pending' (newly reported issues awaiting collection) and 'Completed' (resolved issues cleaned up by supervisors)."
    },
    {
      question: "How to edit profile?",
      answer: "Go to the 'Profile' tab in the bottom navigation. Click on the 'Edit Profile' menu item. Here, you can upload a profile photo, modify your name, DOB, gender, contact number, alternate contact details, and address. Click 'Save Changes' to update and persist your changes."
    },
    {
      question: "How to change password?",
      answer: "In the 'Edit Profile' page, scroll down to the 'Security Settings' card. Enter your Current Password, then type your New Password and confirm it in the Confirm Password field. Click 'Save Changes' to apply the update."
    },
    {
      question: "How to contact support?",
      answer: "You can reach us by email at support@wastereporting.com, call our hotline at +1 (555) 123-4567, or submit a direct query using the message form at the bottom of this page."
    }
  ];

  const handleToggle = (index: number) => {
    setOpenIndex(openIndex === index ? null : index);
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!subject.trim() || !message.trim()) return;

    setFormSubmitted(true);
    setSubject('');
    setMessage('');
    
    setTimeout(() => {
      setFormSubmitted(false);
    }, 4000);
  };

  const filteredFaqs = faqs.filter(faq =>
    faq.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
    faq.answer.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="w-full text-foreground relative animate-slide-up pb-12">
      {/* Page Header */}
      <div className="flex items-center gap-4 mb-6">
        <button 
          className="w-10 h-10 rounded-full flex items-center justify-center bg-muted hover:bg-muted/80 text-foreground transition-colors cursor-pointer border-none animate-fade-in" 
          onClick={() => navigate(-1)}
          aria-label="Go back"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Help & Support</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Find answers to frequently asked questions or get in touch with our team.</p>
        </div>
      </div>

      {/* Grid columns layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 w-full items-start">
        
        {/* Left Column: Contact info & FAQs */}
        <div className="flex flex-col gap-6">
          
          {/* Quick Contact Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="bg-card border border-border rounded-2xl p-4 shadow-sm flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Mail size={20} />
              </div>
              <div className="flex flex-col text-left min-w-0">
                <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Email Support</span>
                <a href="mailto:support@wastereporting.com" className="text-sm font-bold text-foreground hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors truncate">support@wastereporting.com</a>
              </div>
            </div>

            <div className="bg-card border border-border rounded-2xl p-4 shadow-sm flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 dark:bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-600 dark:text-emerald-400">
                <Phone size={20} />
              </div>
              <div className="flex flex-col text-left min-w-0">
                <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Helpline</span>
                <a href="tel:+15551234567" className="text-sm font-bold text-foreground hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors truncate">+1 (555) 123-4567</a>
              </div>
            </div>
          </div>

          {/* FAQs Container */}
          <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex flex-col gap-4 text-left">
            <h2 className="text-base font-bold text-emerald-600 dark:text-emerald-400 border-l-4 border-emerald-500 pl-2.5">Frequently Asked Questions</h2>
            
            <div className="relative flex items-center w-full">
              <Search size={18} className="absolute left-4 text-muted-foreground" />
              <input 
                type="text" 
                className="w-full h-11 bg-background border border-border rounded-xl pl-11 pr-4 text-sm text-foreground outline-none transition-all focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/10" 
                placeholder="Search help topics..." 
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
            </div>

            <div className="flex flex-col border border-border rounded-xl overflow-hidden divide-y divide-border/60">
              {filteredFaqs.length > 0 ? (
                filteredFaqs.map((faq, index) => (
                  <div key={index} className="bg-card">
                    <button 
                      className="w-full p-4 bg-transparent border-none cursor-pointer flex justify-between items-center text-left transition-colors hover:bg-muted/40" 
                      onClick={() => handleToggle(index)}
                      aria-expanded={openIndex === index}
                    >
                      <span className="text-sm font-bold text-foreground pr-2">{faq.question}</span>
                      {openIndex === index ? <ChevronUp size={18} className="text-muted-foreground" /> : <ChevronDown size={18} className="text-muted-foreground" />}
                    </button>
                    {openIndex === index && (
                      <div className="px-4 pb-4 bg-transparent text-left animate-fade-in">
                        <p className="text-xs text-muted-foreground leading-relaxed">{faq.answer}</p>
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <div className="p-6 text-center text-muted-foreground text-sm">
                  <span>No results match your search query.</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Contact form */}
        <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex flex-col gap-4 text-left">
          <h2 className="text-base font-bold text-emerald-600 dark:text-emerald-400 border-l-4 border-emerald-500 pl-2.5 flex items-center gap-2">
            <MessageSquare size={18} /> Send a Message
          </h2>
          <p className="text-xs text-muted-foreground -mt-2">Got a question, suggestion, or complaint? Submit the form below and our support team will handle it shortly.</p>
          
          {formSubmitted && (
            <div className="flex items-center p-3.5 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 rounded-xl text-xs font-semibold text-left">
              <CheckCircle size={20} className="mr-2 shrink-0 animate-fade-in" />
              <span>Your message has been sent successfully! Our team will contact you soon.</span>
            </div>
          )}

          <form onSubmit={handleFormSubmit} className="flex flex-col gap-4 text-left">
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-muted-foreground">Subject</label>
              <input 
                type="text" 
                className="bg-background border border-border rounded-xl h-11 px-4 text-sm text-foreground outline-none transition-all focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/10" 
                placeholder="e.g. Location detection issue"
                value={subject}
                onChange={e => setSubject(e.target.value)}
                required
              />
            </div>
            
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-muted-foreground">Message Details</label>
              <textarea 
                className="bg-background border border-border rounded-xl p-3 text-sm text-foreground outline-none transition-all focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/10 resize-y" 
                placeholder="Describe your issue or suggestions in detail..."
                rows={5}
                value={message}
                onChange={e => setMessage(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="h-11 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold border-none cursor-pointer flex items-center justify-center transition-all shadow-md shadow-emerald-600/15 active:scale-98 mt-2">
              <Send size={16} className="mr-2" />
              Submit Message
            </button>
          </form>
        </div>

      </div>
    </div>
  );
};
