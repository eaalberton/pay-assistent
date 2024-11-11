package com.br.api.payassistent.service;

import com.br.api.payassistent.exceptions.AppException;
import com.br.api.payassistent.model.Bank;
import com.br.api.payassistent.model.dto.FileDTO;
import com.br.api.payassistent.model.dto.ReceiptDTO;
import com.br.api.payassistent.repository.BankRepository;
import net.sf.jasperreports.engine.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ReceiptService {

    @Autowired
    BankRepository bankRepository;

    public static final String REPORTS = "/jasper/reports/";

    public static final String IMAGEBG = "/jasper/img/receipt_layout.png";

    public static final String JRXML_FILE = "receipt.jrxml";

    public static final String DESTINY = "C:\\jasper-reports\\";

    DecimalFormat decimalFormat;

    public FileDTO generateReceiptInMemory(ReceiptDTO receiptDTO) {

        if (!receiptDTO.getMetadata().startsWith("statusCode")
            || !receiptDTO.getMetadata().contains("endToEndId")) {

            throw new AppException("Invalid metadata", HttpStatus.BAD_REQUEST);
        }

        FileDTO fileDTO = new FileDTO();

        try {

            if (receiptDTO.getCompany().equals("PAGFAST")) {

                Map<String, String> mapReceiptData = getInfo(receiptDTO.getMetadata());

                mapReceiptData.keySet().forEach(key -> {
                    System.out.println(key + " - " + mapReceiptData.get(key));
                    System.out.println(" ");
                });

                ReceiptDTO receipt = createReceipt();

                fillReceipt(receipt, mapReceiptData);

                fileDTO.setFile(generateReportInMemory(receipt));

                fileDTO.setName(generateFileName(receipt));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileDTO;
    }

    private String generateFileName(ReceiptDTO receipt) {
        String dataGeneratedAux = receipt.getDateGenerated().replaceAll("/", "_");

        dataGeneratedAux = dataGeneratedAux.replaceAll(":", "_");

        return dataGeneratedAux + " - " + receipt.getReceiverName() + " - Reembolso.pdf";
    }

    public byte[] generateReportInMemory(ReceiptDTO receiptDTO) throws IOException, JRException {

        byte[] imagebg = loadImageBg();

        Map<String, Object> mapParams = parameters(receiptDTO);
        mapParams.put("receipt layout", imagebg);

        JasperReport jasperReport = JasperCompileManager.compileReport(getClass()
                .getResourceAsStream(REPORTS + JRXML_FILE));

        JasperPrint print = JasperFillManager.fillReport(jasperReport, mapParams, new JREmptyDataSource());

        return JasperExportManager.exportReportToPdf(print);

    }

    public Resource generateReceipt(ReceiptDTO receiptDTO) {

        System.out.println(receiptDTO.getMetadata());

        Resource receiptFile = null;

        try {

            if (receiptDTO.getCompany().equals("PAGFAST")) {

                Map<String, String> mapReceiptData = getInfo(receiptDTO.getMetadata());

                mapReceiptData.keySet().forEach(key -> {
                    System.out.println(key + " - " + mapReceiptData.get(key));
                    System.out.println(" ");
                });

                ReceiptDTO receipt = createReceipt();

                fillReceipt(receipt, mapReceiptData);

                receiptFile = generateReport(receipt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return receiptFile;
    }

    public Resource generateReport(ReceiptDTO receiptDTO) throws IOException, JRException {

        byte[] imagebg = loadImageBg();

        Map<String, Object> mapParams = parameters(receiptDTO);
        mapParams.put("receipt layout", imagebg);

        String folder = getPathToSave("receipt");

        JasperReport jasperReport = JasperCompileManager.compileReport(getPathToSave("receipt"));

        JasperPrint print = JasperFillManager.fillReport(jasperReport, mapParams, new JREmptyDataSource());

        JasperExportManager.exportReportToPdfFile(print, folder);

        System.out.println(folder);

        Path path = Path.of(folder).normalize();

        return new UrlResource(path.toUri());

    }

    private byte[] loadImageBg() throws IOException {
        return IOUtils.toByteArray(getClass().getResourceAsStream(ReceiptService.IMAGEBG));
    }

    private String getPathToSave(String fileName) {
        createDestinyPath(DESTINY);
        return DESTINY + fileName.concat(".pdf");
    }

    private void createDestinyPath(String fileName) {
        File dir = new File(fileName);

        if (!dir.exists())
            dir.mkdir();
    }

    private void fillReceipt(ReceiptDTO receipt, Map<String, String> mapReceiptData) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
        DateTimeFormatter dateFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", new Locale("pt", "BR"));//"yyyy-MM-dd'T'HH:mm:ss.SSSz"

        String dateAux = mapReceiptData.get("liquidacao").substring(0, 19);

        String dateTransaction = LocalDateTime.parse(dateAux, dateFormatter1).format(dateFormatter);

        String receiverDocument = " - ";
        if (mapReceiptData.get("payer-documento").length() == 11)
            receiverDocument = "***" + mapReceiptData.get("payer-documento").substring(3, 8) + "***";
        else if (mapReceiptData.get("payer-documento").length() == 14)
            receiverDocument = "***" + mapReceiptData.get("payer-documento").substring(3, 11) + "***";

        String receiverAgency = mapReceiptData.get("payer-agencia");

        String receiverAccount = mapReceiptData.get("payer-numero");

        receipt.setDateTransaction(dateTransaction);
        receipt.setValue(formatMonetaryValue(new BigDecimal(mapReceiptData.get("valor"))));

        receipt.setReceiverName(mapReceiptData.get("payer-nome"));
        receipt.setReceiverDocument(receiverDocument);
        receipt.setReceiverBank(getBank(mapReceiptData.get("endToEndId")));
        if (receiverAgency != null)
            receipt.setReceiverAgency(receiverAgency);
        if (receiverAccount != null)
            receipt.setReceiverAccount(receiverAccount);

        receipt.setPayerBank(getBank(mapReceiptData.get("rtrId")));
        receipt.setTransactionId(mapReceiptData.get("rtrId"));
        receipt.setProtocol(mapReceiptData.get("txId"));

        receipt.setDateGenerated(LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .format(dateFormatter));
    }

    private Map<String, String> getInfo(String receiptInfo) {

        Map<String, String> mapReceiptData = new HashMap<>();

        if (receiptInfo != null && !receiptInfo.isEmpty()) {

            receiptInfo = receiptInfo.replaceAll("cpf: null", "")
                                     .replaceAll("cpf", "documento")
                                     .replaceAll("cnpj", "documento")
                                     .replaceAll("txid", "txId")
                                     .replaceAll("\"", "");

            boolean isPayer = false;
            boolean isRightRefund = false;

            String[] split = receiptInfo.split("\n");

            for (String line : split) {

                String[] splitAux = line.split(": ");

                if (splitAux.length > 1) {
                    if (isPayer)
                        mapReceiptData.put("payer-" + splitAux[0], splitAux[1]);
                    else {

                        if (isRightRefund && splitAux[0].equals("rtrId"))
                            continue;

                        if (isRightRefund && splitAux[0].equals("liquidacao"))
                            continue;

                        mapReceiptData.put(splitAux[0], splitAux[1]);
                    }

                    if (isPayer && splitAux[0].equals("conta"))
                        isPayer = false;

                    if (splitAux[0].equals("status") && splitAux[1].equals("DEVOLVIDO"))
                        isRightRefund = true;

                } else if (splitAux[0].equals("pagador:"))
                    isPayer = true;
                else if (splitAux[0].equals("devolucoes:"))
                    isPayer = false;
            }
        }

        return mapReceiptData;
    }

    private String getBank(String endToEnd) {
        String ispb = endToEnd.substring(1, 9);

        Bank bank = bankRepository.findByIspbCode(ispb);
        if (bank != null)
            return bank.getName();

        return ispb;
    }

    private  String formatMonetaryValue(BigDecimal value) {

        if (decimalFormat == null) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
            decimalFormat = new DecimalFormat("¤ #,###,##0.00", symbols);
        }

        return decimalFormat.format(value);
    }

    private Map<String, Object> parameters(Object obj) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
        }
        return map;
    }

    private ReceiptDTO createReceipt() {
        ReceiptDTO receiptDTO = new ReceiptDTO();

        receiptDTO.setDateTransactionKey("Data:");
        receiptDTO.setDescriptionKey("Descrição:");
        receiptDTO.setValueKey("Valor:");

        receiptDTO.setReceiverNameKey("Nome:");
        receiptDTO.setReceiverDocumentKey("CPF/CNPJ:");
        receiptDTO.setReceiverBankKey("Banco:");
        receiptDTO.setReceiverAgencyKey("Agência:");
        receiptDTO.setReceiverAccountKey("Conta:");

        receiptDTO.setPayerNameKey("Nome:");
        receiptDTO.setPayerDocumentKey("CNPJ:");
        receiptDTO.setPayerBankKey("Banco:");
        receiptDTO.setTransactionIdKey("ID da Transação");
        receiptDTO.setProtocolKey("Protocolo");

        receiptDTO.setDescription("Reembolso de Transação");
        receiptDTO.setPayerName("PagFast Cobrança e Serviço em Tecnologia Ltda");
        receiptDTO.setPayerDocument("46.261.360/0001-48");

        receiptDTO.setReceiverBank(" - ");
        receiptDTO.setReceiverAgency(" - ");
        receiptDTO.setReceiverAccount(" - ");

        return receiptDTO;
    }

}
