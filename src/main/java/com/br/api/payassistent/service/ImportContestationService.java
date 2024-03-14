package com.br.api.payassistent.service;

import com.br.api.payassistent.model.CellsIndex;
import com.br.api.payassistent.model.Contestation;
import com.br.api.payassistent.repository.ContestationRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.ExcelReaderException;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class ImportContestationService {

    @Autowired
    ContestationRepository contestationRepository;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

//    @PostConstruct
    public void importContestationsFromExcelFile() throws IOException {
        String fileLocation = "C:\\Users\\AllBiNo\\Downloads\\14-03-2024.xlsx";

        contestationRepository.deleteAll();

        try (FileInputStream file = new FileInputStream(fileLocation); ReadableWorkbook wb = new ReadableWorkbook(file)) {
            readSheet(wb.getFirstSheet(), 0);//aba contestações
            readSheet(wb.getSheet(1).get(), 1);//aba banco genial
            readSheet(wb.getSheet(2).get(), 2);//aba bit capital
            readSheet(wb.getSheet(3).get(), 3);//aba fastcash
            readSheet(wb.getSheet(4).get(), 4);//aba trio
        }

    }

    public void importContestations(MultipartFile multipartFile) throws IOException {

        File file = multipartToFile(multipartFile);

        try (FileInputStream fileInputStream = new FileInputStream(file); ReadableWorkbook wb = new ReadableWorkbook(fileInputStream)) {
            readSheet(wb.getFirstSheet(), 0);//aba BS2
            readSheet(wb.getSheet(1).get(), 1);//aba banco genial
            readSheet(wb.getSheet(2).get(), 2);//aba bit capital
            readSheet(wb.getSheet(3).get(), 3);//aba fastcash
            readSheet(wb.getSheet(4).get(), 4);//aba trio
        }

        log.info("Novo arquivo: ", file.getAbsolutePath());
        log.info("Recebendo o arquivo: ", multipartFile.getOriginalFilename());

    }

    private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }

    private void readSheet(Sheet sheet, int sheetNumber) {
        try (Stream<Row> rows = sheet.openStream()) {

            CellsIndex cellsIndex = getInstanceCellsIndex(sheetNumber);

            Integer indexE2e = getInstanceCellsIndex(sheetNumber).getIndexE2e();

            List<String> listAllEndToEnd = contestationRepository.findAllEndToEnd();

            LocalDate today = LocalDate.now(ZoneId.of("GMT-3"));

            rows.filter(r -> r.getRowNum() > 1).forEach(r -> {

                if (validateMainRecords(r, indexE2e)) {

                    if (!listAllEndToEnd.contains(r.getCellText(indexE2e).trim()))
                        contestationRepository.save(createContestation(r, cellsIndex, today));
                }
            });
            log.info("List size: " + listAllEndToEnd.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CellsIndex getInstanceCellsIndex(int sheetNumber) {
        if (sheetNumber == 0)//aba BS2
            return new CellsIndex(1,2,3,4,5,8);

        if (sheetNumber == 1)//aba banco genial
            return new CellsIndex(1,2,3,4,5,7);

        if (sheetNumber == 2)//aba bit capital
            return new CellsIndex(0,1,2,3,4,6);

        if (sheetNumber == 3)//aba fastcash
            return new CellsIndex(0,null,2,3,4,6);

        if (sheetNumber == 4)//aba trio
            return new CellsIndex(0,1,2,3,4,7);

        return null;
    }

    private boolean validateMainRecords(Row r, int indexE2e) {

        return !r.getCellText(indexE2e).isEmpty();
    }

    private Contestation createContestation(Row row, CellsIndex cellsIndex, LocalDate today) {

        LocalDateTime date = null;
        if (cellsIndex.getIndexDate() != null) {
            if (isValidDate(row, cellsIndex))
                date = row.getCellAsDate(cellsIndex.getIndexDate()).get();
        }

        BigDecimal value = null;
        if (!row.getCellText(cellsIndex.getIndexValue()).isEmpty())
            value = formatValue(row.getCellText(cellsIndex.getIndexValue()));

        String cpfGenerated = removeNonNumericCharacters(configureScaleCPF(row, cellsIndex.getIndexCpfGenerated()));
        String cpfPaid = removeNonNumericCharacters(configureScaleCPF(row, cellsIndex.getIndexCpfPaid()));

        Contestation contestation = new Contestation(
                null,
                row.getCellText(cellsIndex.getIndexE2e()).trim(),
                date,
                today,
                value,
                row.getCellText(cellsIndex.getIndexMerchant()).toUpperCase().trim(),
                StringUtils.leftPad(cpfGenerated, 11, "0"),
                StringUtils.leftPad(cpfPaid, 11, "0")
        );

        System.out.println(contestation);
        System.out.println("Linha: " + row.getRowNum()+"\n --------------------------");

        return contestation;
    }

    private  boolean isValidDate(Row row, CellsIndex cellsIndex) {

        try {
            Optional<LocalDateTime> date = row.getCellAsDate(cellsIndex.getIndexDate());
            return date.isPresent() && !date.get().toString().contains("+");
        } catch (Exception e) {
            return false;
        }
    }

    private static BigDecimal formatValue(String value) {
        value = value.replaceAll("[^0-9,.]", "");

        if (value.contains(",") && value.contains("."))
            value = value.replaceAll("[^0-9,]", "");

        if (value.contains(",") || value.contains("."))
            value = value.replaceAll(",", ".");

        System.out.println(value + " *****");
        if (value != null && !value.isEmpty())
            return new BigDecimal(value).setScale(2, RoundingMode.CEILING);
        else
            return null;
    }

    private String removeNonNumericCharacters(String value) {
        if (value != null)
            value = value.replaceAll("[^0-9]", "");

        return value;
    }

    /**
     * metodo usado para resolver o problema do FastExcel quanto a leitura
     * das colunas referente ao CPF para que não seja necessario converte-las
     * para tipo texto antes da importação
     *
     * @param row, cellsIndex
     * @return cpf ajustado
     */
    private String configureScaleCPF(Row row, Integer cellsIndex) {
        try {
            BigDecimal cpf = row.getCell(cellsIndex).asNumber();

            return cpf == null ? null : cpf.setScale(0).toString();
        }catch (ExcelReaderException e) {
            return row.getCellText(cellsIndex);
        }
    }

    private static void testPrintCells(CellsIndex cellsIndex, Row r) {
        System.out.println(r.getCellText(cellsIndex.getIndexE2e()));
        System.out.println(r.getCellAsDate(cellsIndex.getIndexDate()));
        System.out.println(r.getCellText(cellsIndex.getIndexValue()));
        System.out.println(r.getCellText(cellsIndex.getIndexMerchant()));
        System.out.println(StringUtils.leftPad(r.getCellText(cellsIndex.getIndexCpfGenerated()), 11, "0"));
        System.out.println(StringUtils.leftPad(r.getCellText(cellsIndex.getIndexCpfPaid()), 11, "0"));
        System.out.println("\n --------------------------");
    }

}
