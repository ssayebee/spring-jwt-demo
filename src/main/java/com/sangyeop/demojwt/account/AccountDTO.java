package com.sangyeop.demojwt.account;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collections;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AccountDTO {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public Account toEntity(PasswordEncoder passwordEncoder) {
        return Account.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Collections.singletonList("ROLE_USER"))
                .build();
    }

}
