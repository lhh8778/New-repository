package com.campus.system.common;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public ApiResponse() {}
    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Operation successful", data);
    }
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    @Override
    public String toString() {
        return "ApiResponse{code=" + code + ", message='" + message + "', data=" + data + "}";
    }
}