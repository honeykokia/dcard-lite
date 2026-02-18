/*
 * 密碼驗證工具函式
 * 1. 必填檢查：密碼不能為空或僅包含空白字元。
 * 2. 長度檢查：密碼長度必須在 8 到 12 個字元之間。
 * 3. 複雜度檢查：密碼必須包含至少一個字母和一個數字。
 */
export const getPasswordValidationErrors = (password: string): string[] => {
    const messages: string[] = [];

    // 1. 必填檢查 (這裡使用原始字串長度判斷，避免 trim 掉使用者刻意輸入的空白)
    if (!password || password.length === 0) {
        messages.push('Password must not be blank');
        return messages;
    }

    // 2. 長度檢查
    if (password.length < 8 || password.length > 12) {
        messages.push('Password length must be between 8 and 12 characters');
        return messages;
    }

    // 3. 複雜度檢查 (字母 + 數字)
    const hasLetter = /[a-zA-Z]/.test(password);
    const hasDigit = /\d/.test(password);

    if (!hasLetter || !hasDigit) {
        messages.push('Password must contain at least one letter and one digit');
    }

    return messages;
}

export const getEmailValidationErrors = (email: string): string[] => {
    const messages: string[] = [];
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
        messages.push('Email must not be blank');
        return messages;
    }

    if (trimmedEmail.length > 100) {
        messages.push('Email maximum length 100 characters');
        return messages;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmedEmail)) {
        messages.push('Email must be in a valid email format');
    }

    return messages;
}