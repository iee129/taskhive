import { test, expect } from '@playwright/test';

test.describe('인증', () => {
  test('로그인 성공', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('이메일').fill('test@example.com');
    await page.getByLabel('비밀번호').fill('Test1234!');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/(tasks|projects|$)/);
  });

  test('로그인 실패 — 잘못된 비밀번호', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('이메일').fill('test@example.com');
    await page.getByLabel('비밀번호').fill('wrongpassword');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page.getByRole('alert').or(page.locator('.ant-message-error'))).toBeVisible({ timeout: 5000 });
  });

  test('로그아웃', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('이메일').fill('test@example.com');
    await page.getByLabel('비밀번호').fill('Test1234!');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/(tasks|projects|$)/);

    const logoutBtn = page.getByRole('button', { name: /로그아웃/ })
      .or(page.getByText('로그아웃'));
    await logoutBtn.click();
    await expect(page).toHaveURL(/\/login/);
  });
});
