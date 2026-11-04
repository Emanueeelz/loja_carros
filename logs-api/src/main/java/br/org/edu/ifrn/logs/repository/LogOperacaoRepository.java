package br.org.edu.ifrn.logs.repository;

import br.org.edu.ifrn.logs.model.LogOperacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogOperacaoRepository
        extends JpaRepository<LogOperacao, String> {
}