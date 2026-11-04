package br.org.edu.ifrn.carros.services;
import br.org.edu.ifrn.carros.dto.AdmRequest;
import br.org.edu.ifrn.carros.events.ModuloEvento;
import br.org.edu.ifrn.carros.model.OperacaoAdm;
import br.org.edu.ifrn.carros.repository.OperacaoAdmRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AdmService {
    private final OperacaoAdmRepository repository;
    private final ApplicationEventPublisher publisher;
    public AdmService(OperacaoAdmRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }
    @Transactional
    public OperacaoAdm criar(AdmRequest request) {
        OperacaoAdm operacao = repository.save(new OperacaoAdm(
        UUID.randomUUID().toString(), request.descricao()));
        publisher.publishEvent(new ModuloEvento(operacao.getEventoId(), "ADM",
        "ADM_OPERACAO_REGISTRADA", operacao.getId(), operacao.getDescricao(),
        Instant.now().toString()));
        return operacao;
    }
    @Transactional(readOnly = true)
    public List<OperacaoAdm> listar() { return repository.findAll(); }
    @Transactional(readOnly = true)
    public OperacaoAdm buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Operacao nao encontrada."));
    }
    @Transactional
    public void confirmar(String eventoId) {
        repository.findByEventoId(eventoId).ifPresent(OperacaoAdm::confirmar);
    }
}
