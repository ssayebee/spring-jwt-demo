package com.sangyeop.demojwt.account;

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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid AccountDTO accountDTO, Errors errors) {
        if(errors.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field Validation Error!");
        }

        if(accountRepository.findByEmail(accountDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exist Account!");
        }

        accountRepository.save(accountDTO.toEntity(passwordEncoder));
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid AccountDTO accountDTO, Errors errors) {
        Account account = accountRepository.findByEmail(accountDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Not Found!"));

        if(!passwordEncoder.matches(accountDTO.getPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password Incorrect!");
        }

        return new ResponseEntity<>(jwtProvider.createToken(account.getEmail(), account.getRoles()), HttpStatus.OK);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> needAuthentication(HttpServletRequest request) {
        Account user = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new ResponseEntity<>(user.getEmail(), HttpStatus.OK);
    }
}
