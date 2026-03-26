package com.example.demo1;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Role {
    ADMIN(1), // Vai trò quản trị viên, có quyền cao nhất trong hệ thống.
    USER(2), // Vai trò người dùng bình thường, có quyền hạn giới hạn.
    MANAGER(3); // Vai trò quản lý, có quyền thêm, sửa sản phẩm.
    public final long value;
}