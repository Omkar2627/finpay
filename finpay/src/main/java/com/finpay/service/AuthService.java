package com.finpay.service;

import com.finpay.dto.request.CreateUserRequest;
import com.finpay.dto.request.LoginRequest;
import com.finpay.dto.response.LoginResponse;
import com.finpay.dto.response.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    UserResponse register(CreateUserRequest request);

}