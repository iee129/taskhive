import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import FilterBar from '../components/FilterBar';

const defaultProps = {
  status: undefined,
  priority: undefined,
  search: '',
  onStatusChange: vi.fn(),
  onPriorityChange: vi.fn(),
  onSearchChange: vi.fn(),
  onClear: vi.fn(),
};

describe('FilterBar', () => {
  it('초기 렌더링 — 검색 입력창 표시', () => {
    render(<FilterBar {...defaultProps} />);
    expect(screen.getByPlaceholderText('제목 검색')).toBeInTheDocument();
  });

  it('검색 입력 — onSearchChange 호출', async () => {
    const onSearchChange = vi.fn();
    render(<FilterBar {...defaultProps} onSearchChange={onSearchChange} />);
    const input = screen.getByPlaceholderText('제목 검색');
    await userEvent.type(input, '태스크');
    expect(onSearchChange).toHaveBeenCalled();
  });

  it('초기화 버튼 — onClear 호출', async () => {
    const onClear = vi.fn();
    render(<FilterBar {...defaultProps} onClear={onClear} />);
    const clearBtn = screen.getByRole('button', { name: /초기화/i });
    await userEvent.click(clearBtn);
    expect(onClear).toHaveBeenCalledTimes(1);
  });

  it('상태 Select placeholder 표시', () => {
    render(<FilterBar {...defaultProps} />);
    expect(screen.getByText('상태 필터')).toBeInTheDocument();
  });

  it('우선순위 Select placeholder 표시', () => {
    render(<FilterBar {...defaultProps} />);
    expect(screen.getByText('우선순위 필터')).toBeInTheDocument();
  });

  it('search 값 — 입력창에 반영', () => {
    render(<FilterBar {...defaultProps} search="spring" />);
    const input = screen.getByPlaceholderText('제목 검색') as HTMLInputElement;
    expect(input.value).toBe('spring');
  });
});
