package br.org.edu.ifrn.carros.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "movimentacao")
public class Movimentacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacao tipo;

    @Column(nullable = false, length = 240)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    protected Movimentacao() {}
    public Movimentacao(TipoMovimentacao tipo, String descricao, BigDecimal valor) { atualizar(tipo, descricao, valor); }
    public void atualizar(TipoMovimentacao tipo, String descricao, BigDecimal valor) {
        this.tipo = tipo;
        this.descricao = descricao.trim();
        this.valor = valor;
    }
    public Long getId() { return id; }
    public TipoMovimentacao getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
}
