package br.org.edu.ifrn.carros.controllers;

import br.org.edu.ifrn.carros.dto.CarroRequest;
import br.org.edu.ifrn.carros.model.Carro;
import br.org.edu.ifrn.carros.services.CarroService;
import br.org.edu.ifrn.carros.services.FotoService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/carros")
public class CarroController {
    private final CarroService service;
    private final FotoService fotos;
    public CarroController(CarroService service, FotoService fotos) {
        this.service = service;
        this.fotos = fotos;
    }
    @GetMapping
    public List<Carro> listar(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Integer ano
    ) {
        return service.listar(marca, modelo, precoMin, precoMax, ano);
    }
    @GetMapping("/{id}")
    public Carro buscar(@PathVariable Long id) {
        return service.buscarDisponivel(id);
    }
    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> buscarFoto(@PathVariable Long id) {
        Carro carro = service.buscarDisponivel(id);
        String nome = carro.getFotoPrincipal();
        Resource recurso = fotos.carregar(nome);

        MediaType tipo = nome.endsWith(".png")
                ? MediaType.IMAGE_PNG
                : nome.endsWith(".webp")
                ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(tipo)
                .header("X-Content-Type-Options", "nosniff")
                .body(recurso);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Carro> criar(
            @Valid @RequestPart("dados") CarroRequest request,
            @RequestPart("foto") MultipartFile foto
    ) {
        Carro carro = service.criar(request, foto);
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
    @PutMapping(
            value = "/{id}/foto",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Carro atualizarFoto(
            @PathVariable Long id,
            @RequestPart("foto") MultipartFile foto
    ) {
        return service.atualizarFoto(id, foto);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
