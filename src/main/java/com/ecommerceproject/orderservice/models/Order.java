package com.ecommerceproject.orderservice.models;

import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order extends BaseEntity{

    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> itemList = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    public void addOrderItem(OrderItem orderItem) {
        itemList.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        itemList.remove(orderItem);
        orderItem.setOrder(null);
    }

}
