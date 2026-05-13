import { test, expect } from '@playwright/test';
import { login } from './helpers';

test.describe('태스크 CRUD', () => {
  test('태스크 생성', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.getByRole('button', { name: '새 태스크' }).click();
    await page.getByLabel('제목').fill('E2E 테스트 태스크');
    await page.getByRole('button', { name: '저장' }).click();
    await expect(page.getByText('E2E 테스트 태스크')).toBeVisible({ timeout: 5000 });
  });

  test('태스크 상태 변경', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    const editBtn = page.getByRole('button', { name: '수정' }).first();
    await editBtn.click();
    await page.getByLabel('상태').click();
    await page.getByText('진행 중').click();
    await page.getByRole('button', { name: '저장' }).click();
    await expect(page.getByText('태스크가 수정되었습니다').or(page.locator('.ant-message-success'))).toBeVisible({ timeout: 5000 });
  });

  test('태스크 삭제', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    const titleBefore = await page.locator('table tbody tr').first().locator('td').first().textContent();
    await page.getByRole('button', { name: '삭제' }).first().click();
    await page.getByRole('button', { name: '확인' }).click();
    await expect(page.getByText('태스크가 삭제되었습니다').or(page.locator('.ant-message-success'))).toBeVisible({ timeout: 5000 });
    if (titleBefore) {
      await expect(page.getByText(titleBefore.trim())).not.toBeVisible({ timeout: 5000 });
    }
  });
});
