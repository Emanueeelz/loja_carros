package br.org.edu.ifrn.carros.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue filaLogs(@Value("${app.rabbitmq.fila}") String nome) {
        return new Queue(nome, true);
    }

    @Bean
    public Queue filaRespostaAdm(@Value("${app.rabbitmq.fila-resposta-adm}") String nome) {
        return new Queue(nome, true);
    }
}
