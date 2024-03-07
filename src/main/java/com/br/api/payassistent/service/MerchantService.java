package com.br.api.payassistent.service;

import com.br.api.payassistent.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    @Autowired
    MerchantRepository repository;

}
