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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCriarClienteERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria", "clienteDesde": "2026-01-10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.clienteDesde").value("2026-01-10"));
    }

    @Test
    void deveRetornar400QuandoNomeEstiverEmBranco() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").value("nome é obrigatório"));
    }

    @Test
    void deveListarEBuscarPorId() throws Exception {
        long id = criarCliente("Ana");
        criarCliente("Bruno");

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void deveRetornar404ParaClienteInexistente() throws Exception {
        mockMvc.perform(get("/clientes/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Cliente com id 999 não encontrado"));
    }

    @Test
    void deveAtualizarCliente() throws Exception {
        long id = criarCliente("Ana");

        mockMvc.perform(put("/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana Paula"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Paula"));
    }

    @Test
    void deveExcluirCliente() throws Exception {
        long id = criarCliente("Ana");

        mockMvc.perform(delete("/clientes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isNotFound());
    }

    private long criarCliente(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"%s\"}".formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }
}
