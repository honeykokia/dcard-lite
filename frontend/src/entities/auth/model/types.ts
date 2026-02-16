export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    userId: number;
    displayName: string;
    role: string;
    accessToken: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export interface RegisterResponse {
    userId: number;
    displayName: string;
    email: string;
    role: string;
    createdAt: string;
}
