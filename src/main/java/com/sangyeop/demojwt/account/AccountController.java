package com.sangyeop.demojwt.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        String email = user.get("email");
        String password = user.get("password");
        if(accountRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exist Account!");
        }
        accountRepository.save(Account.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody Map<String, String> user) {
        Account account = accountRepository.findByEmail(user.get("email"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Not Found!"));
        if(!passwordEncoder.matches(user.get("password"), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password Incorrect!");
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
