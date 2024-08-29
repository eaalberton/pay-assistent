package com.br.api.payassistent.model;

public enum EnumSituation {

    CANCELED(0, "CANCELADA"),
    ACTIVE(1, "ATIVA");

    private Integer cod;
    private String value;

    EnumSituation(Integer cod, String value) {
        this.cod = cod;
        this.value = value;
    }

    public Integer getCod() {
        return cod;
    }

    public String getValue() {
        return value;
    }
}
