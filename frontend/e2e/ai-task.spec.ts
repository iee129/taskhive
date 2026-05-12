import { test, expect } from '@playwright/test';
import { login } from './helpers';

test.describe('AI 태스크 생성', () => {
  test('AI 제안 모달 오픈 및 자연어 입력', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.getByRole('button', { name: /AI 생성|AI로 작성/ }).click();
    await expect(page.getByText('AI 태스크 생성')).toBeVisible();
    await page.getByPlaceholder(/자유롭게 설명/).fill('로그인 API 구현, 이번 주 금요일까지, 높음');
    await page.getByRole('button', { name: 'AI 제안 받기' }).click();
    await expect(
      page.locator('.ant-form-item').first().or(page.locator('.ant-message'))
    ).toBeVisible({ timeout: 15000 });
  });

  test('Ollama 오류 시 에러 메시지 표시 (mock)', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');

    await page.route('**/api/ai/suggest-task', (route) =>
      route.fulfill({ status: 500, body: 'Internal Server Error' })
    );

    await page.getByRole('button', { name: /AI 생성|AI로 작성/ }).click();
    await page.getByPlaceholder(/자유롭게 설명/).fill('테스트 설명');
    await page.getByRole('button', { name: 'AI 제안 받기' }).click();
    await expect(page.locator('.ant-message-error')).toBeVisible({ timeout: 5000 });
  });
});
