import { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Progress, Typography, Spin, Button, Select, Space } from 'antd';
import {
  CheckCircleOutlined, ClockCircleOutlined, ExclamationCircleOutlined,
  ProjectOutlined, MessageOutlined, WarningOutlined, RobotOutlined,
} from '@ant-design/icons';
import { getStats } from '../api/stats';
import { getProjects, type ProjectResponse } from '../api/projects';
import ActivityFeed from '../components/ActivityFeed';
import StandupModal from '../components/StandupModal';
import type { StatsResponse } from '../types/task';

export default function StatsPage() {
  const [stats, setStats] = useState<StatsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [standupOpen, setStandupOpen] = useState(false);

  useEffect(() => {
    setLoading(true);
    getStats().then(setStats).catch(() => {}).finally(() => setLoading(false));
    getProjects().then((ps) => {
      setProjects(ps);
      if (ps.length > 0) setSelectedProjectId(ps[0].id);
    }).catch(() => {});
  }, []);

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 80 }} />;
  if (!stats) return null;

  const doneRate = stats.totalTasks > 0
    ? Math.round((stats.done / stats.totalTasks) * 100)
    : 0;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>통계 대시보드</Typography.Title>
        <Space>
          <Select
            style={{ width: 180 }}
            value={selectedProjectId}
            onChange={setSelectedProjectId}
            placeholder="프로젝트 선택"
            options={projects.map((p) => ({ value: p.id, label: p.name }))}
          />
          <Button
            type="primary"
            ghost
            icon={<RobotOutlined />}
            disabled={selectedProjectId == null}
            onClick={() => setStandupOpen(true)}
          >
            스탠드업 생성
          </Button>
        </Space>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="전체 태스크" value={stats.totalTasks} prefix={<ClockCircleOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="완료" value={stats.done} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="진행 중" value={stats.inProgress} prefix={<ClockCircleOutlined />} valueStyle={{ color: '#1677ff' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="기한 초과" value={stats.overdue} prefix={<WarningOutlined />} valueStyle={{ color: '#ff4d4f' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="프로젝트" value={stats.totalProjects} prefix={<ProjectOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="댓글" value={stats.totalComments} prefix={<MessageOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card>
            <Statistic title="높은 우선순위" value={stats.highPriority} prefix={<ExclamationCircleOutlined />} valueStyle={{ color: '#ff4d4f' }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} md={12}>
          <Card title="완료율">
            <Progress type="circle" percent={doneRate} />
            <Typography.Text style={{ marginLeft: 24 }}>
              {stats.done} / {stats.totalTasks} 완료
            </Typography.Text>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card title="상태별 분포">
            <div style={{ marginBottom: 8 }}>
              <Typography.Text>할 일</Typography.Text>
              <Progress percent={stats.totalTasks > 0 ? Math.round(stats.todo / stats.totalTasks * 100) : 0} status="normal" />
            </div>
            <div style={{ marginBottom: 8 }}>
              <Typography.Text>진행 중</Typography.Text>
              <Progress percent={stats.totalTasks > 0 ? Math.round(stats.inProgress / stats.totalTasks * 100) : 0} status="active" />
            </div>
            <div>
              <Typography.Text>완료</Typography.Text>
              <Progress percent={doneRate} status="success" />
            </div>
          </Card>
        </Col>
      </Row>

      <Row style={{ marginTop: 16 }}>
        <Col xs={24}>
          <Card>
            <ActivityFeed />
          </Card>
        </Col>
      </Row>

      {standupOpen && selectedProjectId != null && (
        <StandupModal
          open={standupOpen}
          onClose={() => setStandupOpen(false)}
          projectId={selectedProjectId}
        />
      )}
    </div>
  );
}
