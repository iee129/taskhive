import { Form, Input, Button, Card, Typography, message, Result } from 'antd';
import { useSearchParams, Link } from 'react-router-dom';
import { useState } from 'react';
import { resetPassword } from '../api/auth';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const [done, setDone] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const token = searchParams.get('token') ?? '';

  const onFinish = async (values: { newPassword: string }) => {
    try {
      await resetPassword(token, values.newPassword);
      setDone(true);
    } catch (err: any) {
      const msg = err.response?.data?.message ?? '비밀번호 재설정에 실패했습니다.';
      messageApi.error(msg);
    }
  };

  if (done) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
        <Card style={{ width: 400 }}>
          <Result
            status="success"
            title="비밀번호 변경 완료"
            subTitle="새 비밀번호로 로그인해주세요."
            extra={<Button type="primary"><Link to="/login">로그인하기</Link></Button>}
          />
        </Card>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
      {contextHolder}
      <Card style={{ width: 400 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>비밀번호 재설정</Typography.Title>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item
            name="newPassword"
            label="새 비밀번호"
            rules={[{ required: true, min: 8, message: '8자 이상 입력해주세요' }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="confirm"
            label="새 비밀번호 확인"
            dependencies={['newPassword']}
            rules={[
              { required: true },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) return Promise.resolve();
                  return Promise.reject(new Error('비밀번호가 일치하지 않습니다'));
                },
              }),
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block disabled={!token}>비밀번호 변경</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
