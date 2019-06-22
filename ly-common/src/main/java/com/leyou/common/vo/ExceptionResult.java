package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnums;
import lombok.Data;

@Data
public class ExceptionResult {
    private int status;
    private String msg;
    private Long timestream;

    public ExceptionResult(ExceptionEnums em) {
        this.status = em.getCode();
        this.msg = em.getMsg();
        this.timestream = System.currentTimeMillis();
    }
}
