import client from './client';

export interface TokenListItem {
  id: number;
  name: string;
  scopes: string;
  lastUsedAt: string | null;
  createdAt: string;
}

export interface TokenCreateResult {
  id: number;
  name: string;
  token: string;
  createdAt: string;
}

export const createToken = (name: string) =>
  client.post<TokenCreateResult>('/api/settings/tokens', { name }).then((r) => r.data);

export const listTokens = () =>
  client.get<TokenListItem[]>('/api/settings/tokens').then((r) => r.data);

export const revokeToken = (id: number) =>
  client.delete(`/api/settings/tokens/${id}`);
