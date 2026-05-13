import { Button } from 'antd';
import { SunOutlined, MoonOutlined } from '@ant-design/icons';
import { useThemeContext } from '../contexts/ThemeContext';

export default function ThemeToggle() {
  const { isDark, toggle } = useThemeContext();
  return (
    <Button
      type="text"
      icon={isDark ? <SunOutlined /> : <MoonOutlined />}
      onClick={toggle}
      aria-label={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}
      title={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}
    />
  );
}
