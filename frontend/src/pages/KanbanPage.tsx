import { useEffect, useState, useCallback } from 'react';
import { Card, Typography, Tag, Button, Spin, Space, message, Tooltip } from 'antd';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { PlusOutlined, WarningOutlined } from '@ant-design/icons';
import { getTasks, updateTask } from '../api/tasks';
import type { TaskResponse, TaskStatus, TaskRequest } from '../types/task';
import { useBoardSync, type TaskEvent } from '../hooks/useBoardSync';
import { getBlockers } from '../api/ai';
import { getProjects } from '../api/projects';

const COLUMNS: { key: TaskStatus; label: string; color: string }[] = [
  { key: 'TODO', label: '할 일', color: '#f0f0f0' },
  { key: 'IN_PROGRESS', label: '진행 중', color: '#e6f4ff' },
  { key: 'DONE', label: '완료', color: '#f6ffed' },
];

const PRIORITY_COLOR: Record<string, string> = {
  HIGH: 'red', MEDIUM: 'orange', LOW: 'green',
};
const PRIORITY_LABEL: Record<string, string> = {
  HIGH: '높음', MEDIUM: '보통', LOW: '낮음',
};

export default function KanbanPage() {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [blockerIds, setBlockerIds] = useState<Set<number>>(new Set());
  const [messageApi, contextHolder] = message.useMessage();

  const fetchTasks = async () => {
    setLoading(true);
    try { setTasks(await getTasks()); }
    catch { messageApi.error('태스크 목록 로드 실패'); }
    finally { setLoading(false); }
  };

  const fetchBlockers = useCallback(async () => {
    try {
      const projects = await getProjects();
      const allBlockerIds = new Set<number>();
      await Promise.all(
        projects.map(async (p) => {
          try {
            const blockers = await getBlockers(p.id);
            blockers.forEach((b) => allBlockerIds.add(b.id));
          } catch {
            // 프로젝트 멤버가 아니거나 오류 시 무시
          }
        })
      );
      setBlockerIds(allBlockerIds);
    } catch {
      // 블로커 로드 실패 시 뱃지 없이 계속
    }
  }, []);

  useEffect(() => { fetchTasks(); fetchBlockers(); }, []);

  const handleBoardEvent = useCallback((event: TaskEvent) => {
    if (event.type === 'TASK_UPDATED') {
      const { status } = event.payload as { status?: TaskStatus; title?: string };
      if (status) {
        setTasks((prev) =>
          prev.map((t) => t.id === event.taskId ? { ...t, status } : t)
        );
      }
    } else if (event.type === 'TASK_CREATED') {
      fetchTasks();
    } else if (event.type === 'TASK_DELETED') {
      setTasks((prev) => prev.filter((t) => t.id !== event.taskId));
    }
  }, []);

  useBoardSync(handleBoardEvent);

  const getColumnTasks = (status: TaskStatus) =>
    tasks.filter((t) => t.status === status);

  const onDragEnd = async (result: DropResult) => {
    if (!result.destination) return;
    const newStatus = result.destination.droppableId as TaskStatus;
    const taskId = Number(result.draggableId);
    const task = tasks.find((t) => t.id === taskId);
    if (!task || task.status === newStatus) return;

    setTasks((prev) =>
      prev.map((t) => t.id === taskId ? { ...t, status: newStatus } : t)
    );

    try {
      const payload: TaskRequest = {
        title: task.title,
        description: task.description,
        status: newStatus,
        priority: task.priority,
        dueDate: task.dueDate,
      };
      await updateTask(taskId, payload);
    } catch {
      messageApi.error('상태 변경 실패');
      fetchTasks();
    }
  };

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 80 }} />;

  return (
    <div>
      {contextHolder}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>칸반 보드</Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} href="/tasks">목록에서 추가</Button>
      </div>

      <DragDropContext onDragEnd={onDragEnd}>
        <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start', overflowX: 'auto' }}>
          {COLUMNS.map((col) => (
            <div key={col.key} style={{ flex: '0 0 300px', minWidth: 280 }}>
              <Card
                title={
                  <Space>
                    <span>{col.label}</span>
                    <Tag>{getColumnTasks(col.key).length}</Tag>
                  </Space>
                }
                style={{ background: col.color, minHeight: 400 }}
                styles={{ body: { padding: 8 } }}
              >
                <Droppable droppableId={col.key}>
                  {(provided, snapshot) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      style={{
                        minHeight: 300,
                        background: snapshot.isDraggingOver ? 'rgba(0,0,0,0.04)' : 'transparent',
                        borderRadius: 4,
                        padding: 4,
                        transition: 'background 0.2s',
                      }}
                    >
                      {getColumnTasks(col.key).map((task, index) => (
                        <Draggable key={task.id} draggableId={String(task.id)} index={index}>
                          {(provided, snapshot) => (
                            <Card
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              size="small"
                              style={{
                                marginBottom: 8,
                                boxShadow: snapshot.isDragging
                                  ? '0 4px 12px rgba(0,0,0,0.15)'
                                  : '0 1px 3px rgba(0,0,0,0.08)',
                                cursor: 'grab',
                                border: blockerIds.has(task.id) ? '1.5px solid #ff4d4f' : undefined,
                                ...provided.draggableProps.style,
                              }}
                            >
                              <Typography.Text strong style={{ display: 'block', marginBottom: 4 }}>
                                {blockerIds.has(task.id) && (
                                  <Tooltip title="14일 이상 진행 중인 블로커 태스크">
                                    <WarningOutlined style={{ color: '#ff4d4f', marginRight: 4 }} />
                                  </Tooltip>
                                )}
                                {task.title}
                              </Typography.Text>
                              {task.description && (
                                <Typography.Text type="secondary" style={{ fontSize: 12, display: 'block', marginBottom: 4 }}>
                                  {task.description.length > 60
                                    ? task.description.substring(0, 60) + '...'
                                    : task.description}
                                </Typography.Text>
                              )}
                              <Space size={4} wrap>
                                <Tag color={PRIORITY_COLOR[task.priority]} style={{ fontSize: 11, margin: 0 }}>
                                  {PRIORITY_LABEL[task.priority]}
                                </Tag>
                                {task.dueDate && (
                                  <Tag style={{ fontSize: 11, margin: 0 }}>{task.dueDate}</Tag>
                                )}
                              </Space>
                            </Card>
                          )}
                        </Draggable>
                      ))}
                      {provided.placeholder}
                    </div>
                  )}
                </Droppable>
              </Card>
            </div>
          ))}
        </div>
      </DragDropContext>
    </div>
  );
}
