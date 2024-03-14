package com.br.api.payassistent.model.dto;

import com.br.api.payassistent.model.CustomerService;
import com.br.api.payassistent.model.Merchant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantCustomerServiceDto {

    private UserDto userDto;

    private Merchant merchant;

    private List<ServiceSummaryDto> listServiceSummary = new ArrayList<>();

    private List<ServiceDetailDto> listServiceDetail = new ArrayList<>();

    private List<CustomerService> listCustomerService = new ArrayList<>();

}
