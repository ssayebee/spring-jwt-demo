package com.sangyeop.demojwt.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody Map<String, String> user) {
        accountRepository.save(Account.builder()
                .email(user.get("email"))
                .password(passwordEncoder.encode(user.get("password")))
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody Map<String, String> user) {
        Account account = accountRepository.findByEmail(user.get("email"))
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 E-MAIL 입니다."));
        if(!passwordEncoder.matches(user.get("password"), account.getPassword())) {
            return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        }
        String token = jwtProvider.createToken(account.getEmail(), account.getRoles());
        return new ResponseEntity<>("{\"token\" :\"" + token + "\" }" , HttpStatus.OK);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> needAuthentication(HttpServletRequest request) {
        String token =  jwtProvider.resolveToken(request);
        String result = jwtProvider.getUserPk(token);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
