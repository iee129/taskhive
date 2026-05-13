import { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import WakingUp from './components/WakingUp';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import TasksPage from './pages/TasksPage';
import KanbanPage from './pages/KanbanPage';
import StatsPage from './pages/StatsPage';
import ProfilePage from './pages/ProfilePage';
import VerifyEmailPage from './pages/VerifyEmailPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ProjectsPage from './pages/ProjectsPage';
import SettingsPage from './pages/SettingsPage';

const DARK_MODE_KEY = 'taskhive_dark_mode';

export default function App() {
  const [isDark, setIsDark] = useState<boolean>(
    () => localStorage.getItem(DARK_MODE_KEY) === 'true'
  );

  const toggleDark = () => {
    setIsDark(prev => {
      const next = !prev;
      localStorage.setItem(DARK_MODE_KEY, String(next));
      return next;
    });
  };

  return (
    <ConfigProvider
      theme={{ algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm }}
    >
      <WakingUp>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/verify-email" element={<VerifyEmailPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route element={<PrivateRoute />}>
              <Route element={<Layout isDark={isDark} onToggleDark={toggleDark} />}>
                <Route path="/projects" element={<ProjectsPage />} />
                <Route path="/tasks" element={<TasksPage />} />
                <Route path="/kanban" element={<KanbanPage />} />
                <Route path="/stats" element={<StatsPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/settings" element={<SettingsPage />} />
                <Route path="/" element={<Navigate to="/tasks" replace />} />
              </Route>
            </Route>
          </Routes>
        </BrowserRouter>
      </WakingUp>
    </ConfigProvider>
  );
}
