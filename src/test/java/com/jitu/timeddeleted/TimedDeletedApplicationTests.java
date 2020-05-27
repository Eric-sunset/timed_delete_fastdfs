package com.jitu.timeddeleted;

import java.time.LocalDate;

public class TimedDeletedApplicationTests {
    public static void main(String[] args) {
        System.out.println(3/2);

        System.out.println(((int)Math.ceil((double)3/(double)2)));

        LocalDate date = LocalDate.now();

        date = date.minusDays(15);

        System.out.println(date);
    }
}
