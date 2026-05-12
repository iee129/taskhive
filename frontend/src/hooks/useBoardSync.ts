import { useEffect, useRef, useCallback } from 'react';
import { Client, IMessage } from '@stomp/stompjs';

export interface TaskEvent {
  type: 'TASK_CREATED' | 'TASK_UPDATED' | 'TASK_DELETED';
  taskId: number;
  updatedBy: string;
  payload: Record<string, unknown>;
}

export function useBoardSync(onEvent: (event: TaskEvent) => void) {
  const clientRef = useRef<Client | null>(null);
  const onEventRef = useRef(onEvent);
  onEventRef.current = onEvent;

  const stableHandler = useCallback((msg: IMessage) => {
    try {
      const event = JSON.parse(msg.body) as TaskEvent;
      onEventRef.current(event);
    } catch {
      // 파싱 실패 무시
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');

    const client = new Client({
      brokerURL: `ws://${window.location.hostname}:8080/ws`,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/topic/tasks', stableHandler);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [stableHandler]);
}
