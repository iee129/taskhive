import { Form, Input, Button, Card, Typography, message, Result } from 'antd';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { forgotPassword } from '../api/auth';

export default function ForgotPasswordPage() {
  const [sent, setSent] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const onFinish = async (values: { email: string }) => {
    try {
      await forgotPassword(values.email);
      setSent(true);
    } catch {
      messageApi.error('요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
  };

  if (sent) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
        <Card style={{ width: 400 }}>
          <Result
            status="success"
            title="이메일 발송 완료"
            subTitle="비밀번호 재설정 링크를 이메일로 발송했습니다. 이메일함을 확인해주세요."
            extra={<Button><Link to="/login">로그인 페이지로</Link></Button>}
          />
        </Card>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
      {contextHolder}
      <Card style={{ width: 400 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>비밀번호 찾기</Typography.Title>
        <Typography.Paragraph style={{ textAlign: 'center', color: '#666' }}>
          가입한 이메일 주소를 입력하시면 비밀번호 재설정 링크를 보내드립니다.
        </Typography.Paragraph>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="email" label="이메일" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>재설정 링크 발송</Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          <Link to="/login">로그인으로 돌아가기</Link>
        </div>
      </Card>
    </div>
  );
}
