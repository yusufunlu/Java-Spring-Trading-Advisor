package com.yusufu.tradingadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

public class StreamTest {

    @Test
    void iterateTest() {
        Stream<Integer> streamIterated = Stream.iterate(40, n -> n + 2).limit(20);
        streamIterated.forEach(item -> System.out.println("iterate:" + item));
    }

    @Test
    void randomTest() {
        IntStream intStream = IntStream.range(1, 3);
        DoubleStream doubleStream = (new Random()).doubles(3);
        intStream.forEach(item -> System.out.println("IntStream:" + item));
    }

    @Test
    void anyAndExceptionAllTest() {
        Stream<String> stream =
                Stream.of("a", "b", "c").filter(element -> element.contains("b"));
        Optional<String> anyElement = stream.findAny();
        Exception thrown = assertThrows(IllegalStateException.class,
                () -> stream.findFirst());
        assertTrue(thrown.getMessage().contains("already"));

    }


    @Test
    void skipTest() {
        Stream<String> onceModifiedStream =
                Stream.of("abcd", "bbcd", "cbcd")
                        .skip(1)
                        .map(element -> element.substring(0, 3));
        onceModifiedStream.forEach(item -> System.out.println("substring: " + item));
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
        assertEquals(0, counter);//No terminal operation so no inter operation called

        list.stream().filter(element -> {
            wasCalled();
            return element.contains("2");
        }).findFirst();
        assertEquals(2, counter);//it filters by order so it will not go after 2nd element
    }

    @Test
    void reduceTest() {
        OptionalInt reduced =
                IntStream.range(1, 4).reduce((a, b) -> a + b);

        int sum = List.of(1, 2, 3, 4)
                .stream()
                .reduce(0, (result, x) -> result + x);

        String joined2 = Stream.of("a", "b", "c")
                .reduce(new StringBuilder(),
                        (b, s) -> b.append(s),                  // accumulator
                        (b1, b2) -> b1.append(b2))              // combiner
                .toString();

        List<? super Integer> sink = new ArrayList<Number>();

    }

    @Test
    void collectToListTest() {
        Pair<Integer, String> pair = Pair.of(1, "One");
        List<Pair<Integer, String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"), Pair.of(13, "lemon"),
                Pair.of(23, "bread"), Pair.of(13, "sugar"));

        List<Integer> collectorCollection =
                productList.stream().map(Pair::getFirst).collect(Collectors.toList());

    }

    @Test
    void collectJoiningTest() {
        List<Pair<Integer, String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"));

        String collectorCollection =
                productList.stream().map(Pair::getSecond)
                        .collect(Collectors.joining(", ", "[", "]"));

        assertEquals("[potatoes, orange]", collectorCollection);
    }

    @Test
    void collectAverageTest() {
        List<Pair<Integer, String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"));

        Double average =
                productList.stream()
                        .collect(Collectors.averagingDouble(p -> p.getFirst()));

        assertEquals(18.5D, average);
    }

    @Test
    void collectGroupByTest() {
        List<Pair<Integer, String>> productList = Arrays.asList(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"),
                Pair.of(14, "orange2"));

        Map<Integer, List<Pair<Integer, String>>> result =
                productList.stream()
                        .collect(Collectors.groupingBy(p -> p.getFirst()));

        assertEquals(2, result.get(14).size());
    }

    @Test
    void collectPartitioningByTest() {
        Set<Pair<Integer, String>> productList = Set.of(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"),
                Pair.of(14, "orange2"));

        Map<Boolean, List<Pair<Integer, String>>> result =
                productList.stream()
                        .collect(Collectors.partitioningBy(p -> p.getFirst() < 15));

        assertEquals(2, result.get(true).size());
    }

    @Test
    void collectCustomTest() {
        Set<Pair<Integer, String>> productList = Set.of(Pair.of(23, "potatoes"),
                Pair.of(14, "orange"),
                Pair.of(14, "orange2"));

        ArrayList<Pair<Integer, String>> result = productList.stream().collect(Collector.of(ArrayList::new, ArrayList::add,
                (first, second) -> {
                    first.addAll(second);
                    return first;
                }
        ));


        assertEquals(3, result.size());
    }

    class Animal {
        final String name;

        Animal(String name) {
            this.name = name;
        }
    }

    class Dog extends Animal {
        Dog(String name) {
            super(name);
        }
    }

    class Puppy extends Dog {
        Puppy(String name) {
            super(name);
        }
    }

    public void mapTest() {
        List<String> names = List.of("x", "yy", "zzz");

        List<Integer> lengths = names.stream()
                .map(a -> a.length()) //<R> Stream<R> map(Function<? super T, ? extends R> mapper);
                .toList();

        int totalLength = names.stream()
                .mapToInt(String::length)       // IntStream
                .sum();

        List<Dog> dogs = List.of(new Dog("Rex"), new Puppy("Mia"));

        Stream<Animal> promoted = dogs.stream()
                .map(a -> new Puppy(a.name + " Jr"));

        System.out.println(lengths);
    }

    public void flatmapTest() {
        List<String> nums = List.of("1 2", "3", "4 5");
        int sum = nums.stream()
                .flatMapToInt(s -> Arrays.stream(s.split("\\s+")).mapToInt(Integer::parseInt))
                .sum();
    }

    public void testBiFunction(String[] args) {
        // A BiFunction that takes a String and an Integer, and returns a String
        BiFunction<String, Integer, String> stringIntBiFunction = (str, num) -> str.repeat(num);

        // Apply the function
        String result = stringIntBiFunction.apply("Hello", 3);
        System.out.println(result); // Output: "HelloHelloHello"
    }


    static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }


    @Test
    public void linkedListTest(){
        StreamTest.ListNode head = new StreamTest.ListNode(1);
        ListNode current = head;
        current.next = new StreamTest.ListNode(2);
        current = current.next;
        current.next = new StreamTest.ListNode(3);
        printListTest(head);
        ListNode reversed = reverseList(head);
        printListTest(reversed);
    }

    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;

        while (curr != null) {
            ListNode temp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = temp;
        }
        return prev;
    }


    public void printListTest(ListNode head) {
        ListNode current = head;
        while (current != null) {
            System.out.println(current.val);
            current = current.next;
        }
    }
}
