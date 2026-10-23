package br.edu.exemplo;

import br.edu.exemplo.model.Produto;
import br.edu.exemplo.repository.ProdutoRepository;
import br.edu.exemplo.service.ProdutoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProdutoServiceIntegrationTest {
    @Autowired
    ProdutoService service;
    @Autowired
    ProdutoRepository repository;
    @Autowired
    EntityManager entityManager;

    @Test
    void deveCadastrarERecuperarProduto() {
        Produto produto = service.cadastrar("Teclado", 150.0);
        assertNotNull(produto.getId());
        entityManager.flush();
        entityManager.clear();
        Produto recuperado = repository.findById(produto.getId()).orElseThrow();
        assertEquals(produto.getId(), recuperado.getId());
        assertEquals("Teclado", recuperado.getNome());
        assertEquals(150.0, recuperado.getPreco(), 0.001);
    }

    @Test
    void naoDevePersistirProdutoComNomeEmBranco() {
        long quantidadeAntes = repository.count();
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar("   ", 80.0));
        entityManager.flush();
        entityManager.clear();
        assertEquals(quantidadeAntes, repository.count());
    }

    @Test
    void deveAtualizarPrecoPersistido() {
        Produto produto = service.cadastrar("Mouse", 80.0);
        service.atualizarPreco(produto.getId(), 95.0);
        // Força a escrita e uma nova leitura do banco, sem reutilizar a entidade em memória.
        entityManager.flush();
        entityManager.clear();
        Produto recuperado = repository.findById(produto.getId()).orElseThrow();
        assertEquals("Mouse", recuperado.getNome());
        assertEquals(95.0, recuperado.getPreco(), 0.001);
    }
}
