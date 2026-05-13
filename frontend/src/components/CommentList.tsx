import { useEffect, useState } from 'react';
import { List, Input, Button, Avatar, Popconfirm, Typography, Space, message } from 'antd';
import { UserOutlined, SendOutlined, DeleteOutlined, RobotOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getComments, addComment, deleteComment } from '../api/comments';
import { summarizeTask } from '../api/ai';
import type { CommentResponse } from '../types/task';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

interface CommentListProps {
  taskId: number;
  currentUserEmail?: string;
}

export default function CommentList({ taskId, currentUserEmail }: CommentListProps) {
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [aiEnabled, setAiEnabled] = useState(false);
  const [summarizing, setSummarizing] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const fetchComments = async () => {
    setLoading(true);
    try { setComments(await getComments(taskId)); }
    catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchComments(); }, [taskId]);

  useEffect(() => {
    fetch(`${API_URL}/api/ai/capabilities`)
      .then((r) => r.json())
      .then((data: { enabled: boolean }) => setAiEnabled(data.enabled))
      .catch(() => null);
  }, []);

  const handleAiSummary = async () => {
    setSummarizing(true);
    try {
      await summarizeTask(taskId);
      fetchComments();
    } catch {
      messageApi.error('AI 요약 생성 실패. AI provider를 사용할 수 없습니다.');
    } finally {
      setSummarizing(false);
    }
  };

  const handleSubmit = async () => {
    if (!content.trim()) return;
    setSubmitting(true);
    try {
      await addComment(taskId, content.trim());
      setContent('');
      fetchComments();
    } catch {
      messageApi.error('댓글 추가 실패');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId: number) => {
    try {
      await deleteComment(taskId, commentId);
      fetchComments();
    } catch {
      messageApi.error('댓글 삭제 실패');
    }
  };

  return (
    <div>
      {contextHolder}
      <Space style={{ marginTop: 16, marginBottom: 4, width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={5} style={{ margin: 0 }}>댓글 ({comments.length})</Typography.Title>
        {aiEnabled && (
          <Button
            icon={<RobotOutlined />}
            size="small"
            onClick={handleAiSummary}
            loading={summarizing}
          >
            AI 요약 생성
          </Button>
        )}
      </Space>
      <List
        loading={loading}
        dataSource={comments}
        renderItem={(comment) => (
          <List.Item
            actions={currentUserEmail === comment.authorEmail ? [
              <Popconfirm title="삭제하시겠습니까?" onConfirm={() => handleDelete(comment.id)} okText="삭제" cancelText="취소">
                <Button type="text" danger icon={<DeleteOutlined />} size="small" />
              </Popconfirm>
            ] : []}
          >
            <List.Item.Meta
              avatar={<Avatar icon={<UserOutlined />} />}
              title={
                <Space>
                  <Typography.Text strong>{comment.authorEmail}</Typography.Text>
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {dayjs(comment.createdAt).format('YYYY-MM-DD HH:mm')}
                  </Typography.Text>
                </Space>
              }
              description={comment.content}
            />
          </List.Item>
        )}
      />
      <Space.Compact style={{ width: '100%', marginTop: 8 }}>
        <Input
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="댓글을 입력하세요"
          onPressEnter={handleSubmit}
        />
        <Button type="primary" icon={<SendOutlined />} onClick={handleSubmit} loading={submitting}>
          등록
        </Button>
      </Space.Compact>
    </div>
  );
}
