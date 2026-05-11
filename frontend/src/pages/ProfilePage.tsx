import { useEffect, useState } from 'react';
import { Descriptions, Card, Spin, message } from 'antd';
import { me } from '../api/auth';
import type { AuthResponse } from '../types/auth';

export default function ProfilePage() {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    me()
      .then(setUser)
      .catch(() => messageApi.error('사용자 정보를 불러오지 못했습니다'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Card title="내 정보" style={{ maxWidth: 500 }}>
      {contextHolder}
      {loading ? (
        <Spin />
      ) : (
        <Descriptions column={1} bordered>
          <Descriptions.Item label="이름">{user?.name}</Descriptions.Item>
          <Descriptions.Item label="이메일">{user?.email}</Descriptions.Item>
        </Descriptions>
      )}
    </Card>
  );
}
