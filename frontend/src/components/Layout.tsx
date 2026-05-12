import { useState } from 'react';
import { Layout as AntLayout, Menu, Drawer, Button, Grid } from 'antd';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import {
  CheckSquareOutlined, UserOutlined, LogoutOutlined,
  AppstoreOutlined, BarChartOutlined, MenuOutlined,
} from '@ant-design/icons';
import ThemeToggle from './ThemeToggle';

const { Sider, Content, Header } = AntLayout;
const { useBreakpoint } = Grid;

const menuItems = [
  { key: '/tasks',   icon: <CheckSquareOutlined />, label: '태스크' },
  { key: '/kanban',  icon: <AppstoreOutlined />,    label: '칸반 보드' },
  { key: '/stats',   icon: <BarChartOutlined />,    label: '통계' },
  { key: '/profile', icon: <UserOutlined />,        label: '프로필' },
  { key: 'logout',   icon: <LogoutOutlined />,      label: '로그아웃', danger: true },
];

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const screens = useBreakpoint();
  const [drawerOpen, setDrawerOpen] = useState(false);

  const isMobile = !screens.lg;

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      localStorage.removeItem('token');
      navigate('/login');
    } else {
      navigate(key);
    }
    setDrawerOpen(false);
  };

  const nav = (
    <Menu
      mode="inline"
      selectedKeys={[location.pathname]}
      items={menuItems}
      onClick={handleMenuClick}
      aria-label="주요 메뉴"
    />
  );

  if (isMobile) {
    return (
      <AntLayout style={{ minHeight: '100vh' }}>
        <Header style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '0 16px' }}>
          <Button
            type="text"
            icon={<MenuOutlined />}
            onClick={() => setDrawerOpen(true)}
            aria-label="메뉴 열기"
            style={{ color: '#fff' }}
          />
          <span style={{ fontWeight: 700, fontSize: 18, color: '#fff', flex: 1 }}>TaskHive</span>
          <ThemeToggle />
        </Header>
        <Drawer
          title="TaskHive"
          placement="left"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          styles={{ body: { padding: 0 } }}
          width={240}
        >
          {nav}
        </Drawer>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </AntLayout>
    );
  }

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider theme="light">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '20px 16px 8px' }}>
          <span style={{ fontWeight: 700, fontSize: 18, color: '#1677ff' }}>TaskHive</span>
          <ThemeToggle />
        </div>
        {nav}
      </Sider>
      <AntLayout>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
}
