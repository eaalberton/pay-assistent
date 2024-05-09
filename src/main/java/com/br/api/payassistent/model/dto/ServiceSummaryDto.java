package com.br.api.payassistent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceSummaryDto {

    private Long id;
    private String merchant;
    private Long quantity;

}
