package com.campus.system.model.entity;

public class UserProfile {
    private Long id;
    private String realName;
    private String studentId;
    private String department;
    private String phone;

    public UserProfile() {}
    public UserProfile(Long id, String realName, String studentId, String department, String phone) {
        this.id = id;
        this.realName = realName;
        this.studentId = studentId;
        this.department = department;
        this.phone = phone;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRealName() { return realName; }
    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return "UserProfile{realName='" + realName + "', studentId='" + studentId + "', department='" + department + "'}";
    }
}