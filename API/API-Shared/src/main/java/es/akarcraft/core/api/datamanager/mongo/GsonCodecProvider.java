package es.akarcraft.core.api.datamanager.mongo;

import com.google.gson.Gson;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class GsonCodecProvider implements CodecProvider {
    private Gson gson;

    public GsonCodecProvider(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry codecRegistry) {
        return new GsonCodec<>(clazz, gson, codecRegistry);
    }
}
