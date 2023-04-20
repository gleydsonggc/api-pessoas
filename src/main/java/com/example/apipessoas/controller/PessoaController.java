package com.example.apipessoas.controller;

import com.example.apipessoas.model.Endereco;
import com.example.apipessoas.model.Pessoa;
import com.example.apipessoas.service.PessoaService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(PessoaController.PATH)
@RequiredArgsConstructor
public class PessoaController {

	public static final String PATH = "/api/v1/pessoas";

	private final PessoaService pessoaService;

	@GetMapping
	public List<Pessoa> listarPessoas() {
		return pessoaService.listarPessoas();
	}

	@PostMapping
	public ResponseEntity<Pessoa> salvar(
			@RequestBody @Valid Pessoa pessoa,
			HttpServletResponse response
	) {
		Pessoa pessoaSalva = pessoaService.salvar(pessoa);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
				.buildAndExpand(pessoaSalva.getId()).toUri();
		response.setHeader("Location", uri.toASCIIString());
		return ResponseEntity.created(uri).body(pessoaSalva);
	}

	@PutMapping("/{pessoaId}")
	public Pessoa atualizar(
			@PathVariable Long pessoaId,
			@RequestBody @Valid Pessoa pessoa
	) {
		return pessoaService.atualizar(pessoaId, pessoa);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		pessoaService.remover(id);
	}

	@GetMapping("/{id}")
	public Pessoa buscarPeloId(@PathVariable Long id) {
		return pessoaService.buscarPessoaPorId(id);
	}

	@GetMapping("/{pessoaId}/enderecos")
	public List<Endereco> listarEnderecos(@PathVariable Long pessoaId) {
		return pessoaService.listarEnderecos(pessoaId);
	}

	@PostMapping("/{id}/enderecos")
	public ResponseEntity<Endereco> adicionarEndereco(
			@PathVariable Long id,
		    @RequestBody @Valid Endereco endereco,
		    HttpServletResponse response
	) {
		Endereco enderecoSalvo = pessoaService.adicionarEndereco(id, endereco);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
				.buildAndExpand(enderecoSalvo.getId()).toUri();
		response.setHeader("Location", uri.toASCIIString());
		return ResponseEntity.created(uri).body(enderecoSalvo);
	}

	@PutMapping("/{pessoaId}/endereco-principal/{enderecoId}")
	public Endereco definirEnderecoPrincipal(
			@PathVariable Long pessoaId,
			@PathVariable Long enderecoId
	) {
		return pessoaService.definirEnderecoPrincipal(pessoaId, enderecoId);
	}

	@DeleteMapping("/{pessoaId}/enderecos/{enderecoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removerEndereco(
			@PathVariable Long pessoaId,
			@PathVariable Long enderecoId
	) {
		pessoaService.removerEndereco(pessoaId, enderecoId);
	}

	@PutMapping("/{pessoaId}/enderecos/{enderecoId}")
	public Endereco atualizarEndereco(
			@PathVariable Long pessoaId,
			@PathVariable Long enderecoId,
			@RequestBody @Valid Endereco endereco
	) {
		return pessoaService.atualizarEndereco(pessoaId, enderecoId, endereco);
	}

	@GetMapping("/{pessoaId}/enderecos/{enderecoId}")
	public Endereco buscarEndereco(
			@PathVariable Long pessoaId,
			@PathVariable Long enderecoId
	) {
		return pessoaService.buscarEndereco(pessoaId, enderecoId);
	}

}