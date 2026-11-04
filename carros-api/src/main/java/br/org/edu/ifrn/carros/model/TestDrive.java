package br.org.edu.ifrn.carros.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "test_drive")
public class TestDrive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long carroId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDateTime dataHora;
    protected TestDrive() {}
    public TestDrive(Long carroId, Long usuarioId, LocalDateTime dataHora) { atualizar(carroId, usuarioId, dataHora); }
    public void atualizar(Long carroId, Long usuarioId, LocalDateTime dataHora) {
        this.carroId = carroId;
        this.usuarioId = usuarioId;
        this.dataHora = dataHora;
    }
    public Long getId() { return id; }
    public Long getCarroId() { return carroId; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getDataHora() { return dataHora; }
}
