import { Layout as AntLayout, Menu } from 'antd';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import {
  CheckSquareOutlined, UserOutlined, LogoutOutlined,
  AppstoreOutlined, BarChartOutlined,
} from '@ant-design/icons';

const { Sider, Content } = AntLayout;

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const menuItems = [
    { key: '/tasks', icon: <CheckSquareOutlined />, label: '태스크' },
    { key: '/kanban', icon: <AppstoreOutlined />, label: '칸반 보드' },
    { key: '/stats', icon: <BarChartOutlined />, label: '통계' },
    { key: '/profile', icon: <UserOutlined />, label: '프로필' },
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
        <Content style={{ padding: 24, background: '#fff' }}>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
}
