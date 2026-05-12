import { Card, Typography, Tag, Button, Spin, Space, message } from 'antd';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { PlusOutlined } from '@ant-design/icons';
import type { TaskStatus, TaskRequest } from '../types/task';
import { useTasks } from '../hooks/useTasks';
import { useOptimisticTaskStatus } from '../hooks/useMutateTask';

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
  const { data: tasks = [], isLoading } = useTasks();
  const optimisticStatus = useOptimisticTaskStatus();
  const [messageApi, contextHolder] = message.useMessage();

  const getColumnTasks = (status: TaskStatus) =>
    tasks.filter((t) => t.status === status);

  const onDragEnd = async (result: DropResult) => {
    if (!result.destination) return;
    const newStatus = result.destination.droppableId as TaskStatus;
    const taskId = Number(result.draggableId);
    const task = tasks.find((t) => t.id === taskId);
    if (!task || task.status === newStatus) return;

    const payload: TaskRequest = {
      title: task.title,
      description: task.description,
      status: newStatus,
      priority: task.priority,
      dueDate: task.dueDate,
    };

    try {
      await optimisticStatus.mutateAsync({ id: taskId, data: payload });
    } catch {
      messageApi.error('상태 변경 실패');
    }
  };

  if (isLoading) return <Spin size="large" style={{ display: 'block', marginTop: 80 }} />;

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
                                ...provided.draggableProps.style,
                              }}
                            >
                              <Typography.Text strong style={{ display: 'block', marginBottom: 4 }}>
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
