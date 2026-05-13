import { useEffect, useState } from 'react';
import { Layout as AntLayout, Menu } from 'antd';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import {
  CheckSquareOutlined, UserOutlined, LogoutOutlined,
  AppstoreOutlined, BarChartOutlined, ProjectOutlined, SettingOutlined,
} from '@ant-design/icons';
import AiProviderBanner from './AiProviderBanner';
import CommandPalette from './CommandPalette';

const { Sider, Content } = AntLayout;

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [paletteOpen, setPaletteOpen] = useState(false);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement).tagName;
      const isInput = tag === 'INPUT' || tag === 'TEXTAREA' || (e.target as HTMLElement).isContentEditable;

      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setPaletteOpen(v => !v);
        return;
      }
      if (isInput || paletteOpen) return;

      if (e.key === 'c') { navigate('/tasks'); }
      else if (e.key === '/') { e.preventDefault(); setPaletteOpen(true); }
      else if (e.key === '?') { setPaletteOpen(true); }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [navigate, paletteOpen]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const menuItems = [
    { key: '/projects', icon: <ProjectOutlined />, label: '프로젝트' },
    { key: '/tasks', icon: <CheckSquareOutlined />, label: '태스크' },
    { key: '/kanban', icon: <AppstoreOutlined />, label: '칸반 보드' },
    { key: '/stats', icon: <BarChartOutlined />, label: '통계' },
    { key: '/profile', icon: <UserOutlined />, label: '프로필' },
    { key: '/settings', icon: <SettingOutlined />, label: '설정' },
    { key: 'logout', icon: <LogoutOutlined />, label: '로그아웃', danger: true },
  ];

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider theme="light" breakpoint="lg" collapsedWidth={0}>
        <div style={{ padding: '20px 16px', fontWeight: 700, fontSize: 18, color: '#1677ff' }}>
          TaskHive
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => key === 'logout' ? handleLogout() : navigate(key)}
        />
      </Sider>
      <AntLayout>
        <AiProviderBanner />
        <Content style={{ padding: 24, background: '#fff' }}>
          <Outlet />
        </Content>
      </AntLayout>

      <CommandPalette open={paletteOpen} onClose={() => setPaletteOpen(false)} />
    </AntLayout>
  );
}
