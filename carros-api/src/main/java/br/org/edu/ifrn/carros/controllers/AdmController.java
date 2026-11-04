package br.org.edu.ifrn.carros.controllers;
import br.org.edu.ifrn.carros.dto.AdmRequest;
import br.org.edu.ifrn.carros.model.OperacaoAdm;
import br.org.edu.ifrn.carros.services.AdmService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/adm/operacoes")
public class AdmController {
    private final AdmService service;
    public AdmController(AdmService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<OperacaoAdm> criar(@Valid @RequestBody AdmRequest request) {
        OperacaoAdm operacao = service.criar(request);
        return ResponseEntity.created(URI.create("/adm/operacoes/" + operacao.getId())).body(operacao);
    }
    @GetMapping
    public List<OperacaoAdm> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public OperacaoAdm buscar(@PathVariable Long id) { return service.buscar(id); }
}
