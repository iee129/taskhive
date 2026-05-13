import { useState, useCallback } from 'react';
import { checkEmail } from '../api/auth';

export function useCheckEmail() {
  const [checking, setChecking] = useState(false);

  const validate = useCallback(async (_: unknown, value: string): Promise<void> => {
    if (!value || !/\S+@\S+\.\S+/.test(value)) return;
    setChecking(true);
    try {
      const { available } = await checkEmail(value);
      if (!available) return Promise.reject(new Error('이미 사용 중인 이메일입니다'));
    } finally {
      setChecking(false);
    }
  }, []);

  return { validate, checking };
}
