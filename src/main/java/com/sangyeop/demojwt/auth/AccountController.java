package com.sangyeop.demojwt.auth;

import com.sangyeop.demojwt.auth.dto.AccountResponse;
import com.sangyeop.demojwt.jwt.JwtProvider;
import com.sangyeop.demojwt.jwt.Token;
import com.sangyeop.demojwt.jwt.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AccountController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid AccountResponse accountResponse, Errors errors) {
        if(errors.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field Validation Error!");
        }

        if(accountRepository.findByEmail(accountResponse.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exist Account!");
        }

        accountRepository.save(accountResponse.toEntity(passwordEncoder));
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid AccountResponse accountResponse, Errors errors) {
        Account account = accountRepository.findByEmail(accountResponse.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Not Found!"));

        if(!passwordEncoder.matches(accountResponse.getPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password Incorrect!");
        }

        return new ResponseEntity<>(jwtProvider.createToken(account.getEmail(), account.getRoles()), HttpStatus.OK);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request) {
        String token = Optional.ofNullable(
                request.getHeader("Authorization"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unAuthorized")
                );
        tokenRepository.save(Token.builder().token(token).addedDate(LocalDate.now()).build());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> needAuthentication(HttpServletRequest request) {
        Account user = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new ResponseEntity<>(user.getEmail(), HttpStatus.OK);
    }
}
