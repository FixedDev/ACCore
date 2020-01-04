package dev.akarcraft.core.commons.datamanager.mongo;

import com.google.gson.Gson;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

public class GsonCodec<T> implements Codec<T> {

    private Class<T> type;
    private Gson gson;
    private Codec<RawBsonDocument> bsonDocumentCodec;

    public GsonCodec(Class<T> type, Gson gson, CodecRegistry codecRegistry) {
        this.type = type;
        this.gson = gson;
        bsonDocumentCodec = codecRegistry.get(RawBsonDocument.class);
    }

    @Override
    public T decode(BsonReader bsonReader, DecoderContext decoderContext) {
        RawBsonDocument raw = bsonDocumentCodec.decode(bsonReader, decoderContext);
        
        return gson.fromJson(raw.toJson(), type);
    }

    @Override
    public void encode(BsonWriter bsonWriter, T object, EncoderContext encoderContext) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        gson.toJson(object, type, writer);

        RawBsonDocument bsonDocument = new RawBsonDocument(outputStream.toByteArray());
        bsonDocumentCodec.encode(bsonWriter, bsonDocument, encoderContext);
    }

    @Override
    public Class<T> getEncoderClass() {
        return type;
    }
}
