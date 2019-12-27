package dev.akarcraft.core.commons.datamanager;

public class SimpleIdGetter implements IdGetter<IdObject, String> {
    @Override
    public String getFromObject(IdObject object) {
        return object.id();
    }

    @Override
    public String toString(String key) {
        return key;
    }
}
