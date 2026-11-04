package br.org.edu.ifrn.carros.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "carro")
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    protected Carro() {
    }

    public Carro(String modelo, Integer ano, BigDecimal preco) {
        atualizar(modelo, ano, preco);
    }

    public void atualizar(String modelo, Integer ano, BigDecimal preco) {
        this.modelo = modelo.trim();
        this.ano = ano;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}