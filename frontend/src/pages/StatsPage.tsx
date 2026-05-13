import { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Progress, Typography, Spin, Button, Select, Space } from 'antd';
import {
  CheckCircleOutlined, ClockCircleOutlined, ExclamationCircleOutlined,
  ProjectOutlined, MessageOutlined, WarningOutlined, RobotOutlined,
} from '@ant-design/icons';
import {
  LineChart, Line, AreaChart, Area, BarChart, Bar,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from 'recharts';
import { getStats } from '../api/stats';
import { getProjects, type ProjectResponse } from '../api/projects';
import { getBurndown, getCfd, getCycleTime, type BurndownPoint, type CfdPoint, type CycleTimeItem } from '../api/analytics';
import ActivityFeed from '../components/ActivityFeed';
import StandupModal from '../components/StandupModal';
import type { StatsResponse } from '../types/task';

function toDateRange(days: number): { from: string; to: string } {
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - days);
  return {
    from: from.toISOString().slice(0, 10),
    to: to.toISOString().slice(0, 10),
  };
}

export default function StatsPage() {
  const [stats, setStats] = useState<StatsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [standupOpen, setStandupOpen] = useState(false);
  const [burndown, setBurndown] = useState<BurndownPoint[]>([]);
  const [cfd, setCfd] = useState<CfdPoint[]>([]);
  const [cycleTime, setCycleTime] = useState<CycleTimeItem[]>([]);

  useEffect(() => {
    setLoading(true);
    getStats().then(setStats).catch(() => {}).finally(() => setLoading(false));
    getProjects().then((ps) => {
      setProjects(ps);
      if (ps.length > 0) setSelectedProjectId(ps[0].id);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (!selectedProjectId) return;
    const { from, to } = toDateRange(14);
    getBurndown(selectedProjectId, from, to).then(setBurndown).catch(() => {});
    getCfd(selectedProjectId, from, to).then(setCfd).catch(() => {});
    getCycleTime(selectedProjectId).then(setCycleTime).catch(() => {});
  }, [selectedProjectId]);

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

      {selectedProjectId != null && (
        <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
          <Col xs={24} md={12}>
            <Card title="번다운 차트 (최근 14일)">
              <ResponsiveContainer width="100%" height={220}>
                <LineChart data={burndown}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Line type="monotone" dataKey="remaining" stroke="#1677ff" name="잔여 태스크" dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </Card>
          </Col>
          <Col xs={24} md={12}>
            <Card title="누적 흐름 다이어그램 (최근 14일)">
              <ResponsiveContainer width="100%" height={220}>
                <AreaChart data={cfd}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Legend />
                  <Area type="monotone" dataKey="done" stackId="1" stroke="#52c41a" fill="#52c41a" name="완료" />
                  <Area type="monotone" dataKey="inProgress" stackId="1" stroke="#1677ff" fill="#1677ff" name="진행 중" />
                  <Area type="monotone" dataKey="todo" stackId="1" stroke="#faad14" fill="#faad14" name="할 일" />
                </AreaChart>
              </ResponsiveContainer>
            </Card>
          </Col>
          <Col xs={24}>
            <Card title="사이클 타임 (완료 태스크별 소요일)">
              {cycleTime.length === 0 ? (
                <Typography.Text type="secondary">완료된 태스크가 없습니다.</Typography.Text>
              ) : (
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={cycleTime}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="title" tick={{ fontSize: 11 }} />
                    <YAxis allowDecimals={false} label={{ value: '일', angle: -90, position: 'insideLeft' }} />
                    <Tooltip />
                    <Bar dataKey="cycleDays" fill="#722ed1" name="소요일" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </Card>
          </Col>
        </Row>
      )}

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
