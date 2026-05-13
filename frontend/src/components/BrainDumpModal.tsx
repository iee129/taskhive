import { useState } from 'react';
import { Modal, Input, Button, Checkbox, Space, message, Tag } from 'antd';
import { RobotOutlined } from '@ant-design/icons';
import { breakdownText, createTasksFromBreakdown } from '../api/ai';
import type { BrainDumpItem } from '../api/ai';

interface BrainDumpModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  projectId?: number;
}

const PRIORITY_COLOR: Record<string, string> = {
  HIGH: 'red',
  MEDIUM: 'orange',
  LOW: 'green',
};
const PRIORITY_LABEL: Record<string, string> = {
  HIGH: '높음',
  MEDIUM: '보통',
  LOW: '낮음',
};

export default function BrainDumpModal({ open, onClose, onSuccess, projectId }: BrainDumpModalProps) {
  const [text, setText] = useState('');
  const [items, setItems] = useState<BrainDumpItem[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [breaking, setBreaking] = useState(false);
  const [creating, setCreating] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleBreakdown = async () => {
    if (!text.trim()) return;
    setBreaking(true);
    setItems([]);
    setSelected([]);
    try {
      const result = await breakdownText(text, projectId);
      setItems(result);
      setSelected(result.map((_, i) => i));
    } catch {
      messageApi.error('AI 분해 실패 (AI가 활성화되어 있는지 확인하세요)');
    } finally {
      setBreaking(false);
    }
  };

  const handleCreate = async () => {
    const selectedItems = items.filter((_, i) => selected.includes(i));
    if (selectedItems.length === 0) {
      messageApi.warning('생성할 태스크를 선택하세요');
      return;
    }
    setCreating(true);
    try {
      await createTasksFromBreakdown(selectedItems, projectId);
      messageApi.success(`${selectedItems.length}개 태스크가 생성되었습니다`);
      onSuccess();
      handleClose();
    } catch {
      messageApi.error('태스크 생성 실패');
    } finally {
      setCreating(false);
    }
  };

  const handleClose = () => {
    setText('');
    setItems([]);
    setSelected([]);
    onClose();
  };

  const toggleItem = (index: number) => {
    setSelected((prev) =>
      prev.includes(index) ? prev.filter((i) => i !== index) : [...prev, index]
    );
  };

  return (
    <Modal
      title={<Space><RobotOutlined />브레인덤프 → 태스크 분해</Space>}
      open={open}
      onCancel={handleClose}
      footer={null}
      width={560}
    >
      {contextHolder}
      <Input.TextArea
        rows={4}
        placeholder="머릿속에 있는 할 일들을 자유롭게 적어보세요. AI가 태스크로 분해해드립니다."
        value={text}
        onChange={(e) => setText(e.target.value)}
        style={{ marginBottom: 12 }}
      />
      <Button
        type="primary"
        ghost
        icon={<RobotOutlined />}
        onClick={handleBreakdown}
        loading={breaking}
        block
        disabled={!text.trim()}
      >
        분해
      </Button>

      {items.length > 0 && (
        <div style={{ marginTop: 16 }}>
          <div style={{ marginBottom: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontWeight: 500 }}>분해된 태스크 ({items.length}개)</span>
            <Space>
              <Button size="small" onClick={() => setSelected(items.map((_, i) => i))}>전체 선택</Button>
              <Button size="small" onClick={() => setSelected([])}>전체 해제</Button>
            </Space>
          </div>
          <div style={{ maxHeight: 300, overflowY: 'auto', border: '1px solid #f0f0f0', borderRadius: 6, padding: 8 }}>
            {items.map((item, i) => (
              <div
                key={i}
                onClick={() => toggleItem(i)}
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: 8,
                  padding: '8px 4px',
                  cursor: 'pointer',
                  borderBottom: i < items.length - 1 ? '1px solid #f5f5f5' : undefined,
                }}
              >
                <Checkbox checked={selected.includes(i)} onClick={(e) => e.stopPropagation()} onChange={() => toggleItem(i)} />
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontWeight: 500 }}>{item.title}</span>
                    <Tag color={PRIORITY_COLOR[item.priority] ?? 'default'}>
                      {PRIORITY_LABEL[item.priority] ?? item.priority}
                    </Tag>
                  </div>
                  {item.description && (
                    <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{item.description}</div>
                  )}
                </div>
              </div>
            ))}
          </div>
          <div style={{ marginTop: 12, display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              type="primary"
              onClick={handleCreate}
              loading={creating}
              disabled={selected.length === 0}
            >
              선택 태스크 생성 ({selected.length}개)
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
}
