import { createContext, useContext, type ReactNode } from 'react';
import { notification } from 'antd';

type NotifyFn = (message: string, description?: string) => void;

interface NotificationContextValue {
  notifySuccess: NotifyFn;
  notifyError: NotifyFn;
  notifyWarning: NotifyFn;
}

const NotificationContext = createContext<NotificationContextValue | null>(null);

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [api, contextHolder] = notification.useNotification();

  return (
    <NotificationContext.Provider value={{
      notifySuccess: (message, description) =>
        api.success({ message, description, placement: 'topRight' }),
      notifyError: (message, description) =>
        api.error({ message, description, placement: 'topRight' }),
      notifyWarning: (message, description) =>
        api.warning({ message, description, placement: 'topRight' }),
    }}>
      {contextHolder}
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotification() {
  const ctx = useContext(NotificationContext);
  if (!ctx) throw new Error('useNotification must be inside NotificationProvider');
  return ctx;
}
