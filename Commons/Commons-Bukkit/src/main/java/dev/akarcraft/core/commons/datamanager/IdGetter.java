package dev.akarcraft.core.commons.datamanager;

public interface IdGetter<O, K> {
    K getFromObject(O object);

    String toString(K key);
}
