package br.org.edu.ifrn.carros.controllers;

import br.org.edu.ifrn.carros.model.Carro;
import br.org.edu.ifrn.carros.services.CarroService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adm/carros")
public class CarroAdminController {

    private final CarroService service;

    public CarroAdminController(CarroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Carro> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Carro buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
