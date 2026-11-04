package br.org.edu.ifrn.logs.messaging;

import br.org.edu.ifrn.logs.events.AdmResposta;
import br.org.edu.ifrn.logs.events.CarroEvento;
import br.org.edu.ifrn.logs.events.ModuloEvento;
import br.org.edu.ifrn.logs.model.LogOperacao;
import br.org.edu.ifrn.logs.repository.LogOperacaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Component
public class LogConsumer {
    private static final Logger log = LoggerFactory.getLogger(LogConsumer.class);
    private final ObjectMapper mapper;
    private final LogOperacaoRepository repository;
    private final RabbitTemplate rabbit;

    public LogConsumer(ObjectMapper mapper, LogOperacaoRepository repository, RabbitTemplate rabbit) {
        this.mapper = mapper;
        this.repository = repository;
        this.rabbit = rabbit;
    }

    @Transactional
    @RabbitListener(queues = "${app.rabbitmq.fila}")
    public void receber(Message mensagem) throws JsonProcessingException {
        String json = new String(mensagem.getBody(), StandardCharsets.UTF_8);
        JsonNode raiz = mapper.readTree(json);
        boolean antigo = !raiz.hasNonNull("modulo");
        LogOperacao registro;
        String eventoId;
        String modulo;
        if (antigo) {
            CarroEvento evento = mapper.treeToValue(raiz, CarroEvento.class);
            eventoId = evento.eventoId();
            modulo = "CARROS";
            registro = new LogOperacao(evento);
        } else {
            ModuloEvento evento = mapper.treeToValue(raiz, ModuloEvento.class);
            eventoId = evento.eventoId();
            modulo = evento.modulo();
            registro = new LogOperacao(evento);
        }
        if (eventoId == null || eventoId.isBlank()) {
            throw new IllegalArgumentException("Mensagem sem eventoId.");
        }
        if (!repository.existsById(eventoId)) {
            repository.saveAndFlush(registro);
            log.info("Log salvo: modulo={} acao={} evento={}",
            modulo, registro.getAcao(), eventoId);
        } else {
            log.info("Evento ja registrado: {}", eventoId);
        }

        // Somente o modulo ADM recebe uma mensagem de retorno.
        String filaResposta = mensagem.getMessageProperties().getReplyTo();
        if ("ADM".equals(modulo) && filaResposta != null && !filaResposta.isBlank()) {
            String resposta = mapper.writeValueAsString(new AdmResposta(
            eventoId, "CONFIRMADO", "Operacao registrada no modulo Log"));
            rabbit.convertAndSend("", filaResposta, resposta);
            log.info("Resposta enviada ao ADM: evento={}", eventoId);
        }
    }
}
