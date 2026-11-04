package br.org.edu.ifrn.carros.model;
import jakarta.persistence.*;

@Entity
@Table(name = "operacao_adm")
public class OperacaoAdm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 36)
    private String eventoId;
    @Column(nullable = false, length = 240)
    private String descricao;
    @Column(nullable = false, length = 30)
    private String status;

    protected OperacaoAdm() {}
    public OperacaoAdm(String eventoId, String descricao) {
        this.eventoId = eventoId;
        this.descricao = descricao.trim();
        this.status = "AGUARDANDO_RESPOSTA";
    }
    public void confirmar() { this.status = "CONFIRMADO"; }
    public Long getId() { return id; }
    public String getEventoId() { return eventoId; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
}
