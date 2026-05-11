import { Layout as AntLayout, Menu, Button, theme } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Sider, Content } = AntLayout;

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();

  const menuItems = [
    { key: '/tasks', label: '태스크 목록' },
    { key: '/profile', label: '내 정보' },
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider theme="light" style={{ borderRight: `1px solid ${token.colorBorderSecondary}` }}>
        <div style={{ padding: '16px', fontWeight: 700, fontSize: 18, color: token.colorPrimary }}>
          TaskHive
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <AntLayout>
        <Header style={{ background: token.colorBgContainer, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', padding: '0 24px', borderBottom: `1px solid ${token.colorBorderSecondary}` }}>
          <Button onClick={handleLogout}>로그아웃</Button>
        </Header>
        <Content style={{ margin: 24 }}>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
}
