import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Card, Spin, Result, Button } from 'antd';
import { verifyEmail } from '../api/auth';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    if (!token) {
      setStatus('error');
      setErrorMsg('유효하지 않은 링크입니다.');
      return;
    }
    verifyEmail(token)
      .then(() => setStatus('success'))
      .catch((err: any) => {
        setStatus('error');
        setErrorMsg(err.response?.data?.message ?? '인증에 실패했습니다.');
      });
  }, []);

  if (status === 'loading') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <Spin size="large" tip="이메일 인증 중..." />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
      <Card style={{ width: 400 }}>
        {status === 'success' ? (
          <Result
            status="success"
            title="이메일 인증 완료!"
            subTitle="이메일 인증이 완료되었습니다. 로그인해주세요."
            extra={<Button type="primary"><Link to="/login">로그인하기</Link></Button>}
          />
        ) : (
          <Result
            status="error"
            title="인증 실패"
            subTitle={errorMsg}
            extra={<Button><Link to="/login">로그인 페이지로</Link></Button>}
          />
        )}
      </Card>
    </div>
  );
}
