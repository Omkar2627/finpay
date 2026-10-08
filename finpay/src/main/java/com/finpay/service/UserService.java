package com.finpay.service;

import com.finpay.dto.request.ChangePasswordRequest;
import com.finpay.dto.request.UpdateUserRequest;
import com.finpay.dto.response.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();

    UserResponse updateCurrentUser(
            UpdateUserRequest request
    );

    void changeCurrentUserPassword(
            ChangePasswordRequest request
    );

    UserResponse getUserById(Long id);
}
