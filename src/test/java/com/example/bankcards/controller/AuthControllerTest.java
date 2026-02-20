package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private AuthRequestDto validAuthRequest;
    private User testUser;
    private Role userRole;
    private Authentication authentication;
    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        validAuthRequest = new AuthRequestDto("testuser", "password123");

        userRole = new Role();
        userRole.setId(roleId);
        userRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setRole(userRole);

        authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void register_WithValidData_ShouldReturnCreatedUser() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(testUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/users" + userId)))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.role").value(testUser.getRole().getName()));
    }

    @Test
    void register_WithInvalidUsername_ShouldReturnBadRequest() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithNullPassword_ShouldReturnBadRequest() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("testuser", null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        String expectedToken = "jwt.token.here";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(validAuthRequest.username(), List.of("ROLE_USER")))
                .thenReturn(expectedToken);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithEmptyUsername_ShouldReturnBadRequest() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithMultipleRoles_ShouldGenerateTokenWithAllRoles() throws Exception {
        String expectedToken = "jwt.token.here";

        Authentication multiRoleAuth = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password123",
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(multiRoleAuth);
        when(jwtService.generateToken(validAuthRequest.username(), List.of("ROLE_USER", "ROLE_ADMIN")))
                .thenReturn(expectedToken);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken));
    }

    @Test
    void register_ShouldReturnValidUuidInResponse() throws Exception {
        UUID customUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testUser.setId(customUserId);

        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(testUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(customUserId.toString()));
    }
}