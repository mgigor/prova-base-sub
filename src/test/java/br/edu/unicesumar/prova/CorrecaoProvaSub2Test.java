package br.edu.unicesumar.prova;

import static br.edu.unicesumar.prova.CorrecaoService.round;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import br.edu.unicesumar.prova.sub2.amqp.RabbitStartup;
import br.edu.unicesumar.prova.sub2.domain.Prova;
import br.edu.unicesumar.prova.sub2.repository.ProvaRepository;

class CorrecaoProvaSub2Test {

    private final AmqpAdmin amqpAdminMock = Mockito.mock(AmqpAdmin.class);

    private final RabbitTemplate rabbitTemplateMock = Mockito.mock(RabbitTemplate.class);

    private final ProvaRepository provaRepositoryMock = Mockito.mock(ProvaRepository.class);

    private RabbitStartup startup = new RabbitStartup(amqpAdminMock, rabbitTemplateMock, provaRepositoryMock);

    @Test
    void correcao() {
        double notaExercicios04 = corrigirExercicio04();
        double notaExercicios05 = corrigirExercicio05();
        double notaExercicios06 = corrigirExercicio06();
        double notaExercicios07 = corrigirExercicio07();
        double notaExercicios08 = corrigirExercicio08();
        double notaExercicios09 = corrigirExercicio09();
        double notaExercicios10 = corrigirExercicio10();

        double total = notaExercicios04 + notaExercicios05 + notaExercicios06 + notaExercicios07 + notaExercicios08 + notaExercicios09 + notaExercicios10;

        System.out.println("\n\n###### CORREÇÃO ######\n");
        System.out.println("Exercício\tNota");
        System.out.println("Ex.04\t\t" + notaExercicios04);
        System.out.println("Ex.05\t\t" + notaExercicios05);
        System.out.println("Ex.06\t\t" + notaExercicios06);
        System.out.println("Ex.07\t\t" + notaExercicios07);
        System.out.println("Ex.08\t\t" + notaExercicios08);
        System.out.println("Ex.09\t\t" + notaExercicios09);
        System.out.println("Ex.10\t\t" + notaExercicios10);
        System.out.println("______________________");
        System.out.println("TOTAL\t\t" + total);
        System.out.println("\nHASH: " + this.getHash() + "\n\n");
    }

