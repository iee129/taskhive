import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Table, Popconfirm, message, Space, Tag, Typography, Alert } from 'antd';
import { PlusOutlined, KeyOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { createToken, listTokens, revokeToken, type TokenListItem } from '../api/tokens';

const { Paragraph, Text } = Typography;

export default function SettingsPage() {
  const [tokens, setTokens] = useState<TokenListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [newToken, setNewToken] = useState<string | null>(null);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const fetchTokens = async () => {
    setLoading(true);
    try {
      setTokens(await listTokens());
    } catch {
      messageApi.error('토큰 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTokens(); }, []);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      const result = await createToken(values.name);
      setNewToken(result.token);
      form.resetFields();
      setCreateOpen(false);
      fetchTokens();
    } catch (err: any) {
      if (err?.errorFields) return;
      messageApi.error('토큰 생성에 실패했습니다');
    }
  };

  const handleRevoke = async (id: number) => {
    try {
      await revokeToken(id);
      messageApi.success('토큰이 폐기되었습니다');
      fetchTokens();
    } catch {
      messageApi.error('폐기에 실패했습니다');
    }
  };

  const columns = [
    { title: '이름', dataIndex: 'name', key: 'name' },
    { title: '권한', dataIndex: 'scopes', key: 'scopes', render: (v: string) => <Tag color="blue">{v}</Tag> },
    {
      title: '마지막 사용',
      dataIndex: 'lastUsedAt',
      key: 'lastUsedAt',
      render: (v: string | null) => v ? dayjs(v).format('YYYY-MM-DD HH:mm') : <Text type="secondary">미사용</Text>,
    },
    {
      title: '생성일',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD'),
    },
    {
      title: '작업',
      key: 'actions',
      render: (_: unknown, record: TokenListItem) => (
        <Popconfirm
          title="이 토큰을 폐기하시겠습니까? 복구할 수 없습니다."
          onConfirm={() => handleRevoke(record.id)}
          okText="폐기"
          cancelText="취소"
          okButtonProps={{ danger: true }}
        >
          <Button size="small" danger>폐기</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <>
      {contextHolder}
      <div style={{ maxWidth: 800 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h2 style={{ margin: 0 }}>개인 API 토큰</h2>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            토큰 생성
          </Button>
        </div>

        <Typography.Paragraph type="secondary">
          개인 API 토큰(PAT)은 스크립트나 외부 도구에서 TaskHive API를 인증하는 데 사용됩니다.
          토큰은 생성 시 한 번만 표시됩니다.
        </Typography.Paragraph>

        {newToken && (
          <Alert
            type="success"
            showIcon
            icon={<KeyOutlined />}
            style={{ marginBottom: 16 }}
            message="새 토큰이 생성되었습니다. 지금 복사하세요 — 다시 표시되지 않습니다."
            description={
              <Space direction="vertical" style={{ width: '100%' }}>
                <Paragraph copyable code style={{ marginBottom: 0 }}>{newToken}</Paragraph>
                <Button size="small" onClick={() => setNewToken(null)}>닫기</Button>
              </Space>
            }
          />
        )}

        <Table
          rowKey="id"
          columns={columns}
          dataSource={tokens}
          loading={loading}
          pagination={false}
        />
      </div>

      <Modal
        title="토큰 생성"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="생성"
        cancelText="취소"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="토큰 이름"
            rules={[{ required: true, message: '이름을 입력하세요' }]}
          >
            <Input placeholder="예: CI/CD Pipeline, Local Dev" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
