package com.br.api.payassistent.service;

import com.br.api.payassistent.exceptions.AppException;
import com.br.api.payassistent.model.CustomerService;
import com.br.api.payassistent.model.User;
import com.br.api.payassistent.model.dto.MerchantCustomerServiceDto;
import com.br.api.payassistent.model.dto.SignUpDto;
import com.br.api.payassistent.model.dto.UserDto;
import com.br.api.payassistent.repository.CustomerServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

//    TODO
//    public UserDto register(SignUpDto userDto) {
//        List<CustomerService> listCustomerService = repository.findByDateBetweenAndUserId(getStartDate(), getEndDate(), );
//
//        if (!listCustomerService.isEmpty()) {
//            //throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
//        }
//
//        User user = userMapper.signUpToUser(userDto);
//        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.password())));
//
//        User savedUser = userRepository.save(user);
//
//        return userMapper.toUserDto(savedUser);
//    }

    private LocalDateTime getStartDate() {
        return LocalDate.now().atTime(0, 0, 0);
    }

    private LocalDateTime getEndDate() {
        return LocalDate.now().atTime(23, 59, 59);
    }

}
