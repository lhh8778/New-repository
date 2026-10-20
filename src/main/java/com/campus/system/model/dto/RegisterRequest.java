package com.campus.system.model.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String realName;
    private String studentId;
    private String department;
    private String phone;

    public RegisterRequest() {}
    public RegisterRequest(String username, String password, String email, String realName, String studentId, String department, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.realName = realName;
        this.studentId = studentId;
        this.department = department;
        this.phone = phone;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getRealName() { return realName; }
    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public String getPhone() { return phone; }
}