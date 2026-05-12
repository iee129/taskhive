import { Input, Select, Space, Button } from 'antd';
import { SearchOutlined, ClearOutlined } from '@ant-design/icons';
import type { TaskStatus, TaskPriority } from '../types/task';

interface FilterBarProps {
  status?: TaskStatus;
  priority?: TaskPriority;
  search?: string;
  onStatusChange: (v?: TaskStatus) => void;
  onPriorityChange: (v?: TaskPriority) => void;
  onSearchChange: (v: string) => void;
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
  status, priority, search,
  onStatusChange, onPriorityChange, onSearchChange, onClear,
}: FilterBarProps) {
  return (
    <Space wrap style={{ marginBottom: 16 }}>
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
      <Button icon={<ClearOutlined />} onClick={onClear}>초기화</Button>
    </Space>
  );
}
