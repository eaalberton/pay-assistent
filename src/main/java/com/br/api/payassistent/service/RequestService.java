package com.br.api.payassistent.service;

import com.br.api.payassistent.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    @Autowired
    RequestRepository repository;

}
