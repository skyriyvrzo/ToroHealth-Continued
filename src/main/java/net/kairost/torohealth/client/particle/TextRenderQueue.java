package net.kairost.torohealth.client.particle;

import java.util.List;
import java.util.ArrayList;


public final class TextRenderQueue {
    private static final List<TextRenderEntry> QUEUE = new ArrayList<>();

    public static void submit(TextRenderEntry instance) {
        QUEUE.add(instance);
    }

    public static List<TextRenderEntry> consume() {
        List<TextRenderEntry> copy = List.copyOf(QUEUE);
        QUEUE.clear();
        return copy;
    }
}