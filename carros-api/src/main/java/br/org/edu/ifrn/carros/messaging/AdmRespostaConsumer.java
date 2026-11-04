package br.org.edu.ifrn.carros.messaging;
import br.org.edu.ifrn.carros.events.AdmResposta;
import br.org.edu.ifrn.carros.services.AdmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AdmRespostaConsumer {
    private static final Logger log = LoggerFactory.getLogger(AdmRespostaConsumer.class);
    private final AdmService service;
    private final ObjectMapper mapper;
    public AdmRespostaConsumer(AdmService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    @RabbitListener(queues = "${app.rabbitmq.fila-resposta-adm}")
    public void receber(String json) throws JsonProcessingException {
        AdmResposta resposta = mapper.readValue(json, AdmResposta.class);
        if ("CONFIRMADO".equals(resposta.status())) {
            service.confirmar(resposta.eventoId());
            log.info("ADM recebeu resposta: evento={} status={}", resposta.eventoId(), resposta.status());
        }
    }
}
