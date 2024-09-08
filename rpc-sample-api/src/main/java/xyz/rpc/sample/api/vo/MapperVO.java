package xyz.rpc.sample.api.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MapperVO {

    private String str;

    private BigDecimal decimal;

    private Integer integer;

    private Double d;

    private Long l;

}
