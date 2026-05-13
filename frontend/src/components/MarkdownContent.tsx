import ReactMarkdown from 'react-markdown';
import DOMPurify from 'dompurify';

export default function MarkdownContent({ content }: { content: string }) {
  const clean = DOMPurify.sanitize(content);
  return <ReactMarkdown>{clean}</ReactMarkdown>;
}
