import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, Spin } from 'antd';
import { useThemeContext } from './contexts/ThemeContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

const TasksPage   = lazy(() => import('./pages/TasksPage'));
const KanbanPage  = lazy(() => import('./pages/KanbanPage'));
const StatsPage   = lazy(() => import('./pages/StatsPage'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));

const fallback = <Spin size="large" style={{ display: 'block', margin: '80px auto' }} />;

export default function App() {
  const { algorithm } = useThemeContext();
  return (
    <ConfigProvider theme={{ algorithm }}>
    <Routes>
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<PrivateRoute />}>
        <Route element={<Layout />}>
          <Route path="/tasks"   element={<Suspense fallback={fallback}><TasksPage /></Suspense>} />
          <Route path="/kanban"  element={<Suspense fallback={fallback}><KanbanPage /></Suspense>} />
          <Route path="/stats"   element={<Suspense fallback={fallback}><StatsPage /></Suspense>} />
          <Route path="/profile" element={<Suspense fallback={fallback}><ProfilePage /></Suspense>} />
          <Route path="/"        element={<Navigate to="/tasks" replace />} />
        </Route>
      </Route>
    </Routes>
    </ConfigProvider>
  );
}
