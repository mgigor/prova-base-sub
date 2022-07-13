package br.edu.unicesumar.prova.sub1.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.edu.unicesumar.prova.sub1.domain.Pessoa;

@Service
public class PessoaService {

    public Optional<Pessoa> buscarPorId(Long id) {
        return null;
    }

    public Page<Pessoa> buscarTodosPaginado(Pageable pageable) {
        return null;
    }

    public Pessoa salvarNovaPessoa(Pessoa pessoa) {
        return null;
    }

    public Pessoa atualizarPessoaExistente(Pessoa pessoa) {
        return null;
    }

    public void deletarPessoaPorId(Long id) {

    }

}
