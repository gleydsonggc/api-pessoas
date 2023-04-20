package com.example.apipessoas.service;

import com.example.apipessoas.exception.EnderecoNaoPertenceAPessoaException;
import com.example.apipessoas.exception.PessoaNaoEncontradaException;
import com.example.apipessoas.model.Endereco;
import com.example.apipessoas.model.Pessoa;
import com.example.apipessoas.repository.EnderecoRepository;
import com.example.apipessoas.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

	@Mock(strictness = Mock.Strictness.LENIENT)
	private PessoaRepository pessoaRepository;

	@Mock(strictness = Mock.Strictness.LENIENT)
	private EnderecoRepository enderecoRepository;

	@InjectMocks
	private PessoaService pessoaService;

	private Pessoa pessoa1;

	private Pessoa pessoa2;

	private Endereco endereco1;

	private Endereco endereco2;

	private Endereco endereco3;

	private List<Pessoa> pessoas;

	private final long pessoaIdInexistente = 12345L;

	@BeforeEach
	public void setUp() {
		pessoa1 = new Pessoa();
		pessoa1.setId(1L);
		pessoa1.setNome("Fulano");
		pessoa1.setDataNascimento(LocalDate.of(1990, 1, 1));
		pessoa1.setEnderecos(new ArrayList<>());
		pessoa1.setEnderecoPrincipal(new Endereco());

		pessoa2 = new Pessoa();
		pessoa2.setId(2L);
		pessoa2.setNome("Ciclano");
		pessoa2.setDataNascimento(LocalDate.of(1992, 2, 2));
		pessoa2.setEnderecos(new ArrayList<>());
		pessoa2.setEnderecoPrincipal(new Endereco());

		endereco1 = new Endereco();
		endereco1.setId(1L);
		endereco1.setLogradouro("Rua A");
		endereco1.setCep("11111-111");
		endereco1.setNumero(1);
		endereco1.setCidade("Cidade A");

		endereco2 = new Endereco();
		endereco2.setId(2L);
		endereco2.setLogradouro("Rua B");
		endereco2.setCep("22222-222");
		endereco2.setNumero(2);
		endereco2.setCidade("Cidade B");

		endereco3 = new Endereco();
		endereco3.setId(3L);
		endereco3.setLogradouro("Rua C");
		endereco3.setCep("33333-333");
		endereco3.setNumero(3);
		endereco3.setCidade("Cidade C");

		pessoa1.getEnderecos().add(endereco1);
		pessoa1.setEnderecoPrincipal(endereco1);
		pessoa2.getEnderecos().add(endereco2);
		pessoa2.setEnderecoPrincipal(endereco2);

		pessoas = new ArrayList<>(List.of(pessoa1, pessoa2));

		when(pessoaRepository.save(any(Pessoa.class)))
				.thenReturn(pessoa1);
		when(pessoaRepository.findAll())
				.thenReturn(pessoas);
		when(pessoaRepository.findById(pessoa1.getId()))
				.thenReturn(Optional.of(pessoa1));
		when(pessoaRepository.findById(pessoaIdInexistente))
				.thenThrow(new PessoaNaoEncontradaException(pessoaIdInexistente));
		doThrow(EmptyResultDataAccessException.class)
				.when(pessoaRepository).deleteById(pessoaIdInexistente);

		when(enderecoRepository.findById(endereco1.getId()))
				.thenReturn(Optional.ofNullable(endereco1));
		when(enderecoRepository.save(endereco3))
				.thenReturn(endereco3);
	}

	@Test
	void deveListarPessoas() {
		assertThat(pessoaService.listarPessoas())
				.hasSize(2)
				.usingRecursiveComparison()
				.isEqualTo(pessoas);
	}

	@Test
	void deveSalvarPessoa() {
		assertThat(pessoaService.salvar(pessoa1))
				.usingRecursiveComparison()
				.isEqualTo(pessoa1);
	}

	@Test
	void deveAtualizarPessoa() {
		Pessoa pessoa1Atualizada = pessoa1.withNome("Pessoa1 Atualizada");
		pessoa1Atualizada.setDataNascimento(LocalDate.of(2000, 1, 1));
		pessoa1Atualizada.getEnderecos().add(endereco2);
		pessoa1Atualizada.setEnderecoPrincipal(endereco2);
		when(pessoaRepository.save(pessoa1Atualizada))
				.thenReturn(pessoa1Atualizada);

		assertThat(pessoaService.atualizar(pessoa1.getId(), pessoa1Atualizada))
				.isEqualTo(pessoa1Atualizada);
	}

	@Test
	void deveLancarExcecao_QuandoAtualizarPessoaInexistente() {
		Pessoa pessoaInexistente = Pessoa.builder().id(pessoaIdInexistente).build();
		assertThatThrownBy(() -> pessoaService.atualizar(pessoaIdInexistente, pessoaInexistente))
				.isInstanceOf(PessoaNaoEncontradaException.class);
	}

	@Test
	void deveExcluirPessoa() {
		when(pessoaRepository.existsById(pessoa1.getId()))
				.thenReturn(true);

		pessoaService.remover(pessoa1.getId());

		verify(pessoaRepository).deleteById(pessoa1.getId());
	}

	@Test
	void deveLancarExcecao_QuandoExcluirPessoaInexistente() {
		assertThatThrownBy(() -> pessoaService.remover(pessoaIdInexistente))
				.isInstanceOf(PessoaNaoEncontradaException.class);
		verify(pessoaRepository, never()).deleteById(pessoaIdInexistente);
	}

	@Test
	void deveBuscarPessoaPeloId() {
		assertThat(pessoaService.buscarPessoaPorId(pessoa1.getId()))
				.usingRecursiveComparison()
				.isEqualTo(pessoa1);
		verify(pessoaRepository).findById(pessoa1.getId());
	}

	@Test
	void deveLancarExcecao_QuandoBuscarPessoaInexistente() {
		assertThatThrownBy(() -> pessoaService.buscarPessoaPorId(pessoaIdInexistente))
				.isInstanceOf(PessoaNaoEncontradaException.class);
		verify(pessoaRepository).findById(pessoaIdInexistente);
	}

	@Test
	void deveListarEnderecosDaPessoa() {
		assertThat(pessoaService.listarEnderecos(pessoa1.getId()))
				.singleElement()
				.isEqualTo(endereco1);
		verify(pessoaRepository).findById(pessoa1.getId());
	}

	@Test
	void deveAdicionarEnderecoAPessoa() {
		assertThat(pessoaService.adicionarEndereco(pessoa1.getId(), endereco3))
				.usingRecursiveComparison()
				.isEqualTo(endereco3);
		assertThat(pessoa1.getEnderecos()).contains(endereco3);
		verify(pessoaRepository).findById(pessoa1.getId());
		verify(enderecoRepository).save(endereco3);
	}
	@Test
	void deveLancarExcecao_QuandoAdicionarEnderecoAPessoaInexistente() {
		assertThatThrownBy(() -> pessoaService.adicionarEndereco(pessoaIdInexistente, endereco1))
				.isInstanceOf(PessoaNaoEncontradaException.class);
		verify(pessoaRepository).findById(pessoaIdInexistente);
	}

	@Test
	void deveRemoverEnderecoDaPessoa() {
		pessoaService.removerEndereco(pessoa1.getId(), endereco1.getId());

		when(pessoaRepository.findById(pessoa1.getId()))
				.thenReturn(Optional.of(pessoa1.withEnderecos(new ArrayList<>())));
		assertThat(pessoaService.listarEnderecos(pessoa1.getId()))
				.doesNotContain(endereco1);
		verify(pessoaRepository, times(2)).findById(pessoa1.getId());
		verify(enderecoRepository).delete(endereco1);
	}

	@Test
	void deveLancarExcecao_QuandoTentarRemoverEnderecoQueNaoPertenceAPessoa() {
		when(enderecoRepository.findById(endereco2.getId()))
				.thenReturn(Optional.ofNullable(endereco2));

		assertThatThrownBy(() -> pessoaService.removerEndereco(pessoa1.getId(), endereco2.getId()))
				.isInstanceOf(EnderecoNaoPertenceAPessoaException.class);

		verify(pessoaRepository).findById(pessoa1.getId());
		verify(enderecoRepository).findById(endereco2.getId());
	}

	@Test
	void deveDefinirEnderecoPrincipalDaPessoa() {
		pessoa1.getEnderecos().add(endereco3);

		assertThat(pessoaService.definirEnderecoPrincipal(pessoa1.getId(), endereco3.getId()))
				.usingRecursiveComparison()
				.isEqualTo(endereco3);
		assertThat(pessoa1.getEnderecoPrincipal()).isEqualTo(endereco3);
		verify(pessoaRepository).findById(pessoa1.getId());
		verify(pessoaRepository).save(pessoa1);
	}

	@Test
	void deveDefinirEnderecoPrincipalDaPessoa_QuandoAdicionarSeuPrimeiroEndereco() {
		pessoa1.setEnderecoPrincipal(null);
		pessoa1.getEnderecos().clear();

		assertThat(pessoaService.adicionarEndereco(pessoa1.getId(), endereco1))
				.usingRecursiveComparison()
				.isEqualTo(endereco1);

		assertThat(pessoa1.getEnderecoPrincipal()).isEqualTo(endereco1);
		verify(pessoaRepository).findById(pessoa1.getId());
		verify(pessoaRepository).save(pessoa1);
		verify(enderecoRepository).save(endereco1);
	}

	@Test
	void deveDefinirEnderecoPrincipalDaPessoa_QuandoRemoverOPrincipalAtualEAindaPossuirOutrosEnderecos() {
		pessoa1.setEnderecoPrincipal(endereco1);
		pessoa1.getEnderecos().add(endereco2);

		pessoaService.removerEndereco(pessoa1.getId(), endereco1.getId());

		assertThat(pessoa1.getEnderecoPrincipal()).isEqualTo(endereco2);
		verify(pessoaRepository).findById(pessoa1.getId());
		verify(enderecoRepository).findById(endereco1.getId());
		verify(enderecoRepository).delete(endereco1);
	}

	@Test
	void deveAtualizarEndereco() {
		endereco2.setId(null);
		when(enderecoRepository.save(endereco1)).thenReturn(endereco2);

		assertThat(pessoaService.atualizarEndereco(pessoa1.getId(), endereco1.getId(), endereco2))
				.usingRecursiveComparison()
				.isEqualTo(endereco2);

		verify(pessoaRepository).findById(pessoa1.getId());
		verify(enderecoRepository).findById(endereco1.getId());
		verify(enderecoRepository).save(endereco2.withId(endereco1.getId()));
	}

	@Test
	void deveLancarExcecao_QuandoTentarAtualizarEnderecoQueNaoPertenceAPessoa() {
		when(enderecoRepository.findById(endereco2.getId())).thenReturn(Optional.of(endereco2));

		assertThatThrownBy(() -> pessoaService.atualizarEndereco(pessoa1.getId(), endereco2.getId(), endereco3))
				.isInstanceOf(EnderecoNaoPertenceAPessoaException.class);

		verify(pessoaRepository).findById(pessoa1.getId());
		verify(enderecoRepository).findById(endereco2.getId());
	}

}
