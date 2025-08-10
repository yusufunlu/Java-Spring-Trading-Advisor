package com.yusufu.javaspringfeatures;

public class PolygonDataException extends RuntimeException {
    public PolygonDataException(String message) {
        super("PolygonDataException Message: " + message);
    }

}