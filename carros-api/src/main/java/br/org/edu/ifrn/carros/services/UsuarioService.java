
package br.org.edu.ifrn.carros.services;

import br.org.edu.ifrn.carros.dto.CadastroUsuarioRequest;
import br.org.edu.ifrn.carros.dto.UsuarioRequest;
import br.org.edu.ifrn.carros.events.ModuloEvento;
import br.org.edu.ifrn.carros.model.Usuario;
import br.org.edu.ifrn.carros.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            ApplicationEventPublisher publisher,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    @Transactional
    public Usuario criar(CadastroUsuarioRequest request) {
        String email = request.email().trim();

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario(
                request.nome(),
                email,
                passwordEncoder.encode(request.senha())
        );

        Usuario salvo = repository.save(usuario);
        emitir("USUARIO_CRIADO", salvo);

        return salvo;
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        String email = request.email().trim();

        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }

        usuario.atualizar(request.nome(), email);
        emitir("USUARIO_ATUALIZADO", usuario);

        return usuario;
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscar(id);
        repository.delete(usuario);
        emitir("USUARIO_EXCLUIDO", usuario);
    }

    private void emitir(String acao, Usuario usuario) {
        publisher.publishEvent(new ModuloEvento(
                UUID.randomUUID().toString(),
                "USUARIOS",
                acao,
                usuario.getId(),
                usuario.getNome(),
                Instant.now().toString()
        ));
    }
}
