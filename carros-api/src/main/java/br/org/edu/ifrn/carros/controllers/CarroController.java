
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

    // Fotografia principal utilizada pelo catálogo.
    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> buscarFoto(@PathVariable Long id) {
        return responderFoto(service.buscarNomeFotoDisponivel(id, 0));
    }

    // Foto 0 = capa; índices 1 a 4 = fotografias adicionais.
    @GetMapping("/{id}/fotos/{indice}")
    public ResponseEntity<Resource> buscarFotoGaleria(
            @PathVariable Long id,
            @PathVariable int indice
    ) {
        return responderFoto(service.buscarNomeFotoDisponivel(id, indice));
    }

    private ResponseEntity<Resource> responderFoto(String nome) {
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
            @RequestPart("fotos") List<MultipartFile> fotos
    ) {
        Carro carro = service.criar(request, fotos);

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
            value = "/{id}/fotos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Carro atualizarGaleria(
            @PathVariable Long id,
            @RequestPart("fotos") List<MultipartFile> imagens
    ) {
        return service.atualizarGaleria(id, imagens);
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
