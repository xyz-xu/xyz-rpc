package xyz.rpc.sample.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FundVO {

    private String str;

    private BigDecimal decimal;

    private Integer integer;

    private Double d;

    private Long l;

    private LocalDateTime dateTime;

    private byte[] bytes;

    private Object o;

}
