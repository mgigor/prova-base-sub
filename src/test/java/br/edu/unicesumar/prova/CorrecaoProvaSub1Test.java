package br.edu.unicesumar.prova;

import static br.edu.unicesumar.prova.CorrecaoService.round;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.unicesumar.prova.sub1.domain.Filho;
import br.edu.unicesumar.prova.sub1.domain.Pessoa;
import br.edu.unicesumar.prova.sub1.repository.PessoaRepository;
import br.edu.unicesumar.prova.sub1.service.PessoaService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWebMvc
class CorrecaoProvaSub1Test {

    @Autowired
    private CorrecaoService correcaoService;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void correcao() {
        double notaExercicios05 = corrigirExercicio05();
        double notaExercicios06 = corrigirExercicio06();
        double notaExercicios07 = corrigirExercicio07();
        double notaExercicios08 = corrigirExercicio08();
        double notaExercicios09 = corrigirExercicio09();
        double notaExercicios10 = corrigirExercicio10();

        double total = notaExercicios05 + notaExercicios06 + notaExercicios07 + notaExercicios08 + notaExercicios09 + notaExercicios10;

        System.out.println("\n\n###### CORREÇÃO ######\n");
        System.out.println("Exercício\tNota");
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

    private double corrigirExercicio05() {

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> Pessoa.class.isAnnotationPresent(Entity.class), 0.2);
        avaliacao.put(() -> pessoaFieldsStream().anyMatch(f -> f.isAnnotationPresent(Id.class)), 0.2);
        avaliacao.put(() -> pessoaFieldsStream().anyMatch(f -> f.isAnnotationPresent(GeneratedValue.class)), 0.1);

        avaliacao.put(() -> existsFieldInPessoa(String.class, "nome"), 0.1);
        avaliacao.put(() -> existsFieldInPessoa(LocalDate.class, "dataNascimento"), 0.1);
        avaliacao.put(() -> existsFieldInPessoa(String.class, "email"), 0.1);
        avaliacao.put(() -> existsFieldInPessoa(String.class, "cpf"), 0.1);
        avaliacao.put(() -> existsFieldInPessoa(String.class, "rg"), 0.1);

        avaliacao.put(() -> pessoaFieldsStream().anyMatch(f -> f.getType().isAssignableFrom(List.class)
                && f.isAnnotationPresent(OneToMany.class)
                && Stream.of(f.getAnnotation(OneToMany.class).cascade()).anyMatch(c -> c.equals(CascadeType.ALL))), 0.5);

        avaliacao.put(() -> existsValidationInClass(Pessoa.class, "email", "Email"), 0.1);
        avaliacao.put(() -> existsValidationInClass(Pessoa.class, "nome", "CPF"), 0.1);

        avaliacao.put(() -> existsValidationInClass(Pessoa.class, "nome", "NotBlank", "NotEmpty", "NotNull")
                && existsValidationInClass(Pessoa.class, "nome", "NotBlank", "NotEmpty", "NotNull")
                && existsValidationInClass(Pessoa.class, "email", "NotBlank", "NotEmpty", "NotNull")
                && existsValidationInClass(Pessoa.class, "rg", "NotBlank", "NotEmpty", "NotNull")
                && existsValidationInClass(Filho.class, "nome", "NotBlank", "NotEmpty", "NotNull")
                && existsValidationInClass(Filho.class, "sexo", "NotBlank", "NotEmpty", "NotNull"), 0.2);

        avaliacao.put(() -> existsValidationInClass(Pessoa.class, "dataNascimento", "NotNull")
                && existsValidationInClass(Filho.class, "dataNascimento", "NotNull"), 0.1);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio06() {

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> Arrays.asList(PessoaRepository.class.getInterfaces()).contains(JpaRepository.class), 0.3);
        avaliacao.put(() -> Optional.ofNullable(PessoaRepository.class.getGenericInterfaces())
                .map(gf -> Stream.of(gf).findFirst().orElse(null))
                .map(f -> f.getTypeName().contains("Long") && f.getTypeName().contains("Pessoa")).orElse(false), 0.2);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio07() {

        Pessoa pessoa = fillPessoa();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> !pessoaService.buscarPorId(999L).isPresent(), 0.2);
        avaliacao.put(() -> pessoaService.buscarTodosPaginado(Pageable.unpaged()) != null, 0.2);
        avaliacao.put(() -> pessoaService.salvarNovaPessoa(pessoa).getId() != null, 0.2);

        avaliacao.put(() -> {
            Pessoa pessoaSalva = pessoaService.salvarNovaPessoa(pessoa);
            Pessoa pessoaEditada = pessoaService.atualizarPessoaExistente(pessoaSalva);
            return pessoaSalva.getId() == pessoaEditada.getId();
        }, 0.3);

        avaliacao.put(() -> {
            Pessoa pessoaSalva = pessoaService.salvarNovaPessoa(pessoa);
            pessoaService.deletarPessoaPorId(pessoaSalva.getId());
            return !pessoaService.buscarPorId(pessoaSalva.getId()).isPresent();
        }, 0.3);

        avaliacao.put(() -> {
            Pessoa pessoaSalva = pessoaService.salvarNovaPessoa(pessoa);
            return pessoaService.buscarPorId(pessoaSalva.getId()).isPresent();
        }, 0.3);

        avaliacao.put(() -> {
            pessoaService.salvarNovaPessoa(pessoa);
            try {
                pessoaService.salvarNovaPessoa(pessoa);
            } catch (Exception e) {
                return true;
            }
            return false;
        }, 0.5);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio08() {
        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        Pessoa pessoa = fillPessoa();

        avaliacao.put(() -> {
            try {
                MvcResult andReturn = mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoa)))
                        .andReturn();

                String contentAsString = andReturn.getResponse().getContentAsString();

                return (andReturn.getResponse().getStatus() == 200 || andReturn.getResponse().getStatus() == 201) && contentAsString.contains("id");
            } catch (Exception e) {
                return false;
            }
        }, 0.2);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaSalvaReturn = mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoa)))
                        .andReturn();

                Pessoa pessoaSalva = objectMapper.readValue(pessoaSalvaReturn.getResponse().getContentAsString(), Pessoa.class);

                MvcResult pessoaPaginadaReturn = mockMvc.perform(get("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                PageImpl<Pessoa> pessoaPaginada = objectMapper.readValue(pessoaPaginadaReturn.getResponse().getContentAsString(),
                        new TypeReference<PageImpl<Pessoa>>() {
                        });

                return pessoaPaginada.getTotalElements() == 1L
                        && pessoaPaginada.getContent().stream().findFirst().filter(p -> p.getId() == pessoaSalva.getId()).isPresent();
            } catch (Exception e) {
                return false;
            }
        }, 0.3);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaSalvaReturn = mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoa)))
                        .andReturn();

                Pessoa pessoaSalva = objectMapper.readValue(pessoaSalvaReturn.getResponse().getContentAsString(), Pessoa.class);

                MvcResult pessoaPorIdReturn = mockMvc.perform(get("/pessoa/" + pessoaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                Pessoa pessoaPorId = objectMapper.readValue(pessoaPorIdReturn.getResponse().getContentAsString(), Pessoa.class);

                return pessoaPorId.getId() == pessoaSalva.getId();
            } catch (Exception e) {
                return false;
            }
        }, 0.3);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaSalvaReturn = mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoa)))
                        .andReturn();

                Pessoa pessoaSalva = objectMapper.readValue(pessoaSalvaReturn.getResponse().getContentAsString(), Pessoa.class);

                MvcResult pessoaEditadaReturn = mockMvc.perform(put("/pessoa/" + pessoaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pessoaSalvaReturn.getResponse().getContentAsString()))
                        .andReturn();

                Pessoa pessoaEditada = objectMapper.readValue(pessoaEditadaReturn.getResponse().getContentAsString(), Pessoa.class);

                return pessoaSalva.getId() == pessoaEditada.getId();
            } catch (Exception e) {
                return false;
            }
        }, 0.4);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaSalvaReturn = mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoa)))
                        .andReturn();

                Pessoa pessoaSalva = objectMapper.readValue(pessoaSalvaReturn.getResponse().getContentAsString(), Pessoa.class);

                mockMvc.perform(delete("/pessoa/" + pessoaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                MvcResult pessoaPorIdReturn = mockMvc.perform(put("/pessoa/" + pessoaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pessoaSalvaReturn.getResponse().getContentAsString()))
                        .andReturn();

                try {
                    objectMapper.readValue(pessoaPorIdReturn.getResponse().getContentAsString(), Pessoa.class);
                } catch (Exception e) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }, 0.3);

        return runAvaliacao(avaliacao);
    }

    private double corrigirExercicio09() {
        Pessoa pessoa = fillPessoa();

        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        avaliacao.put(() -> {
            try {
                MvcResult pessoaSalvaReturn = mockMvc.perform(post("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoa)))
                        .andReturn();

                return pessoaSalvaReturn.getResponse().getStatus() == 201;
            } catch (Exception e) {
                return false;
            }
        }, 0.2);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaGetPorIdReturn = mockMvc.perform(get("/pessoa/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                return pessoaGetPorIdReturn.getResponse().getStatus() == 404;
            } catch (Exception e) {
                return false;
            }
        }, 0.2);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaGetPorIdReturn = mockMvc.perform(get("/pessoa")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                return pessoaGetPorIdReturn.getResponse().getStatus() == 200;
            } catch (Exception e) {
                return false;
            }
        }, 0.2);

        avaliacao.put(() -> {
            try {
                MvcResult pessoaGetPorIdReturn = mockMvc.perform(delete("/pessoa/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                return pessoaGetPorIdReturn.getResponse().getStatus() == 404;
            } catch (Exception e) {
                return false;
            }
        }, 0.2);

        avaliacao.put(() -> {
            try {
                Pessoa pessoaIdNotExists = fillPessoa();
                pessoaIdNotExists.setId(9999L);
                MvcResult pessoaGetPorIdReturn = mockMvc.perform(put("/pessoa/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(pessoaIdNotExists)))
                        .andReturn();

                return pessoaGetPorIdReturn.getResponse().getStatus() == 404;
            } catch (Exception e) {
                return false;
            }
        }, 0.2);

        return runAvaliacao(avaliacao);
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

    private double corrigirExercicio10() {
        Map<BooleanSupplier, Double> avaliacao = new HashMap<>();

        try {
            String json = IOUtils.toString(
                    ProvaApplication.class.getResource("sub1/questao10.json"),
                    "UTF-8");

            avaliacao.put(() -> {
                try {
                    objectMapper.readValue(json, Pessoa.class);
                    return json.length() > 3;
                } catch (Exception e) {
                    return false;
                }
            }, 0.3);

            avaliacao.put(() -> {
                try {
                    objectMapper.readValue(json, Pessoa.class);

                    String jsonFilhos = json.substring(json.indexOf("["), json.indexOf("]") + 1);

                    List<Filho> filhos = objectMapper.readValue(jsonFilhos, new TypeReference<List<Filho>>() {
                    });

                    return filhos.size() == 2;
                } catch (Exception e) {
                    return false;
                }
            }, 0.7);

        } catch (IOException e) {
            return 0;
        }

        return runAvaliacao(avaliacao);
    }

    private String objectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Stream<Field> pessoaFieldsStream() {
        return Stream.of(Pessoa.class.getDeclaredFields());
    }

    private Boolean existsValidationInClass(Class<?> clazz, String field, String... validator) {
        return Stream.of(clazz.getDeclaredFields())
                .anyMatch(f -> Stream.of(f.getDeclaredAnnotations())
                        .anyMatch(ann -> Stream.of(validator).anyMatch(v -> v.equalsIgnoreCase(ann.annotationType().getSimpleName()))));
    }

    private Boolean existsFieldInPessoa(Class<?> type, String field) {
        return pessoaFieldsStream().anyMatch(f -> f.getType().equals(type) && f.getName().equalsIgnoreCase(field));
    }

    private double runAvaliacao(Map<BooleanSupplier, Double> avaliacao) {
        return round(avaliacao.entrySet().stream().filter(entry -> correcaoService.getResult(entry)).mapToDouble(Entry::getValue).sum(), 2);
    }

    private Pessoa fillPessoa() {

        Pessoa pessoa = new Pessoa();

        Stream.of(pessoa.getClass().getDeclaredFields()).filter(f -> !f.isAnnotationPresent(Id.class)).forEach(f -> {
            f.setAccessible(true);

            try {

                if (f.getType().equals(String.class)) {
                    f.set(pessoa, "96164950031");
                    if (f.isAnnotationPresent(Email.class)) {
                        f.set(pessoa, "email@email");
                    }
                }

                if (f.getType().equals(Long.class)) {
                    f.set(pessoa, 0L);
                }

                if (f.getType().equals(Integer.class)) {
                    f.set(pessoa, 0);
                }

                if (f.getType().equals(LocalDate.class)) {
                    f.set(pessoa, LocalDate.now());
                }

                if (f.getType().equals(Date.class)) {
                    f.set(pessoa, new Date());
                }

                if (f.getType().equals(LocalDateTime.class)) {
                    f.set(pessoa, LocalDateTime.now());
                }

                if (f.getType().equals(UUID.class)) {
                    f.set(pessoa, UUID.randomUUID());
                }
            } catch (Exception e) {
            }

        });

        return pessoa;
    }

}

@Service
class CorrecaoService {

    @Autowired
    private EntityManager em;

    @Rollback
    @Transactional
    public Boolean getResult(Entry<BooleanSupplier, ?> entry) {
        try {
            return entry.getKey().getAsBoolean();
        } catch (Exception e) {
            return false;
        } finally {
            em.flush();
            em.clear();
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        }
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String noHandlerFoundException(
            NoHandlerFoundException ex) {
        return "No handler found for your request.";
    }

}

class PageImpl<T> {
    private List<T> content;
    private int number;
    private int size;
    private Long totalElements;

    public PageImpl() {

    }

    public PageImpl(List<T> content, int number, int size, Long totalElements) {
        this.content = content;
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

}
