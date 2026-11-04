package br.org.edu.ifrn.carros.messaging;

import br.org.edu.ifrn.carros.events.ModuloEvento;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ModuloEventoPublisher {
    private static final Logger log = LoggerFactory.getLogger(ModuloEventoPublisher.class);
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;
    private final String fila;
    private final String filaRespostaAdm;

    public ModuloEventoPublisher(RabbitTemplate rabbit, ObjectMapper mapper,
    @Value("${app.rabbitmq.fila}") String fila,
    @Value("${app.rabbitmq.fila-resposta-adm}") String filaRespostaAdm) {
        this.rabbit = rabbit;
        this.mapper = mapper;
        this.fila = fila;
        this.filaRespostaAdm = filaRespostaAdm;
    }

    // Publicar somente depois que os dados forem confirmados no MySQL.
    @TransactionalEventListener
    public void publicar(ModuloEvento evento) {
        try {
            String json = mapper.writeValueAsString(evento);
            rabbit.convertAndSend("", fila, json, mensagem -> {
                mensagem.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                if ("ADM".equals(evento.modulo())) {
                    mensagem.getMessageProperties().setReplyTo(filaRespostaAdm);
                    mensagem.getMessageProperties().setCorrelationId(evento.eventoId());
                }
                return mensagem;
            });
            log.info("Evento enviado: modulo={} acao={} id={}",
            evento.modulo(), evento.acao(), evento.eventoId());
        } catch (JsonProcessingException | AmqpException ex) {
            log.error("Dados salvos, mas nao foi possivel publicar o evento {}", evento.eventoId(), ex);
        }
    }
}
