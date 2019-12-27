package dev.akarcraft.core.commons.datamanager.redis;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.Gson;
import dev.akarcraft.core.commons.datamanager.DataManager;
import dev.akarcraft.core.commons.datamanager.DelegateCachedDataManager;
import dev.akarcraft.core.commons.datamanager.IdGetter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Type;
import java.util.Optional;

public class RedisCacheDataManager<K, O> extends DelegateCachedDataManager<K, O> {
    private JedisPool jedisPool;
    private ListeningExecutorService executorService;

    private IdGetter<O, K> idGetter;

    private Gson gson;
    private Type type;

    private String namespace;

    public RedisCacheDataManager(DataManager<K, O> delegate, JedisPool pool, ListeningExecutorService service, TypeToken<O> objectType, Gson gson, String namespace, IdGetter<O, K> idGetter) {
        super(delegate);

        jedisPool = pool;
        executorService = service;

        this.idGetter = idGetter;

        this.gson = gson;
        type = objectType.getType();

        this.namespace = namespace;
    }

    @Override
    public Optional<O> getIfCached(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String redisKey = namespace + ":" + idGetter.toString(key);

            String jsonObject = jedis.get(redisKey);
            if (jsonObject == null || jsonObject.equals("nil")) {
                return Optional.empty();
            }

            O object = gson.fromJson(jsonObject, type);

            return Optional.of(object);
        }
    }

    @Override
    public ListenableFuture<O> getOrFind(K key) {
        return executorService.submit(() ->
                getIfCached(key).orElseGet(() -> {
                    Optional<O> optionalObject = getObject(key);
                    optionalObject.ifPresent(this::cache);

                    return optionalObject.orElse(null);
                })
        );
    }

    @Override
    public void cache(O object) {
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonObject = gson.toJson(object);

            String id = idGetter.toString(idGetter.getFromObject(object));
            String redisId = namespace + ":" + id;

            jedis.set(redisId, jsonObject);
            // Five minutes cache
            jedis.expire(redisId, 60 * 5);
        }
    }

    @Override
    public void invalidate(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String id = idGetter.toString(key);
            String redisId = namespace + ":" + id;

            jedis.del(redisId);
        }
    }

    @Override
    public void refresh(K key) {
        executorService.submit(() -> {
           getObject(key).ifPresent(this::cache);
        });
    }
}
