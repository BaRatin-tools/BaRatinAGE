package org.baratinage.translation;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import org.baratinage.utils.ConsoleLogger;

public class T {

    static private class TranslatableList extends ArrayList<Translatable> {

    }

    static private Map<Object, TranslatableList> translatables;
    static private TResources resources;
    static private Locale currentLocale;
    static private final String NO_OWNER = "NO_OWNER";

    static public void init() {
        resources = new TResources();
        currentLocale = Locale.getDefault();
        translatables = new WeakHashMap<>();
        hierarchy = new WeakHashMap<>();
    }

    static public void reset() {
        translatables.clear();
    }

    static public void reloadResources() {
        resources = new TResources();
        updateTranslations();
    }

    static public void setLocale(String localeKey) {
        setLocale(Locale.forLanguageTag(localeKey));
    }

    static public void setLocale(Locale locale) {
        currentLocale = locale;
        Locale.setDefault(locale);
        updateTranslations();
    }

    static public Locale getLocale() {
        return currentLocale;
    }

    static public String getLocaleKey() {
        return currentLocale.getLanguage();
    }

    static public String text(String itemKey) {
        // return resourcesOLD.getTranslation(currentLocale.getLanguage(), itemKey);
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
        return html(itemKey, new Object[0]);
    }

    static public String html(String itemKey, Object... args) {
        return "<html>" + text(itemKey, args) + "</html>";
    }

    static public Locale[] getAvailableLocales() {
        return resources.getAllLocales();
    }

    static public void permanent(Translatable translatable) {
        t(NO_OWNER, translatable);
    }

    static public void t(Object owner, Translatable translatable) {
        TranslatableList trList = new TranslatableList();
        if (translatables.containsKey(owner)) {
            trList = translatables.get(owner);
        }
        trList.add(translatable);
        translatables.put(owner, trList);
        translatable.translate();
    }

    static public void t(Object owner, JLabel label, boolean useHtml, String key, Object... args) {
        t(owner, () -> {
            label.setText(useHtml ? html(key, args) : text(key, args));
        });
    }

    static public void t(Object owner, AbstractButton button, boolean useHtml, String key, Object... args) {
        t(owner, () -> {
            button.setText(useHtml ? html(key, args) : text(key, args));
        });
    }

    static public void updateTranslation(Object owner) {
        if (translatables.containsKey(owner)) {
            TranslatableList list = translatables.get(owner);
            for (Translatable tr : list) {
                tr.translate();
            }
        }
    }

    static public void updateTranslations() {
        for (Object object : translatables.keySet()) {
            TranslatableList list = translatables.get(object);
            if (list == null) {
                // Note: I've had some issue if Object is an array list that
                // is emptied with clear() after having elements added to it....
                ConsoleLogger.error("translatables list is null for the following object > " +
                        object + " (" + object.getClass() + ")");
                continue;
            }
            for (Translatable tr : list) {
                tr.translate();
            }
        }
    }

    static public void printStats(boolean printList) {
        printTranslatableStats(printList);
        printHierachyStats(printList);
    }

    static public void printTranslatableStats(boolean printList) {
        int nChildren = 0;
        for (Object o : translatables.keySet()) {
            nChildren += translatables.get(o).size();
        }
        ConsoleLogger.log("There are " + translatables.size() + " translatables owners (" + nChildren + ").");
        if (printList) {
            for (Object o : translatables.keySet()) {
                String oStr = o.toString();
                oStr = oStr.substring(0, Math.min(oStr.length(), 70));
                int n = translatables.get(o).size();
                ConsoleLogger.log("> " + oStr + "  >  " + n + " translatable(s).");
            }
        }
    }

    static public void printHierachyStats(boolean printList) {
        int nChildren = 0;
        for (Object o : hierarchy.keySet()) {
            nChildren += hierarchy.get(o).size();
        }
        ConsoleLogger.log(
                "There are " + hierarchy.size() + " translatable owners with children owners (" + nChildren + ").");
        if (printList) {
            for (Object o : hierarchy.keySet()) {
                String oStr = o.toString();
                oStr = oStr.substring(0, Math.min(oStr.length(), 70));
                int n = hierarchy.get(o).size();
                ConsoleLogger.log("> " + oStr + "  >  " + (n < 2 ? n + " child." : n + " children."));
            }
        }
    }

    static private Map<Object, List<WeakReference<Object>>> hierarchy;

    static public void updateHierarchy(Object parent, Object child) {
        List<WeakReference<Object>> children = hierarchy.containsKey(parent) ? hierarchy.get(parent)
                : new ArrayList<>();
        children.add(new WeakReference<Object>(child));
        hierarchy.put(parent, children);
    }

    static public void clear(Object owner) {
        clearOwnerRecursively(owner);
        // remove owners from children list of other owners
        clearOwnerFromOthersChildrenList(owner);
    }

    static private void clearOwnerRecursively(Object owner) {
        // remove owner and all its children (recursively)
        if (hierarchy.containsKey(owner)) {
            List<WeakReference<Object>> children = hierarchy.get(owner);
            for (WeakReference<Object> childRef : children) {
                Object child = childRef.get();
                if (child != null) {
                    clearOwnerRecursively(child);
                }
            }
            hierarchy.remove(owner);
        }
        // remove translatables associated with the owner
        if (translatables.containsKey(owner)) {
            translatables.remove(owner);
        }
    }

    static private void clearOwnerFromOthersChildrenList(Object owner) {
        for (Object o : hierarchy.keySet()) {
            List<WeakReference<Object>> children = hierarchy.get(o);
            children.removeIf(weakRef -> {
                Object wro = weakRef.get();
                if (wro != null && wro.equals(owner)) {
                    return true;
                }
                return false;
            });
        }
    }
}
