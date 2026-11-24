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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CarroService {

    private static final int MIN_FOTOS = 3;
    private static final int MAX_FOTOS = 5;

    private final CarroRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final FotoService fotos;

    public CarroService(CarroRepository repository,
                        ApplicationEventPublisher eventPublisher,
                        FotoService fotos) {
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
                precoMin, precoMax, ano
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

    @Transactional(readOnly = true)
    public String buscarNomeFotoDisponivel(Long id, int indice) {
        Carro carro = buscarDisponivel(id);
        String arquivo = carro.getNomeFoto(indice);
        if (arquivo == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Fotografia não encontrada.");
        }
        return arquivo;
    }

    @Transactional
    public Carro criar(CarroRequest request, List<MultipartFile> imagens) {
        validarQuantidade(imagens);

        // Registrar antes de salvar permite apagar fotos em caso de rollback.
        List<String> arquivosNovos = salvarImagensComRollback(imagens);

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

        carro.definirGaleria(arquivosNovos);
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
    public Carro atualizarGaleria(Long id, List<MultipartFile> imagens) {
        validarQuantidade(imagens);
        Carro carro = buscar(id);
        List<String> arquivosAntigos = carro.getNomesDasFotos();
        List<String> arquivosNovos = salvarImagensComRollback(imagens);

        // Remove fotos antigas somente depois que o banco confirmar a troca.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        arquivosAntigos.forEach(fotos::apagar);
                    }
                }
        );

        carro.definirGaleria(arquivosNovos);
        emitirEvento("CARRO_GALERIA_ATUALIZADA", carro);
        return carro;
    }

    // Mantido por compatibilidade com clientes antigos da API.
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

    private void validarQuantidade(List<MultipartFile> imagens) {
        if (imagens == null || imagens.size() < MIN_FOTOS
                || imagens.size() > MAX_FOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione de 3 a 5 fotografias.");
        }
    }

    private List<String> salvarImagensComRollback(List<MultipartFile> imagens) {
        List<String> arquivos = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            arquivos.forEach(fotos::apagar);
                        }
                    }
                }
        );

        for (MultipartFile imagem : imagens) {
            arquivos.add(fotos.salvar(imagem));
        }
        return arquivos;
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