package es.akarcraft.core.api.datamanager;

import java.util.Optional;

public interface CachedDataManager<K, O> extends DataManager<K, O> {
    Optional<O> getIfCached(K key);

    void cache(K key, O object);

    void invalidate(K key);

    void refresh(K key);
}
