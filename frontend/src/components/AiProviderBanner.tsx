import { useEffect, useState } from 'react';
import { Alert } from 'antd';

interface AiCapabilities {
  provider: string;
  cloudProvider: boolean;
}

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export default function AiProviderBanner() {
  const [capabilities, setCapabilities] = useState<AiCapabilities | null>(null);

  useEffect(() => {
    fetch(`${API_URL}/api/ai/capabilities`)
      .then((r) => r.json())
      .then((data: AiCapabilities) => setCapabilities(data))
      .catch(() => null);
  }, []);

  if (!capabilities?.cloudProvider) return null;

  return (
    <Alert
      type="warning"
      banner
      message={`AI가 클라우드(${capabilities.provider}) 모드로 동작 중입니다. 입력한 태스크 설명이 외부 서버로 전송됩니다. 프라이버시가 중요한 경우 Ollama(로컬)로 전환하세요.`}
    />
  );
}
