package com.br.api.payassistent.service;

import com.br.api.payassistent.model.Merchant;
import com.br.api.payassistent.repository.MerchantRepository;
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
import java.util.List;

@Service
public class MerchantService {

    @Autowired
    MerchantRepository repository;

//    @PostConstruct
    private void saveMerchantsFromCsvFile() {
        try {
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\AllBiNo\\Documents\\Infos PB Assistent\\Merchants - 1.csv"));

            // create csv reader
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    // Skip the header
                    //.withSkipLines(1)
                    .build();

            // read one record at a time
            String[] record;
            while ((record = csvReader.readNext()) != null) {

                Merchant merchant = new Merchant(
                        null,
                        record[0],                   //"nome: "
                        record[1],                   //"plataforma: "
                        record[2],                   //"plataforma atendimento: "
                        null                         //"gerente de conta: "

                );

                System.out.println(merchant);
                repository.save(merchant);
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

    public List<Merchant> findAll() {
        return repository.findAllByOrderByNameAsc();
    }

}
