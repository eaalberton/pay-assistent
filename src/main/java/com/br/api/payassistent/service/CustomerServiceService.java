package com.br.api.payassistent.service;

import com.br.api.payassistent.repository.CustomerServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceService {

    @Autowired
    CustomerServiceRepository repository;

}
