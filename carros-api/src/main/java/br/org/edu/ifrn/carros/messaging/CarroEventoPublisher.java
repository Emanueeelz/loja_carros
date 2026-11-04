package br.org.edu.ifrn.carros.messaging;

import br.org.edu.ifrn.carros.events.CarroEvento;
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
public class CarroEventoPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(CarroEventoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String fila;

    public CarroEventoPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${app.rabbitmq.fila}") String fila
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.fila = fila;
    }

    @TransactionalEventListener
    public void publicar(CarroEvento evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend("", fila, json, mensagem -> {
                mensagem.getMessageProperties()
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT);

                return mensagem;
            });

            log.info(
                    "Publicação solicitada: evento={} acao={} carroId={}",
                    evento.eventoId(),
                    evento.acao(),
                    evento.carroId()
            );
        } catch (JsonProcessingException | AmqpException ex) {
            log.error(
                    "Operação salva no banco, mas publicação falhou. evento={}",
                    evento.eventoId(),
                    ex
            );
        }
    }
}