package com.infoworks.utils.rest.data.model;

import com.infoworks.objects.Message;

import java.util.Map;

public class Page extends Message {
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private Integer number;

    public Page() {}

    public Page(Map<String, Object> dataMap) {
        unmarshalling(dataMap, false);
    }

    public int getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
