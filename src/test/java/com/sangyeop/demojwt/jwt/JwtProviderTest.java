package com.sangyeop.demojwt.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class JwtProviderTest {
    @Autowired
    JwtProvider jwtProvider;

    @Test
    @DisplayName("token validation test")
    public void tokenVailation() {
        String email = "test@email.com";
        String inValidToken = "A.A.A";

        jwtProvider.createToken(email, Collections.singletonList("ROLE_USER"));
        assertFalse(jwtProvider.validateToken(inValidToken));
    }
}