package org.baratinage.ui.lg;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class LgTest {

    @FunctionalInterface
    private static interface LgTranslator {
        public void setTranslatedText(Object obj);
    }

    // private static final Map<Object, LgTranslator> registered = new
    // WeakHashMap<>();

    private static final Map<Object, LgTranslator> registered = new WeakHashMap<>();

    // public static void register(Object object, LgTranslator translator) {
    // registered.put(object, translator);
    // }

    // public static void register(T object, Consumer<T> translator) {
    // registered.put(object, translator);
    // }

    static public <A> void register(A object, Consumer<A> translator) {
        registered.put(object, (o) -> {
            @SuppressWarnings("unchecked")
            A castedObj = (A) o;
            translator.accept(castedObj);
        });
    }

    public static void checkRegisteredObject() {
        int k = 0;
        int n = registered.size();
        System.out.println("============================");
        System.out.println("> There are " + n + " registered tranlsations");
        for (Object obj : registered.keySet()) {
            System.out.println("> Checking registered object: " + obj);
            LgTranslator T = registered.get(obj);
            T.setTranslatedText(obj);
            k++;
        }
    }

    public static void cleanup() {

    }
}
