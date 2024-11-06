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
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ReceiptService {

    @Autowired
    BankRepository bankRepository;

    public static final String REPORTS = "classpath:jasper/reports/";

    public static final String IMAGEBG = "classpath:jasper/img/receipt layout.png";

    public static final String JRXML_FILE = "receipt.jrxml";

    public static final String DESTINY = "C:\\jasper-reports\\";

    DecimalFormat decimalFormat;
    DateTimeFormatter dateFormat;

    public FileDTO generateReceiptInMemory(ReceiptDTO receiptDTO) throws IOException {

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

        byte[] imagebg = loadImageBg(IMAGEBG);

        Map<String, Object> mapParams = parameters(receiptDTO);
        mapParams.put("receipt layout", imagebg);

        String absolutPath = getAbsolutPath();

        JasperReport jasperReport = JasperCompileManager.compileReport(absolutPath);

        JasperPrint print = JasperFillManager.fillReport(jasperReport, mapParams, new JREmptyDataSource());

        return JasperExportManager.exportReportToPdf(print);

    }

    public Resource generateReceipt(ReceiptDTO receiptDTO) throws IOException {

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

        byte[] imagebg = loadImageBg(IMAGEBG);

        Map<String, Object> mapParams = parameters(receiptDTO);
        mapParams.put("receipt layout", imagebg);

        String absolutPath = getAbsolutPath();

        String folder = getPathToSave("receipt");

        JasperReport jasperReport = JasperCompileManager.compileReport(absolutPath);

        JasperPrint print = JasperFillManager.fillReport(jasperReport, mapParams, new JREmptyDataSource());

        JasperExportManager.exportReportToPdfFile(print, folder);

        System.out.println(folder);

        Path path = Path.of(folder).normalize();

        return new UrlResource(path.toUri());

    }

    private byte[] loadImageBg(String imagebg) throws IOException {
        String image = ResourceUtils.getFile(imagebg).getAbsolutePath();
        File file = new File(image);
        try(InputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private String getPathToSave(String fileName) throws FileNotFoundException {
        createDestinyPath(DESTINY);
        return DESTINY + fileName.concat(".pdf");
    }

    private void createDestinyPath(String fileName) {
        File dir = new File(fileName);

        if (!dir.exists())
            dir.mkdir();
    }

    private String getAbsolutPath() throws FileNotFoundException {
        return ResourceUtils.getFile(REPORTS + JRXML_FILE).getAbsolutePath();
    }

    private void fillReceipt(ReceiptDTO receipt, Map<String, String> mapReceiptData) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
        DateTimeFormatter dateFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", new Locale("pt", "BR"));//"yyyy-MM-dd'T'HH:mm:ss.SSSz"

        String dateAux = mapReceiptData.get("liquidacao").substring(0, 19);

        String dateTransaction = LocalDateTime.parse(dateAux, dateFormatter1).format(dateFormatter);

        String receiverDocument = "***" + mapReceiptData.get("payer-documento").substring(3, 8) + "***";

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

        receipt.setDateGenerated(LocalDateTime.now().format(dateFormatter));
    }

    private Map<String, String> getInfo(String receiptInfo) {

        Map<String, String> mapReceiptData = new HashMap<>();

        if (receiptInfo != null && !receiptInfo.isEmpty()) {

            receiptInfo = receiptInfo.replaceAll("cpf", "documento")
                                     .replaceAll("txid", "txId")
                                     .replaceAll("\"", "");

            boolean isPayer = false;

            String[] split = receiptInfo.split("\n");

            for (int i = 0; i< split.length; i++) {

                String[] splitAux = split[i].split(": ");

                if (splitAux.length > 1) {
                    if (isPayer)
                        mapReceiptData.put("payer-" + splitAux[0], splitAux[1]);
                    else
                        mapReceiptData.put(splitAux[0], splitAux[1]);

                    if (isPayer && splitAux[0].equals("conta"))
                        isPayer = false;

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
        receiptDTO.setReceiverDocumentKey("CPF/CPNJ:");
        receiptDTO.setReceiverBankKey("Banco:");
        receiptDTO.setReceiverAgencyKey("Agência:");
        receiptDTO.setReceiverAccountKey("Conta:");

        receiptDTO.setPayerNameKey("Nome:");
        receiptDTO.setPayerDocumentKey("Documento:");
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

    //test convert object entity to map by reflection
//    public static void main(String[] args) {
//        ReceiptDTO receiptDTO = new ReceiptDTO();
//
//        receiptDTO.setDateTransactionKey("Data:");
//        receiptDTO.setDescriptionKey("Descrição:");
//        receiptDTO.setValueKey("Valor:");
//
//        receiptDTO.setReceiverNameKey("Nome:");
//        receiptDTO.setReceiverDocumentKey("CPF/CPNJ:");
//        receiptDTO.setReceiverBankKey("Bank:");
//        receiptDTO.setReceiverAgencyKey("Agência:");
//        receiptDTO.setReceiverAccountKey("Conta:");
//
//        receiptDTO.setPayerNameKey("Nome:");
//        receiptDTO.setPayerDocumentKey("Documento:");
//        receiptDTO.setPayerBankKey("Banco:");
//        receiptDTO.setTransactionIdKey("ID da Transação");
//        receiptDTO.setProtocolKey("Protocolo");
//
//        receiptDTO.setDescription("Reembolso de Transação");
//        receiptDTO.setPayerName("PagFast Cobrança e Serviço em Tecnologia Ltda");
//        receiptDTO.setPayerDocument("46.261.360/0001-48");
//
//        receiptDTO.setReceiverBank(" - ");
//        receiptDTO.setReceiverAgency(" - ");
//        receiptDTO.setReceiverAccount(" - ");
//
//        Map<String, Object> map = parameters(receiptDTO);
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//    }

//    public static void main(String[] args) {
//        String data = "statusCode: \"Done\"\n" +
//                "data:\n" +
//                "endToEndId: \"E004169682024102323553n5nAGPMHOP\"\n" +
//                "endToEndIdOrigem: null\n" +
//                "recebimentoId: 1441172580\n" +
//                "txId: \"006d3786d82d4291838760452dc3b90e\"\n" +
//                "data: \"2024-10-23T20:55:40.247-03:00\"\n" +
//                "valor: 160\n" +
//                "status: \"Efetivado\"\n" +
//                "pagador:\n" +
//                "ispb: \"00416968\"\n" +
//                "conta:\n" +
//                "banco: \"77\"\n" +
//                "bancoNome: \"BANCO INTER\"\n" +
//                "agencia: \"0001\"\n" +
//                "numero: \"0332106870\"\n" +
//                "tipo: \"ContaCorrente\"\n" +
//                "pessoa:\n" +
//                "documento: \"09004705775\"\n" +
//                "tipoDocumento: \"CPF\"\n" +
//                "nome: \"VIVIANE LOURENCO NUNES GOMES GAMA\"\n" +
//                "nomeFantasia: null\n" +
//                "conta: null\n" +
//                "recebedor:\n" +
//                "ispb: \"71027866\"\n" +
//                "conta:\n" +
//                "banco: \"218\"\n" +
//                "bancoNome: \"BCO BS2 S.A.\"\n" +
//                "agencia: \"0001\"\n" +
//                "numero: \"11384190\"\n" +
//                "tipo: \"ContaCorrente\"\n" +
//                "pessoa:\n" +
//                "documento: \"46261360000148\"\n" +
//                "tipoDocumento: \"CNPJ\"\n" +
//                "nome: \"PAGFAST\"\n" +
//                "nomeFantasia: null\n" +
//                "conta: null\n" +
//                "chaveDict: \"b9e544eb-ee92-4d57-8665-c42f60d6fd65\"\n" +
//                "campoLivre: null\n" +
//                "situacao: \"ACCC\"\n" +
//                "devolucoes:\n" +
//                "0:\n" +
//                "id: \"1f6604e9a95d4d7896cac2a224d8d4e1\"\n" +
//                "rtrId: \"D71027866202410232355426139JCKBD\"\n" +
//                "valor: 160\n" +
//                "horario:\n" +
//                "solicitacao: \"2024-10-23T20:55:42.613-03:00\"\n" +
//                "liquidacao: \"2024-10-23T20:55:44.263-03:00\"\n" +
//                "status: \"DEVOLVIDO\"\n" +
//                "motivo: \"Solicitado pelo cliente\"\n" +
//                "erroDescricao: null\n" +
//                "erroCodigo: null\n" +
//                "pagina:\n" +
//                "qtd: 1\n" +
//                "paginaAtual: 1\n" +
//                "itensPorPagina: 1";
//
//        Map<String, String> mapReceiptData = new HashMap<>();
//
//        boolean isPayer = false;
//
//        data = data.replaceAll("\"", "");
//
//        String[] split = data.split("\n");
//
//        for (int i = 0; i< split.length; i++) {
//
//            String[] splitAux = split[i].split(": ");
//
//            if (splitAux.length > 1) {
//                if (isPayer)
//                    mapReceiptData.put("payer-" + splitAux[0], splitAux[1]);
//                else
//                    mapReceiptData.put(splitAux[0], splitAux[1]);
//
//                if (isPayer && splitAux[0].equals("conta"))
//                    isPayer = false;
//
//            } else if (splitAux[0].equals("pagador:"))
//                isPayer = true;
//            else if (splitAux[0].equals("devolucoes:"))
//                isPayer = false;
//
//        }
//
//        mapReceiptData.keySet().forEach(key -> {
//            System.out.println(key + " - " + mapReceiptData.get(key));
//            System.out.println(" ");
//        });
//    }

}
