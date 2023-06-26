package com.br.api.payassistent.controller;

import com.br.api.payassistent.service.ContestationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/contestation")
public class ContestationController {

    @Autowired
    private ContestationService contestationService;

    @GetMapping(value = "/check")
    public ResponseEntity<Object> checkContestationsByCpfAndMerchant(@RequestParam String cpf, @RequestParam String merchant) {
        try {
            return new ResponseEntity<>(contestationService.checkContestationsByCpfAndMerchant(cpf, merchant), HttpStatus.OK);
        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

}
