package com.finpay.service.impl;

import com.finpay.dto.request.CreateUserRequest;
import com.finpay.dto.request.LoginRequest;
import com.finpay.dto.response.LoginResponse;
import com.finpay.dto.response.UserResponse;
import com.finpay.entity.User;
import com.finpay.exception.EmailAlreadyExistsException;
import com.finpay.mapper.UserMapper;
import com.finpay.repository.UserRepository;
import com.finpay.security.JwtService;
import com.finpay.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;

//    @Override
//    public LoginResponse login(LoginRequest request) {
//
//        System.out.println("Email: [" + request.getEmail() + "]");
//        System.out.println("Password: [" + request.getPassword() + "]");
//
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("USER NOT FOUND"));
//
//        System.out.println("DB Email: [" + user.getEmail() + "]");
//        System.out.println("DB Password Hash: " + user.getPassword());
//
//        System.out.println(
//                "Password Match: " +
//                        passwordEncoder.matches(
//                                request.getPassword(),
//                                user.getPassword()
//                        )
//        );
//
//        Authentication authentication =
//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                request.getEmail(),
//                                request.getPassword()
//                        )
//                );
//
//        UserDetails userDetails =
//                (UserDetails) authentication.getPrincipal();
//
//        String token = jwtService.generateToken(userDetails);
//
//        return new LoginResponse(token, "Bearer");
//    }


    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token, "Bearer");
    }


    @Override
    @Transactional
    public UserResponse register(CreateUserRequest request) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists"
            );
        }
        User user = userMapper.toEntity(request);
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        User savedUser =
                userRepository.save(user);

        // 5. Convert Entity → Response DTO
        return userMapper.toResponse(savedUser);
    }
    }


