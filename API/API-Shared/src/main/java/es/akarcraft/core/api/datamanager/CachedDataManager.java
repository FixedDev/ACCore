package es.akarcraft.core.api.datamanager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;

public interface CachedDataManager<K, O> extends DataManager<K, O> {
    Optional<O> getIfCached(K key);

    ListenableFuture<O> getOrFind(K key);

    void cache(O object);

    void invalidate(K key);

    void refresh(K key);
}
