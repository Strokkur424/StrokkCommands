package net.strokkur.commands.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NullMarked
public interface SetMap<K, V> extends Map<K, V> {

    default Set<@Nullable V> putFor(V value, K... keys) {
        Set<V> out = new HashSet<>(keys.length);
        for (K key : keys) {
            out.add(this.put(key, value));
        }
        return out;
    }
}