import { Form, Input, Button, Card, Typography, message } from 'antd';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../api/auth';
import type { AuthRequest } from '../types/auth';

export default function LoginPage() {
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();

  const onFinish = async (values: AuthRequest) => {
    try {
      const res = await login(values);
      localStorage.setItem('token', res.token!);
      navigate('/tasks');
    } catch {
      messageApi.error('이메일 또는 비밀번호가 올바르지 않습니다');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
      {contextHolder}
      <Card style={{ width: 400 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>로그인</Typography.Title>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="email" label="이메일" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="비밀번호" rules={[{ required: true }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>로그인</Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </div>
      </Card>
    </div>
  );
}
