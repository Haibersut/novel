package com.wtl.novel.CDO;

import com.wtl.novel.entity.User;

public class UserCTO extends User {
    public UserCTO() {
        super(); // 可选：调用父类的无参构造函数
    }

    public UserCTO(User user) {
        if (user != null) {
            this.setEmail(user.getEmail());
            this.setPoint(user.getPoint());
            this.setHideReadBooks(user.getHideReadBooks());
        }
    }
}
