import { useEffect, useState } from 'react';
import {
  Card, Button, Modal, Form, Input, Avatar, Tooltip, Popconfirm,
  message, Row, Col, Typography, Space, Tag, List, AutoComplete,
} from 'antd';
import {
  PlusOutlined, TeamOutlined, DeleteOutlined, UserAddOutlined, SettingOutlined,
} from '@ant-design/icons';
import { getProjects, createProject, deleteProject, type ProjectResponse, type MemberResponse } from '../api/projects';
import { getMembers, addMember, removeMember, searchUsers, type UserSearchResult } from '../api/members';
import WebhookManager from '../components/WebhookManager';

const { Title, Text } = Typography;

const MAX_VISIBLE_AVATARS = 4;

function MemberAvatar({ member }: { member: MemberResponse }) {
  return (
    <Tooltip title={`${member.name} (${member.role === 'OWNER' ? '오너' : '멤버'})`}>
      <Avatar
        style={{ backgroundColor: member.role === 'OWNER' ? '#1677ff' : '#87ceeb', cursor: 'default' }}
        size="small"
      >
        {member.name[0]}
      </Avatar>
    </Tooltip>
  );
}

function MemberAvatarGroup({
  members,
  onClick,
}: {
  members: MemberResponse[];
  onClick: () => void;
}) {
  return (
    <div
      onClick={(e) => { e.stopPropagation(); onClick(); }}
      style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
    >
      <Avatar.Group size="small" max={{ count: MAX_VISIBLE_AVATARS }}>
        {members.map((m) => (
          <MemberAvatar key={m.userId} member={m} />
        ))}
      </Avatar.Group>
      <Tooltip title="멤버 관리">
        <Button size="small" type="text" icon={<TeamOutlined />} />
      </Tooltip>
    </div>
  );
}

function MemberModal({
  project,
  open,
  onClose,
}: {
  project: ProjectResponse;
  open: boolean;
  onClose: () => void;
}) {
  const [members, setMembers] = useState<MemberResponse[]>(project.members);
  const [searchResults, setSearchResults] = useState<UserSearchResult[]>([]);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviting, setInviting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const fetchMembers = async () => {
    const data = await getMembers(project.id);
    setMembers(data);
  };

  const handleSearch = async (value: string) => {
    if (!value || value.length < 2) { setSearchResults([]); return; }
    const results = await searchUsers(value, project.id);
    setSearchResults(results);
  };

  const handleInvite = async () => {
    if (!inviteEmail) return;
    setInviting(true);
    try {
      await addMember(project.id, inviteEmail);
      messageApi.success(`${inviteEmail} 님을 초대했습니다`);
      setInviteEmail('');
      setSearchResults([]);
      await fetchMembers();
    } catch (err: any) {
      const code = err.response?.data?.code;
      if (code === 'MEMBER_ALREADY_EXISTS') messageApi.error('이미 프로젝트 멤버입니다');
      else if (code === 'USER_NOT_FOUND') messageApi.error('가입된 사용자를 찾을 수 없습니다');
      else messageApi.error('초대에 실패했습니다');
    } finally {
      setInviting(false);
    }
  };

  const handleRemove = async (userId: number) => {
    try {
      await removeMember(project.id, userId);
      messageApi.success('멤버를 제거했습니다');
      await fetchMembers();
    } catch (err: any) {
      const code = err.response?.data?.code;
      if (code === 'LAST_OWNER') messageApi.error('유일한 오너는 제거할 수 없습니다');
      else messageApi.error('제거에 실패했습니다');
    }
  };

  const options = searchResults.map((u) => ({
    value: u.email,
    label: `${u.name} (${u.email})`,
  }));

  return (
    <Modal
      title={`${project.name} 멤버 관리`}
      open={open}
      onCancel={onClose}
      footer={null}
      width={480}
    >
      {contextHolder}
      <Space.Compact style={{ width: '100%', marginBottom: 16 }}>
        <AutoComplete
          style={{ flex: 1 }}
          options={options}
          value={inviteEmail}
          onChange={setInviteEmail}
          onSearch={handleSearch}
          onSelect={(value) => setInviteEmail(value)}
          placeholder="이메일로 사용자 검색"
        />
        <Button
          type="primary"
          icon={<UserAddOutlined />}
          loading={inviting}
          onClick={handleInvite}
          disabled={!inviteEmail}
        >
          초대
        </Button>
      </Space.Compact>

      <List
        dataSource={members}
        renderItem={(m) => (
          <List.Item
            key={m.userId}
            actions={[
              <Popconfirm
                key="remove"
                title="멤버를 제거하시겠습니까?"
                onConfirm={() => handleRemove(m.userId)}
                okText="제거"
                cancelText="취소"
              >
                <Button size="small" danger icon={<DeleteOutlined />} />
              </Popconfirm>,
            ]}
          >
            <List.Item.Meta
              avatar={
                <Avatar style={{ backgroundColor: m.role === 'OWNER' ? '#1677ff' : '#87ceeb' }}>
                  {m.name[0]}
                </Avatar>
              }
              title={
                <Space>
                  {m.name}
                  <Tag color={m.role === 'OWNER' ? 'blue' : 'default'}>
                    {m.role === 'OWNER' ? '오너' : '멤버'}
                  </Tag>
                </Space>
              }
              description={m.email}
            />
          </List.Item>
        )}
      />
    </Modal>
  );
}

