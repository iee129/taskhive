import { useEffect, useState, useCallback, useRef } from 'react';
import { Modal, Input, List, Typography, Tag, Divider, Space } from 'antd';
import {
  SearchOutlined, CheckSquareOutlined, ProjectOutlined,
  AppstoreOutlined, BarChartOutlined, SettingOutlined,
  UserOutlined, QuestionOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { search, type SearchResult } from '../api/search';

const { Text } = Typography;

const NAV_ITEMS = [
  { label: '프로젝트', path: '/projects', icon: <ProjectOutlined /> },
  { label: '태스크', path: '/tasks', icon: <CheckSquareOutlined /> },
  { label: '칸반 보드', path: '/kanban', icon: <AppstoreOutlined /> },
  { label: '통계', path: '/stats', icon: <BarChartOutlined /> },
  { label: '프로필', path: '/profile', icon: <UserOutlined /> },
  { label: '설정', path: '/settings', icon: <SettingOutlined /> },
];

const SHORTCUTS = [
  { key: 'Cmd+K', desc: '커맨드 팔레트 열기' },
  { key: 'c', desc: '태스크 페이지로 이동' },
  { key: '/', desc: '검색 포커스' },
  { key: '?', desc: '단축키 목록' },
  { key: 'Esc', desc: '닫기' },
];

interface Props {
  open: boolean;
  onClose: () => void;
}

export default function CommandPalette({ open, onClose }: Props) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [shortcutSheet, setShortcutSheet] = useState(false);
  const inputRef = useRef<any>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!open) { setQuery(''); setResults([]); return; }
    setTimeout(() => inputRef.current?.focus(), 100);
  }, [open]);

  useEffect(() => {
    if (!query.trim()) { setResults([]); return; }
    const timer = setTimeout(async () => {
      setLoading(true);
      try {
        setResults(await search(query));
      } catch {
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 250);
    return () => clearTimeout(timer);
  }, [query]);

  const handleNav = useCallback((path: string) => {
    navigate(path);
    onClose();
  }, [navigate, onClose]);

  const handleResult = useCallback((r: SearchResult) => {
    if (r.type === 'task') navigate('/tasks');
    else if (r.type === 'project') navigate('/projects');
    onClose();
  }, [navigate, onClose]);

  return (
    <>
      <Modal
        open={open}
        onCancel={onClose}
        footer={null}
        width={600}
        style={{ top: 80 }}
        styles={{ body: { padding: 0 } }}
        closable={false}
      >
        <Input
          ref={inputRef}
          prefix={<SearchOutlined />}
          placeholder="페이지 이동, 태스크 검색..."
          value={query}
          onChange={e => setQuery(e.target.value)}
          size="large"
          style={{ border: 'none', borderBottom: '1px solid #f0f0f0', borderRadius: 0 }}
          allowClear
        />

        {!query && (
          <>
            <div style={{ padding: '8px 12px 4px', color: '#999', fontSize: 12 }}>이동</div>
            <List
              dataSource={NAV_ITEMS}
              renderItem={item => (
                <List.Item
                  onClick={() => handleNav(item.path)}
                  style={{ padding: '8px 16px', cursor: 'pointer' }}
                  className="palette-item"
                >
                  <Space>
                    {item.icon}
                    <Text>{item.label}</Text>
                  </Space>
                </List.Item>
              )}
            />
            <Divider style={{ margin: 0 }} />
            <div
              style={{ padding: '8px 16px', cursor: 'pointer', color: '#666', fontSize: 13 }}
              onClick={() => { onClose(); setShortcutSheet(true); }}
            >
              <QuestionOutlined style={{ marginRight: 8 }} />단축키 목록 보기
            </div>
          </>
        )}

        {query && (
          <List
            loading={loading}
            dataSource={results}
            locale={{ emptyText: loading ? '검색 중...' : '결과 없음' }}
            renderItem={r => (
              <List.Item
                onClick={() => handleResult(r)}
                style={{ padding: '8px 16px', cursor: 'pointer' }}
              >
                <Space>
                  <Tag color={r.type === 'task' ? 'blue' : 'green'}>
                    {r.type === 'task' ? '태스크' : '프로젝트'}
                  </Tag>
                  <Text>{r.title}</Text>
                  {r.subtitle && <Text type="secondary" style={{ fontSize: 12 }}>{r.subtitle}</Text>}
                </Space>
              </List.Item>
            )}
          />
        )}

        <div style={{ padding: '4px 16px 8px', color: '#bbb', fontSize: 11 }}>
          ↑↓ 탐색 · Enter 선택 · Esc 닫기
        </div>
      </Modal>

      <Modal
        open={shortcutSheet}
        onCancel={() => setShortcutSheet(false)}
        footer={null}
        title="키보드 단축키"
        width={400}
      >
        {SHORTCUTS.map(s => (
          <div key={s.key} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0' }}>
            <Text type="secondary">{s.desc}</Text>
            <Tag>{s.key}</Tag>
          </div>
        ))}
      </Modal>
    </>
  );
}
