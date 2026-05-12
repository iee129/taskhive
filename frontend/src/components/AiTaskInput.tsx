import { useState } from 'react';
import { Modal, Input, Button, Typography, Tag, Space, message } from 'antd';
import { RobotOutlined } from '@ant-design/icons';
import { suggestTask } from '../api/ai';
import { createTask } from '../api/tasks';
import type { TaskRequest } from '../types/task';

interface AiTaskInputProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

const PRIORITY_COLOR: Record<string, string> = {
  HIGH: 'red', MEDIUM: 'orange', LOW: 'green',
};
const PRIORITY_LABEL: Record<string, string> = {
  HIGH: '높음', MEDIUM: '보통', LOW: '낮음',
};

export default function AiTaskInput({ open, onClose, onCreated }: AiTaskInputProps) {
  const [description, setDescription] = useState('');
  const [suggested, setSuggested] = useState<TaskRequest | null>(null);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleSuggest = async () => {
    if (!description.trim()) return;
    setLoading(true);
    try {
      const result = await suggestTask(description);
      setSuggested(result);
    } catch {
      messageApi.error('AI 제안 생성 실패 (Ollama가 실행 중인지 확인하세요)');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    if (!suggested) return;
    setCreating(true);
    try {
      await createTask(suggested);
      messageApi.success('태스크가 생성되었습니다');
      onCreated();
      handleClose();
    } catch {
      messageApi.error('태스크 생성 실패');
    } finally {
      setCreating(false);
    }
  };

  const handleClose = () => {
    setDescription('');
    setSuggested(null);
    onClose();
  };

  return (
    <Modal
      title={<Space><RobotOutlined />AI 태스크 생성</Space>}
      open={open}
      onCancel={handleClose}
      footer={null}
      width={520}
    >
      {contextHolder}
      <Input.TextArea
        rows={3}
        placeholder="어떤 태스크를 만들고 싶은지 자유롭게 설명해 주세요"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        style={{ marginBottom: 12 }}
      />
      <Button type="primary" ghost icon={<RobotOutlined />} onClick={handleSuggest} loading={loading} block>
        AI 제안 받기
      </Button>

      {suggested && (
        <div style={{ marginTop: 16, padding: 12, background: '#f6f8fa', borderRadius: 6 }}>
          <Typography.Title level={5} style={{ margin: 0 }}>{suggested.title}</Typography.Title>
          {suggested.description && (
            <Typography.Text type="secondary" style={{ display: 'block', marginTop: 4 }}>
              {suggested.description}
            </Typography.Text>
          )}
          {suggested.priority && (
            <Tag color={PRIORITY_COLOR[suggested.priority]} style={{ marginTop: 8 }}>
              {PRIORITY_LABEL[suggested.priority]}
            </Tag>
          )}
          <div style={{ marginTop: 12, display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button onClick={() => setSuggested(null)}>다시 생성</Button>
            <Button type="primary" onClick={handleCreate} loading={creating}>태스크 생성</Button>
          </div>
        </div>
      )}
    </Modal>
  );
}
