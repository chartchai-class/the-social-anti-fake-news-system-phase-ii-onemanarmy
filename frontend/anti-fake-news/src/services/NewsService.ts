import apiClient from './AxiosClient';
import type { InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../stores/auth';

const normalizeToken = (value: string | null) => {
  if (!value) return null;
  const withoutSpaces = value.trim();
  if (!withoutSpaces) return null;

  if (withoutSpaces.startsWith('"') && withoutSpaces.endsWith('"')) {
    return withoutSpaces.slice(1, -1);
  }
  if (withoutSpaces.startsWith("'") && withoutSpaces.endsWith("'")) {
    return withoutSpaces.slice(1, -1);
  }
  return withoutSpaces;
};

const getStorage = (): Storage | null => {
  try {
    if (typeof globalThis === 'undefined') {
      return null;
    }
    const maybeStorage = (globalThis as typeof globalThis & { localStorage?: Storage }).localStorage;
    return maybeStorage ?? null;
  } catch {
    return null;
  }
};

const getStoredToken = () => {
  const storage = getStorage();
  if (!storage) return null;
  const raw = storage.getItem('access_token');
  return normalizeToken(raw);
};

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  let token: string | null = null;
  try {
    const authStore = useAuthStore();
    token = normalizeToken(authStore?.token ?? null);
  } catch {
    token = getStoredToken();
  }
  if (!token) {
    token = getStoredToken();
  }
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default {
  getNews(params?: any) {
    return apiClient.get('/api/news', { params })
  },

  getRemovedNews() {
    return apiClient.get('/api/news/removed');
  },

  getNewsById(id: number, includeRemoved: boolean = false) {
    const params: any = {}
    if (includeRemoved) {
      params.includeRemoved = true
    }
    return apiClient.get(`/api/news/${id}`, { params })
  },

  createNews(payload: {
    topic: string;
    shortDetail: string;
    fullDetail: string;
    image: string;
    reporter: string;
    dateTime?: string;
  }) {
    return apiClient.post('/api/news', payload);
  },

  getCommentsByNewsId(newsId: number, page: number = 0, size: number = 100) {
    return apiClient.get(`/api/comments/news/${newsId}`, {
      params: { page, size }
    });
  },

  createComment(data: {
    username: string;
    text: string;
    image: string | null;
    vote: 'fake' | 'real';
    newsId: number;
  }) {
    return apiClient.post('/api/comments', data);
  },

  getVoteSummary(newsId: number) {
    return apiClient.get(`/api/comments/news/${newsId}/summary`);
  },

  deleteComment(id: number) {
    return apiClient.delete(`/api/comments/${id}`);
  },


  removeNews(id: number) {
    return apiClient.delete(`/api/news/${id}`);
  },

  searchNews(params: {
    title?: string;
    status?: string;
    _page?: number;
    _limit?: number;
    includeRemoved?: boolean;
  }) {
    return apiClient.get('/api/news/search', { params });
  }
};
