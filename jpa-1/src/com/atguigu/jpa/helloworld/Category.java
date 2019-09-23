package com.atguigu.jpa.helloworld;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Table(name = "JPA_CATEGORY")
@Entity
public class Category {
    private  Integer id;
    private  String categoryName;

    private Set<Item> items = new HashSet<>();

    @GeneratedValue
    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "CATEGORY_NAME")
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // 指定多对多的关系维护方为Item里面的categories,这是所有双向关系中不可少的
    @ManyToMany(mappedBy = "categories")
    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }
}
