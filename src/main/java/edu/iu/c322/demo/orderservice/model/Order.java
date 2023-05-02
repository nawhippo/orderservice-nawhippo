package edu.iu.c322.demo.orderservice.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

@Entity
    public class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private int id;
        private int customerId;
        private float total;
        private String status;


        @OneToOne(cascade = CascadeType.ALL)
        @JoinColumn(name = "address_id")
        private Address shippingAddress;


        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
        private List<OrderItem> items;

        @OneToOne(cascade = CascadeType.ALL)
        @JoinColumn(name = "payment_id")
        private Payment payment;

        public void returnOrderItem(OrderItem item, String returnReason) {
            if (items.contains(item)) {
                item.setReturnReason(returnReason);
            }
        }

        public void addOrderItem(OrderItem item){
            items.add(item);
            item.setOrder(this);
        }

        public void removeOrderItem(OrderItem item){
            items.remove(item);
            item.setOrder(null);
        }

        // getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public float getTotal() {
            return total;
        }

        public void setTotal(float total) {
            this.total = total;
        }

        public Address getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(Address shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public List<OrderItem> getItems() {
            if (items != null) {
                itemIds = items.stream().map(OrderItem::getId).collect(Collectors.toList());
            }
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }

        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }


        @Transient
        private List<Integer> itemIds;



        public List<Integer> getItemIds() {
            if (items != null) {
                itemIds = items.stream().map(OrderItem::getId).collect(Collectors.toList());
            }
            return itemIds;
        }

        public void setItemIds(List<Integer> itemIds) {
            this.itemIds = itemIds;
        }


    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", total=" + total +
                ", status='" + status + '\'' +
                ", shippingAddress=" + shippingAddress +
                ", items=" + items +
                ", payment=" + payment +
                ", itemIds=" + itemIds +
                '}';
    }
}
