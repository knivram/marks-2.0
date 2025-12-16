export interface User {
  userId: string;
  userName: string;
  email: string;
  role: UserRole;
}

export enum UserRole {
  ADMIN = "ADMIN",
  APPRENTICE = "APPRENTICE",
  SUPERVISOR = "SUPERVISOR",
}
