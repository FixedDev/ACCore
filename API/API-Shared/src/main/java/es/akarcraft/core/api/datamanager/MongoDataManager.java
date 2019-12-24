package es.akarcraft.core.api.datamanager;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoDataManager<K, O> implements DataManager<K, O> {
    private MongoCollection<O> collection;
    private ListeningExecutorService executorService;

    private IdGetter<O, K> idGetter;

    @SuppressWarnings("unchecked")
    public MongoDataManager(MongoDatabase database, ListeningExecutorService service, TypeToken<O> objectType, String namespace, IdGetter<O, K> idGetter) {
        this.idGetter = idGetter;

        this.collection = database.getCollection(namespace, (Class<O>) objectType.getRawType());
        executorService = service;
    }

    @Override
    public Optional<O> getObject(K key) {
        return Optional.ofNullable(collection.find(Filters.eq("_id", key)).first());
    }

    @Override
    public ListenableFuture<O> getObjectAsync(K key) {
        return executorService.submit(() -> getObject(key).orElse(null));
    }

    @Override
    public List<O> getObjects(List<K> keys) {
        List<O> foundObjects = new ArrayList<>();
        collection.find(Filters.in("_id", keys)).iterator().forEachRemaining(foundObjects::add);

        return foundObjects;
    }

    @Override
    public ListenableFuture<List<O>> getObjectsAsync(List<K> keys) {
        return executorService.submit(() -> getObjects(keys));
    }

    @Override
    public ListenableFuture<?> deleteObject(K key) {
        return executorService.submit(() -> collection.deleteOne(Filters.eq("_id", key)));
    }

    @Override
    public ListenableFuture<?> save(O object) {
        return executorService.submit(() -> collection.replaceOne(Filters.eq("_id", idGetter.getFromObject(object)), object, new ReplaceOptions().upsert(true)));

    }
}
