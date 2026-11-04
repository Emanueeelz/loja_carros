package br.org.edu.ifrn.carros.services;
import br.org.edu.ifrn.carros.dto.UsuarioRequest;
import br.org.edu.ifrn.carros.events.ModuloEvento;
import br.org.edu.ifrn.carros.model.Usuario;
import br.org.edu.ifrn.carros.repository.UsuarioRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final ApplicationEventPublisher publisher;

    public UsuarioService(UsuarioRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;

    }
    @Transactional(readOnly = true)
    public List<Usuario> listar() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
    }

    @Transactional
    public Usuario criar(UsuarioRequest request) {

        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado.");
        }
        Usuario entidade = repository.save(new Usuario(request.nome(), request.email()));
        emitir("USUARIO_CRIADO", entidade);
        return entidade;
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequest request) {

        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado.");
        }
        Usuario entidade = buscar(id);
        entidade.atualizar(request.nome(), request.email());
        emitir("USUARIO_ATUALIZADO", entidade);
        return entidade;
    }

    @Transactional
    public void excluir(Long id) {
        Usuario entidade = buscar(id);
        repository.delete(entidade);
        emitir("USUARIO_EXCLUIDO", entidade);
    }

    private void emitir(String acao, Usuario entidade) {
        publisher.publishEvent(new ModuloEvento(
        UUID.randomUUID().toString(), "USUARIOS", acao,
        entidade.getId(), entidade.getNome(), Instant.now().toString()));
    }
}
