package br.org.edu.ifrn.logs.model;

import br.org.edu.ifrn.logs.events.CarroEvento;
import br.org.edu.ifrn.logs.events.ModuloEvento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "log_operacao")
public class LogOperacao {
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    @Id
    @Column(length = 36)
    private String eventoId;
    @Column(nullable = false)
    private String acao;
    // Preservados para manter compatibilidade com os registros do trabalho passado.
    private Long carroId;
    private String modelo;
    private Integer ano;
    @Column(precision = 12, scale = 2)
    private BigDecimal preco;
    // Dados comuns a todos os novos modulos.
    @Column(length = 50)
    private String modulo;
    private Long entidadeId;
    @Column(length = 500)
    private String descricao;
    @Column(nullable = false)
    private LocalDateTime ocorridoEm;
    @Column(nullable = false)
    private LocalDateTime recebidoEm;

    protected LogOperacao() {}

    public LogOperacao(CarroEvento evento) {
        this.eventoId = evento.eventoId();
        this.acao = evento.acao();
        this.carroId = evento.carroId();
        this.modelo = evento.modelo();
        this.ano = evento.ano();
        this.preco = evento.preco();
        this.modulo = "CARROS";
        this.entidadeId = evento.carroId();
        this.descricao = evento.modelo();
        registrarHorarios(evento.ocorridoEm());
    }

    public LogOperacao(ModuloEvento evento) {
        this.eventoId = evento.eventoId();
        this.acao = evento.acao();
        this.modulo = evento.modulo();
        this.entidadeId = evento.entidadeId();
        this.descricao = evento.descricao();
        registrarHorarios(evento.ocorridoEm());
    }

    private void registrarHorarios(String ocorridoEm) {
        this.ocorridoEm = LocalDateTime.ofInstant(Instant.parse(ocorridoEm), BRASILIA);
        this.recebidoEm = LocalDateTime.now(BRASILIA);
    }

    public String getEventoId() { return eventoId; }
    public String getAcao() { return acao; }
    public Long getCarroId() { return carroId; }
    public String getModelo() { return modelo; }
    public Integer getAno() { return ano; }
    public BigDecimal getPreco() { return preco; }
    public String getModulo() { return modulo == null ? "CARROS" : modulo; }
    public Long getEntidadeId() { return entidadeId == null ? carroId : entidadeId; }
    public String getDescricao() { return descricao == null ? modelo : descricao; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
    public LocalDateTime getRecebidoEm() { return recebidoEm; }
}
