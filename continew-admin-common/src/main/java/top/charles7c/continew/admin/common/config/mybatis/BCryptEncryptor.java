package top.charles7c.continew.admin.common.config.mybatis;

import org.springframework.security.crypto.password.PasswordEncoder;
import top.charles7c.continew.starter.security.crypto.encryptor.IEncryptor;

/**
 * BCrypt 加/解密处理器（不可逆）
 *
 * @author Charles7c
 * @since 2024/2/8 22:29
 */
public class BCryptEncryptor implements IEncryptor {

    private final PasswordEncoder passwordEncoder;

    public BCryptEncryptor(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encrypt(String plaintext, String password, String publicKey) throws Exception {
        return passwordEncoder.encode(plaintext);
    }

    @Override
    public String decrypt(String ciphertext, String password, String privateKey) throws Exception {
        return ciphertext;
    }
}
