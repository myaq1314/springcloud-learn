package com.zz.scgatewaynew.respdefine;

import java.util.HashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2021-12-14 14:43
 * ************************************
 */
public enum OrderEnum {
    OPEN_CARD_RECHARGE_TYPE(1),
    OPEN_CARD_TYPE(2, 91),
    RECHARGE_TYPE(3, 999),
    COUPON_TYPE(4, 90),
    PAY_CHANNEL_TYPE(5),
    CAR_RIDE_TYPE(6);

    private Integer value;
    private int order;

    OrderEnum(Integer value) {
        this.value = value;
    }

    OrderEnum(Integer value, int order) {
        this.value = value;
        this.order = order;
    }

    public Integer getValue() {
        return value;
    }

    public int getOrder() {
        return order;
    }

    private static Map<Integer, OrderEnum> data = new HashMap<>();

    static {
        for(OrderEnum ot : OrderEnum.values()) {
            data.put(ot.value, ot);
        }
    }

    public static OrderEnum fromValue(Integer value) {
        return data.get(value);
    }
}
