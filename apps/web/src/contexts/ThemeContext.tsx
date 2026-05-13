import { createContext, useContext, useState, type ReactNode } from 'react';
import { theme } from 'antd';

type ThemeMode = 'light' | 'dark';

interface ThemeContextValue {
  isDark: boolean;
  algorithm: typeof theme.defaultAlgorithm;
  toggle: () => void;
}

const ThemeContext = createContext<ThemeContextValue | null>(null);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<ThemeMode>(
    () => (localStorage.getItem('theme') as ThemeMode) ?? 'light'
  );

  const toggle = () => {
    setMode((prev) => {
      const next = prev === 'light' ? 'dark' : 'light';
      localStorage.setItem('theme', next);
      return next;
    });
  };

  return (
    <ThemeContext.Provider value={{
      isDark: mode === 'dark',
      algorithm: mode === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm,
      toggle,
    }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useThemeContext() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useThemeContext must be inside ThemeProvider');
  return ctx;
}
