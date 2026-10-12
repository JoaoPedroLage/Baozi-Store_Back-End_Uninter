package br.com.baozistore.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRegistrarPedidoDeUmClienteParaUmProduto() throws Exception {
        long clienteId = criar("/clientes", """
                {"nome": "Ana"}
                """);
        long produtoId = criar("/produtos", """
                {"nome": "Baozi de Porco", "preco": 7.50, "estoque": true}
                """);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(clienteId, produtoId, 6)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.clienteNome").value("Ana"))
                .andExpect(jsonPath("$.produtoNome").value("Baozi de Porco"))
                .andExpect(jsonPath("$.quantidade").value(6))
                .andExpect(jsonPath("$.valorTotal").value(45.0));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        long produtoId = criar("/produtos", """
                {"nome": "Baozi de Porco", "preco": 7.50, "estoque": true}
                """);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(999, produtoId, 2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar422QuandoProdutoEstiverSemEstoque() throws Exception {
        long clienteId = criar("/clientes", """
                {"nome": "Ana"}
                """);
        long produtoId = criar("/produtos", """
                {"nome": "Baozi de Frango", "preco": 6.00, "estoque": false}
                """);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(clienteId, produtoId, 1)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void deveRetornar400QuandoQuantidadeForInvalida() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(1, 1, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.quantidade").exists());
    }

    @Test
    void naoDeveExcluirClienteComPedido() throws Exception {
        long clienteId = criar("/clientes", """
                {"nome": "Ana"}
                """);
        long produtoId = criar("/produtos", """
                {"nome": "Baozi de Porco", "preco": 7.50, "estoque": true}
                """);
        criar("/pedidos", pedidoJson(clienteId, produtoId, 3));

        mockMvc.perform(delete("/clientes/{id}", clienteId))
                .andExpect(status().isConflict());
    }

    @Test
    void deveExcluirPedido() throws Exception {
        long clienteId = criar("/clientes", """
                {"nome": "Ana"}
                """);
        long produtoId = criar("/produtos", """
                {"nome": "Baozi de Porco", "preco": 7.50, "estoque": true}
                """);
        long pedidoId = criar("/pedidos", pedidoJson(clienteId, produtoId, 3));

        mockMvc.perform(delete("/pedidos/{id}", pedidoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }

    private long criar(String uri, String json) throws Exception {
        String resposta = mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }

    private static String pedidoJson(long clienteId, long produtoId, int quantidade) {
        return """
                {"clienteId": %d, "produtoId": %d, "quantidade": %d}
                """.formatted(clienteId, produtoId, quantidade);
    }
}
