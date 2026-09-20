package com.finpay.mapper;

import com.finpay.dto.request.CreateUserRequest;
import com.finpay.dto.response.UserResponse;
import com.finpay.entity.User;
import com.finpay.entity.UserStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

        public User toEntity(CreateUserRequest request) {
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setMobile(request.getMobile());
            user.setStatus(UserStatus.ACTIVE);
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            return user;
        }

    public UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setMobile(user.getMobile());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}
