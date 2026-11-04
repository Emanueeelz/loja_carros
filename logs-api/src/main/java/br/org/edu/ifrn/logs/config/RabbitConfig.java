package br.org.edu.ifrn.logs.config;

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
}
