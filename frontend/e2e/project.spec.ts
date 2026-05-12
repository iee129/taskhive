import { test, expect } from '@playwright/test';
import { login } from './helpers';

test.describe('프로젝트', () => {
  test('프로젝트 생성', async ({ page }) => {
    await login(page);
    await page.goto('/projects');
    await page.getByRole('button', { name: '새 프로젝트' }).click();
    await page.getByLabel('프로젝트 이름').fill('E2E 테스트 프로젝트');
    await page.getByRole('button', { name: '생성' }).or(page.getByRole('button', { name: '저장' })).click();
    await expect(page.getByText('E2E 테스트 프로젝트')).toBeVisible({ timeout: 5000 });
  });

  test('멤버 초대', async ({ page }) => {
    await login(page);
    await page.goto('/projects');
    const inviteBtn = page.getByRole('button', { name: /초대|멤버/ }).first();
    await inviteBtn.click();
    await page.getByLabel(/이메일/).fill('member@example.com');
    await page.getByRole('button', { name: '초대' }).click();
    await expect(page.getByText('member@example.com').or(page.locator('.ant-message-success'))).toBeVisible({ timeout: 5000 });
  });
});
