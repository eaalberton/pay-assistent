package com.br.api.payassistent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceDetailDto {

    private String service;
    private String supportLevel;
    private String resolutionTime;
    private Integer quantity;

}
