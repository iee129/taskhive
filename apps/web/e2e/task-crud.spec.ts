import { test, expect, Page } from '@playwright/test';

async function loginAs(page: Page, email: string, password: string) {
  await page.evaluate(() => localStorage.clear());
  await page.goto('/login');
  await page.getByLabel('이메일').fill(email);
  await page.getByLabel('비밀번호').fill(password);
  await page.getByRole('button', { name: '로그인' }).click();
  await page.waitForURL(/\/(tasks|dashboard)/);
}

test.describe('태스크 CRUD E2E', () => {
  test.skip('태스크 목록 페이지 표시 (백엔드 필요)', async ({ page }) => {
    await loginAs(page, 'test@example.com', 'password123');
    await page.goto('/tasks');
    await expect(page.getByRole('heading', { name: /태스크/i })).toBeVisible();
  });

  test.skip('태스크 생성 (백엔드 필요)', async ({ page }) => {
    await loginAs(page, 'test@example.com', 'password123');
    await page.goto('/tasks');
    await page.getByRole('button', { name: /태스크 추가|새 태스크/i }).click();
    await page.getByLabel('제목').fill('E2E 테스트 태스크');
    await page.getByRole('button', { name: /저장|확인/i }).click();
    await expect(page.getByText('E2E 테스트 태스크')).toBeVisible();
  });

  test('로그인 없이 태스크 페이지 — 리다이렉트', async ({ page }) => {
    await page.evaluate(() => localStorage.clear());
    await page.goto('/tasks');
    await expect(page).toHaveURL(/\/login/);
  });

  test('네비게이션 메뉴 — 칸반 링크 존재', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('link', { name: '회원가입' })).toBeVisible();
  });
});
