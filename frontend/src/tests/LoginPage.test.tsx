import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

function renderLogin() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );
}

describe('LoginPage', () => {
  it('로그인 폼 렌더링', () => {
    renderLogin();
    expect(screen.getByRole('heading', { name: '로그인' })).toBeInTheDocument();
    expect(screen.getByLabelText('이메일')).toBeInTheDocument();
    expect(screen.getByLabelText('비밀번호')).toBeInTheDocument();
  });

  it('로그인 버튼 표시', () => {
    renderLogin();
    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument();
  });

  it('정상 로그인 — 토큰 저장 후 이동', async () => {
    renderLogin();
    await userEvent.type(screen.getByLabelText('이메일'), 'test@example.com');
    await userEvent.type(screen.getByLabelText('비밀번호'), 'password123');
    await userEvent.click(screen.getByRole('button', { name: '로그인' }));

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
      expect(mockNavigate).toHaveBeenCalledWith('/tasks');
    });
  });

  it('잘못된 자격증명 — 에러 메시지', async () => {
    renderLogin();
    await userEvent.type(screen.getByLabelText('이메일'), 'test@example.com');
    await userEvent.type(screen.getByLabelText('비밀번호'), 'wrongpassword');
    await userEvent.click(screen.getByRole('button', { name: '로그인' }));

    await waitFor(() => {
      expect(screen.getByText('이메일 또는 비밀번호가 올바르지 않습니다')).toBeInTheDocument();
    });
  });

  it('회원가입 링크 표시', () => {
    renderLogin();
    expect(screen.getByRole('link', { name: '회원가입' })).toBeInTheDocument();
  });
});
