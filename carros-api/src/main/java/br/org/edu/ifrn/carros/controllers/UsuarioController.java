package br.org.edu.ifrn.carros.controllers;
import br.org.edu.ifrn.carros.dto.UsuarioRequest;
import br.org.edu.ifrn.carros.model.Usuario;
import br.org.edu.ifrn.carros.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<Usuario> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public Usuario buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody UsuarioRequest request) {
        Usuario salvo = service.criar(request);
        return ResponseEntity.created(URI.create("/usuarios/" + salvo.getId())).body(salvo);
    }
    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return service.atualizar(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
