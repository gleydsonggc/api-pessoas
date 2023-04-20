package com.example.apipessoas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EnderecoNaoPertenceAPessoaException extends EntidadeNaoEncontradaException {

	private static final long serialVersionUID = 1L;

	public EnderecoNaoPertenceAPessoaException(String mensagem) {
		super(mensagem);
	}

	public EnderecoNaoPertenceAPessoaException(Long enderecoId) {
		this(String.format("O endereço com id %d não pertence a esta pessoa", enderecoId));
	}
	
}