import { Form, Input, Button, Card, Typography, message } from 'antd';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../api/auth';
import type { RegisterRequest } from '../types/auth';
import { useCheckEmail } from '../hooks/useCheckEmail';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const { validate: validateEmail, checking } = useCheckEmail();

  const onFinish = async (values: RegisterRequest) => {
    try {
      await register(values);
      messageApi.success('인증 이메일을 발송했습니다. 이메일을 확인한 후 로그인해주세요.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      const msg = err.response?.data?.message ?? '회원가입에 실패했습니다';
      messageApi.error(msg);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
      {contextHolder}
      <Card style={{ width: 400 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>회원가입</Typography.Title>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="name" label="이름" rules={[{ required: true, min: 2, max: 50 }]}>
            <Input />
          </Form.Item>
          <Form.Item
            name="email"
            label="이메일"
            rules={[
              { required: true, type: 'email' },
              { validator: validateEmail },
            ]}
            validateTrigger="onBlur"
          >
            <Input disabled={checking} />
          </Form.Item>
          <Form.Item name="password" label="비밀번호" rules={[{ required: true, min: 8 }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>회원가입</Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </div>
      </Card>
    </div>
  );
}
