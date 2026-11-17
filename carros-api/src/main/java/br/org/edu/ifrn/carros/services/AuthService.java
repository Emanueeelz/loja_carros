package br.org.edu.ifrn.carros.services;

import br.org.edu.ifrn.carros.dto.LoginRequest;
import br.org.edu.ifrn.carros.dto.LoginResponse;
import br.org.edu.ifrn.carros.model.Usuario;
import br.org.edu.ifrn.carros.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    public AuthService(UsuarioRepository usuarios,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::credenciaisInvalidas);
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw credenciaisInvalidas();
        }

        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("carros-api")
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(1, ChronoUnit.HOURS))
                .claim("perfil", usuario.getPerfil().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new LoginResponse(token, "Bearer");
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }
}
