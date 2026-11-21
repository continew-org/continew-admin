/** Mirrors the backend password policy: 8-32 characters with at least one digit and one lowercase letter. */
export const PASSWORD_PATTERN = /^(?=.*\d)(?=.*[a-z]).{8,32}$/

/** Shown under a password field until the value breaks the rule. */
export const PASSWORD_HINT = '密码长度为 8-32 个字符，且至少包含一个小写字母和一个数字'
