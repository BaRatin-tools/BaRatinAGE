package org.baratinage.translation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import org.baratinage.ui.plot.PlotItem;

public class T {

    private static interface ITranslator {
        public void setTranslatedText(Object object);
    }

    private static record TranslatorRecord(ITranslator translator, boolean lasting) {
    };

    static private TRessources resources;
    static private Locale currentLocale;
    static private WeakHashMap<Object, List<TranslatorRecord>> registered;

    static public void init() {
        resources = new TRessources();
        currentLocale = Locale.getDefault();
        registered = new WeakHashMap<>();
    }

    static public void reloadResources() {
        resources = new TRessources();
        updateRegisteredObjects();
    }

    static public void setLocale(String localeKey) {
        setLocale(Locale.forLanguageTag(localeKey));
    }

    static public void setLocale(Locale locale) {
        currentLocale = locale;
        updateRegisteredObjects();
    }

    static public Locale getLocale() {
        return currentLocale;
    }

    static public String getLocaleKey() {
        return currentLocale.getLanguage();
    }

    static private void register(Object o, ITranslator translator, boolean lasting) {
        TranslatorRecord newTr = new TranslatorRecord(translator, lasting);
        List<TranslatorRecord> trList = registered.containsKey(o) ? registered.get(o) : new ArrayList<>();
        if (!lasting) {
            trList.removeIf(tr -> !tr.lasting);
        }
        trList.add(newTr);
        registered.put(o, trList);
    }

    static public void printStats() {
        System.out.println("\n-------------------------------------------------\n");
        System.out.println("There are " + registered.size() + " registered objects with translators\n");
        for (Object o : registered.keySet()) {
            String objStr = o.toString();
            objStr = objStr.substring(0, Math.min(objStr.length(), 50));
            System.out.println("Object: " + objStr);
        }
        System.out.println("\n-------------------------------------------------\n");
    }

    static public <A> void tLasting(A object, Consumer<A> translator) {
        register(object, (o) -> {
            @SuppressWarnings("unchecked")
            A castedObj = (A) o;
            translator.accept(castedObj);
        }, true);
        translator.accept(object);
    }

    // IMPORTANT NOTE: if the translator consumer needs the 'object' it should
    // use the varbiable provided as argument, not the original reference
    // located in the encapsulating object (where T.t() is called)
    static public <A> void t(A object, Consumer<A> translator) {
        register(object, (o) -> {
            @SuppressWarnings("unchecked")
            A castedObj = (A) o;
            translator.accept(castedObj);
        }, false);
        // translator.accept(object);
        updateRegisteredObject(object);
    }

    static public void t(JLabel label, boolean useHtml, String key, Object... args) {
        t(label, (l) -> {
            l.setText(useHtml ? html(key, args) : text(key, args));
        });
    }

    static public void t(AbstractButton button, boolean useHtml, String key, Object... args) {
        t(button, (btn) -> {
            btn.setText(useHtml ? html(key, args) : text(key, args));
        });
    }

    static public void t(PlotItem plotItem, String key, Object... args) {
        t(plotItem, (pltItem) -> {
            pltItem.setLabel(text(key, args));
        });
    }

    static public void updateRegisteredObjects() {
        for (Object obj : registered.keySet()) {
            List<TranslatorRecord> trList = registered.get(obj);
            for (TranslatorRecord tr : trList) {
                tr.translator.setTranslatedText(obj);
            }
        }
    }

    static public void updateRegisteredObject(Object obj) {
        if (registered.containsKey(obj)) {
            for (TranslatorRecord tr : registered.get(obj)) {
                tr.translator.setTranslatedText(obj);
            }
        }
    }

    static public String text(String itemKey) {
        return resources.getTranslation(currentLocale.getLanguage(), itemKey);
    }

    static public String text(String itemKey, Object... args) {
        String rawText = text(itemKey);
        // escaping single quotes, see: https://stackoverflow.com/q/17544794
        rawText = rawText.replaceAll("(?<!')'(?!')", "''");
        MessageFormat msgFormat = new MessageFormat(rawText);
        msgFormat.setLocale(currentLocale);
        String formattedText = msgFormat.format(args);
        return formattedText;
    }

    static public String html(String itemKey) {
        return "<html><nobr>" + text(itemKey) + "</nobr></html>";
    }

    static public String html(String itemKey, Object... args) {
        return "<html><nobr>" + text(itemKey, args) + "</nobr></html>";
    }

    static public List<String> getAvailableLocales() {
        return resources.getAvailableLocales();
    }
}
