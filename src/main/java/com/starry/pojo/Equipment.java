package com.starry.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {
    private Integer id;
    private String name;
    private Integer category;
    private Integer subCategory;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Double deposit;
}
