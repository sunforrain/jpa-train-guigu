package com.atguigu.jpa.helloworld;

import javax.persistence.*;

/**
 *
 * 一个单向多对一的关联关系,Order里面有Customer ,但是Customer里面并没有set来对应Order
 */
@Entity
@Table(name = " JPA_ORDERS")
public class Order {
    private Integer id;
    private String orderName;

    private Customer customer;

    @GeneratedValue
    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "ORDER_NAME")
    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    // 映射单向多对一的关联关系
    //使用 @ManyToOne 来映射多对一的关联关系
    //使用 @JoinColumn 来映射外键.
    //可使用 @ManyToOne 的 fetch 属性来修改默认的关联属性的加载策略
    @JoinColumn(name = "CUSTOMER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
