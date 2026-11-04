package br.org.edu.ifrn.carros.repository;

import br.org.edu.ifrn.carros.model.Carro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarroRepository extends JpaRepository<Carro, Long> {
}