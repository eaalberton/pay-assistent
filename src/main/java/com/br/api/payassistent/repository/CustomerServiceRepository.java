package com.br.api.payassistent.repository;

import com.br.api.payassistent.model.Contestation;
import com.br.api.payassistent.model.CustomerService;
import com.br.api.payassistent.model.dto.ServiceSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerServiceRepository extends JpaRepository<CustomerService, Long> {

    List<CustomerService> findByDateStartBetweenAndUserIdAndMerchantId(LocalDateTime startDate, LocalDateTime endDate, Long idUser, Long idMerchant);

    @Query(
    "SELECT NEW com.br.api.payassistent.model.dto.ServiceSummaryDto (   "
    + "   me.id as id,                                                  "
    + "   me.name as merchant,                                          "
    + "   sum(cs.quantity) as quantity                                  "
    + ")                                                                "
    + "FROM CustomerService cs                                          "
    + "INNER JOIN  Merchant me on cs.merchant.id = me.id                "
    + "WHERE cs.user.id = ?3                                            "
    + "AND cs.dateStart BETWEEN ?1 AND ?2                              "
    + "GROUP BY me.id, me.name                                                 "
    )
    List<ServiceSummaryDto> findCustomerServicesSummaryOfDayByUser(LocalDateTime startDate, LocalDateTime endDate, Long idUser);

}
