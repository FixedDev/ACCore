package es.akarcraft.core.api.datamanager;

public interface IdGetter<O, K> {
    K getFromObject(O object);
}
