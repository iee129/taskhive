import { Skeleton, Space } from 'antd';

interface Props {
  rows?: number;
}

export default function SkeletonTable({ rows = 5 }: Props) {
  return (
    <Space direction="vertical" style={{ width: '100%' }} role="status" aria-label="로딩 중">
      {Array.from({ length: rows }).map((_, i) => (
        <Skeleton key={i} active paragraph={{ rows: 1 }} />
      ))}
    </Space>
  );
}
