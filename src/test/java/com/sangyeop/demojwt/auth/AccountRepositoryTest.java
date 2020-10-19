package com.sangyeop.demojwt.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    public Account saveAccount(String email, String password) {
        return accountRepository.save(
            Account.builder()
            .email(email)
            .password(password)
            .roles(Collections.singletonList("ROLE_USER"))
            .build()
        );
    }

    @Test
    @DisplayName("Create Account")
    public void accountSave() {
        Account savedAccount = saveAccount("test@email.com", "password");
        Account foundAccount = accountRepository.findByEmail("test@email.com").orElseThrow(
                () -> new UsernameNotFoundException("존재하지 않는 사용자")
        );
        assertThat(savedAccount.getEmail()).isEqualTo(foundAccount.getEmail());
        assertThat(savedAccount.getPassword()).isEqualTo(foundAccount.getPassword());
        assertThat(savedAccount.getRoles().toArray()).isEqualTo(foundAccount.getRoles().toArray());
        assertTrue(foundAccount.isAccountNonExpired());
        assertTrue(foundAccount.isAccountNonLocked());
        assertTrue(foundAccount.isCredentialsNonExpired());
        assertTrue(foundAccount.isEnabled());
    }

    @Test
    @DisplayName("Account Email Unique")
    public void accountEmailUniq() {
        String email = "test@email.com";

        saveAccount(email, "password");
        assertThrows(
            DataIntegrityViolationException.class,
            () -> saveAccount(email, "password")
        );
    }


    @Test
    @DisplayName("Account Email Length Limit 50")
    public void accountEmailLength() {
        String lessThenEqaulFifty = IntStream.rangeClosed(1, 50).mapToObj(i -> "x").collect(Collectors.joining());
        assertDoesNotThrow(
            () -> saveAccount(lessThenEqaulFifty, "password")
        );

        String moreThenFifty = IntStream.rangeClosed(1, 51).mapToObj(i -> "x").collect(Collectors.joining());
        assertThrows(
            DataIntegrityViolationException.class,
            () -> saveAccount(moreThenFifty, "password")
        );
    }

    @Test
    @DisplayName("Account Password Length Limit 300")
    public void accountPasswordLength() {
        String lessThenEqualThreeHundreds = IntStream.rangeClosed(1, 300).mapToObj(i -> "x").collect(Collectors.joining());
        assertDoesNotThrow(
                () -> saveAccount("test1@email.com", lessThenEqualThreeHundreds)
        );
        String moreThenThreeHundreds = IntStream.rangeClosed(1, 301).mapToObj(i -> "x").collect(Collectors.joining());
        assertThrows(
                DataIntegrityViolationException.class,
                () -> saveAccount("test2@email.com", moreThenThreeHundreds)
        );
    }

    @Test
    @DisplayName("Delete Account")
    public void accountDelete() {
        String email = "deleted@email.com";
        Account savedAccount = saveAccount(email, "password");
        accountRepository.deleteById(savedAccount.getId());
        assertThat(accountRepository.findByEmail(email)).isEqualTo(Optional.empty());
    }

}