import { useEffect, useState } from 'react';
import { Timeline, Tag, Typography, Spin } from 'antd';
import { CheckCircleOutlined, EditOutlined, DeleteOutlined, CommentOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getActivities } from '../api/stats';
import type { TaskActivityResponse } from '../types/task';

const ACTION_CONFIG: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
  CREATED: { color: 'green', icon: <CheckCircleOutlined />, label: '생성' },
  UPDATED: { color: 'blue', icon: <EditOutlined />, label: '수정' },
  DELETED: { color: 'red', icon: <DeleteOutlined />, label: '삭제' },
  COMMENTED: { color: 'purple', icon: <CommentOutlined />, label: '댓글' },
};

export default function ActivityFeed() {
  const [activities, setActivities] = useState<TaskActivityResponse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    getActivities()
      .then(setActivities)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin />;

  const items = activities.map((a) => {
    const cfg = ACTION_CONFIG[a.action] ?? { color: 'gray', icon: null, label: a.action };
    return {
      color: cfg.color,
      dot: cfg.icon,
      children: (
        <div>
          <Tag color={cfg.color}>{cfg.label}</Tag>
          <Typography.Text strong>{a.actorEmail}</Typography.Text>
          {a.taskTitle && <Typography.Text> — {a.taskTitle}</Typography.Text>}
          {a.detail && <Typography.Text type="secondary"> ({a.detail})</Typography.Text>}
          <div>
            <Typography.Text type="secondary" style={{ fontSize: 11 }}>
              {dayjs(a.occurredAt).format('MM-DD HH:mm')}
            </Typography.Text>
          </div>
        </div>
      ),
    };
  });

  return (
    <div>
      <Typography.Title level={5}>최근 활동</Typography.Title>
      {activities.length === 0 ? (
        <Typography.Text type="secondary">활동 내역이 없습니다</Typography.Text>
      ) : (
        <Timeline items={items} />
      )}
    </div>
  );
}
