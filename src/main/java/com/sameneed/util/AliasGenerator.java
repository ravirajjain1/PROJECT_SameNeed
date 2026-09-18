package com.sameneed.util;

public class AliasGenerator {

    private AliasGenerator() {}

    public static String generate(int position) {
        if (position == 1) {
            return "Group Creator";
        }
        return String.format("Member %02d", position);
    }

    public static String generateForCount(int currentCount) {
        return generate(currentCount + 1);
    }
}
