package com.halofour.functionally.util;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IOTest {
    @Test
    public void test() throws Throwable {

        // simulate Console.readLine()
        Scanner scanner = new Scanner("333\n111\n");

        IO<String> io = IO.apply(() -> {
            System.out.println("Reading from stdin!");
            return scanner.nextLine();
        });
        IO<Integer> parsed = io.map(Integer::parseInt);
        IO<Integer> divide = parsed.flatMap(x -> parsed.map(y -> x / y));

        // bad, only here to test
        int result = divide.get();
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void testAsync() throws Throwable {
        Scanner scanner = new Scanner("333\n111\n");
        Timer timer = new Timer("test-timer", true);

        // simulate waiting for input from the console
        IO<String> io = IO.async(onFinished -> timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String line = scanner.nextLine();
                    onFinished.accept(Success.of(line));
                } catch (Throwable ignored) { }
            }
        }, 1000));

        IO<Integer> parsed = io.map(Integer::parseInt);
        IO<Integer> divide = parsed.flatMap(x -> parsed.map(y -> x / y));

        // bad, only here to test
        int result = divide.get();
        assertThat(result).isEqualTo(3);
    }
}
