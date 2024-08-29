package com.br.api.payassistent.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contestations")
public class Contestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String endToEnd;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate ImportDate;

    private BigDecimal value;

    private String merchant;

    private String cpfGenerated;

    private String cpfPaid;

    private EnumSituation situation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contestation that = (Contestation) o;
        return Objects.equals(endToEnd, that.endToEnd) && Objects.equals(date, that.date) && Objects.equals(value, that.value) && Objects.equals(merchant, that.merchant) && Objects.equals(cpfGenerated, that.cpfGenerated) && Objects.equals(cpfPaid, that.cpfPaid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endToEnd, date, value, merchant, cpfGenerated, cpfPaid);
    }
}
