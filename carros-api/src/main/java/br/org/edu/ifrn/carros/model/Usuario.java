package br.org.edu.ifrn.carros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Locale;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false, length = 180, unique = true)
    private String email;
    @Column(nullable = false, length = 60)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    protected Usuario() {
    }
    public Usuario(String nome, String email, String senhaHash) {
        atualizar(nome, email);
        this.senhaHash = senhaHash;
        this.perfil = PerfilUsuario.CLIENTE;
    }
    public void atualizar(String nome, String email) {
        this.nome = nome.trim();
        this.email = email.trim().toLowerCase(Locale.ROOT);
    }
    public Long getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public String getEmail() {
        return email;
    }
    @JsonIgnore
    public String getSenhaHash() {
        return senhaHash;
    }
    public PerfilUsuario getPerfil() {
        return perfil;
    }
}
