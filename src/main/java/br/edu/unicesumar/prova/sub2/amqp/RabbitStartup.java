package br.edu.unicesumar.prova.sub2.amqp;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.unicesumar.prova.sub2.domain.Prova;
import br.edu.unicesumar.prova.sub2.repository.ProvaRepository;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RabbitStartup {

    @Autowired
    public AmqpAdmin amqpAdmin;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    public ProvaRepository provaRepository;

    @PostConstruct
    public void rabbitStartup() { // NÃO MEXER
        questao04();
        questao05();
        questao06();
        questao07();
    }

    public void questao04() {
        // RESPONDA A QUESTAO 04 AQUI
    }

    public void questao05() {
        // RESPONDA A QUESTAO 05 AQUI
    }

    public void questao06() {
        // RESPONDA A QUESTAO 06 AQUI
    }

    public void questao07() {
        // RESPONDA A QUESTAO 07 AQUI
    }

    public void questao08(Prova prova) {
        // RESPONDA A QUESTAO 08 AQUI
    }
}
