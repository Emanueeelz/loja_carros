package br.org.edu.ifrn.carros.repository;
import br.org.edu.ifrn.carros.model.OperacaoAdm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface OperacaoAdmRepository extends JpaRepository<OperacaoAdm, Long> {
    Optional<OperacaoAdm> findByEventoId(String eventoId);
}
