package com.finpay.service.impl;

import com.finpay.dto.request.ChangePasswordRequest;
import com.finpay.dto.request.CreateUserRequest;
import com.finpay.dto.request.UpdateUserRequest;
import com.finpay.dto.response.UserResponse;
import com.finpay.entity.User;
import com.finpay.exception.EmailAlreadyExistsException;
import com.finpay.exception.UserNotFoundException;
import com.finpay.mapper.UserMapper;
import com.finpay.repository.UserRepository;
import com.finpay.security.SecurityUtils;
import com.finpay.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;



    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
             SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }


    @Override
    @Transactional
    public UserResponse getUserById(Long Id) {
        User user = userRepository.findById(Id).orElseThrow(()->
                new UserNotFoundException(
                        "User not found with id: " + Id
                ));

        return userMapper.toResponse(user);
    }


@Override
@Transactional
public UserResponse updateCurrentUser(
        UpdateUserRequest request
) {

    User user = getAuthenticatedUser();

    if (request.getName() != null) {
        user.setName(request.getName());
    }

    if (request.getEmail() != null &&
            !request.getEmail().equals(user.getEmail())) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists"
            );
        }

        user.setEmail(request.getEmail());
    }

    if (request.getMobile() != null) {
        user.setMobile(request.getMobile());
    }

    user.setUpdatedAt(LocalDateTime.now());

    User updatedUser = userRepository.save(user);

    return userMapper.toResponse(updatedUser);
}

private User getAuthenticatedUser() {

    String email = securityUtils.getCurrentUserEmail();

    return userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new UserNotFoundException(
                            "Authenticated user not found"
                    )
            );
}

    @Override
    @Transactional
    public UserResponse getCurrentUser() {

        User user = getAuthenticatedUser();

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changeCurrentUserPassword(
            ChangePasswordRequest request
    ) {

        User user = getAuthenticatedUser();

        boolean currentPasswordMatches =
                passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPassword()
                );

        if (!currentPasswordMatches) {
            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}
