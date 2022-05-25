package com.example.android.zldc.db.entity;

import com.example.android.zldc.util.BuildRandomNumber;

/**
 * 订单映射实体
 */
public class OrderMapping {
    /**
     * id
     */
    private Integer id;
    /**
     * 订单号
     */
    private String orderNum;
    /**
     * 映射订单号
     */
    private String orderNumMapping;


    /**
     * 设置订单映射号
     *
     * @param orderNum 订单号
     */
    public void setOrderNumMappingByOrderNum(String orderNum) {
        this.orderNumMapping = BuildRandomNumber.createTimeStampLast(8);
    }

    public Integer getId() {
        return id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public String getOrderNumMapping() {
        return orderNumMapping;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public void setOrderNumMapping(String orderNumMapping) {
        this.orderNumMapping = orderNumMapping;
    }

    @Override
    public String toString() {
        return "OrderMapping{" +
                "id=" + id +
                ", orderNum='" + orderNum + '\'' +
                ", orderNumMapping='" + orderNumMapping + '\'' +
                '}';
    }
}
