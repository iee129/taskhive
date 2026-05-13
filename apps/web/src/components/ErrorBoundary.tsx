import { Component, type ReactNode, type ErrorInfo } from 'react';
import { Button, Result } from 'antd';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  reset = () => this.setState({ hasError: false, error: undefined });

  render() {
    if (this.state.hasError) {
      return this.props.fallback ?? (
        <Result
          status="error"
          title="예기치 않은 오류가 발생했습니다"
          subTitle={this.state.error?.message}
          extra={<Button type="primary" onClick={this.reset}>다시 시도</Button>}
        />
      );
    }
    return this.props.children;
  }
}
