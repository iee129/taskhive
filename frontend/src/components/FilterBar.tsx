import { useEffect, useRef, useState } from 'react';
import { Input, Select, Space, Button, message } from 'antd';
import { SearchOutlined, ClearOutlined, RobotOutlined } from '@ant-design/icons';
import type { TaskStatus, TaskPriority } from '../types/task';
import type { LabelResponse } from '../api/labels';
import { parseFilter } from '../api/ai';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

interface FilterBarProps {
  status?: TaskStatus;
  priority?: TaskPriority;
  search?: string;
  labelId?: number;
  labels?: LabelResponse[];
  onStatusChange: (v?: TaskStatus) => void;
  onPriorityChange: (v?: TaskPriority) => void;
  onSearchChange: (v: string) => void;
  onLabelChange: (v?: number) => void;
  onClear: () => void;
}

const STATUS_OPTIONS = [
  { value: 'TODO', label: '할 일' },
  { value: 'IN_PROGRESS', label: '진행 중' },
  { value: 'DONE', label: '완료' },
];

const PRIORITY_OPTIONS = [
  { value: 'HIGH', label: '높음' },
  { value: 'MEDIUM', label: '보통' },
  { value: 'LOW', label: '낮음' },
];

export default function FilterBar({
  status, priority, search, labelId, labels,
  onStatusChange, onPriorityChange, onSearchChange, onLabelChange, onClear,
}: FilterBarProps) {
  const [aiEnabled, setAiEnabled] = useState(false);
  const [nlQuery, setNlQuery] = useState('');
  const [nlLoading, setNlLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const fetchedRef = useRef(false);

  useEffect(() => {
    if (fetchedRef.current) return;
    fetchedRef.current = true;
    fetch(`${API_URL}/api/ai/capabilities`)
      .then((r) => r.json())
      .then((data: { enabled: boolean }) => setAiEnabled(data.enabled))
      .catch(() => null);
  }, []);

  const handleNlFilter = async () => {
    if (!nlQuery.trim()) return;
    setNlLoading(true);
    try {
      const result = await parseFilter(nlQuery);
      if (result.status) onStatusChange(result.status as TaskStatus);
      if (result.priority) onPriorityChange(result.priority as TaskPriority);
      setNlQuery('');
    } catch {
      messageApi.error('AI 필터 파싱 실패');
    } finally {
      setNlLoading(false);
    }
  };

  return (
    <Space wrap style={{ marginBottom: 16 }}>
      {contextHolder}
      {aiEnabled && (
        <Input.Search
          placeholder="자연어로 필터 입력 (예: 이번 주 마감 HIGH)"
          value={nlQuery}
          onChange={(e) => setNlQuery(e.target.value)}
          onSearch={handleNlFilter}
          enterButton={<Button icon={<RobotOutlined />} loading={nlLoading}>AI 필터</Button>}
          style={{ width: 320 }}
          loading={nlLoading}
        />
      )}
      <Input
        placeholder="제목 검색"
        prefix={<SearchOutlined />}
        value={search}
        onChange={(e) => onSearchChange(e.target.value)}
        style={{ width: 200 }}
        allowClear
      />
      <Select
        placeholder="상태 필터"
        value={status}
        onChange={onStatusChange}
        allowClear
        style={{ width: 130 }}
        options={STATUS_OPTIONS}
      />
      <Select
        placeholder="우선순위 필터"
        value={priority}
        onChange={onPriorityChange}
        allowClear
        style={{ width: 130 }}
        options={PRIORITY_OPTIONS}
      />
      {labels && labels.length > 0 && (
        <Select
          placeholder="라벨 필터"
          value={labelId}
          onChange={onLabelChange}
          allowClear
          style={{ width: 140 }}
          options={labels.map((l) => ({ value: l.id, label: l.name }))}
        />
      )}
      <Button icon={<ClearOutlined />} onClick={onClear}>초기화</Button>
    </Space>
  );
}
