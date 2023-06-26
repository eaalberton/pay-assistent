package com.br.api.payassistent.repository;

import com.br.api.payassistent.model.Contestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestationRepository extends JpaRepository<Contestation, Long> {

    List<Contestation> findByCpfGeneratedOrCpfPaid(String cpfGenereted, String cpfPaid);

}
