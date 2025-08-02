package com.yusufu.javaspringfeatures;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionalInterface {


    @Test
    void test1(){
        HashMap<String,String> example = new HashMap<>();

        //example.computeIfAbsent();

        Function<Integer, String> intToString = Object::toString;
        Function<String, String> quote = s -> "'" + s + "'";

        Function<Integer, String> quoteIntToString = quote.compose(intToString);

        assertEquals("'5'", quoteIntToString.apply(5));
    }
}
