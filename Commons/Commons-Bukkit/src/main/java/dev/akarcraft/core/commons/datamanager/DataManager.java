package dev.akarcraft.core.commons.datamanager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Optional;

/**
 * This class manages the serialization of the object of type {@param O}
 * and also provides the basic operations like get, delete and save(update) for this type of object
 * on a datastore
 *
 * @param <K> The key of the object
 * @param <O> The actual object type
 */
public interface DataManager<K, O> {
    /**
     * Finds an object on the datastore with the specified key
     * @param key The key of the object to find
     *
     * @return An optional object of type {@param O}, absent if it doesn't exists on the datastore
     */
    Optional<O> getObject(K key);

    /**
     * Finds an object using another thread on the datastore with the specified key
     * @param key The key of the object to find
     *
     * @return A ListenableFuture of type {@param O}, the object inside the Future can be null,
     *          that means that the object doesn't exists on the datastore
     */
    ListenableFuture<O> getObjectAsync(K key);

    /**
     * Finds a list of objects using a provided list of {@param keys} on the datastore
     * @param keys The keys to search on the datastore
     * @return The list of the objects found with the keys as id
     */
    List<O> getObjects(List<K> keys);

    /**
     * The principal behaviour of this method is the same as {@see getObjects(List<K>)}
     * but it's executed on another thread
     * @param keys The keys to find
     * @return A future object that contains the list of the objects found with the keys as id
     */
    ListenableFuture<List<O>> getObjectsAsync(List<K> keys);

    ListenableFuture<?> deleteObject(K key);

    ListenableFuture<?> save(O object);
}
