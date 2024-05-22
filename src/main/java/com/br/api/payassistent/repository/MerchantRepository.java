package com.br.api.payassistent.repository;

import com.br.api.payassistent.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    List<Merchant> findAllByOrderByNameAsc();
}
