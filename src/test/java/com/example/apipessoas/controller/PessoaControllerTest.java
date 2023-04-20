package com.example.apipessoas.controller;

import com.example.apipessoas.model.Endereco;
import com.example.apipessoas.model.Pessoa;
import com.example.apipessoas.repository.EnderecoRepository;
import com.example.apipessoas.repository.PessoaRepository;
import com.example.apipessoas.service.PessoaService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PessoaControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
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
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = PessoaController.PATH;
        baseUrl = RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath;

        pessoa1 = new Pessoa();
        pessoa1.setId(1L);
        pessoa1.setNome("Fulano");
        pessoa1.setDataNascimento(LocalDate.of(1990, 1, 1));
        pessoa1.setEnderecos(new ArrayList<>());

        pessoa2 = new Pessoa();
        pessoa2.setId(2L);
        pessoa2.setNome("Ciclano");
        pessoa2.setDataNascimento(LocalDate.of(1992, 2, 2));
        pessoa2.setEnderecos(new ArrayList<>());

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

        pessoas = new ArrayList<>(List.of(pessoa1, pessoa2));
    }

    @Test
    void deveSalvarUmaPessoaERetornar201() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(pessoa1)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .header("Location", equalTo(baseUrl + "/1"))
            .body("id", equalTo(pessoa1.getId().intValue()))
            .body("nome", equalTo(pessoa1.getNome()))
            .body("dataNascimento", equalTo(pessoa1.getDataNascimento().toString()))
            .body("enderecos", is(empty()))
            .body("enderecoPrincipal", nullValue());
    }

    @Test
    void deveRetornarUmaPessoaPeloId() {
        pessoaRepository.save(pessoa1);

        given()
            .pathParam("id", pessoa1.getId())
        .when()
            .get("/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(pessoa1.getId().intValue()))
            .body("nome", equalTo(pessoa1.getNome()))
            .body("dataNascimento", equalTo(pessoa1.getDataNascimento().toString()))
            .body("enderecos", is(empty()))
            .body("enderecoPrincipal", nullValue());
    }

    @Test
    void deveRetornarUmaListaDePessoas() {
        pessoaRepository.save(pessoa1);
        pessoaRepository.save(pessoa2);

        given().log().all()
        .when()
            .get()
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(2))
            .body("[0].id", equalTo(pessoa1.getId().intValue()))
            .body("[0].nome", equalTo(pessoa1.getNome()))
            .body("[0].dataNascimento", equalTo(pessoa1.getDataNascimento().toString()))
            .body("[0].enderecos", is(empty()))
            .body("[0].enderecoPrincipal", nullValue())
            .body("[1].id", equalTo(pessoa2.getId().intValue()))
            .body("[1].nome", equalTo(pessoa2.getNome()))
            .body("[1].dataNascimento", equalTo(pessoa2.getDataNascimento().toString()))
            .body("[1].enderecos", is(empty()))
            .body("[1].enderecoPrincipal", nullValue());
    }

    @Test
    void deveRemoverUmaPessoaPeloId() {
        pessoaRepository.save(pessoa1);

        given()
            .pathParam("id", pessoa1.getId())
        .when()
            .delete("/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void deveAtualizarUmaPessoaPeloId() {
        pessoaRepository.save(pessoa1);
        pessoa2.setId(null);

        given()
            .pathParam("id", pessoa1.getId())
            .body(pessoa2)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/{id}")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(pessoa1.getId().intValue()))
            .body("nome", equalTo(pessoa2.getNome()))
            .body("dataNascimento", equalTo(pessoa2.getDataNascimento().toString()))
            .body("enderecos", is(empty()))
            .body("enderecoPrincipal", nullValue());
    }

    @Test
    void deveAdicionarUmEnderecoAUmaPessoaERetornar201() {
        pessoaRepository.save(pessoa1);

        given()
            .pathParam("id", pessoa1.getId())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(endereco1)
        .when()
            .post("/{id}/enderecos")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.CREATED.value())
            .header("Location", equalTo(baseUrl + "/1/enderecos/1"))
            .body("id", equalTo(endereco1.getId().intValue()))
            .body("logradouro", equalTo(endereco1.getLogradouro()))
            .body("numero", equalTo(endereco1.getNumero()))
            .body("cidade", equalTo(endereco1.getCidade()))
            .body("cep", equalTo(endereco1.getCep()));
    }

    @Test
    void deveListarEnderecosDeUmaPessoa() {
        pessoa1 = pessoaService.salvar(pessoa1);
        pessoaService.adicionarEndereco(pessoa1.getId(), endereco1);
        pessoaService.adicionarEndereco(pessoa1.getId(), endereco2);

        given()
            .pathParam("id", pessoa1.getId())
        .when()
            .get("/{id}/enderecos")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(2))
            .body("[0].id", equalTo(endereco1.getId().intValue()))
            .body("[0].logradouro", equalTo(endereco1.getLogradouro()))
            .body("[0].numero", equalTo(endereco1.getNumero()))
            .body("[0].cidade", equalTo(endereco1.getCidade()))
            .body("[0].cep", equalTo(endereco1.getCep()))
            .body("[1].id", equalTo(endereco2.getId().intValue()))
            .body("[1].logradouro", equalTo(endereco2.getLogradouro()))
            .body("[1].numero", equalTo(endereco2.getNumero()))
            .body("[1].cidade", equalTo(endereco2.getCidade()))
            .body("[1].cep", equalTo(endereco2.getCep()));
    }

    @Test
    void deveDefinirEnderecoPrincipal() {
        pessoaRepository.save(pessoa1);
        pessoaService.adicionarEndereco(pessoa1.getId(), endereco1);
        pessoaService.adicionarEndereco(pessoa1.getId(), endereco2);

        given()
            .pathParam("pessoaId", pessoa1.getId())
            .pathParam("enderecoId", endereco2.getId())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(endereco1)
        .when()
            .put("/{pessoaId}/endereco-principal/{enderecoId}")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(endereco2.getId().intValue()))
            .body("logradouro", equalTo(endereco2.getLogradouro()))
            .body("numero", equalTo(endereco2.getNumero()))
            .body("cidade", equalTo(endereco2.getCidade()))
            .body("cep", equalTo(endereco2.getCep()));

        assertThat(pessoaService.buscarPessoaPorId(pessoa1.getId()).getEnderecoPrincipal())
                .isEqualTo(endereco2);
    }

    @Test
    void deveRemoverEndereco() {
        pessoa1.setId(null);
        pessoa1 = pessoaService.salvar(pessoa1);
        pessoaService.adicionarEndereco(pessoa1.getId(), endereco1);

        given()
            .pathParam("pessoaId", pessoa1.getId())
            .pathParam("enderecoId", endereco1.getId())
        .when()
            .delete("/{pessoaId}/enderecos/{enderecoId}")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        given()
            .pathParam("id", pessoa1.getId())
        .when()
            .get("/{id}/enderecos")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(0));
    }

    @Test
    void deveAtualizarEndereco() {
        pessoa1 = pessoaService.salvar(pessoa1);
        pessoaService.adicionarEndereco(pessoa1.getId(), endereco1);

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("pessoaId", pessoa1.getId())
            .pathParam("enderecoId", endereco1.getId())
            .body(endereco2)
        .when()
            .put("/{pessoaId}/enderecos/{enderecoId}")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value());

        given()
            .pathParam("pessoaId", pessoa1.getId())
            .pathParam("enderecoId", endereco1.getId())
        .when()
            .get("/{pessoaId}/enderecos/{enderecoId}")
        .then().log().all().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(endereco1.getId().intValue()))
            .body("logradouro", equalTo(endereco2.getLogradouro()))
            .body("numero", equalTo(endereco2.getNumero()))
            .body("cidade", equalTo(endereco2.getCidade()))
            .body("cep", equalTo(endereco2.getCep()));
    }

}
