package com.aichat.aichatdemo2.feature.chat.repository;

import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public final class PageSlice<T> {

    private final List<T> items;
    private final String nextCursor;

    private PageSlice(List<T> items, String nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public static <T> PageSlice<T> take(List<T> fetched, int limit, Function<T, String> cursorOf) {
        if (fetched.size() <= limit) {
            return new PageSlice<>(List.copyOf(fetched), null);
        }
        List<T> items = List.copyOf(fetched.subList(0, limit));
        return new PageSlice<>(items, cursorOf.apply(items.get(items.size() - 1)));
    }
}
