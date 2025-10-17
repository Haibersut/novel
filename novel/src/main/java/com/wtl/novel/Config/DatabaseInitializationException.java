package com.wtl.novel.Config;

/**
 * 数据库初始化异常
 */
public class DatabaseInitializationException extends RuntimeException {
    
    public DatabaseInitializationException(String message) {
        super(message);
    }
    
    public DatabaseInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