    private double corrigirExercicio04() {

        final HashSet<String> captured = new HashSet<>();

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {

                Stream.of(invocation.getArguments()).map(a -> (AbstractExchange) a).findFirst().ifPresent(exchange -> {
                    try {
                        if (exchange.getName().equalsIgnoreCase("exchange-direct-sub")) {
                            captured.add("exchange-direct-sub");
                        }
                        if (exchange.getName().equalsIgnoreCase("exchange-topic-sub")) {
                            captured.add("exchange-topic-sub");
                        }
                        if (exchange.getName().equalsIgnoreCase("exchange-fanout-sub")) {
                            captured.add("exchange-fanout-sub");
                        }
                    } catch (Exception e) {

                    }
                });
                return null;
            }
        }).when(amqpAdminMock).declareExchange(Mockito.any());

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {

                Stream.of(invocation.getArguments()).map(a -> (Queue) a).findFirst().ifPresent(queue -> {

                    try {
                        if (queue.getName().equalsIgnoreCase("fila-sub-programacao")) {
                            captured.add("fila-sub-programacao");
                        }
                        if (queue.getName().equalsIgnoreCase("fila-sub-arquitetura")) {
                            captured.add("fila-sub-arquitetura");
                        }
                        if (queue.getName().equalsIgnoreCase("fila-sub")) {
                            captured.add("fila-sub");
                        }
                    } catch (Exception e) {
                    }

                });
                return null;
            }
        }).when(amqpAdminMock).declareQueue(Mockito.any());

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {

                Stream.of(invocation.getArguments()).map(a -> (Binding) a).findFirst().ifPresent(binding -> {

                    try {

                        if (binding.getDestination().equalsIgnoreCase("fila-sub-programacao")
                                && binding.getDestinationType().equals(DestinationType.QUEUE)
                                && binding.getExchange().equalsIgnoreCase("exchange-direct-sub")
                                && binding.getRoutingKey().equalsIgnoreCase("programacao")) {
                            captured.add("b1");
                        }
                        if (binding.getDestination().equalsIgnoreCase("fila-sub-arquitetura")
                                && binding.getDestinationType().equals(DestinationType.QUEUE)
                                && binding.getExchange().equalsIgnoreCase("exchange-direct-sub")
                                && binding.getRoutingKey().equalsIgnoreCase("arquitetura")) {
                            captured.add("b2");
                        }
                        if (binding.getDestination().equalsIgnoreCase("fila-sub-programacao")
                                && binding.getDestinationType().equals(DestinationType.QUEUE)
                                && binding.getExchange().equalsIgnoreCase("exchange-topic-sub")
                                && binding.getRoutingKey().equalsIgnoreCase("*.programacao.#")) {
                            captured.add("b3");
                        }
                        if (binding.getDestination().equalsIgnoreCase("fila-sub-arquitetura")
                                && binding.getDestinationType().equals(DestinationType.QUEUE)
                                && binding.getExchange().equalsIgnoreCase("exchange-topic-sub")
                                && binding.getRoutingKey().equalsIgnoreCase("*.arquitetura.#")) {
                            captured.add("b4");
                        }
                        if (binding.getDestination().equalsIgnoreCase("fila-sub")
                                && binding.getDestinationType().equals(DestinationType.QUEUE)
                                && binding.getExchange().equalsIgnoreCase("exchange-topic-sub")
                                && binding.getRoutingKey().equalsIgnoreCase("sub.#")) {
                            captured.add("b5");
                        }
                        if (binding.getDestination().equalsIgnoreCase("fila-sub")
                                && binding.getDestinationType().equals(DestinationType.QUEUE)
                                && binding.getExchange().equalsIgnoreCase("exchange-fanout-sub")) {
                            captured.add("b6");
                        }
                    } catch (Exception e) {
                    }

                });
                return null;
            }
        }).when(amqpAdminMock).declareBinding(Mockito.any());

        startup.questao04();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> captured.contains("exchange-direct-sub"), 0.2);
        avaliacao.put(() -> captured.contains("exchange-topic-sub"), 0.2);
        avaliacao.put(() -> captured.contains("exchange-fanout-sub"), 0.2);
        avaliacao.put(() -> captured.contains("fila-sub-programacao"), 0.1);
        avaliacao.put(() -> captured.contains("fila-sub-arquitetura"), 0.1);
        avaliacao.put(() -> captured.contains("fila-sub"), 0.1);
        avaliacao.put(() -> captured.contains("b1"), 0.1);
        avaliacao.put(() -> captured.contains("b2"), 0.1);
        avaliacao.put(() -> captured.contains("b3"), 0.1);
        avaliacao.put(() -> captured.contains("b4"), 0.1);
        avaliacao.put(() -> captured.contains("b5"), 0.1);
        avaliacao.put(() -> captured.contains("b6"), 0.1);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio05() {

        final HashSet<String> captured = new HashSet<>();

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                try {
                    String exchange = (String) args[0];
                    String routingKey = (String) args[1];

                    if (exchange.equalsIgnoreCase("exchange-direct-sub") && routingKey.equalsIgnoreCase("programacao")) {
                        captured.add("send1");

                        if (args[2] instanceof Prova) {
                            captured.add("send1Object");
                        }
                    }

                    if (exchange.equalsIgnoreCase("exchange-direct-sub") && routingKey.equalsIgnoreCase("arquitetura")) {
                        captured.add("send2");

                        if (args[2] instanceof Prova) {
                            captured.add("send2Object");
                        }
                    }

                } catch (Exception e) {
                }

                return null;
            }
        }).when(rabbitTemplateMock).convertAndSend(Mockito.anyString(), Mockito.anyString(), Mockito.any(Object.class));

        startup.questao05();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> captured.contains("send1"), 0.3);
        avaliacao.put(() -> captured.contains("send2"), 0.3);
        avaliacao.put(() -> captured.contains("send1Object"), 0.2);
        avaliacao.put(() -> captured.contains("send2Object"), 0.2);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio06() {
        final HashSet<String> captured = new HashSet<>();

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                try {
                    String exchange = (String) args[0];

                    if (exchange.equalsIgnoreCase("exchange-fanout-sub")) {
                        captured.add("send1");

                        if (args[2] instanceof Prova) {
                            captured.add("send1Object");
                        }
                    }

                } catch (Exception e) {
                }

                return null;
            }
        }).when(rabbitTemplateMock).convertAndSend(Mockito.anyString(), Mockito.anyString(), Mockito.any(Object.class));

        startup.questao06();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> captured.contains("send1"), 0.3);
        avaliacao.put(() -> captured.contains("send1Object"), 0.2);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio07() {
        final HashSet<String> captured = new HashSet<>();

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                try {
                    String exchange = (String) args[0];
                    String routingKey = (String) args[1];

                    if (exchange.equalsIgnoreCase("exchange-topic-sub") && routingKey.equalsIgnoreCase("sub.programacao")) {
                        captured.add("send1");

                        if (args[2] instanceof Prova) {
                            captured.add("send1Object");
                        }
                    }

                    if (exchange.equalsIgnoreCase("exchange-topic-sub") && routingKey.equalsIgnoreCase("sub.arquitetura")) {
                        captured.add("send2");

                        if (args[2] instanceof Prova) {
                            captured.add("send2Object");
                        }
                    }

                } catch (Exception e) {
                }

                return null;
            }
        }).when(rabbitTemplateMock).convertAndSend(Mockito.anyString(), Mockito.anyString(), Mockito.any(Object.class));

        startup.questao07();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> captured.contains("send1"), 0.4);
        avaliacao.put(() -> captured.contains("send2"), 0.4);
        avaliacao.put(() -> captured.contains("send1Object"), 0.4);
        avaliacao.put(() -> captured.contains("send2Object"), 0.3);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio08() {
        final HashSet<String> captured = new HashSet<>();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {

                try {
                    captured.add("repository");
                } catch (Exception e) {
                }

                return null;
            }
        }).when(provaRepositoryMock).save(Mockito.any());

        startup.questao08(null);

        avaliacao.put(() -> captured.contains("repository"), 0.5);
        avaliacao.put(() -> {
            try {
                Method declaredMethod = RabbitStartup.class.getDeclaredMethod("questao08", Prova.class);

                return declaredMethod.isAnnotationPresent(RabbitListener.class)
                        && Stream.of(declaredMethod.getAnnotation(RabbitListener.class).queues()).anyMatch(q -> q.equalsIgnoreCase("fila-sub"));

            } catch (Exception e) {
                return false;
            }
        }, 1.0);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio09() {
        try {
            String context = IOUtils.toString(new FileInputStream(new ClassPathResource("sub1/questao09.json").getFile()), "UTF-8");

            Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

            avaliacao.put(() -> StringUtils.countOccurrencesOf(context, "Person") == 2, 0.2);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(context, "System_Ext") == 1, 0.3);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(context, "System") == 2, 0.2);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(context, "Rel") >= 4, 0.3);

            return runAvaliacao(avaliacao);
        } catch (Exception e) {
            return 0;
        }

    }

    private double corrigirExercicio10() {
        try {
            String file = IOUtils.toString(new FileInputStream(new ClassPathResource("sub1/questao10.json").getFile()), "UTF-8");

            final String container = file.replaceAll("C4_Container", "");

            Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

            avaliacao.put(() -> StringUtils.countOccurrencesOf(container, "Person") == 2, 0.2);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(container, "System_Boundary") == 1, 0.2);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(container, "System_Ext") == 1, 0.2);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(container, "Container") == 4, 0.4);
            avaliacao.put(() -> StringUtils.countOccurrencesOf(container, "Rel") >= 7, 0.5);

            return runAvaliacao(avaliacao);
        } catch (Exception e) {
            return 0;
        }
    }

    private double runAvaliacao(Map<BooleanSupplier, Double> avaliacao) {
        return round(avaliacao.entrySet().stream().filter(entry -> entry.getKey().getAsBoolean()).mapToDouble(Entry::getValue).sum(), 2);
    }

    private String getHash() {

        try {
            String java = IOUtils.toString(
                    this.getClass().getResource("hash.txt"),
                    "UTF-8");
            return Hex.encodeHexString(DigestUtils.sha256(java));
        } catch (Exception e) {
            return null;
        }

    }

}
