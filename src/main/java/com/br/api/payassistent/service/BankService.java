package com.br.api.payassistent.service;

import com.br.api.payassistent.model.Bank;
import com.br.api.payassistent.repository.BankRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class BankService {

    @Autowired
    BankRepository bankRepository;

//    @PostConstruct
    private void saveBanksFromCsvFile() {
        try {
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\AllBiNo\\Documents\\lista de bancos.csv"));

            // create csv reader
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    // Skip the header
                    //.withSkipLines(1)
                    .build();

            // read one record at a time
            String[] record;
            while ((record = csvReader.readNext()) != null) {

                Bank bank = new Bank(
                        null,
                        record[0],                  //"ISPB: "
                        record[1]                   //"Name: "
                        );

                System.out.println(bank);
                bankRepository.save(bank);
            }

            // close readers
            csvReader.close();
            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
