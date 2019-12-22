package es.akarcraft.core.api.datamanager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public interface DataManager<K, O> {
    O getObject(K key);

    ListenableFuture<O> getObjectAsync(K key);

    List<O> getObjects(List<K> keys);

    ListenableFuture<O> getObjectsAsync(List<K> keys);

    ListenableFuture<O> deleteObject(K key);

    ListenableFuture<?> save(O object);
}
