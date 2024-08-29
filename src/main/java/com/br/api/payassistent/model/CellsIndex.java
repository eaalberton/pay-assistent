package com.br.api.payassistent.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CellsIndex {

    private Integer indexE2e;
    private Integer indexDate;
    private Integer indexValue;
    private Integer indexMerchant;
    private Integer indexCpfGenerated;
    private Integer indexCpfPaid;
    private Integer indexSituation;

}
