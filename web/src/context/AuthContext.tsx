import React, { createContext, useContext, useState, useEffect } from 'react';

export interface User {
  id: number;
  email: string;
  full_name: string;
  phone?: string;
  dob?: string;
  gender?: string;
  address?: string;
  city?: string;
  country?: string;
  profile_image_url?: string;
  role: 'citizen' | 'supervisor' | 'admin';
  eco_points: number;
  created_at: string;
  updated_at: string;
}

export interface SupervisorDetails {
  id: number;
  employee_id: string;
  user_id: number;
  assigned_area?: string;
  created_at: string;
  name?: string;
  email?: string;
  phone?: string;
  profile_picture?: string;
  performance_score?: number;
}

interface AuthContextType {
  user: User | null;
  supervisor: SupervisorDetails | null;
  token: string | null;
  role: 'citizen' | 'supervisor' | 'admin' | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string, requiredRole: 'citizen' | 'admin') => Promise<void>;
  loginSupervisor: (employeeId: string, password: string) => Promise<void>;
  registerCitizen: (fullName: string, email: string, phone: string, dob: string, password: string) => Promise<void>;
  logout: () => void;
  apiCall: <T = any>(path: string, options?: RequestInit) => Promise<T>;
  backendHost: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [supervisor, setSupervisor] = useState<SupervisorDetails | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [role, setRole] = useState<'citizen' | 'supervisor' | 'admin' | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const backendHost = import.meta.env.VITE_BACKEND_URL || `http://${window.location.hostname}:8000`;
  const apiBase = `${backendHost}/api`;

  // Restore session
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedRole = localStorage.getItem('role') as 'citizen' | 'supervisor' | 'admin' | null;
    const storedUser = localStorage.getItem('user');
    const storedSupervisor = localStorage.getItem('supervisor');

    if (storedToken && storedRole) {
      setToken(storedToken);
      setRole(storedRole);
      if (storedUser) {
        setUser(JSON.parse(storedUser));
      }
      if (storedSupervisor) {
        setSupervisor(JSON.parse(storedSupervisor));
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string, requiredRole: 'citizen' | 'admin') => {
    setIsLoading(true);
    try {
      const res = await fetch(`${apiBase}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.detail || 'Incorrect email or password');
      }

      const data = await res.json();
      
      // Verify role
      if (data.user.role !== requiredRole) {
        throw new Error(`Unauthorized. You do not have access as a ${requiredRole}.`);
      }

      setToken(data.token);
      setRole(data.user.role);
      setUser(data.user);
      setSupervisor(null);

      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.user.role);
      localStorage.setItem('user', JSON.stringify(data.user));
      localStorage.removeItem('supervisor');
    } catch (error) {
      setIsLoading(false);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const loginSupervisor = async (employeeId: string, password: string) => {
    setIsLoading(true);
    try {
      const res = await fetch(`${apiBase}/auth/supervisor/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ employee_id: employeeId, password }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.detail || 'Incorrect Employee ID or password');
      }

      const data = await res.json();

      setToken(data.token);
      setRole('supervisor');
      setSupervisor(data.supervisor);
      
      // Build a user object for convenience
      const mockUser: User = {
        id: data.supervisor.user_id,
        email: data.supervisor.email,
        full_name: data.supervisor.name || 'Supervisor',
        phone: data.supervisor.phone,
        role: 'supervisor',
        eco_points: 0,
        profile_image_url: data.supervisor.profile_picture,
        created_at: data.supervisor.created_at,
        updated_at: data.supervisor.created_at,
      };
      setUser(mockUser);

      localStorage.setItem('token', data.token);
      localStorage.setItem('role', 'supervisor');
      localStorage.setItem('user', JSON.stringify(mockUser));
      localStorage.setItem('supervisor', JSON.stringify(data.supervisor));
    } catch (error) {
      setIsLoading(false);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const registerCitizen = async (fullName: string, email: string, phone: string, dob: string, password: string) => {
    setIsLoading(true);
    try {
      const res = await fetch(`${apiBase}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          full_name: fullName,
          email,
          phone,
          dob,
          password
        }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.detail || 'Registration failed');
      }

      const data = await res.json();

      setToken(data.token);
      setRole('citizen');
      setUser(data.user);
      setSupervisor(null);

      localStorage.setItem('token', data.token);
      localStorage.setItem('role', 'citizen');
      localStorage.setItem('user', JSON.stringify(data.user));
      localStorage.removeItem('supervisor');
    } catch (error) {
      setIsLoading(false);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setRole(null);
    setUser(null);
    setSupervisor(null);

    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('user');
    localStorage.removeItem('supervisor');
  };

  const apiCall = async <T = any>(path: string, options: RequestInit = {}): Promise<T> => {
    const headers = new Headers(options.headers || {});
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    const res = await fetch(`${apiBase}${path}`, {
      ...options,
      headers,
    });

    if (res.status === 401) {
      logout();
      throw new Error('Session expired. Please log in again.');
    }

    if (!res.ok) {
      const err = await res.json().catch(() => ({ detail: 'An error occurred' }));
      throw new Error(err.detail || `Request failed with status ${res.status}`);
    }

    return res.json();
  };

  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider
      value={{
        user,
        supervisor,
        token,
        role,
        isAuthenticated,
        isLoading,
        login,
        loginSupervisor,
        registerCitizen,
        logout,
        apiCall,
        backendHost,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
