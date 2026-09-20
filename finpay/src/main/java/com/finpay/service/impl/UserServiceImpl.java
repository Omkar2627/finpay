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
import com.finpay.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email Already Exists");
        }
        User user = userMapper.toEntity(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
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
    public UserResponse updateUser(Long Id, UpdateUserRequest request) {

        User user = userRepository.findById(Id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + Id
                        )
                );

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

    @Override
    @Transactional
    public void changePassword(
            Long id,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

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
