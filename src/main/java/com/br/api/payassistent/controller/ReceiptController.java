package com.br.api.payassistent.controller;

import com.br.api.payassistent.model.dto.ReceiptDTO;
import com.br.api.payassistent.service.ReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/receipt")
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    @PostMapping(value = "/generate2")
    public ResponseEntity<Object> generateReceipt2(@RequestBody ReceiptDTO receiptDTO, HttpServletRequest request) {

        try {
            Resource resource = receiptService.generateReceipt(receiptDTO);
            System.out.println(resource);
            String contentType = null;

            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                            resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

    @PostMapping(value = "/generate-in-memory")
    public ResponseEntity<Object> generateReceipt(@RequestBody ReceiptDTO receiptDTO, HttpServletRequest request) {

        try {
            return new ResponseEntity<Object>(receiptService.generateReceiptInMemory(receiptDTO), HttpStatus.OK);
        } catch (Exception ei) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body("Erro interno.");
        }
    }

}
