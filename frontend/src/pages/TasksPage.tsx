import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, DatePicker, Popconfirm, message, Space, Tag, Drawer, Divider, List, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { RobotOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getTasks, createTask, updateTask, deleteTask } from '../api/tasks';
import type { TaskResponse, TaskRequest, TaskStatus, TaskPriority } from '../types/task';
import { getProjectLabels, addLabelToTask, removeLabelFromTask, type LabelResponse } from '../api/labels';
import FilterBar from '../components/FilterBar';
import CommentList from '../components/CommentList';
import AiTaskInput from '../components/AiTaskInput';
import BrainDumpModal from '../components/BrainDumpModal';
import { prioritizeTasks, estimateTask, type PrioritizeItem } from '../api/ai';
import { getProjects, type ProjectResponse } from '../api/projects';

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
  const [filterLabelId, setFilterLabelId] = useState<number | undefined>();
  const [allLabels, setAllLabels] = useState<LabelResponse[]>([]);
  const [projectLabels, setProjectLabels] = useState<LabelResponse[]>([]);
  const [selectedLabelIds, setSelectedLabelIds] = useState<number[]>([]);

  const [prioritizeOpen, setPrioritizeOpen] = useState(false);
  const [prioritizeItems, setPrioritizeItems] = useState<PrioritizeItem[]>([]);
  const [prioritizeLoading, setPrioritizeLoading] = useState(false);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>();
  const [prioritizeTitleMap, setPrioritizeTitleMap] = useState<Record<number, string>>({});
  const [estimateLoading, setEstimateLoading] = useState(false);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      setTasks(await getTasks({
        status: filterStatus,
        priority: filterPriority,
        search: filterSearch || undefined,
        labelId: filterLabelId,
      }));
    } catch {
      messageApi.error('태스크 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTasks(); }, [filterStatus, filterPriority, filterSearch, filterLabelId]);

  useEffect(() => {
    fetch(`${API_URL}/api/ai/capabilities`)
      .then((r) => r.json())
      .then((data) => setAiEnabled(data.enabled))
      .catch(() => setAiEnabled(false));
  }, []);

  useEffect(() => {
    getProjects().then((ps) => {
      setProjects(ps);
      // Load all labels across all projects for the filter bar
      Promise.all(ps.map((p) => getProjectLabels(p.id)))
        .then((results) => setAllLabels(results.flat()))
        .catch(() => {});
    }).catch(() => {});
  }, []);

  const handleFormProjectChange = (projectId: number | undefined) => {
    setSelectedLabelIds([]);
    if (projectId) {
      getProjectLabels(projectId).then(setProjectLabels).catch(() => setProjectLabels([]));
    } else {
      setProjectLabels([]);
    }
  };

  const handlePrioritize = async () => {
    if (!selectedProjectId) return;
    setPrioritizeLoading(true);
    try {
      const items = await prioritizeTasks(selectedProjectId);
      setPrioritizeItems(items);
      const titleMap: Record<number, string> = {};
      tasks.forEach((t) => { titleMap[t.id] = t.title; });
      setPrioritizeTitleMap(titleMap);
    } catch {
      messageApi.error('우선순위화에 실패했습니다 (AI가 활성화되어 있는지 확인하세요)');
    } finally {
      setPrioritizeLoading(false);
    }
  };

  const handleEstimate = async () => {
    const title = form.getFieldValue('title') as string;
    if (!title?.trim()) {
      messageApi.warning('제목을 먼저 입력하세요');
      return;
    }
    setEstimateLoading(true);
    try {
      const description = form.getFieldValue('description') as string | undefined;
      const result = await estimateTask(title, description);
      form.setFieldValue('dueDate', dayjs(result.suggestedDueDate));
      messageApi.success(`추정: ${result.effort}급, ${result.estimatedDays}일 → ${result.suggestedDueDate}`);
    } catch {
      messageApi.error('추정에 실패했습니다');
    } finally {
      setEstimateLoading(false);
    }
  };

  const openCreate = () => {
    setEditingTask(null);
    setProjectLabels([]);
    setSelectedLabelIds([]);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (task: TaskResponse) => {
    setEditingTask(task);
    const existingLabelIds = (task.labels ?? []).map((l) => l.id);
    setSelectedLabelIds(existingLabelIds);
    if (task.projectId) {
      getProjectLabels(task.projectId).then(setProjectLabels).catch(() => setProjectLabels([]));
    } else {
      setProjectLabels([]);
    }
    form.setFieldsValue({
      title: task.title,
      description: task.description,
      status: task.status,
      priority: task.priority,
      dueDate: task.dueDate ? dayjs(task.dueDate) : undefined,
      projectId: task.projectId,
      labelIds: existingLabelIds,
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
        projectId: values.projectId,
      };
      if (editingTask) {
        await updateTask(editingTask.id, payload);
        // Sync labels: remove old, add new
        const oldIds = (editingTask.labels ?? []).map((l) => l.id);
        const newIds: number[] = values.labelIds ?? [];
        const toRemove = oldIds.filter((id) => !newIds.includes(id));
        const toAdd = newIds.filter((id) => !oldIds.includes(id));
        await Promise.all([
          ...toRemove.map((lid) => removeLabelFromTask(editingTask.id, lid)),
          ...toAdd.map((lid) => addLabelToTask(editingTask.id, lid)),
        ]);
        messageApi.success('태스크가 수정되었습니다');
      } else {
        const created = await createTask(payload);
        const newIds: number[] = values.labelIds ?? [];
        await Promise.all(newIds.map((lid) => addLabelToTask(created.id, lid)));
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
    {
      title: '라벨', key: 'labels',
      render: (_: unknown, record: TaskResponse) => (
        <Space size={4} wrap>
          {(record.labels ?? []).map((l) => (
            <Tag key={l.id} color={l.color}>{l.name}</Tag>
          ))}
        </Space>
      ),
    },
    { title: '마감일', dataIndex: 'dueDate', key: 'dueDate', render: (v: string | undefined) => v ?? '-' },
    { title: '생성일', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => dayjs(v).format('YYYY-MM-DD') },
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
          {aiEnabled && (
            <Button icon={<RobotOutlined />} onClick={() => { setPrioritizeItems([]); setPrioritizeOpen(true); }}>백로그 우선순위화</Button>
          )}
          <Button type="primary" onClick={openCreate}>새 태스크</Button>
        </Space>
      </div>

      <FilterBar
        status={filterStatus}
        priority={filterPriority}
        search={filterSearch}
        labelId={filterLabelId}
        labels={allLabels}
        onStatusChange={setFilterStatus}
        onPriorityChange={setFilterPriority}
        onSearchChange={setFilterSearch}
        onLabelChange={setFilterLabelId}
        onClear={() => { setFilterStatus(undefined); setFilterPriority(undefined); setFilterSearch(''); setFilterLabelId(undefined); }}
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
          <Form.Item name="projectId" label="프로젝트">
            <Select
              placeholder="프로젝트 선택"
              allowClear
              options={projects.map((p) => ({ value: p.id, label: p.name }))}
              onChange={handleFormProjectChange}
            />
          </Form.Item>
          {projectLabels.length > 0 && (
            <Form.Item name="labelIds" label="라벨">
              <Select
                mode="multiple"
                placeholder="라벨 선택"
                allowClear
                options={projectLabels.map((l) => ({ value: l.id, label: l.name }))}
                onChange={(ids: number[]) => setSelectedLabelIds(ids)}
                value={selectedLabelIds}
              />
            </Form.Item>
          )}
          <Form.Item name="dueDate" label="마감일">
            <Space.Compact style={{ width: '100%' }}>
              <Form.Item name="dueDate" noStyle>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
              {aiEnabled && (
                <Button
                  icon={<RobotOutlined />}
                  loading={estimateLoading}
                  onClick={handleEstimate}
                  title="AI 공수 추정으로 마감일 자동 채움"
                >
                  추정
                </Button>
              )}
            </Space.Compact>
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

      <Modal
        title={<Space><RobotOutlined />백로그 우선순위화</Space>}
        open={prioritizeOpen}
        onCancel={() => setPrioritizeOpen(false)}
        footer={null}
        width={520}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <Select
            placeholder="프로젝트 선택"
            style={{ width: '100%' }}
            value={selectedProjectId}
            onChange={setSelectedProjectId}
            options={projects.map((p) => ({ value: p.id, label: p.name }))}
          />
          <Button
            type="primary"
            ghost
            icon={<RobotOutlined />}
            onClick={handlePrioritize}
            loading={prioritizeLoading}
            disabled={!selectedProjectId}
            block
          >
            우선순위화
          </Button>
          {prioritizeItems.length > 0 && (
            <List
              size="small"
              dataSource={prioritizeItems}
              renderItem={(item, index) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={<Tag color="blue">{index + 1}</Tag>}
                    title={<Typography.Text strong>{prioritizeTitleMap[item.taskId] ?? `태스크 #${item.taskId}`}</Typography.Text>}
                    description={item.reason}
                  />
                </List.Item>
              )}
            />
          )}
          {!prioritizeLoading && prioritizeItems.length === 0 && selectedProjectId && (
            <Typography.Text type="secondary">우선순위화 버튼을 눌러 결과를 확인하세요.</Typography.Text>
          )}
        </Space>
      </Modal>
    </>
  );
}
