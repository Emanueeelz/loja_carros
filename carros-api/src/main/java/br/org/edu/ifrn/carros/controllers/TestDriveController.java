package br.org.edu.ifrn.carros.controllers;
import br.org.edu.ifrn.carros.dto.TestDriveRequest;
import br.org.edu.ifrn.carros.model.TestDrive;
import br.org.edu.ifrn.carros.services.TestDriveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/test-drives")
public class TestDriveController {
    private final TestDriveService service;
    public TestDriveController(TestDriveService service) { this.service = service; }

    @GetMapping
    public List<TestDrive> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public TestDrive buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<TestDrive> criar(@Valid @RequestBody TestDriveRequest request) {
        TestDrive salvo = service.criar(request);
        return ResponseEntity.created(URI.create("/test-drives/" + salvo.getId())).body(salvo);
    }
    @PutMapping("/{id}")
    public TestDrive atualizar(@PathVariable Long id, @Valid @RequestBody TestDriveRequest request) {
        return service.atualizar(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
