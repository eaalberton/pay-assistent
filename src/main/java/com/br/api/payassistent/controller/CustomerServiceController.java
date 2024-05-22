package com.br.api.payassistent.controller;

import com.br.api.payassistent.model.CustomerService;
import com.br.api.payassistent.model.dto.MerchantCustomerServiceDto;
import com.br.api.payassistent.model.dto.UserDto;
import com.br.api.payassistent.service.CustomerServiceService;
import com.br.api.payassistent.service.MerchantService;
import com.br.api.payassistent.service.RequestService;
import com.br.api.payassistent.service.UserService;
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

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<CustomerService> save(@RequestBody @Valid CustomerService customerService) {
        return new ResponseEntity<CustomerService>(service.save(customerService), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.findById(id).map(recordFound -> {
            service.deleteById(id);
            return  ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/find-summary")
    @ResponseBody
    public ResponseEntity<Object> findSummary(@RequestParam Long userId) {
        try {
            return new ResponseEntity<Object>(service.findCustomerServicesSummaryOfDayByUser(userId), HttpStatus.OK);
        } catch (Exception ei) {
            ei.printStackTrace();
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

    @GetMapping(value = "/find-all-merchants")
    @ResponseBody
    public ResponseEntity<Object> findAllMerchants() {
        try {
            return new ResponseEntity<Object>(merchantService.findAll(), HttpStatus.OK);
        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

    @GetMapping(value = "/find-all-requests")
    @ResponseBody
    public ResponseEntity<Object> findAllRequests() {
        try {
            return new ResponseEntity<Object>(requestService.findAll(), HttpStatus.OK);
        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

}
