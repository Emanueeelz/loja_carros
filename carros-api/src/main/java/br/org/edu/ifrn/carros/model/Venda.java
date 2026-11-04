package br.org.edu.ifrn.carros.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "venda")
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long carroId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    protected Venda() {}
    public Venda(Long carroId, Long usuarioId, BigDecimal valor) { atualizar(carroId, usuarioId, valor); }
    public void atualizar(Long carroId, Long usuarioId, BigDecimal valor) {
        this.carroId = carroId;
        this.usuarioId = usuarioId;
        this.valor = valor;
    }
    public Long getId() { return id; }
    public Long getCarroId() { return carroId; }
    public Long getUsuarioId() { return usuarioId; }
    public BigDecimal getValor() { return valor; }
}
