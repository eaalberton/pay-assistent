package com.br.api.payassistent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDTO {

    private String company;
    private String metadata;

    private String dateTransactionKey;
    private String dateTransaction;
    private String descriptionKey;
    private String description;
    private String valueKey;
    private String value;
    private String receiverNameKey;
    private String receiverName;
    private String receiverDocumentKey;
    private String receiverDocument;
    private String receiverBankKey;
    private String receiverBank;
    private String receiverAgencyKey;
    private String receiverAgency;
    private String receiverAccountKey;
    private String receiverAccount;
    private String payerNameKey;
    private String payerName;
    private String payerDocumentKey;
    private String payerDocument;
    private String payerBankKey;
    private String payerBank;
    private String transactionIdKey;
    private String transactionId;
    private String protocolKey;
    private String protocol;
    private String dateGenerated;

}
