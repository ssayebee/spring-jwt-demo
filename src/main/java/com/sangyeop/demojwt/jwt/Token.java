package com.sangyeop.demojwt.jwt;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Entity(name = "expired_token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String token;

    @Column
    private LocalDate addedDate;
}
