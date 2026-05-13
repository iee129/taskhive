import { useState } from 'react';
import { Modal, Button, Card, Typography, Space, message, Spin, Empty } from 'antd';
import { RobotOutlined, UserOutlined } from '@ant-design/icons';
import { generateStandup } from '../api/ai';
import type { StandupItem } from '../api/ai';

interface StandupModalProps {
  open: boolean;
  onClose: () => void;
  projectId: number;
}

export default function StandupModal({ open, onClose, projectId }: StandupModalProps) {
  const [items, setItems] = useState<StandupItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [generated, setGenerated] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleGenerate = async () => {
    setLoading(true);
    setGenerated(false);
    setItems([]);
    try {
      const result = await generateStandup(projectId);
      setItems(result);
      setGenerated(true);
    } catch {
      messageApi.error('스탠드업 생성 실패 (AI가 활성화되어 있는지 확인하세요)');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setItems([]);
    setGenerated(false);
    onClose();
  };

  return (
    <Modal
      title={<Space><RobotOutlined />AI 스탠드업 생성</Space>}
      open={open}
      onCancel={handleClose}
      footer={
        <Button type="primary" icon={<RobotOutlined />} onClick={handleGenerate} loading={loading}>
          스탠드업 생성
        </Button>
      }
      width={560}
    >
      {contextHolder}
      <Typography.Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
        지난 24시간 동안의 팀원별 활동을 AI가 요약합니다.
      </Typography.Text>

      {loading && <Spin style={{ display: 'block', textAlign: 'center', margin: '24px 0' }} />}

      {!loading && generated && items.length === 0 && (
        <Empty description="지난 24시간 동안 활동이 없습니다." />
      )}

      {!loading && items.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {items.map((item) => (
            <Card key={item.userId ?? item.name} size="small">
              <Space style={{ marginBottom: 6 }}>
                <UserOutlined />
                <Typography.Text strong>{item.name}</Typography.Text>
              </Space>
              <Typography.Paragraph style={{ margin: 0, fontSize: 13 }}>
                {item.summary}
              </Typography.Paragraph>
            </Card>
          ))}
        </div>
      )}
    </Modal>
  );
}
