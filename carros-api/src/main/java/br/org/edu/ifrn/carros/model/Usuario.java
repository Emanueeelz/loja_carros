package br.org.edu.ifrn.carros.model;
import jakarta.persistence.*;

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
    protected Usuario() {}
    public Usuario(String nome, String email) { atualizar(nome, email); }
    public void atualizar(String nome, String email) {
        this.nome = nome.trim();
        this.email = email.trim();
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
}
