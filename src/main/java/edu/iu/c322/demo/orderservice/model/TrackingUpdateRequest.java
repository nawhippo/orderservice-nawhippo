package edu.iu.c322.demo.orderservice.model;

import java.util.List;

public class TrackingUpdateRequest {
    private List<Integer> itemIds;
    private String status;

    public List<Integer> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Integer> itemIds) {
        this.itemIds = itemIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}