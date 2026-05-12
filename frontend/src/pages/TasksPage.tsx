import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, DatePicker, Popconfirm, message, Space, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { getTasks, createTask, updateTask, deleteTask } from '../api/tasks';
import type { TaskResponse, TaskRequest, TaskStatus } from '../types/task';

const STATUS_LABEL: Record<TaskStatus, string> = {
  TODO: '할 일',
  IN_PROGRESS: '진행 중',
  DONE: '완료',
};

const STATUS_COLOR: Record<TaskStatus, string> = {
  TODO: 'default',
  IN_PROGRESS: 'blue',
  DONE: 'green',
};

export default function TasksPage() {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<TaskResponse | null>(null);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const fetchTasks = async () => {
    setLoading(true);
    try {
      setTasks(await getTasks());
    } catch {
      messageApi.error('태스크 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTasks(); }, []);

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
    { title: '제목', dataIndex: 'title', key: 'title' },
    {
      title: '상태', dataIndex: 'status', key: 'status',
      render: (status: TaskStatus) => <Tag color={STATUS_COLOR[status]}>{STATUS_LABEL[status]}</Tag>,
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
        <Button type="primary" onClick={openCreate}>새 태스크</Button>
      </div>
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
          <Form.Item name="dueDate" label="마감일">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
