package com.wyjson.module_main.event;

public class CustomEvent {

    public long id;
    public String name;

    public CustomEvent(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
