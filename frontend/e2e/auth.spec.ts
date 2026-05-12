import { test, expect } from '@playwright/test';

test.describe('인증 E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.evaluate(() => localStorage.clear());
  });

  test('로그인 페이지 접근', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByText('로그인')).toBeVisible();
    await expect(page.getByLabel('이메일')).toBeVisible();
    await expect(page.getByLabel('비밀번호')).toBeVisible();
  });

  test('미인증 상태 — /tasks 접근 시 /login 리다이렉트', async ({ page }) => {
    await page.goto('/tasks');
    await expect(page).toHaveURL(/\/login/);
  });

  test('로그인 폼 — 필수 필드 유효성 검사', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page.getByText(/필수/i).first()).toBeVisible();
  });

  test('회원가입 링크 동작', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('link', { name: '회원가입' }).click();
    await expect(page).toHaveURL(/\/register/);
  });
});
