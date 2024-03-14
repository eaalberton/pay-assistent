package com.br.api.payassistent.repository;

import com.br.api.payassistent.model.Contestation;
import com.br.api.payassistent.model.CustomerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerServiceRepository extends JpaRepository<CustomerService, Long> {

    List<CustomerService> findByDateBetweenAndUserId(LocalDateTime startDate, LocalDateTime endDate, Integer userId);

}
