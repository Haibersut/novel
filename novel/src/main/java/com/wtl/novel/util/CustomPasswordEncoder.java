package com.wtl.novel.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class CustomPasswordEncoder {

    private static final Logger log = LoggerFactory.getLogger(CustomPasswordEncoder.class);

    public static void main(String[] args) {
//        CustomPasswordEncoder encoder = new CustomPasswordEncoder();
//        log.info("Encoded password: {}", encoder.encode("qwerasdf123"));
        log.info("Password valid: {}", isValid("123_)%dsahdWAS"));
    }
    /**
     * 对密码进行加密
     */
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    // 使用和前端一样的正则表达式
    private static final Pattern NEW_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$");

    /**
     * 校验密码是否符合复杂性要求。
     * @param password 待校验的密码字符串
     * @return 如果密码符合要求，返回 true；否则返回 false。
     */
    public static boolean isValid(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return NEW_PASSWORD_PATTERN.matcher(password).matches();
    }

}