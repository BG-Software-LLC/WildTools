package com.bgsoftware.wildtools.utils.reflections;

import java.lang.reflect.Constructor;
import java.util.function.Predicate;

public final class ReflectConstructor<T> {

    private final Constructor<?> constructor;

    public ReflectConstructor(Class<?> clazz, Class<?>... parameterTypes){
        this.constructor = getConstructor(clazz, parameterTypes);
    }

    public ReflectConstructor(Class<?> clazz, Predicate<Constructor<?>> predicate){
        this.constructor = getConstructor(clazz, predicate);
    }

    public T newInstance(Object... args){
        Object result = null;

        try{
            if(constructor != null)
                result = constructor.newInstance(args);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        //noinspection unchecked
        return result == null ? null : (T) result;
    }

    public boolean isValid(){
        return constructor != null;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes){
        Constructor<?> constructor = null;

        if(clazz != null) {
            try {
                constructor = clazz.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
            } catch (Exception ignored) {}
        }

        return constructor;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Predicate<Constructor<?>> predicate){
        if(clazz != null){
            try {
                for(Constructor<?> constructor : clazz.getDeclaredConstructors()){
                    if(predicate.test(constructor)) {
                        constructor.setAccessible(true);
                        return constructor;
                    }
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

}
