package com.sangyeop.demojwt.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    boolean existsByToken(String Token);
}
