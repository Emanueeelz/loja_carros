package br.org.edu.ifrn.carros.services;
import br.org.edu.ifrn.carros.dto.TestDriveRequest;
import br.org.edu.ifrn.carros.events.ModuloEvento;
import br.org.edu.ifrn.carros.model.TestDrive;
import br.org.edu.ifrn.carros.repository.TestDriveRepository;
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
public class TestDriveService {
    private final TestDriveRepository repository;
    private final ApplicationEventPublisher publisher;
    private final CarroRepository carros;
    private final UsuarioRepository usuarios;
    public TestDriveService(TestDriveRepository repository, ApplicationEventPublisher publisher, CarroRepository carros, UsuarioRepository usuarios) {
        this.repository = repository;
        this.publisher = publisher;

        this.carros = carros;
        this.usuarios = usuarios;

    }
    @Transactional(readOnly = true)
    public List<TestDrive> listar() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public TestDrive buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "TestDrive nao encontrado."));
    }

    @Transactional
    public TestDrive criar(TestDriveRequest request) {

        if (!carros.existsById(request.carroId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carro nao encontrado.");
        }
        if (!usuarios.existsById(request.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao encontrado.");
        }

        TestDrive entidade = repository.save(new TestDrive(request.carroId(), request.usuarioId(), request.dataHora()));
        emitir("TEST_DRIVE_AGENDADO", entidade);
        return entidade;
    }

    @Transactional
    public TestDrive atualizar(Long id, TestDriveRequest request) {

        if (!carros.existsById(request.carroId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carro nao encontrado.");
        }
        if (!usuarios.existsById(request.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao encontrado.");
        }

        TestDrive entidade = buscar(id);
        entidade.atualizar(request.carroId(), request.usuarioId(), request.dataHora());
        emitir("TEST_DRIVE_ATUALIZADO", entidade);
        return entidade;
    }

    @Transactional
    public void excluir(Long id) {
        TestDrive entidade = buscar(id);
        repository.delete(entidade);
        emitir("TEST_DRIVE_CANCELADO", entidade);
    }

    private void emitir(String acao, TestDrive entidade) {
        publisher.publishEvent(new ModuloEvento(
        UUID.randomUUID().toString(), "TEST_DRIVE", acao,
        entidade.getId(), "TestDrive " + entidade.getId(), Instant.now().toString()));
    }
}
