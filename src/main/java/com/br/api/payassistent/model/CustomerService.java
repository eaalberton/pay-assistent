package com.br.api.payassistent.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_services")
public class CustomerService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_merchant")
    private Merchant merchant;

    @ManyToOne
    @JoinColumn(name = "id_request")
    private Request request;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime date;

    @NotBlank
    private String shift;

    @NotBlank
    private String supportLevel;

    @NotBlank
    private String resolutionTime;

}
