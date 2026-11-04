package br.org.edu.ifrn.carros.controllers;
import br.org.edu.ifrn.carros.dto.VendaRequest;
import br.org.edu.ifrn.carros.model.Venda;
import br.org.edu.ifrn.carros.services.VendaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaController {
    private final VendaService service;
    public VendaController(VendaService service) { this.service = service; }

    @GetMapping
    public List<Venda> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public Venda buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<Venda> criar(@Valid @RequestBody VendaRequest request) {
        Venda salvo = service.criar(request);
        return ResponseEntity.created(URI.create("/vendas/" + salvo.getId())).body(salvo);
    }
    @PutMapping("/{id}")
    public Venda atualizar(@PathVariable Long id, @Valid @RequestBody VendaRequest request) {
        return service.atualizar(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
