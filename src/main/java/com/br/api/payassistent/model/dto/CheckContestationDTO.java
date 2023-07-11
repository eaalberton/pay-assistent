package com.br.api.payassistent.model.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CheckContestationDTO {

    private String document;
    private String merchant;
    private String result;

}
