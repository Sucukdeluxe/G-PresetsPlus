package extension.logger;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import utils.Messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Logger {

    private static class Entry {
        final String key;
        final Object[] args;
        final String literal;
        final String className;

        Entry(String key, Object[] args, String literal, String className) {
            this.key = key;
            this.args = args;
            this.literal = literal;
            this.className = className;
        }

        String render() {
            return key == null ? literal : Messages.get(key, args) + "\n";
        }
    }

    private StyleClassedTextArea area;
    private volatile boolean initialized = false;
    private final List<Element> appendOnLoad = new ArrayList<>();
    private final List<Entry> history = new ArrayList<>();

    public void initialize(BorderPane borderPane) {
        area = new StyleClassedTextArea();
        area.getStyleClass().add("themed-background");
        area.setEditable(false);

        VirtualizedScrollPane<StyleClassedTextArea> vsPane = new VirtualizedScrollPane<>(area);
        borderPane.setCenter(vsPane);

        synchronized (appendOnLoad) {
            initialized = true;
            if (!appendOnLoad.isEmpty()) {
                appendLog(appendOnLoad);
                appendOnLoad.clear();
            }
        }
    }

    private synchronized void appendLog(List<Element> elements) {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder();
            StyleSpansBuilder<Collection<String>> styleSpansBuilder = new StyleSpansBuilder<>(0);

            for (Element element : elements) {
                sb.append(element.text);
                styleSpansBuilder.add(Collections.singleton(element.className), element.text.length());
            }

            int oldLen = area.getLength();
            area.appendText(sb.toString());
            area.setStyleSpans(oldLen, styleSpansBuilder.create());

            area.moveTo(area.getLength());
            area.requestFollowCaret();
        });
    }

    public void log(String s, String className) {
        logNoNewline(s + "\n", className);
    }

    public void logKey(String key, String className, Object... args) {
        write(new Entry(key, args, null, className.toLowerCase()));
    }

    public void logNoNewline(String s, String className) {
        write(new Entry(null, null, s, className.toLowerCase()));
    }

    private void write(Entry entry) {
        List<Element> elements = new ArrayList<>();
        elements.add(new Element(cleanTextContent(entry.render()), entry.className));

        synchronized (appendOnLoad) {
            history.add(entry);
            if (initialized) {
                appendLog(elements);
            } else {
                appendOnLoad.addAll(elements);
            }
        }
    }

    public void retranslate() {
        List<Entry> snapshot;
        synchronized (appendOnLoad) {
            if (history.isEmpty()) {
                return;
            }
            snapshot = new ArrayList<>(history);
            if (!initialized) {
                appendOnLoad.clear();
                for (Entry entry : snapshot) {
                    appendOnLoad.add(new Element(cleanTextContent(entry.render()), entry.className));
                }
                return;
            }
        }

        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder();
            StyleSpansBuilder<Collection<String>> styleSpansBuilder = new StyleSpansBuilder<>(0);
            for (Entry entry : snapshot) {
                String text = cleanTextContent(entry.render());
                sb.append(text);
                styleSpansBuilder.add(Collections.singleton(entry.className), text.length());
            }

            area.replaceText(sb.toString());
            if (sb.length() > 0) {
                area.setStyleSpans(0, styleSpansBuilder.create());
            }
            area.moveTo(area.getLength());
            area.requestFollowCaret();
        });
    }

    private static String cleanTextContent(String text) {
        return text.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
    }
}
