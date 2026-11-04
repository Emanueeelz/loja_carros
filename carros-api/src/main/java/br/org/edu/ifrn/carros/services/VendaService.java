package br.org.edu.ifrn.carros.services;
import br.org.edu.ifrn.carros.dto.VendaRequest;
import br.org.edu.ifrn.carros.events.ModuloEvento;
import br.org.edu.ifrn.carros.model.Venda;
import br.org.edu.ifrn.carros.repository.VendaRepository;
import br.org.edu.ifrn.carros.repository.CarroRepository;
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
public class VendaService {
    private final VendaRepository repository;
    private final ApplicationEventPublisher publisher;
    private final CarroRepository carros;
    private final UsuarioRepository usuarios;
    public VendaService(VendaRepository repository, ApplicationEventPublisher publisher, CarroRepository carros, UsuarioRepository usuarios) {
        this.repository = repository;
        this.publisher = publisher;

        this.carros = carros;
        this.usuarios = usuarios;

    }
    @Transactional(readOnly = true)
    public List<Venda> listar() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Venda buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda nao encontrada."));
    }

    @Transactional
    public Venda criar(VendaRequest request) {

        if (!carros.existsById(request.carroId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carro nao encontrado.");
        }
        if (!usuarios.existsById(request.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao encontrado.");
        }

        Venda entidade = repository.save(new Venda(request.carroId(), request.usuarioId(), request.valor()));
        emitir("VENDA_CRIADA", entidade);
        return entidade;
    }

    @Transactional
    public Venda atualizar(Long id, VendaRequest request) {

        if (!carros.existsById(request.carroId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carro nao encontrado.");
        }
        if (!usuarios.existsById(request.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao encontrado.");
        }

        Venda entidade = buscar(id);
        entidade.atualizar(request.carroId(), request.usuarioId(), request.valor());
        emitir("VENDA_ATUALIZADA", entidade);
        return entidade;
    }

    @Transactional
    public void excluir(Long id) {
        Venda entidade = buscar(id);
        repository.delete(entidade);
        emitir("VENDA_EXCLUIDA", entidade);
    }

    private void emitir(String acao, Venda entidade) {
        publisher.publishEvent(new ModuloEvento(
        UUID.randomUUID().toString(), "VENDAS", acao,
        entidade.getId(), "Venda " + entidade.getId(), Instant.now().toString()));
    }
}
