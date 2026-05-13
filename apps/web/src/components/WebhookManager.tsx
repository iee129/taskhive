import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Table, Popconfirm, message, Space, Tag, Typography } from 'antd';
import { PlusOutlined, LinkOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import client from '../api/client';

interface Webhook {
  id: number;
  url: string;
  events: string;
  enabled: boolean;
  consecutiveFailures: number;
  createdAt: string;
}

interface Props {
  projectId: number;
}

export default function WebhookManager({ projectId }: Props) {
  const [webhooks, setWebhooks] = useState<Webhook[]>([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const fetch = async () => {
    setLoading(true);
    try {
      const res = await client.get<Webhook[]>(`/api/projects/${projectId}/webhooks`);
      setWebhooks(res.data);
    } catch {
      messageApi.error('웹훅 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetch(); }, [projectId]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await client.post(`/api/projects/${projectId}/webhooks`, values);
      form.resetFields();
      setCreateOpen(false);
      messageApi.success('웹훅이 등록되었습니다');
      fetch();
    } catch (err: any) {
      if (err?.errorFields) return;
      const msg = err?.response?.data?.message ?? '등록에 실패했습니다';
      messageApi.error(msg);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await client.delete(`/api/projects/${projectId}/webhooks/${id}`);
      messageApi.success('웹훅이 삭제되었습니다');
      fetch();
    } catch {
      messageApi.error('삭제에 실패했습니다');
    }
  };

  const columns = [
    { title: 'URL', dataIndex: 'url', key: 'url', render: (v: string) => <Typography.Text code>{v}</Typography.Text> },
    { title: '이벤트', dataIndex: 'events', key: 'events', render: (v: string) => v.split(',').map((e) => <Tag key={e}>{e}</Tag>) },
    {
      title: '상태', dataIndex: 'enabled', key: 'enabled',
      render: (v: boolean, r: Webhook) => v
        ? <Tag color="green">활성</Tag>
        : <Tag color="red">비활성 (실패 {r.consecutiveFailures}회)</Tag>,
    },
    { title: '등록일', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => dayjs(v).format('YYYY-MM-DD') },
    {
      title: '작업', key: 'actions',
      render: (_: unknown, r: Webhook) => (
        <Popconfirm title="이 웹훅을 삭제하시겠습니까?" onConfirm={() => handleDelete(r.id)} okText="삭제" cancelText="취소" okButtonProps={{ danger: true }}>
          <Button size="small" danger>삭제</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <>
      {contextHolder}
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Text strong>아웃고잉 웹훅</Typography.Text>
        <Button size="small" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>웹훅 추가</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={webhooks} loading={loading} size="small" pagination={false} />

      <Modal title={<Space><LinkOutlined />웹훅 등록</Space>} open={createOpen}
        onOk={handleCreate} onCancel={() => { setCreateOpen(false); form.resetFields(); }} okText="등록" cancelText="취소">
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="url" label="URL" rules={[{ required: true, message: 'URL을 입력하세요' }, { type: 'url', message: '올바른 URL 형식이어야 합니다' }]}>
            <Input placeholder="https://example.com/hook" />
          </Form.Item>
          <Form.Item name="secret" label="시크릿 (선택)">
            <Input.Password placeholder="HMAC-SHA256 서명에 사용됩니다" />
          </Form.Item>
          <Form.Item name="events" label="이벤트">
            <Input defaultValue="task.created,task.updated,task.deleted" placeholder="task.created,task.updated,task.deleted" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
