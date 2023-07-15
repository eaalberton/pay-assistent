package com.br.api.payassistent.service;

import com.br.api.payassistent.model.Bank;
import com.br.api.payassistent.model.Contestation;
import com.br.api.payassistent.model.dto.CheckContestationDTO;
import com.br.api.payassistent.repository.BankRepository;
import com.br.api.payassistent.repository.ContestationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContestationService {

    @Autowired
    ContestationRepository repository;

    @Autowired
    BankRepository bankRepository;

    Map<String, String> mapBanks = new HashMap<>();

    DecimalFormat decimalFormat;
    DateTimeFormatter dateFormat;

    public CheckContestationDTO checkContestations(CheckContestationDTO check) {

        try {
            String result = checkContestationsByCpfAndMerchant(check.getDocument(), check.getMerchant());

            check.setResult(result);

        } catch (Exception e) {
            e.printStackTrace();
            check.setResult(e.getMessage());
        }

        return check;
    }

    public String checkContestationsByCpfAndMerchant(String cpf, String merchant) {

        StringBuilder sbContestations = new StringBuilder();

        try {
            if (isValidFilters(cpf, merchant, sbContestations)) {
                List<Contestation> contestations = repository.findByCpfGeneratedOrCpfPaid(cpf, cpf);

                validateContestations(contestations, sbContestations, cpf, merchant.toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return sbContestations.toString();
    }

    private void validateContestations(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        if (contestations != null && !contestations.isEmpty()) {
            createReplyMessage(contestations, sbContestations, cpf, merchant);
        } else {
            sbContestations.append("No contestation was found!");
        }
    }

    private void createReplyMessage(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        createSameMerchantMessage(contestations,sbContestations, cpf, merchant);
        createDifferentMerchantMessage(contestations,sbContestations, cpf, merchant);
        createFooterMessage(sbContestations);
    }

    private void createSameMerchantMessage(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        contestations.stream()
            .filter(contestation -> contestation.getMerchant().contains(merchant))
            .findAny().ifPresent(c -> {
                sbContestations.append("*Este CPF consta na Blacklist por contestação:* \n\n");
        });

        contestations.stream()
            .filter(contestation -> contestation.getMerchant().contains(merchant))
            .forEach(contestation -> {
                sbContestations.append("ID: ");
                sbContestations.append(contestation.getEndToEnd());
                sbContestations.append("    Valor: ");
                sbContestations.append(formatMonetaryValue(contestation.getValue()));
                sbContestations.append("\n");
                sbContestations.append("Data: ");
                sbContestations.append(formatDateToString(contestation.getDate()));
                sbContestations.append("    Banco: ");
                sbContestations.append(getBank(contestation.getEndToEnd()));
                if (!isCpfPayer(cpf, contestation.getCpfPaid()))
                    sbContestations.append("  (PAGO POR OUTRO CPF)");
                sbContestations.append("\n\n");
        });
    }

    private void createDifferentMerchantMessage(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        contestations.stream()
                .filter(contestation -> !contestation.getMerchant().contains(merchant))
                .findAny().ifPresent(c -> {
                    sbContestations.append("*Este CPF consta na Blacklist por contestação em outro site:* \n\n");
                });

        contestations.stream()
                .filter(contestation -> !contestation.getMerchant().contains(merchant))
                .forEach(contestation -> {
                    sbContestations.append("Valor: ");
                    sbContestations.append(formatMonetaryValue(contestation.getValue()));
                    sbContestations.append("    Data: ");
                    sbContestations.append(formatDateToString(contestation.getDate()));
                    sbContestations.append("    Banco: ");
                    sbContestations.append(getBank(contestation.getEndToEnd()));
                    if (!isCpfPayer(cpf, contestation.getCpfPaid()))
                        sbContestations.append("  (PAGO POR OUTRO CPF)");
                    sbContestations.append("\n\n");
                });
    }

    private String getBank(String endToEnd) {
        String ispb = endToEnd.substring(1, 9);

        if (mapBanks.containsKey(ispb)) {
            return mapBanks.get(ispb);
        } else {
            Bank bank = bankRepository.findByIspbCode(ispb);
            mapBanks.put(bank.getIspbCode(), bank.getName());
            return bank.getName();
        }
    }

    private void createFooterMessage(StringBuilder sbContestations) {
        if (sbContestations.toString().contains("(PAGO POR OUTRO CPF)")) {
            sbContestations.append("*(PAGO POR OUTRO CPF) = O portador do CPF pagador é quem deve remover a contestação.*");
            sbContestations.append("\n\n");
        }

        sbContestations.append("*Para ser removido ele precisa entrar em contato com o banco dele reconhecendo ");
        sbContestations.append("as transações e encerrando as mesmas, e nos enviar provas do cancelamento ");
        sbContestations.append("(print da conversa com o banco); na prova precisa conter os IDs das transações ");
        sbContestations.append("para que seja enviado para análise e assim ser possível removê-lo da blacklist.*");
    }

    private boolean isCpfPayer(String cpf, String cpfPayer) {
        if (cpfPayer.equals("00000000000") || cpfPayer.equals(cpf))
            return true;

        return false;
    }

    private String formatDateToString(LocalDateTime date) {
        if (date == null)
            return "Não Informado";

        if (dateFormat == null)
            dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return dateFormat.format(date);
    }

    private String formatMonetaryValue(BigDecimal value) {
        if (value == null)
            return "Não Informado";

        if (decimalFormat == null)
            decimalFormat = new DecimalFormat("¤ #,###,##0.00");

        return decimalFormat.format(value);
    }

    private boolean isValidFilters(String cpf, String merchant, StringBuilder sbContestations) {
        if ((cpf == null || cpf.isEmpty()) || (cpf == null || cpf.isEmpty())) {
            sbContestations.append("CPF and Merchant are mandatory!");
            return false;
        }
         return true;
    }

}
