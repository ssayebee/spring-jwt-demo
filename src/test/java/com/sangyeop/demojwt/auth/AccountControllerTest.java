package com.sangyeop.demojwt.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyeop.demojwt.auth.dto.AccountResponse;
import com.sangyeop.demojwt.jwt.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    public void signUp(String email, String password) throws Exception  {
        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up 201")
    public void signUpSuccess() throws Exception {
        signUp("test@email.com", "password");
    }

    @ParameterizedTest(name = "{2}")
    @DisplayName("POST /api/auth/sign-up 400 InValid Params")
    @MethodSource("signUpSource")
    public void signUpFail(String email, String password, String message) throws Exception {
        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> signUpSource() {
        return Stream.of(
                Arguments.of(null, null, "email: null, password: null"),
                Arguments.of(null, "password", "email: null"),
                Arguments.of("test@email.com", null, "password: null"),
                Arguments.of("", "", "email: empty, password: empty"),
                Arguments.of("test@email.com", "", "password: empty"),
                Arguments.of("", "password", "email: empty"),
                Arguments.of("t", "password", "email: t"),
                Arguments.of("t@", "password", "email: t@"),
                Arguments.of("@t", "password", "email: @t")
        );
    }

    @Test
    @DisplayName("POST /api/auth/sign-up 400 Duplicate Eamil")
    public void signUpDuplicateEmail() throws Exception {
        String email = "email@email.com";
        String password = "password";
        signUp(email, password);

        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/sign-in 200")
    public void signInSuccess() throws Exception {
        String email = "test@email.com";
        String password = "password";
        signUp(email, password);
        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/sign-in 400 Wrong Password")
    public void signUpFail() throws Exception {
        String email = "test@email.com";
        String password = "password";
        signUp(email, password);

        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password("wrong password")
                .build();

        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/auth/detail 401")
    public void getDeatilWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/detail")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/detail 200")
    public void getDeatialWithToken() throws Exception {
        String email = "test@email.com";
        String password = "password";
        signUp(email, password);

        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password(password)
                .build();

        ResultActions resultActions = mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk());

        String token = resultActions.andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/auth/detail")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/sign-out 200")
    public void signOut() throws Exception {
        String email = "logout@email.com";
        String password = "password";
        signUp(email, password);

        AccountResponse user = AccountResponse.builder()
                .email(email)
                .password(password)
                .build();

        ResultActions resultActions = mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk());

        String token = resultActions.andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/auth/detail")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/sign-out")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk());

        assertTrue(tokenRepository.existsByToken(token));

        mockMvc.perform(get("/api/auth/detail")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}