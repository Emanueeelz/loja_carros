
package br.org.edu.ifrn.carros.controllers;

import br.org.edu.ifrn.carros.model.Carro;
import br.org.edu.ifrn.carros.services.CarroService;
import br.org.edu.ifrn.carros.services.FotoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/adm/carros")
public class CarroAdminController {

    private final CarroService service;
    private final FotoService fotos;

    public CarroAdminController(CarroService service, FotoService fotos) {
        this.service = service;
        this.fotos = fotos;
    }

    @GetMapping
    public List<Carro> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Carro buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // Fotografias disponíveis para o administrador,
    // inclusive de veículos inativos.
    @GetMapping("/{id}/fotos/{indice}")
    public ResponseEntity<Resource> buscarFoto(
            @PathVariable Long id,
            @PathVariable int indice
    ) {
        String nome = service.buscar(id).getNomeFoto(indice);

        if (nome == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Fotografia não encontrada."
            );
        }

        MediaType tipo = nome.endsWith(".png")
                ? MediaType.IMAGE_PNG
                : nome.endsWith(".webp")
                ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(tipo)
                .header("X-Content-Type-Options", "nosniff")
                .body(fotos.carregar(nome));
    }
}
