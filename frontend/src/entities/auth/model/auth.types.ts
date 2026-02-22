export interface User {
    userId: number;
    token: string;
}

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

export interface LoginForm {
    email: string;
    password: string;
}

export interface LoginFormErrors {
    email: string[];
    password: string[];
}

export interface RegisterUserRequest {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export interface RegisterUserResponse {
    userId: number;
    displayName: string;
    email: string;
    role: string;
    createdAt: string;
}

export interface RegisterForm {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export interface RegisterFormErrors {
    name: string[];
    email: string[];
    password: string[];
    confirmPassword: string[];
}

