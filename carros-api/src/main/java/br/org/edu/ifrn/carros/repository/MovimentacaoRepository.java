package br.org.edu.ifrn.carros.repository;
import br.org.edu.ifrn.carros.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {}
