import apiClient from './AxiosClient';
import type { UserAuthDTO } from '@/types';

export default {
  getAllUsers(): Promise<{ data: UserAuthDTO[] }> {
    return apiClient.get('/api/v1/users');
  },

  promoteUserToMember(userId: number): Promise<{ data: UserAuthDTO }> {
    return apiClient.put(`/api/v1/users/${userId}/promote`);
  },

  demoteUserToReader(userId: number): Promise<{ data: UserAuthDTO }> {
    return apiClient.put(`/api/v1/users/${userId}/demote`);
  },
};
