package br.org.edu.ifrn.logs.controllers;

import br.org.edu.ifrn.logs.model.LogOperacao;
import br.org.edu.ifrn.logs.repository.LogOperacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogOperacaoRepository repository;

    public LogController(LogOperacaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<LogOperacao> listar(
            @RequestParam(defaultValue = "0") int pagina
    ) {
        return repository.findAll(
                PageRequest.of(
                        Math.max(pagina, 0),
                        50,
                        Sort.by("recebidoEm").descending()
                )
        );
    }
}