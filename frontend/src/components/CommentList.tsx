import { useEffect, useState } from 'react';
import { List, Input, Button, Avatar, Popconfirm, Typography, Space, message } from 'antd';
import { UserOutlined, SendOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getComments, addComment, deleteComment } from '../api/comments';
import type { CommentResponse } from '../types/task';

interface CommentListProps {
  taskId: number;
  currentUserEmail?: string;
}

export default function CommentList({ taskId, currentUserEmail }: CommentListProps) {
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const fetchComments = async () => {
    setLoading(true);
    try { setComments(await getComments(taskId)); }
    catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchComments(); }, [taskId]);

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
      <Typography.Title level={5} style={{ marginTop: 16 }}>댓글 ({comments.length})</Typography.Title>
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
