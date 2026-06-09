package com.naukma.model;

import lombok.Getter;

@Getter
public class ProductFilter {

    private final String nameContains;
    private final String groupId;
    private final Double minPrice;
    private final Double maxPrice;
    private final Integer minQuantity;
    private final Integer maxQuantity;
    private final int page;
    private final int pageSize;

    private ProductFilter(Builder b) {
        this.nameContains  = b.nameContains;
        this.groupId       = b.groupId;
        this.minPrice      = b.minPrice;
        this.maxPrice      = b.maxPrice;
        this.minQuantity   = b.minQuantity;
        this.maxQuantity   = b.maxQuantity;
        this.page          = b.page;
        this.pageSize      = b.pageSize;
    }

    public int getOffset() { return (page - 1) * pageSize; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String  nameContains;
        private String  groupId;
        private Double  minPrice;
        private Double  maxPrice;
        private Integer minQuantity;
        private Integer maxQuantity;
        private int page     = 1;
        private int pageSize = 20;

        public Builder nameContains(String v)  { this.nameContains = v; return this; }
        public Builder groupId(String v)        { this.groupId = v;      return this; }
        public Builder minPrice(Double v)       { this.minPrice = v;     return this; }
        public Builder maxPrice(Double v)       { this.maxPrice = v;     return this; }
        public Builder minQuantity(Integer v)   { this.minQuantity = v;  return this; }
        public Builder maxQuantity(Integer v)   { this.maxQuantity = v;  return this; }
        public Builder page(int v)              { this.page = v;         return this; }
        public Builder pageSize(int v)          { this.pageSize = v;     return this; }

        public ProductFilter build() {
            if (page < 1)     throw new IllegalArgumentException("page must be >= 1");
            if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
            return new ProductFilter(this);
        }
    }
}