package com.br.api.payassistent.controller;

import com.br.api.payassistent.model.dto.MerchantCustomerServiceDto;
import com.br.api.payassistent.service.CustomerServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/find-summary")
    @ResponseBody
    public ResponseEntity<Object> findSummary(@RequestParam Long userId) {
        try {
            return new ResponseEntity<Object>(service.findCustomerServicesSummaryOfDayByUser(userId), HttpStatus.OK);
        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

    @GetMapping(value = "/find-detail")
    @ResponseBody
    public ResponseEntity<Object> findDetail(@RequestParam Long userId, @RequestParam Long merchantId) {
        try {
            return new ResponseEntity<Object>(service.findCustomerServicesDetailOfDayByUserAndMerchant(userId, merchantId), HttpStatus.OK);
        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

}
