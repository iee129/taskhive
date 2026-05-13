import { Page } from '@playwright/test';

export async function login(page: Page) {
  await page.goto('/login');
  await page.getByLabel('이메일').fill('test@example.com');
  await page.getByLabel('비밀번호').fill('Test1234!');
  await page.getByRole('button', { name: '로그인' }).click();
  await page.waitForURL(/\/(tasks|projects|$)/);
}
