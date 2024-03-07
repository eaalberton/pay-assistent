package com.br.api.payassistent.repository;

import com.br.api.payassistent.model.CustomerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerServiceRepository extends JpaRepository<CustomerService, Long> {

}
