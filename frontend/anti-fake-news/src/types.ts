export interface MessageState {
  message: string
}

export interface UserAuthDTO {
  id: number;
  username: string;
  firstname: string;
  lastname: string;
  email: string;
  profileImage: string | null;
  roles: string[];
}