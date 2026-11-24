package br.org.edu.ifrn.carros.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String perfil = jwt.getClaimAsString("perfil");
            if (!"ADMIN".equals(perfil) && !"CLIENTE".equals(perfil)) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
        });

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/detalhes.html",
                                "/login.html", "/cadastro.html", "/admin.html",
                                "/css/**", "/js/**").permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/auth/login", "/usuarios").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/carros", "/carros/*", "/carros/*/foto",
                                "/carros/*/fotos/*").permitAll()

                        .requestMatchers("/test-drives/**").denyAll()

                        .requestMatchers("/carros/**", "/usuarios/**",
                                "/vendas/**", "/contabilidade/**", "/adm/**")
                        .hasRole("ADMIN")

                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(converter)))
                .build();
    }
}