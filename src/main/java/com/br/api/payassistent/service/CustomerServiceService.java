package com.br.api.payassistent.service;

import com.br.api.payassistent.model.CustomerService;
import com.br.api.payassistent.model.dto.ServiceSummaryDto;
import com.br.api.payassistent.repository.CustomerServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class CustomerServiceService {

    @Autowired
    CustomerServiceRepository repository;

    public CustomerService save(CustomerService customerService) {

        if (customerService != null) {
            customerService.setDateStart(customerService.getDateStart().atZone(ZoneId.of("GMT-3")).toLocalDateTime());
            customerService.setDateEnd(customerService.getDateEnd().atZone(ZoneId.of("GMT-3")).toLocalDateTime());

            System.out.println("Log datas SALVAR ********");
            System.out.println(customerService.getDateStart());
            System.out.println(customerService.getDateEnd());

            customerService = repository.save(customerService);
        }

        return customerService;
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Optional<CustomerService> findById(Long id) {
        return repository.findById(id);
    }

    public List<ServiceSummaryDto> findCustomerServicesSummaryOfDayByUser(Long userId) {

        return repository.findCustomerServicesSummaryOfDayByUser(getStartDate(), getEndDate(), userId);

    }

    public List<CustomerService> findCustomerServicesDetailOfDayByUserAndMerchant(Long userId, Long merchantId) {

        System.out.println("Log datas PESQUISAR ********");
        System.out.println(getStartDate());
        System.out.println(getEndDate());
        return repository.findByDateStartBetweenAndUserIdAndMerchantId(getStartDate(), getEndDate(), userId, merchantId);

    }

    private LocalDateTime getStartDate() {
        return LocalDate.now(ZoneId.of("GMT-3")).atTime(0, 0, 0);
    }

    private LocalDateTime getEndDate() {
        return LocalDate.now(ZoneId.of("GMT-3")).atTime(23, 59, 59);
    }

}
