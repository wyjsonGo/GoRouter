package com.wyjson.module_common.entity;

import java.io.Serializable;

public class UserEntity implements Serializable {
    public long id;
    public String name;

    public UserEntity(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
