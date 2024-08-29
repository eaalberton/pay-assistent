package com.br.api.payassistent.service;

import com.br.api.payassistent.model.Bank;
import com.br.api.payassistent.model.Contestation;
import com.br.api.payassistent.model.EnumSituation;
import com.br.api.payassistent.model.dto.CheckContestationDTO;
import com.br.api.payassistent.repository.BankRepository;
import com.br.api.payassistent.repository.ContestationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
            String result = checkContestationsByCpfAndMerchant(check.getDocument(), check.getMerchant(), false);

            check.setResult(result);

        } catch (Exception e) {
            e.printStackTrace();
            check.setResult(e.getMessage());
        }

        return check;
    }

    public CheckContestationDTO checkContestationsEnglish(CheckContestationDTO check) {

        try {
            String result = checkContestationsByCpfAndMerchant(check.getDocument(), check.getMerchant(), true);

            check.setResult(result);

        } catch (Exception e) {
            e.printStackTrace();
            check.setResult(e.getMessage());
        }

        return check;
    }

    public String checkContestationsByCpfAndMerchant(String cpf, String merchant, boolean isEnglish) {

        StringBuilder sbContestations = new StringBuilder();

        try {
            if (isValidFilters(cpf, merchant, sbContestations)) {

                List<Contestation> contestations = repository.findByCpfGeneratedOrCpfPaid(cpf, cpf);

                if (!merchant.toUpperCase().trim().equals("PAYBROKERS")) {
                    //remove canceled contestations from the list
                    if (contestations != null && !contestations.isEmpty()) {
                        contestations = contestations.stream().filter(contestation ->
                                !EnumSituation.CANCELED.equals(contestation.getSituation())).toList();
                    }
                }

                validateContestations(contestations, sbContestations, cpf, merchant.toUpperCase(), isEnglish);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return sbContestations.toString();
    }

    private void validateContestations(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant, boolean isEnglish) {
        if (contestations != null && !contestations.isEmpty()) {

            contestations = contestations.stream().distinct().collect(Collectors.toList());

            createReplyMessage(contestations, sbContestations, cpf, merchant, isEnglish);
        } else {
            sbContestations.append("No contestation was found!");
        }
    }

    private void createReplyMessage(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant, boolean isEnglish) {

        if (merchant.toUpperCase().trim().equals("PAYBROKERS")) {
            createPaybrokersTeamResearchMessage(contestations,sbContestations);
        } else {
            if (isEnglish) {
                createSameMerchantMessageEnglish(contestations,sbContestations, cpf, merchant);
                createDifferentMerchantMessageEnglish(contestations,sbContestations, cpf, merchant);
                createFooterMessageEnglish(sbContestations);
            } else {
                createSameMerchantMessage(contestations,sbContestations, cpf, merchant);
                createDifferentMerchantMessage(contestations,sbContestations, cpf, merchant);
                createFooterMessage(sbContestations);
            }
        }
    }

    private void createPaybrokersTeamResearchMessage(List<Contestation> contestations, StringBuilder sbContestations) {

        contestations.forEach(contestation -> {
                    sbContestations.append("ID: ");
                    sbContestations.append(contestation.getEndToEnd());
                    sbContestations.append("    Valor: ");
                    sbContestations.append(formatMonetaryValue(contestation.getValue(), false));

                    sbContestations.append("\n");
                    sbContestations.append("Data: ");
                    sbContestations.append(formatDateToString(contestation.getDate(), false));
                    sbContestations.append("    Banco: ");
                    sbContestations.append(getBank(contestation.getEndToEnd()));

                    sbContestations.append("\n");
                    sbContestations.append("Gerado por: ");
                    sbContestations.append(contestation.getCpfGenerated());
                    sbContestations.append("    Pago por: ");
                    sbContestations.append(contestation.getCpfPaid());

                    sbContestations.append("\n");
                    sbContestations.append("Merchant: ");
                    sbContestations.append(contestation.getMerchant());
                    sbContestations.append("    Situação: ");
                    sbContestations.append(getTextSituation(contestation.getSituation()));

                    sbContestations.append("\n\n");
                });
    }

    private void createSameMerchantMessage(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        contestations.stream()
            .filter(contestation -> contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
            .findAny().ifPresent(c -> {
                sbContestations.append("*Este CPF consta na Blacklist por contestação:* \n\n");
        });

        contestations.stream()
            .filter(contestation -> contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
            .forEach(contestation -> {
                sbContestations.append("ID: ");
                sbContestations.append(contestation.getEndToEnd());
                sbContestations.append("    Valor: ");
                sbContestations.append(formatMonetaryValue(contestation.getValue(), false));
                sbContestations.append("\n");
                sbContestations.append("Data: ");
                sbContestations.append(formatDateToString(contestation.getDate(), false));
                sbContestations.append("    Banco: ");
                sbContestations.append(getBank(contestation.getEndToEnd()));
                if (!isCpfGeneratorSameCpfPayer(contestation.getCpfGenerated(), contestation.getCpfPaid()))
                    sbContestations.append("  (GERADO E PAGO POR CPF DIFERENTES)");
                sbContestations.append("\n\n");
        });
    }

    private void createSameMerchantMessageEnglish(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        contestations.stream()
            .filter(contestation -> contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
            .findAny().ifPresent(c -> {
                sbContestations.append("*This CPF is on the Blacklist due bank dispute:* \n\n");
        });

        contestations.stream()
            .filter(contestation -> contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
            .forEach(contestation -> {
                sbContestations.append("ID: ");
                sbContestations.append(contestation.getEndToEnd());
                sbContestations.append("    Amount: ");
                sbContestations.append(formatMonetaryValue(contestation.getValue(), true));
                sbContestations.append("\n");
                sbContestations.append("Date: ");
                sbContestations.append(formatDateToString(contestation.getDate(), true));
                sbContestations.append("    Bank: ");
                sbContestations.append(getBank(contestation.getEndToEnd()));
                if (!isCpfGeneratorSameCpfPayer(contestation.getCpfGenerated(), contestation.getCpfPaid()))
                    sbContestations.append("  (GENERATED AND PAID BY DIFFERENT CPF)");
                sbContestations.append("\n\n");
        });
    }

    private void createDifferentMerchantMessage(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        contestations.stream()
                .filter(contestation -> !contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
                .findAny().ifPresent(c -> {
                    sbContestations.append("*Este CPF consta na Blacklist por contestação em outro site:* \n\n");
                });

        contestations.stream()
                .filter(contestation -> !contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
                .forEach(contestation -> {
                    sbContestations.append("Valor: ");
                    sbContestations.append(formatMonetaryValue(contestation.getValue(), false));
                    sbContestations.append("    Data: ");
                    sbContestations.append(formatDateToString(contestation.getDate(), false));
                    sbContestations.append("    Banco: ");
                    sbContestations.append(getBank(contestation.getEndToEnd()));
                    if (!isCpfGeneratorSameCpfPayer(contestation.getCpfGenerated(), contestation.getCpfPaid()))
                        sbContestations.append("  (GERADO E PAGO POR CPF DIFERENTES)");
                    sbContestations.append("\n\n");
                });
    }

    private void createDifferentMerchantMessageEnglish(List<Contestation> contestations, StringBuilder sbContestations, String cpf, String merchant) {
        contestations.stream()
                .filter(contestation -> !contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
                .findAny().ifPresent(c -> {
                    sbContestations.append("*This CPF is on the Blacklist due bank dispute on another site:* \n\n");
                });

        contestations.stream()
                .filter(contestation -> !contestation.getMerchant().replaceAll(" ", "").contains(merchant.replaceAll(" ", "")))
                .forEach(contestation -> {
                    sbContestations.append("Amount: ");
                    sbContestations.append(formatMonetaryValue(contestation.getValue(), true));
                    sbContestations.append("    Date: ");
                    sbContestations.append(formatDateToString(contestation.getDate(), true));
                    sbContestations.append("    Bank: ");
                    sbContestations.append(getBank(contestation.getEndToEnd()));
                    if (!isCpfGeneratorSameCpfPayer(contestation.getCpfGenerated(), contestation.getCpfPaid()))
                        sbContestations.append("  (GENERATED AND PAID BY DIFFERENT CPF)");
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
        if (sbContestations.toString().contains("(GERADO E PAGO POR CPF DIFERENTES)")) {
            sbContestations.append("*(GERADO E PAGO POR CPF DIFERENTES) = O portador do CPF pagador é quem deve remover a contestação.*");
            sbContestations.append("\n\n");

            sbContestations.append("*Neste caso, como o QR Code foi gerado por um CPF e pago por outro CPF, nos ");
            sbContestations.append("envie também junto com as provas solicitadas abaixo, uma imagem de um documento ");
            sbContestations.append("com foto e uma selfie do portador do CPF PAGADOR segurando o mesmo documento.*");
            sbContestations.append("\n\n");
        }

        sbContestations.append("*Para ser removido ele precisa entrar em contato com o banco dele reconhecendo ");
        sbContestations.append("as transações e encerrando as mesmas, e nos enviar provas do cancelamento ");
        sbContestations.append("(print da conversa com o banco); na prova precisa conter os IDs das transações ");
        sbContestations.append("para que seja enviado para análise e assim ser possível removê-lo da blacklist.*");

    }

    private void createFooterMessageEnglish(StringBuilder sbContestations) {
        if (sbContestations.toString().contains("(GENERATED AND PAID BY DIFFERENT CPF)")) {
            sbContestations.append("*(GENERATED AND PAID BY DIFFERENT CPF) = The holder of the paying CPF is the one who must remove the dispute.*");
            sbContestations.append("\n\n");

            sbContestations.append("*In this case, as the QR Code was generated by a CPF and paid by another CPF, ");
            sbContestations.append("send us together with the proof requested below, an image of a document with ");
            sbContestations.append("photo and a selfie of the holder of the PAYER CPF holding the same document.*");
            sbContestations.append("\n\n");
        }

        sbContestations.append("*To be removed, they need to contact their bank, acknowledging the transactions and ");
        sbContestations.append("closing the dispute, afterwards they need to send us proof of cancellation (Screenshot ");
        sbContestations.append("of the conversation with the bank); in the proof it needs to contain the transaction IDs so that it ");
        sbContestations.append("can be sent for analysis and thus being able to remove it from the blacklist upon aproval.*");
    }

    private String getTextSituation(EnumSituation situation) {
        if (EnumSituation.CANCELED.equals(situation))
            return "CANCELADA";
        else
            return "ATIVA";
    }

    private boolean isCpfGeneratorSameCpfPayer(String cpfGenerator, String cpfPayer) {
        if (cpfPayer == null || cpfPayer.equals("00000000000") || cpfPayer.equals(cpfGenerator))
            return true;

        return false;
    }

    private String formatDateToString(LocalDateTime date, boolean isEnglish) {
        if (date == null)
            return isEnglish ? "Uninformed" : "Não Informado";

        if (dateFormat == null)
            dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return dateFormat.format(date);
    }

    private  String formatMonetaryValue(BigDecimal value, boolean isEnglish) {
        if (value == null)
            return isEnglish ? "Uninformed" : "Não Informado";;

        if (decimalFormat == null) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
            decimalFormat = new DecimalFormat("¤ #,###,##0.00", symbols);
        }

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
