package com.example.demo.core.security;

import java.util.HashSet;
import java.util.Set;

public class Blacklist {
    private static Set<String> blacklist = new HashSet<>();

    public static void addToken(String token) {
        blacklist.add(token);
    }

    public static void removeToken(String token) {
        blacklist.remove(token);
    }

    public static boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
