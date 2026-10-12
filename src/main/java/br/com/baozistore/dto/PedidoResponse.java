package br.com.baozistore.dto;

import br.com.baozistore.model.Pedido;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonPropertyOrder({"id", "clienteId", "clienteNome", "produtoId", "produtoNome", "quantidade", "valorTotal"})
public record PedidoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal valorTotal) {

    public static PedidoResponse de(Pedido pedido) {
        var cliente = pedido.getCliente();
        var produto = pedido.getProduto();
        var valorTotal = produto.getPreco().multiply(BigDecimal.valueOf(pedido.getQuantidade()));

        return new PedidoResponse(
                pedido.getId(),
                cliente.getId(),
                cliente.getNome(),
                produto.getId(),
                produto.getNome(),
                pedido.getQuantidade(),
                valorTotal);
    }
}
