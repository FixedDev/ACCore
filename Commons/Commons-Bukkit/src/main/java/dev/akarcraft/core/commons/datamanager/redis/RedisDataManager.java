package dev.akarcraft.core.commons.datamanager.redis;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.Gson;
import dev.akarcraft.core.commons.datamanager.DataManager;
import dev.akarcraft.core.commons.datamanager.IdGetter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RedisDataManager<K, O> implements DataManager<K, O> {

    private JedisPool jedisPool;
    private ListeningExecutorService executorService;

    private IdGetter<O, K> idGetter;

    private Gson gson;
    private Type type;

    private String namespace;

    public RedisDataManager(JedisPool pool, ListeningExecutorService service, TypeToken<O> objectType, Gson gson, String namespace, IdGetter<O, K> idGetter) {
        jedisPool = pool;
        executorService = service;

        this.idGetter = idGetter;

        this.gson = gson;
        type = objectType.getType();

        this.namespace = namespace;
    }

    @Override
    public Optional<O> getObject(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return getObject(key, jedis);
        }
    }

    private Optional<O> getObject(K key, Jedis jedis) {
        String redisKey = namespace + ":" + idGetter.toString(key);

        String jsonObject = jedis.get(redisKey);
        if (jsonObject == null || jsonObject.equals("nil")) {
            return Optional.empty();
        }

        O object = gson.fromJson(jsonObject, type);

        return Optional.of(object);
    }

    @Override
    public ListenableFuture<O> getObjectAsync(K key) {
        return executorService.submit(() -> getObject(key).orElse(null));
    }

    @Override
    public List<O> getObjects(List<K> keys) {
        List<O> objects = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            for (K key : keys) {
                getObject(key, jedis).ifPresent(objects::add);
            }
        }

        return objects;
    }

    @Override
    public ListenableFuture<List<O>> getObjectsAsync(List<K> keys) {
        return executorService.submit(() -> getObjects(keys));
    }

    @Override
    public ListenableFuture<?> deleteObject(K key) {
        return executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(idGetter.toString(key));
            }
        });
    }

    @Override
    public ListenableFuture<?> save(O object) {
        return executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String jsonObject = gson.toJson(object);
                String id = idGetter.toString(idGetter.getFromObject(object));

                jedis.set(namespace + ":" + id, jsonObject);
            }
        });
    }
}
