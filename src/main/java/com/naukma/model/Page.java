package com.naukma.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Page<T> {

    private final List<T> content;
    private final int page;
    private final int pageSize;
    private final long totalElements;

    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    public boolean hasNext() {
        return page < getTotalPages();
    }
}