import { useState } from 'react';
import { Modal, Input, Button, Typography, Space, message, Form, Select, DatePicker } from 'antd';
import { RobotOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { suggestTask } from '../api/ai';
import { createTask } from '../api/tasks';
import type { TaskRequest } from '../types/task';

interface AiTaskInputProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function AiTaskInput({ open, onClose, onCreated }: AiTaskInputProps) {
  const [description, setDescription] = useState('');
  const [suggested, setSuggested] = useState<TaskRequest | null>(null);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [form] = Form.useForm();

  const handleSuggest = async () => {
    if (!description.trim()) return;
    setLoading(true);
    try {
      const result = await suggestTask(description);
      setSuggested(result);
      form.setFieldsValue({
        title: result.title,
        description: result.description,
        priority: result.priority,
        dueDate: result.dueDate ? dayjs(result.dueDate) : undefined,
      });
    } catch {
      messageApi.error('AI 제안 생성 실패 (Ollama가 실행 중인지 확인하세요)');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    let values;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setCreating(true);
    try {
      const payload: TaskRequest = {
        title: values.title,
        description: values.description,
        priority: values.priority,
        dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : undefined,
      };
      await createTask(payload);
      messageApi.success('태스크가 생성되었습니다');
      onCreated();
      handleClose();
    } catch {
      messageApi.error('태스크 생성 실패');
    } finally {
      setCreating(false);
    }
  };

  const handleReset = () => {
    setSuggested(null);
    form.resetFields();
  };

  const handleClose = () => {
    setDescription('');
    setSuggested(null);
    form.resetFields();
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
        <div style={{ marginTop: 16 }}>
          <Typography.Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
            AI가 제안한 내용을 수정 후 저장하세요
          </Typography.Text>
          <Form form={form} layout="vertical">
            <Form.Item name="title" label="제목" rules={[{ required: true, message: '제목을 입력해 주세요' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="설명">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="priority" label="우선순위">
              <Select
                options={[
                  { value: 'HIGH', label: '높음' },
                  { value: 'MEDIUM', label: '보통' },
                  { value: 'LOW', label: '낮음' },
                ]}
              />
            </Form.Item>
            <Form.Item name="dueDate" label="마감일">
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </Form>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button onClick={handleReset}>다시 생성</Button>
            <Button type="primary" onClick={handleCreate} loading={creating}>태스크 생성</Button>
          </div>
        </div>
      )}
    </Modal>
  );
}
