export interface ErrorResponse {
    status: number;
    error: string;
    message: string;
    code: string;
    path: string;
    timestamp: string;
}