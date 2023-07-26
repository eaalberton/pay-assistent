package com.br.api.payassistent.mappers;

import com.br.api.payassistent.model.User;
import com.br.api.payassistent.model.dto.SignUpDto;
import com.br.api.payassistent.model.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);

}
