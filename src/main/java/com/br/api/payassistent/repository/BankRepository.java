package com.br.api.payassistent.repository;

import com.br.api.payassistent.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository  extends JpaRepository<Bank, Long> {

    Bank findByIspbCode(String ispbCode);

}
