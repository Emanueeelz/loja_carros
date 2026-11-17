
package br.org.edu.ifrn.carros.repository;

import br.org.edu.ifrn.carros.model.Carro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CarroRepository extends JpaRepository<Carro, Long> {

    @Query("""
            SELECT c FROM Carro c
            WHERE c.situacao = 'DISPONIVEL'
            AND (:marca IS NULL OR LOWER(c.marca) LIKE LOWER(CONCAT('%', :marca, '%')))
            AND (:modelo IS NULL OR LOWER(c.modelo) LIKE LOWER(CONCAT('%', :modelo, '%')))
            AND (:precoMin IS NULL OR c.preco >= :precoMin)
            AND (:precoMax IS NULL OR c.preco <= :precoMax)
            AND (:ano IS NULL OR c.anoFabricacao = :ano)
            ORDER BY c.id DESC
            """)
    List<Carro> pesquisar(
            @Param("marca") String marca,
            @Param("modelo") String modelo,
            @Param("precoMin") BigDecimal precoMin,
            @Param("precoMax") BigDecimal precoMax,
            @Param("ano") Integer ano
    );
}
