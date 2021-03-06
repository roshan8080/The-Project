package com.java.project.onlinematchsim.controller;

import com.java.project.onlinematchsim.exception.AppException;
import com.java.project.onlinematchsim.model.*;
import com.java.project.onlinematchsim.apiCalls.requestCalls.DeleteUserRequest;

import com.java.project.onlinematchsim.apiCalls.requestCalls.LoginRequest;
import com.java.project.onlinematchsim.apiCalls.requestCalls.ResetPasswordRequest;
import com.java.project.onlinematchsim.apiCalls.requestCalls.SignUpRequest;
import com.java.project.onlinematchsim.apiCalls.responseCalls.JwtAuthenticationResponse;
import com.java.project.onlinematchsim.apiCalls.responseCalls.ApiResponse;
import com.java.project.onlinematchsim.repos.*;
import com.java.project.onlinematchsim.security.JwtTokenProvider;
import com.java.project.onlinematchsim.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

@SuppressWarnings({"unchecked","rawtypes"})
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    CalendarService calendarService;

    @Autowired
    JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getDistrict(), signUpRequest.getSchoolname());
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
    
    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
       
    	User delUser = userRepository.findById(Long.parseLong(resetPasswordRequest.getId())).orElseThrow(() -> new AppException("User can't be found"));
    	userRepository.delete(delUser);

        // Creating user's account
    	
       

    	 User user = new User(resetPasswordRequest.getName(), resetPasswordRequest.getUsername(),
                 delUser.getEmail(), resetPasswordRequest.getPassword(), resetPasswordRequest.getDistrict(), resetPasswordRequest.getSchoolname());
         
         user.setPassword(passwordEncoder.encode(user.getPassword()));

         Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                 .orElseThrow(() -> new AppException("User Role not set."));

         user.setRoles(Collections.singleton(userRole));

         User result = userRepository.save(user);

         URI location = ServletUriComponentsBuilder
                 .fromCurrentContextPath().path("/users/{username}")
                 .buildAndExpand(result.getUsername()).toUri();

         return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
    
    
    @PostMapping("/deleteuser")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSIGNOR')")
    public ResponseEntity<?> deleteUser(@Valid @RequestBody DeleteUserRequest deleteUserRequest)
    {
    	User delUser = userRepository.findById(Long.parseLong(deleteUserRequest.getId())).orElseThrow(() -> new AppException("User can't be found"));
    	userRepository.delete(delUser);
    	
    	URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{matchId}").buildAndExpand(deleteUserRequest.getId()).toUri();
    	return ResponseEntity.created(location).body(new ApiResponse(true, "User deleted successfully"));
    }

    @PostMapping("/signup/assignor")
    public ResponseEntity<?> registerAssignor(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getDistrict(), signUpRequest.getSchoolname());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_ASSIGNOR)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/signup/admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getDistrict(), signUpRequest.getSchoolname());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}