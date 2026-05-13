import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, DatePicker, Popconfirm, message, Space, Tag, Drawer, Divider } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { RobotOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getTasks, createTask, updateTask, deleteTask } from '../api/tasks';
import type { TaskResponse, TaskRequest, TaskStatus, TaskPriority } from '../types/task';
import FilterBar from '../components/FilterBar';
import CommentList from '../components/CommentList';
import AiTaskInput from '../components/AiTaskInput';
import BrainDumpModal from '../components/BrainDumpModal';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const STATUS_LABEL: Record<TaskStatus, string> = {
  TODO: '할 일', IN_PROGRESS: '진행 중', DONE: '완료',
};
const STATUS_COLOR: Record<TaskStatus, string> = {
  TODO: 'default', IN_PROGRESS: 'blue', DONE: 'green',
};
const PRIORITY_LABEL: Record<TaskPriority, string> = {
  LOW: '낮음', MEDIUM: '보통', HIGH: '높음',
};
const PRIORITY_COLOR: Record<TaskPriority, string> = {
  LOW: 'green', MEDIUM: 'orange', HIGH: 'red',
};

export default function TasksPage() {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<TaskResponse | null>(null);
  const [drawerTask, setDrawerTask] = useState<TaskResponse | null>(null);
  const [aiOpen, setAiOpen] = useState(false);
  const [brainDumpOpen, setBrainDumpOpen] = useState(false);
  const [aiEnabled, setAiEnabled] = useState(false);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const [filterStatus, setFilterStatus] = useState<TaskStatus | undefined>();
  const [filterPriority, setFilterPriority] = useState<TaskPriority | undefined>();
  const [filterSearch, setFilterSearch] = useState('');

  const fetchTasks = async () => {
    setLoading(true);
    try {
      setTasks(await getTasks({
        status: filterStatus,
        priority: filterPriority,
        search: filterSearch || undefined,
      }));
    } catch {
      messageApi.error('태스크 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTasks(); }, [filterStatus, filterPriority, filterSearch]);

  useEffect(() => {
    fetch(`${API_URL}/api/ai/capabilities`)
      .then((r) => r.json())
      .then((data) => setAiEnabled(data.enabled))
      .catch(() => setAiEnabled(false));
  }, []);

  const openCreate = () => {
    setEditingTask(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (task: TaskResponse) => {
    setEditingTask(task);
    form.setFieldsValue({
      title: task.title,
      description: task.description,
      status: task.status,
      priority: task.priority,
      dueDate: task.dueDate ? dayjs(task.dueDate) : undefined,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload: TaskRequest = {
        title: values.title,
        description: values.description,
        status: values.status,
        priority: values.priority,
        dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : undefined,
      };
      if (editingTask) {
        await updateTask(editingTask.id, payload);
        messageApi.success('태스크가 수정되었습니다');
      } else {
        await createTask(payload);
        messageApi.success('태스크가 생성되었습니다');
      }
      setModalOpen(false);
      fetchTasks();
    } catch (err: any) {
      if (err?.errorFields) return;
      messageApi.error('저장에 실패했습니다');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      messageApi.success('태스크가 삭제되었습니다');
      fetchTasks();
    } catch {
      messageApi.error('삭제에 실패했습니다');
    }
  };

  const columns: ColumnsType<TaskResponse> = [
    {
      title: '제목', dataIndex: 'title', key: 'title',
      render: (title, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => setDrawerTask(record)}>{title}</Button>
      ),
    },
    {
      title: '상태', dataIndex: 'status', key: 'status',
      render: (status: TaskStatus) => <Tag color={STATUS_COLOR[status]}>{STATUS_LABEL[status]}</Tag>,
    },
    {
      title: '우선순위', dataIndex: 'priority', key: 'priority',
      render: (priority: TaskPriority) => <Tag color={PRIORITY_COLOR[priority]}>{PRIORITY_LABEL[priority]}</Tag>,
    },
    { title: '마감일', dataIndex: 'dueDate', key: 'dueDate', render: (v) => v ?? '-' },
    { title: '생성일', dataIndex: 'createdAt', key: 'createdAt', render: (v) => dayjs(v).format('YYYY-MM-DD') },
    {
      title: '작업', key: 'actions',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => openEdit(record)}>수정</Button>
          <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(record.id)} okText="삭제" cancelText="취소">
            <Button size="small" danger>삭제</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      {contextHolder}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>태스크 목록</h2>
        <Space>
          <Button icon={<RobotOutlined />} onClick={() => setAiOpen(true)}>AI 생성</Button>
          {aiEnabled && (
            <Button icon={<RobotOutlined />} onClick={() => setBrainDumpOpen(true)}>브레인덤프</Button>
          )}
          <Button type="primary" onClick={openCreate}>새 태스크</Button>
        </Space>
      </div>

      <FilterBar
        status={filterStatus}
        priority={filterPriority}
        search={filterSearch}
        onStatusChange={setFilterStatus}
        onPriorityChange={setFilterPriority}
        onSearchChange={setFilterSearch}
        onClear={() => { setFilterStatus(undefined); setFilterPriority(undefined); setFilterSearch(''); }}
      />

      <Table rowKey="id" columns={columns} dataSource={tasks} loading={loading} />

      <Modal
        title={editingTask ? '태스크 수정' : '새 태스크'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText={editingTask ? '수정' : '생성'}
        cancelText="취소"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="title" label="제목" rules={[{ required: true, message: '제목을 입력하세요' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="설명">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="status" label="상태" initialValue="TODO">
            <Select options={Object.entries(STATUS_LABEL).map(([v, l]) => ({ value: v, label: l }))} />
          </Form.Item>
          <Form.Item name="priority" label="우선순위" initialValue="MEDIUM">
            <Select options={[
              { value: 'HIGH', label: '높음' },
              { value: 'MEDIUM', label: '보통' },
              { value: 'LOW', label: '낮음' },
            ]} />
          </Form.Item>
          <Form.Item name="dueDate" label="마감일">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title={drawerTask?.title}
        open={!!drawerTask}
        onClose={() => setDrawerTask(null)}
        width={480}
      >
        {drawerTask && (
          <>
            <p><strong>상태:</strong> <Tag color={STATUS_COLOR[drawerTask.status]}>{STATUS_LABEL[drawerTask.status]}</Tag></p>
            <p><strong>우선순위:</strong> <Tag color={PRIORITY_COLOR[drawerTask.priority]}>{PRIORITY_LABEL[drawerTask.priority]}</Tag></p>
            {drawerTask.dueDate && <p><strong>마감일:</strong> {drawerTask.dueDate}</p>}
            {drawerTask.description && <p><strong>설명:</strong> {drawerTask.description}</p>}
            <Divider />
            <CommentList taskId={drawerTask.id} />
          </>
        )}
      </Drawer>

      <AiTaskInput open={aiOpen} onClose={() => setAiOpen(false)} onCreated={fetchTasks} />
      <BrainDumpModal open={brainDumpOpen} onClose={() => setBrainDumpOpen(false)} onSuccess={fetchTasks} />
    </>
  );
}
