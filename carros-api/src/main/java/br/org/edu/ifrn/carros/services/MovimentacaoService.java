package br.org.edu.ifrn.carros.services;
import br.org.edu.ifrn.carros.dto.MovimentacaoRequest;
import br.org.edu.ifrn.carros.events.ModuloEvento;
import br.org.edu.ifrn.carros.model.Movimentacao;
import br.org.edu.ifrn.carros.repository.MovimentacaoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MovimentacaoService {
    private final MovimentacaoRepository repository;
    private final ApplicationEventPublisher publisher;

    public MovimentacaoService(MovimentacaoRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;

    }
    @Transactional(readOnly = true)
    public List<Movimentacao> listar() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Movimentacao buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentacao nao encontrada."));
    }

    @Transactional
    public Movimentacao criar(MovimentacaoRequest request) {

        Movimentacao entidade = repository.save(new Movimentacao(request.tipo(), request.descricao(), request.valor()));
        emitir("MOVIMENTACAO_CRIADA", entidade);
        return entidade;
    }

    @Transactional
    public Movimentacao atualizar(Long id, MovimentacaoRequest request) {

        Movimentacao entidade = buscar(id);
        entidade.atualizar(request.tipo(), request.descricao(), request.valor());
        emitir("MOVIMENTACAO_ATUALIZADA", entidade);
        return entidade;
    }

    @Transactional
    public void excluir(Long id) {
        Movimentacao entidade = buscar(id);
        repository.delete(entidade);
        emitir("MOVIMENTACAO_EXCLUIDA", entidade);
    }

    private void emitir(String acao, Movimentacao entidade) {
        publisher.publishEvent(new ModuloEvento(
        UUID.randomUUID().toString(), "CONTABILIDADE", acao,
        entidade.getId(), entidade.getDescricao(), Instant.now().toString()));
    }
}
