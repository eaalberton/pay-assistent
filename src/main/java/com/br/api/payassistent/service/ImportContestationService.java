package com.br.api.payassistent.service;

import com.br.api.payassistent.model.CellsIndex;
import com.br.api.payassistent.model.Contestation;
import com.br.api.payassistent.repository.ContestationRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
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
        String fileLocation = "C:\\Users\\AllBiNo\\Downloads\\13-07-2023.xlsx";

        contestationRepository.deleteAll();

        try (FileInputStream file = new FileInputStream(fileLocation); ReadableWorkbook wb = new ReadableWorkbook(file)) {
            readSheet(wb.getFirstSheet(), 0);//aba contestações
            readSheet(wb.getSheet(2).get(), 2);//aba bit capital
        }

    }

    public void importContestations(MultipartFile multipartFile) throws IOException {

        File file = multipartToFile(multipartFile);

        contestationRepository.deleteAll();

        try (FileInputStream fileInputStream = new FileInputStream(file); ReadableWorkbook wb = new ReadableWorkbook(fileInputStream)) {
            readSheet(wb.getFirstSheet(), 0);//aba contestações
            readSheet(wb.getSheet(2).get(), 2);//aba bit capital
        }

        log.info("Novo arquivo: " + file.getAbsolutePath());
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

            rows.filter(r -> r.getRowNum() > 1).forEach(r -> {

                if (validateMainRecords(r, sheetNumber)) {
                    contestationRepository.save(createContestation(r, cellsIndex));
                }

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CellsIndex getInstanceCellsIndex(int sheetNumber) {
        if (sheetNumber == 0)//aba contestações
            return new CellsIndex(1,2,3,4,5,8);

        if (sheetNumber == 2)//aba bit capital
            return new CellsIndex(1,null,2,3,4,6);

        return null;
    }

    private boolean validateMainRecords(Row r, int sheetNumber) {
        int endToEndCellNumber = 0;

        if (sheetNumber == 0)
            endToEndCellNumber = 1;

        if (sheetNumber == 2)
            endToEndCellNumber = 2;

        return !r.getCellText(endToEndCellNumber).isEmpty();
    }

    private Contestation createContestation(Row r, CellsIndex cellsIndex) {

        LocalDateTime date = null;
        if (cellsIndex.getIndexDate() != null) {
            if (isValidDate(r, cellsIndex))
                date = r.getCellAsDate(cellsIndex.getIndexDate()).get();
        }

        BigDecimal value = null;
        if (!r.getCellText(cellsIndex.getIndexValue()).isEmpty())
            value = formatValue(r.getCellText(cellsIndex.getIndexValue()));

        Contestation contestation = new Contestation(
                null,
                r.getCellText(cellsIndex.getIndexE2e()).trim(),
                date,
                value,
                r.getCellText(cellsIndex.getIndexMerchant()).toUpperCase().trim(),
                StringUtils.leftPad(r.getCellText(cellsIndex.getIndexCpfGenerated()), 11, "0"),
                StringUtils.leftPad(r.getCellText(cellsIndex.getIndexCpfPaid()), 11, "0")
        );

        System.out.println(contestation);
        System.out.println("Linha: " + r.getRowNum()+"\n --------------------------");

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
