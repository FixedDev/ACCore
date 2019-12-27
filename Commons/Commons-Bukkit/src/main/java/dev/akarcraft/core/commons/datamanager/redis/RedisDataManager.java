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

/**
 * The RedisDataManager has a limitation, it is that only supports String as key, nothing else
 *
 * @param <O> The object type
 */
public class RedisDataManager<O> implements DataManager<String, O> {

    private JedisPool jedisPool;
    private ListeningExecutorService executorService;

    private IdGetter<O, String> idGetter;

    private Gson gson;
    private Type type;

    private String namespace;

    public RedisDataManager(JedisPool pool, ListeningExecutorService service, TypeToken<O> objectType, Gson gson, String namespace, IdGetter<O, String> idGetter) {
        jedisPool = pool;
        executorService = service;

        this.idGetter = idGetter;

        this.gson = gson;
        type = objectType.getType();

        this.namespace = namespace;
    }

    @Override
    public Optional<O> getObject(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return getObject(key, jedis);
        }
    }

    private Optional<O> getObject(String key, Jedis jedis) {
        String redisKey = namespace + ":" + key;

        String jsonObject = jedis.get(redisKey);
        if (jsonObject == null || jsonObject.equals("nil")) {
            return Optional.empty();
        }

        O object = gson.fromJson(jsonObject, type);

        return Optional.of(object);
    }

    @Override
    public ListenableFuture<O> getObjectAsync(String key) {
        return executorService.submit(() -> getObject(key).orElse(null));
    }

    @Override
    public List<O> getObjects(List<String> keys) {
        List<O> objects = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            for (String key : keys) {
                getObject(key, jedis).ifPresent(objects::add);
            }
        }

        return objects;
    }

    @Override
    public ListenableFuture<List<O>> getObjectsAsync(List<String> keys) {
        return executorService.submit(() -> getObjects(keys));
    }

    @Override
    public ListenableFuture<?> deleteObject(String key) {
        return executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(key);
            }
        });
    }

    @Override
    public ListenableFuture<?> save(O object) {
        return executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String jsonObject = gson.toJson(object);
                String id = idGetter.getFromObject(object);

                jedis.set(namespace + ":" + id, jsonObject);
            }
        });
    }
}
