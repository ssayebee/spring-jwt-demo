package com.sangyeop.demojwt.account;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    public void signUp(String email, String password) throws Exception  {
        AccountDTO user = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/accounts/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 성공")
    public void signUpSuccess() throws Exception {
        signUp("test@email.com", "password");
    }

    @ParameterizedTest(name = "{index}) email={0}, password={1}")
    @DisplayName("회원가입 실패 유효성 검증")
    @MethodSource("signUpSource")
    public void signUpFail(String email, String password) throws Exception {
        AccountDTO user = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/accounts/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> signUpSource() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, "password"),
                Arguments.of("test@email.com", null),
                Arguments.of("test@email.com", ""),
                Arguments.of("", ""),
                Arguments.of("test@email.com", ""),
                Arguments.of("", "password"),
                Arguments.of("t", "password"),
                Arguments.of("t@", "password"),
                Arguments.of("@t", "password")
        );
    }

    @Test
    @DisplayName("회원가입 실패 중복된 아이디")
    public void signUpDuplicateEmail() throws Exception {
        String email = "email@email.com";
        String password = "password";
        signUp(email, password);

        AccountDTO user = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/accounts/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    public void signInSuccess() throws Exception {
        String email = "test@email.com";
        String password = "password";
        signUp(email, password);
        AccountDTO user = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/accounts/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 비밀번호 불일치")
    public void signUpFail() throws Exception {
        String email = "test@email.com";
        String password = "password";
        signUp(email, password);

        AccountDTO user = AccountDTO.builder()
                .email(email)
                .password("wrong password")
                .build();

        mockMvc.perform(post("/api/accounts/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JWT 없이 Detail 조회 -> 401")
    public void getDeatilWithoutToken() throws Exception {
        mockMvc.perform(get("/api/accounts/detail")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT로 Detail 조회")
    public void getDeatialWithToken() throws Exception {
        String email = "test@email.com";
        String password = "password";
        signUp(email, password);

        AccountDTO user = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        ResultActions resultActions = mockMvc.perform(post("/api/accounts/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk());

        String token = resultActions.andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/accounts/detail")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk());
    }

}