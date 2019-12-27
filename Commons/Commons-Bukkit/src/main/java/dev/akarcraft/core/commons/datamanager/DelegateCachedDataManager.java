package dev.akarcraft.core.commons.datamanager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Optional;

/**
 * This class delegates all the DataManager methods to another instance with the same types
 * And leaves the methods of the CachedDataManager to be implemented
 * @param <K> The key type
 * @param <O> The object type
 */
public abstract class DelegateCachedDataManager<K, O> implements CachedDataManager<K, O> {
    private DataManager<K, O> delegate;

    public DelegateCachedDataManager(DataManager<K, O> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<O> getObject(K key) {
        return delegate.getObject(key);
    }

    @Override
    public ListenableFuture<O> getObjectAsync(K key) {
        return delegate.getObjectAsync(key);
    }

    @Override
    public List<O> getObjects(List<K> keys) {
        return delegate.getObjects(keys);
    }

    @Override
    public ListenableFuture<List<O>> getObjectsAsync(List<K> keys) {
        return delegate.getObjectsAsync(keys);
    }

    @Override
    public ListenableFuture<?> deleteObject(K key) {
        return delegate.deleteObject(key);
    }

    @Override
    public ListenableFuture<?> save(O object) {
        return delegate.save(object);
    }

}
