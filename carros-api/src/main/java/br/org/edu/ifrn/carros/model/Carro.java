package br.org.edu.ifrn.carros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // Mantido para preservar as fotos dos veículos já cadastrados.
    @Column(name = "foto_principal", length = 80)
    private String fotoPrincipal;

    // Hibernate cria a tabela carro_fotos_adicionais automaticamente.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "carro_fotos_adicionais",
            joinColumns = @JoinColumn(name = "carro_id")
    )
    @OrderColumn(name = "ordem")
    @Column(name = "arquivo", nullable = false, length = 80)
    private List<String> fotosAdicionais = new ArrayList<>();

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

    // Usado pelo endpoint antigo de troca de uma única fotografia.
    public void definirFoto(String fotoPrincipal) {
        this.fotoPrincipal = fotoPrincipal;
    }

    public void definirGaleria(List<String> arquivos) {
        if (arquivos == null || arquivos.isEmpty()) {
            throw new IllegalArgumentException("A galeria precisa ter fotografias.");
        }
        this.fotoPrincipal = arquivos.get(0);
        this.fotosAdicionais.clear();
        this.fotosAdicionais.addAll(arquivos.subList(1, arquivos.size()));
    }

    @JsonIgnore
    public List<String> getNomesDasFotos() {
        List<String> arquivos = new ArrayList<>();
        if (fotoPrincipal != null && !fotoPrincipal.isBlank()) {
            arquivos.add(fotoPrincipal);
        }
        arquivos.addAll(fotosAdicionais);
        return arquivos;
    }

    @JsonIgnore
    public String getNomeFoto(int indice) {
        List<String> arquivos = getNomesDasFotos();
        return indice >= 0 && indice < arquivos.size()
                ? arquivos.get(indice)
                : null;
    }

    // Campo disponível no JSON para o frontend construir as miniaturas.
    public int getQuantidadeFotos() {
        return getNomesDasFotos().size();
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
    public String getFotoPrincipal() { return fotoPrincipal; }
}