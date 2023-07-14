package com.br.api.payassistent.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ImportContestationDTO {

    private String fileName;
    private MultipartFile file;
    private String result;

}
