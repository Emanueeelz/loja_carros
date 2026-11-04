package br.org.edu.ifrn.carros.services;

import br.org.edu.ifrn.carros.dto.CarroRequest;
import br.org.edu.ifrn.carros.events.CarroEvento;
import br.org.edu.ifrn.carros.model.Carro;
import br.org.edu.ifrn.carros.repository.CarroRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CarroService {

    private final CarroRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public CarroService(
            CarroRepository repository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<Carro> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Carro buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Carro não encontrado."
                ));
    }

    @Transactional
    public Carro criar(CarroRequest request) {
        Carro carro = repository.save(
                new Carro(request.modelo(), request.ano(), request.preco())
        );

        emitirEvento("CARRO_CRIADO", carro);

        return carro;
    }

    @Transactional
    public Carro atualizar(Long id, CarroRequest request) {
        Carro carro = buscar(id);

        carro.atualizar(
                request.modelo(),
                request.ano(),
                request.preco()
        );

        emitirEvento("CARRO_ATUALIZADO", carro);

        return carro;
    }

    @Transactional
    public void excluir(Long id) {
        Carro carro = buscar(id);

        repository.delete(carro);

        emitirEvento("CARRO_EXCLUIDO", carro);
    }

    private void emitirEvento(String acao, Carro carro) {
        eventPublisher.publishEvent(
                new CarroEvento(
                        UUID.randomUUID().toString(),
                        acao,
                        carro.getId(),
                        carro.getModelo(),
                        carro.getAno(),
                        carro.getPreco(),
                        Instant.now().toString()
                )
        );
    }
}