export default function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [memberProject, setMemberProject] = useState<ProjectResponse | null>(null);
  const [webhookProject, setWebhookProject] = useState<ProjectResponse | null>(null);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const fetchProjects = async () => {
    setLoading(true);
    try {
      setProjects(await getProjects());
    } catch {
      messageApi.error('프로젝트 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchProjects(); }, []);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await createProject({ name: values.name, description: values.description });
      messageApi.success('프로젝트가 생성되었습니다');
      form.resetFields();
      setCreateOpen(false);
      fetchProjects();
    } catch (err: any) {
      if (err?.errorFields) return;
      messageApi.error('프로젝트 생성에 실패했습니다');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteProject(id);
      messageApi.success('프로젝트가 삭제되었습니다');
      fetchProjects();
    } catch (err: any) {
      const code = err.response?.data?.code;
      if (code === 'FORBIDDEN') messageApi.error('오너만 프로젝트를 삭제할 수 있습니다');
      else messageApi.error('삭제에 실패했습니다');
    }
  };

  return (
    <div>
      {contextHolder}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={4} style={{ margin: 0 }}>프로젝트</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          새 프로젝트
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        {projects.map((project) => (
          <Col key={project.id} xs={24} sm={12} lg={8}>
            <Card
              loading={loading}
              title={project.name}
              extra={
                <Space>
                  <Button size="small" type="text" icon={<SettingOutlined />} title="웹훅 관리" onClick={(e) => { e.stopPropagation(); setWebhookProject(project); }} />
                  <Popconfirm
                    title="프로젝트를 삭제하시겠습니까?"
                    onConfirm={() => handleDelete(project.id)}
                    okText="삭제"
                    cancelText="취소"
                  >
                    <Button size="small" type="text" danger icon={<DeleteOutlined />} />
                  </Popconfirm>
                </Space>
              }
              style={{ height: '100%' }}
            >
              {project.description && (
                <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
                  {project.description}
                </Text>
              )}
              <div style={{ marginTop: 'auto' }}>
                <MemberAvatarGroup
                  members={project.members}
                  onClick={() => setMemberProject(project)}
                />
              </div>
            </Card>
          </Col>
        ))}

        {!loading && projects.length === 0 && (
          <Col span={24}>
            <div style={{ textAlign: 'center', padding: 48, color: '#999' }}>
              <TeamOutlined style={{ fontSize: 48, marginBottom: 16 }} />
              <div>프로젝트가 없습니다. 새 프로젝트를 만들어보세요.</div>
            </div>
          </Col>
        )}
      </Row>

      <Modal
        title="새 프로젝트"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="생성"
        cancelText="취소"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="프로젝트 이름" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="프로젝트 이름" />
          </Form.Item>
          <Form.Item name="description" label="설명">
            <Input.TextArea rows={3} placeholder="프로젝트 설명 (선택)" />
          </Form.Item>
        </Form>
      </Modal>

      {memberProject && (
        <MemberModal
          project={memberProject}
          open={!!memberProject}
          onClose={() => setMemberProject(null)}
        />
      )}

      {webhookProject && (
        <Modal
          title={`웹훅 관리 — ${webhookProject.name}`}
          open={!!webhookProject}
          onCancel={() => setWebhookProject(null)}
          footer={null}
          width={700}
        >
          <WebhookManager projectId={webhookProject.id} />
        </Modal>
      )}
    </div>
  );
}
