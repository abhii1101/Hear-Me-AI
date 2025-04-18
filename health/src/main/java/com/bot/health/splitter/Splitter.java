package com.bot.health.splitter;


import java.util.List;

public interface Splitter<T> {
    List<T> split(T t);
}