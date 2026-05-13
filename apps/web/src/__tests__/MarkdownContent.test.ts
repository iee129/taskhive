import { describe, it, expect } from 'vitest';
import DOMPurify from 'dompurify';

// vitest runs in jsdom environment (configured in vite.config.ts),
// so DOMPurify can use the global window directly.

describe('XSS sanitization', () => {
  it('removes script tags', () => {
    const result = DOMPurify.sanitize('<script>alert("xss")</script>hello');
    expect(result).not.toContain('<script>');
    expect(result).toContain('hello');
  });

  it('removes onerror attributes', () => {
    const result = DOMPurify.sanitize('<img src=x onerror="alert(1)">');
    expect(result).not.toContain('onerror');
  });

  it('allows safe markdown content', () => {
    const result = DOMPurify.sanitize('**bold** and _italic_');
    expect(result).toBe('**bold** and _italic_');
  });

  it('removes javascript: href', () => {
    const result = DOMPurify.sanitize('<a href="javascript:alert(1)">click</a>');
    expect(result).not.toContain('javascript:');
  });
});
