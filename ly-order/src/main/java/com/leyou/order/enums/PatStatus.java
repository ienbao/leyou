package com.leyou.order.enums;

import javax.swing.plaf.PanelUI;

public enum  PatStatus {

    NON_PAY(0),SUCCESS(1),FAIL(2)
    ;
    int value;

    PatStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
