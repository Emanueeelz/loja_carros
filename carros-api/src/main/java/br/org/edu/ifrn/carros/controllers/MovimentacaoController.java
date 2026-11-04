package br.org.edu.ifrn.carros.controllers;
import br.org.edu.ifrn.carros.dto.MovimentacaoRequest;
import br.org.edu.ifrn.carros.model.Movimentacao;
import br.org.edu.ifrn.carros.services.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/contabilidade")
public class MovimentacaoController {
    private final MovimentacaoService service;
    public MovimentacaoController(MovimentacaoService service) { this.service = service; }

    @GetMapping
    public List<Movimentacao> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public Movimentacao buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<Movimentacao> criar(@Valid @RequestBody MovimentacaoRequest request) {
        Movimentacao salvo = service.criar(request);
        return ResponseEntity.created(URI.create("/contabilidade/" + salvo.getId())).body(salvo);
    }
    @PutMapping("/{id}")
    public Movimentacao atualizar(@PathVariable Long id, @Valid @RequestBody MovimentacaoRequest request) {
        return service.atualizar(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
