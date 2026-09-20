package com.finpay.service;

import com.finpay.dto.request.ChangePasswordRequest;
import com.finpay.dto.request.CreateUserRequest;
import com.finpay.dto.request.UpdateUserRequest;
import com.finpay.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long Id);

    UserResponse updateUser(Long Id, UpdateUserRequest request);

    void changePassword(Long id,ChangePasswordRequest request);
}
