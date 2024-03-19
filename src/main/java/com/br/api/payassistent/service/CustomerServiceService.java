package com.br.api.payassistent.service;

import com.br.api.payassistent.model.CustomerService;
import com.br.api.payassistent.model.Merchant;
import com.br.api.payassistent.model.dto.MerchantCustomerServiceDto;
import com.br.api.payassistent.model.dto.ServiceDetailDto;
import com.br.api.payassistent.model.dto.ServiceSummaryDto;
import com.br.api.payassistent.model.dto.UserDto;
import com.br.api.payassistent.repository.CustomerServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerServiceService {

    @Autowired
    CustomerServiceRepository repository;

    public MerchantCustomerServiceDto register(MerchantCustomerServiceDto merchantCustomerServiceDto) {

        if (merchantCustomerServiceDto != null) {
            merchantCustomerServiceDto.getListCustomerService().forEach(customerService -> {
                customerService.setDate(LocalDateTime.now());
                customerService = repository.save(customerService);
            });
        }

        return merchantCustomerServiceDto;
    }

    public MerchantCustomerServiceDto findCustomerServicesSummaryOfDayByUser(Long userId) {

        MerchantCustomerServiceDto response = new MerchantCustomerServiceDto();

        List<CustomerService> listCustomerService = repository.findByDateBetweenAndUserId(getStartDate(), getEndDate(), userId);

        if (!listCustomerService.isEmpty()) {

            Map<Long, List<CustomerService>> mapIdMerchant_customerService = new HashMap<>();

            listCustomerService.forEach(customerService -> {
                if (mapIdMerchant_customerService.containsKey(customerService.getMerchant().getId())) {
                    mapIdMerchant_customerService.get(customerService.getMerchant().getId()).add(customerService);
                } else {
                    List<CustomerService> listMerchantServices = new ArrayList<>();
                    listMerchantServices.add(customerService);
                    mapIdMerchant_customerService.put(customerService.getMerchant().getId(), listMerchantServices);
                }
            });

            createServiceSummary(response, mapIdMerchant_customerService);
        }

        return response;
    }

    private void createServiceSummary(MerchantCustomerServiceDto response, Map<Long, List<CustomerService>> mapIdMerchantCustomerService) {
        mapIdMerchantCustomerService.keySet().forEach(idMerchant -> {
            ServiceSummaryDto summaryDto = new ServiceSummaryDto(
                    mapIdMerchantCustomerService.get(idMerchant).get(0).getMerchant().getName(),
                    mapIdMerchantCustomerService.get(idMerchant).size()
            );

            response.getListServiceSummary().add(summaryDto);
        });
    }

    public MerchantCustomerServiceDto findCustomerServicesDetailOfDayByUserAndMerchant(Long userId, Long merchantId) {

        MerchantCustomerServiceDto response = new MerchantCustomerServiceDto();

        List<CustomerService> listCustomerService = repository.findByDateBetweenAndUserIdAndMerchantId(getStartDate(), getEndDate(), userId, merchantId);

        if (!listCustomerService.isEmpty()) {

            Map<Long, List<CustomerService>> mapIdRequest_customerService = new HashMap<>();

            listCustomerService.forEach(customerService -> {
                if (mapIdRequest_customerService.containsKey(customerService.getRequest().getId())) {
                    mapIdRequest_customerService.get(customerService.getRequest().getId()).add(customerService);
                } else {
                    List<CustomerService> listRequestServices = new ArrayList<>();
                    listRequestServices.add(customerService);
                    mapIdRequest_customerService.put(customerService.getRequest().getId(), listRequestServices);
                }
            });

            createServiceDetail(response, mapIdRequest_customerService);
        }

        return response;
    }

    private void createServiceDetail(MerchantCustomerServiceDto response, Map<Long, List<CustomerService>> mapIdRequestCustomerService) {
        mapIdRequestCustomerService.keySet().forEach(idRequest -> {

            String supportLevel = mapIdRequestCustomerService.get(idRequest).get(0).getSupportLevel();
            String resolutionTime = mapIdRequestCustomerService.get(idRequest).get(0).getResolutionTime();

            for (CustomerService service : mapIdRequestCustomerService.get(idRequest) ) {
                if (!supportLevel.equals(service.getSupportLevel()))
                    supportLevel = "Variado";

                if (!resolutionTime.equals(service.getResolutionTime()))
                    resolutionTime = "Variado";

                if (supportLevel.equals("Variado") && resolutionTime.equals("Variado"))
                    break;
            }

            ServiceDetailDto detailDto = new ServiceDetailDto(
                    mapIdRequestCustomerService.get(idRequest).get(0).getRequest().getName(),
                    supportLevel,
                    resolutionTime,
                    mapIdRequestCustomerService.get(idRequest).size()
            );

            response.getListServiceDetail().add(detailDto);
        });
    }

    private LocalDateTime getStartDate() {
        return LocalDate.now().atTime(0, 0, 0);
    }

    private LocalDateTime getEndDate() {
        return LocalDate.now().atTime(23, 59, 59);
    }

}
