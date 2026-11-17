
package br.org.edu.ifrn.carros.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "carro")
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 120)
    private String modelo;

    @Column(name = "ano", nullable = false)
    private Integer anoFabricacao;

    @Column(nullable = false)
    private Integer anoModelo;

    @Column(nullable = false, length = 60)
    private String cor;

    @Column(nullable = false)
    private Integer quilometragem;

    @Column(nullable = false, length = 40)
    private String combustivel;

    @Column(nullable = false, length = 40)
    private String transmissao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoCarro situacao;

    @Column(name = "foto_principal", length = 80)
    private String fotoPrincipal;

    protected Carro() {
    }

    public Carro(String marca, String modelo, Integer anoFabricacao,
                 Integer anoModelo, String cor, Integer quilometragem,
                 String combustivel, String transmissao, BigDecimal preco,
                 String descricao, SituacaoCarro situacao) {
        atualizar(marca, modelo, anoFabricacao, anoModelo, cor,
                quilometragem, combustivel, transmissao, preco,
                descricao, situacao);
    }

    public void atualizar(String marca, String modelo, Integer anoFabricacao,
                          Integer anoModelo, String cor, Integer quilometragem,
                          String combustivel, String transmissao, BigDecimal preco,
                          String descricao, SituacaoCarro situacao) {
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
        this.cor = cor.trim();
        this.quilometragem = quilometragem;
        this.combustivel = combustivel.trim();
        this.transmissao = transmissao.trim();
        this.preco = preco;
        this.descricao = descricao.trim();
        this.situacao = situacao;
    }

    public void desativar() {
        this.situacao = SituacaoCarro.INATIVO;
    }

    public Long getId() { return id; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public Integer getAnoFabricacao() { return anoFabricacao; }
    public Integer getAnoModelo() { return anoModelo; }
    public String getCor() { return cor; }
    public Integer getQuilometragem() { return quilometragem; }
    public String getCombustivel() { return combustivel; }
    public String getTransmissao() { return transmissao; }
    public BigDecimal getPreco() { return preco; }
    public String getDescricao() { return descricao; }
    public SituacaoCarro getSituacao() { return situacao; }
    public String getFotoPrincipal() {
        return fotoPrincipal;
    }
    public void definirFoto(String fotoPrincipal) {
        this.fotoPrincipal = fotoPrincipal;
    }
}
