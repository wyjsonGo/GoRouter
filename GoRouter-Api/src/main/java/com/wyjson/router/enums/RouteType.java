package com.wyjson.router.enums;

public enum RouteType {
    ACTIVITY("/activity"),
    FRAGMENT("/fragment");
    
    private final String type;

    public String getType() {
        return type;
    }

    RouteType(String type) {
        this.type = type;
    }

    public static RouteType getType(String v) {
        for (RouteType each : values()) {
            if (each.type.equals(v))
                return each;
        }
        return null;
    }

    public static String toStringByValues() {
        StringBuilder routeTypeSB = new StringBuilder();
        for (RouteType routeType : RouteType.values()) {
            routeTypeSB.append("[");
            routeTypeSB.append(routeType.getType());
            routeTypeSB.append("]");
        }
        return routeTypeSB.toString();
    }
}