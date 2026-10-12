package br.com.baozistore.service;

import br.com.baozistore.dto.PedidoRequest;
import br.com.baozistore.dto.PedidoResponse;
import br.com.baozistore.exception.RecursoNaoEncontradoException;
import br.com.baozistore.exception.RegraNegocioException;
import br.com.baozistore.model.Cliente;
import br.com.baozistore.model.Pedido;
import br.com.baozistore.model.Produto;
import br.com.baozistore.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    public PedidoService(PedidoRepository pedidoRepository, ClienteService clienteService, ProdutoService produtoService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll().stream()
                .map(PedidoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return PedidoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Pedido pedido = new Pedido();
        preencher(pedido, request);
        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarEntidade(id);
        preencher(pedido, request);
        return PedidoResponse.de(pedido);
    }

    @Transactional
    public void excluir(Long id) {
        pedidoRepository.delete(buscarEntidade(id));
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    private void preencher(Pedido pedido, PedidoRequest request) {
        Cliente cliente = clienteService.buscarPorId(request.clienteId());
        Produto produto = produtoService.buscarPorId(request.produtoId());
        if (!Boolean.TRUE.equals(produto.getEstoque())) {
            throw new RegraNegocioException("Produto '" + produto.getNome() + "' está sem estoque");
        }

        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(request.quantidade());
    }
}
