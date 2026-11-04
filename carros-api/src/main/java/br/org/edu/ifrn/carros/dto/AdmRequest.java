package br.org.edu.ifrn.carros.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AdmRequest(@NotBlank @Size(max = 240) String descricao) {}
