package com.br.api.payassistent.controller;

import com.br.api.payassistent.model.dto.MerchantCustomerServiceDto;
import com.br.api.payassistent.service.CustomerServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customer-service")
public class CustomerServiceController {

    @Autowired
    private CustomerServiceService service;

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<MerchantCustomerServiceDto> register(@RequestBody @Valid MerchantCustomerServiceDto merchantCustomerServiceDto) {
        return new ResponseEntity<MerchantCustomerServiceDto>(service.register(merchantCustomerServiceDto), HttpStatus.OK);
    }


}
