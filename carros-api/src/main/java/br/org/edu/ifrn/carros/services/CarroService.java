package br.org.edu.ifrn.carros.services;

import br.org.edu.ifrn.carros.dto.CarroRequest;
import br.org.edu.ifrn.carros.events.CarroEvento;
import br.org.edu.ifrn.carros.model.Carro;
import br.org.edu.ifrn.carros.model.SituacaoCarro;
import br.org.edu.ifrn.carros.repository.CarroRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CarroService {
    private final CarroRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final FotoService fotos;

    public CarroService(
            CarroRepository repository,
            ApplicationEventPublisher eventPublisher,
            FotoService fotos
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.fotos = fotos;
    }

    @Transactional(readOnly = true)
    public List<Carro> listar(String marca, String modelo,
                              BigDecimal precoMin, BigDecimal precoMax,
                              Integer ano) {
        if ((precoMin != null && precoMin.signum() < 0)
                || (precoMax != null && precoMax.signum() < 0)
                || (precoMin != null && precoMax != null
                && precoMin.compareTo(precoMax) > 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Faixa de preço inválida.");
        }

        return repository.pesquisar(
                marca == null || marca.isBlank() ? null : marca.trim(),
                modelo == null || modelo.isBlank() ? null : modelo.trim(),
                precoMin,
                precoMax,
                ano
        );
    }

    @Transactional(readOnly = true)
    public List<Carro> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Carro buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Carro não encontrado."));
    }

    @Transactional(readOnly = true)
    public Carro buscarDisponivel(Long id) {
        Carro carro = buscar(id);
        if (carro.getSituacao() != SituacaoCarro.DISPONIVEL) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Carro não encontrado.");
        }
        return carro;
    }

    @Transactional
    public Carro criar(CarroRequest request, MultipartFile foto) {
        String arquivo = fotos.salvar(foto);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            fotos.apagar(arquivo);
                        }
                    }
                }
        );
        Carro carro = new Carro(
                request.marca(),
                request.modelo(),
                request.anoFabricacao(),
                request.anoModelo(),
                request.cor(),
                request.quilometragem(),
                request.combustivel(),
                request.transmissao(),
                request.preco(),
                request.descricao(),
                request.situacao()
        );

        carro.definirFoto(arquivo);
        Carro salvo = repository.save(carro);
        emitirEvento("CARRO_CRIADO", salvo);
        return salvo;
    }

    @Transactional
    public Carro atualizar(Long id, CarroRequest request) {
        Carro carro = buscar(id);
        carro.atualizar(
                request.marca(),
                request.modelo(),
                request.anoFabricacao(),
                request.anoModelo(),
                request.cor(),
                request.quilometragem(),
                request.combustivel(),
                request.transmissao(),
                request.preco(),
                request.descricao(),
                request.situacao()
        );
        emitirEvento("CARRO_ATUALIZADO", carro);
        return carro;
    }

    @Transactional
    public Carro atualizarFoto(Long id, MultipartFile foto) {
        Carro carro = buscar(id);
        String fotoAnterior = carro.getFotoPrincipal();
        String novaFoto = fotos.salvar(foto);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_COMMITTED) {
                            if (fotoAnterior != null) {
                                fotos.apagar(fotoAnterior);
                            }
                        } else {
                            fotos.apagar(novaFoto);
                        }
                    }
                }
        );

        carro.definirFoto(novaFoto);
        emitirEvento("CARRO_FOTO_ATUALIZADA", carro);
        return carro;
    }

    @Transactional
    public void excluir(Long id) {
        Carro carro = buscar(id);
        carro.desativar();
        emitirEvento("CARRO_DESATIVADO", carro);
    }
    private void emitirEvento(String acao, Carro carro) {
        eventPublisher.publishEvent(new CarroEvento(
                UUID.randomUUID().toString(),
                acao,
                carro.getId(),
                carro.getModelo(),
                carro.getAnoFabricacao(),
                carro.getPreco(),
                Instant.now().toString()
        ));
    }
}
