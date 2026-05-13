import ReactMarkdown from 'react-markdown';
import DOMPurify from 'dompurify';

interface Props {
  content: string;
}

export default function MarkdownContent({ content }: Props) {
  const clean = DOMPurify.sanitize(content);
  return <ReactMarkdown>{clean}</ReactMarkdown>;
}
