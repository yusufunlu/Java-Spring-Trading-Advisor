package com.yusufu.tradingadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

public class StreamTest {

    @Test
    void iterateTest() {
        Stream<Integer> streamIterated = Stream.iterate(40, n -> n + 2).limit(20);
        streamIterated.forEach(item-> System.out.println("iterate:"+item));
    }

    @Test
    void randomTest() {
        IntStream intStream = IntStream.range(1, 3);
        DoubleStream doubleStream = (new Random()).doubles(3);
        intStream.forEach(item-> System.out.println("IntStream:"+item));
    }

    @Test
    void anyAndExceptionAllTest() {
        Stream<String> stream =
                Stream.of("a", "b", "c").filter(element -> element.contains("b"));
        Optional<String> anyElement = stream.findAny();
        Exception thrown = assertThrows(IllegalStateException.class,
                ()-> stream.findFirst());
        assertTrue(thrown.getMessage().contains("already"));

    }


    @Test
    void skipTest() {
        Stream<String> onceModifiedStream =
                Stream.of("abcd", "bbcd", "cbcd")
                        .skip(1)
                        .map(element -> element.substring(0, 3));
        onceModifiedStream.forEach(item-> System.out.println("substring: "+item));
    }


    long counter;
    private void wasCalled() {
        counter++;
    }

    @Test
    void lazyInvocationTest() {

        List<String> list = Arrays.asList("abc1", "abc2", "abc3");
        counter = 0;
        list.stream().filter(element -> {
            wasCalled();
            return element.contains("2");
        });
        assertEquals(0,counter);//No terminal operation so no inter operation called

        list.stream().filter(element -> {
            wasCalled();
            return element.contains("2");
        }).findFirst();
        assertEquals(2,counter);//it filters by order so it will not go after 2nd element
    }

    @Test
    void reduceTest(){

    }

    @Test
    void collectToListTest(){
        Pair<Integer, String> pair = Pair.of(1, "One");
        List<Pair<Integer,String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"), Pair.of(13, "lemon"),
                Pair.of(23, "bread"), Pair.of(13, "sugar"));

        List<Integer> collectorCollection =
                productList.stream().map(Pair::getFirst).collect(Collectors.toList());

    }

    @Test
    void collectJoiningTest(){
        List<Pair<Integer,String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"));

        String collectorCollection =
                productList.stream().map(Pair::getSecond)
                        .collect(Collectors.joining(", ", "[", "]"));

        assertEquals("[potatoes, orange]", collectorCollection);
    }

    @Test
    void collectAverageTest(){
        List<Pair<Integer,String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"));

        Double average =
                productList.stream()
                        .collect(Collectors.averagingDouble(p->p.getFirst()));

        assertEquals(18.5D, average);
    }

    @Test
    void collectGroupByTest(){
        List<Pair<Integer,String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"),
                Pair.of(14, "orange2"));

        Map<Integer, List<Pair<Integer,String>>> result =
                productList.stream()
                        .collect(Collectors.groupingBy(p->p.getFirst()));

        assertEquals(2, result.get(14).size());
    }

    @Test
    void collectPartitioningByTest(){
        Set<Pair<Integer,String>> productList =  Set.of(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"),
                Pair.of(14, "orange2"));

        Map<Boolean, List<Pair<Integer,String>>> result =
                productList.stream()
                        .collect(Collectors.partitioningBy(p->p.getFirst() < 15));

        assertEquals(2, result.get(true).size());
    }

    @Test
    void collectCustomTest(){
        Set<Pair<Integer,String>> productList =  Set.of(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"),
                Pair.of(14, "orange2"));

        ArrayList<Pair<Integer,String>>  result = productList.stream().collect(Collector.of(ArrayList::new,ArrayList::add,
                (first, second)-> {
                    first.addAll(second);
                    return first;
                }
        ));


        assertEquals(3, result.size());
    }
}
