package br.org.edu.ifrn.carros.controllers;

import br.org.edu.ifrn.carros.dto.CarroRequest;
import br.org.edu.ifrn.carros.model.Carro;
import br.org.edu.ifrn.carros.services.CarroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/carros")
public class CarroController {

    private final CarroService service;

    public CarroController(CarroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Carro> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Carro buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Carro> criar(
            @Valid @RequestBody CarroRequest request
    ) {
        Carro carro = service.criar(request);

        return ResponseEntity
                .created(URI.create("/carros/" + carro.getId()))
                .body(carro);
    }

    @PutMapping("/{id}")
    public Carro atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CarroRequest request
    ) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}