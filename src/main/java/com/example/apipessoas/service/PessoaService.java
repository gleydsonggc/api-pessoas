package com.example.apipessoas.service;

import com.example.apipessoas.exception.EnderecoNaoEncontradoException;
import com.example.apipessoas.exception.EnderecoNaoPertenceAPessoaException;
import com.example.apipessoas.exception.PessoaNaoEncontradaException;
import com.example.apipessoas.model.Endereco;
import com.example.apipessoas.model.Pessoa;
import com.example.apipessoas.repository.EnderecoRepository;
import com.example.apipessoas.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PessoaService {

	private final PessoaRepository pessoaRepository;

	private final EnderecoRepository enderecoRepository;

	public List<Pessoa> listarPessoas() {
		return pessoaRepository.findAll();
	}

	public Pessoa salvar(Pessoa pessoa) {
		Pessoa pessoaASalvar = Pessoa.builder()
				.nome(pessoa.getNome())
				.dataNascimento(pessoa.getDataNascimento())
				.build();
		return pessoaRepository.save(pessoaASalvar);
	}

	public Pessoa atualizar(Long id, Pessoa pessoa) {
		Pessoa pessoaSalva = buscarPessoaPorId(id);
		BeanUtils.copyProperties(pessoa, pessoaSalva, "id", "enderecos", "enderecoPrincipal");
		return pessoaRepository.save(pessoaSalva);
	}

	public void remover(Long pessoaId) {
		if (!pessoaRepository.existsById(pessoaId)) {
			throw new PessoaNaoEncontradaException(pessoaId);
		}
		pessoaRepository.deleteById(pessoaId);
	}

	public Pessoa buscarPessoaPorId(Long id) {
		return pessoaRepository.findById(id)
				.orElseThrow(() -> new PessoaNaoEncontradaException(id));
	}

	public List<Endereco> listarEnderecos(Long pessoaId) {
		Pessoa pessoa = buscarPessoaPorId(pessoaId);
		return Collections.unmodifiableList(pessoa.getEnderecos());
	}

	public Endereco adicionarEndereco(Long pessoaId, Endereco endereco) {
		Pessoa pessoa = buscarPessoaPorId(pessoaId);
		endereco.setId(null);
		pessoa.getEnderecos().add(endereco);
		enderecoRepository.save(endereco);
		if (pessoa.getEnderecoPrincipal() == null) {
			pessoa.setEnderecoPrincipal(endereco);
			pessoaRepository.save(pessoa);
		}
		return endereco;
	}

	public void removerEndereco(Long pessoaId, Long enderecoId) {
		Pessoa pessoa = buscarPessoaPorId(pessoaId);
		Endereco endereco = enderecoRepository.findById(enderecoId)
				.orElseThrow(() -> new EnderecoNaoEncontradoException(enderecoId));
		if (!pessoa.getEnderecos().contains(endereco)) {
			throw new EnderecoNaoPertenceAPessoaException(enderecoId);
		}
		if (pessoa.getEnderecoPrincipal().equals(endereco)) {
			pessoa.setEnderecoPrincipal(null);
		}
		pessoa.getEnderecos().remove(endereco);
		if (!pessoa.getEnderecos().isEmpty()) {
			pessoa.setEnderecoPrincipal(pessoa.getEnderecos().get(0));
		}
		enderecoRepository.delete(endereco);
	}

	public Endereco definirEnderecoPrincipal(Long pessoaId, Long enderecoId) {
		Pessoa pessoa = buscarPessoaPorId(pessoaId);
		List<Endereco> enderecos = pessoa.getEnderecos();
		Endereco novoEnderecoPrincipal = enderecos.stream()
				.filter(endereco -> endereco.getId().equals(enderecoId))
				.findFirst()
				.orElseThrow(() -> new EnderecoNaoEncontradoException(enderecoId));

		pessoa.setEnderecoPrincipal(novoEnderecoPrincipal);
		pessoaRepository.save(pessoa);
		return novoEnderecoPrincipal;
	}

	public Endereco atualizarEndereco(Long pessoaId, Long enderecoId, Endereco endereco) {
		Endereco enderecoEncontrado = buscarEndereco(pessoaId, enderecoId);
		BeanUtils.copyProperties(endereco, enderecoEncontrado, "id");
		return enderecoRepository.save(enderecoEncontrado);
	}

	public Endereco buscarEndereco(Long pessoaId, Long enderecoId) {
		Pessoa pessoa = buscarPessoaPorId(pessoaId);
		Endereco enderecoEncontrado = enderecoRepository.findById(enderecoId)
				.orElseThrow(() -> new EnderecoNaoEncontradoException(enderecoId));
		if (!pessoa.getEnderecos().contains(enderecoEncontrado)) {
			throw new EnderecoNaoPertenceAPessoaException(enderecoId);
		}
		return enderecoEncontrado;
	}

}
