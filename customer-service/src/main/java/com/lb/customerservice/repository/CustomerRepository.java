package com.lb.customerservice.repository;

import com.lb.customerservice.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    @Query(value = "SELECT * FROM customers c WHERE UPPER(c.name) LIKE UPPER(CONCAT('%', :name, '%')) ORDER BY c.name",
            nativeQuery = true)
    List<Customer> searchByNameNative(@Param("name") String name);
}