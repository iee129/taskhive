import { useEffect, useState } from 'react';

const API_URL = import.meta.env.VITE_API_URL as string | undefined;

export default function WakingUp({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(!API_URL);
  const [elapsed, setElapsed] = useState(0);

  useEffect(() => {
    if (!API_URL) return;

    let cancelled = false;

    const poll = async () => {
      try {
        const res = await fetch(`${API_URL}/actuator/health`, { signal: AbortSignal.timeout(5000) });
        if (res.ok && !cancelled) setReady(true);
      } catch {
        // backend still sleeping
      }
    };

    poll();
    const pollTimer = setInterval(poll, 3000);
    const elapsedTimer = setInterval(() => setElapsed((s) => s + 1), 1000);

    return () => {
      cancelled = true;
      clearInterval(pollTimer);
      clearInterval(elapsedTimer);
    };
  }, []);

  if (ready) return <>{children}</>;

  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      justifyContent: 'center', minHeight: '100vh', gap: 16,
      fontFamily: 'sans-serif', color: '#555',
    }}>
      <div style={{ fontSize: 48 }}>⏳</div>
      <h2 style={{ margin: 0 }}>서버를 깨우는 중...</h2>
      <p style={{ margin: 0, color: '#888' }}>
        무료 호스팅 특성상 최대 30–60초 소요될 수 있습니다.
      </p>
      <p style={{ margin: 0, fontSize: 13, color: '#aaa' }}>{elapsed}초 경과</p>
    </div>
  );
}
