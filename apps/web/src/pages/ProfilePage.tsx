import { useEffect, useState } from 'react';
import { Descriptions, Card, Spin, message, Button, Modal } from 'antd';
import { useNavigate } from 'react-router-dom';
import { me, withdraw } from '../api/auth';
import type { AuthResponse } from '../types/auth';

export default function ProfilePage() {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [withdrawOpen, setWithdrawOpen] = useState(false);
  const [withdrawing, setWithdrawing] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();

  useEffect(() => {
    me()
      .then(setUser)
      .catch(() => messageApi.error('사용자 정보를 불러오지 못했습니다'))
      .finally(() => setLoading(false));
  }, []);

  const handleWithdraw = async () => {
    setWithdrawing(true);
    try {
      await withdraw();
      localStorage.removeItem('token');
      messageApi.success('계정이 삭제되었습니다');
      navigate('/login');
    } catch {
      messageApi.error('계정 탈퇴에 실패했습니다. 다시 시도해주세요');
    } finally {
      setWithdrawing(false);
      setWithdrawOpen(false);
    }
  };

  return (
    <>
      {contextHolder}
      <Card title="내 정보" style={{ maxWidth: 500 }}>
        {loading ? (
          <Spin />
        ) : (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="이름">{user?.name}</Descriptions.Item>
            <Descriptions.Item label="이메일">{user?.email}</Descriptions.Item>
          </Descriptions>
        )}
      </Card>

      <Card
        style={{ maxWidth: 500, marginTop: 24, border: '1px solid #ff4d4f' }}
      >
        <h3 style={{ color: '#ff4d4f', marginTop: 0 }}>위험 구역</h3>
        <p>계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.</p>
        <Button danger onClick={() => setWithdrawOpen(true)}>
          계정 탈퇴
        </Button>
      </Card>

      <Modal
        title="계정 탈퇴"
        open={withdrawOpen}
        onOk={handleWithdraw}
        onCancel={() => setWithdrawOpen(false)}
        okText="탈퇴"
        okButtonProps={{ danger: true, loading: withdrawing }}
        cancelText="취소"
      >
        <p>정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.</p>
      </Modal>
    </>
  );
}
