package net.kairost.torohealth.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class ColorJsonAdapter extends TypeAdapter<Integer> {

    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // Write as hex string with leading # and uppercase letters (e.g. "#FF00FF")
        out.value(String.format("#%06X", value & 0xFFFFFF));
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
        // Handle null values in JSON
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null; // or return a default color if your config fields are non-nullable
        }

        String s = in.nextString().trim();

        try {
            if (s.startsWith("#")) {
                // Hex format: "#RRGGBB" (case-insensitive)
                return Integer.parseInt(s.substring(1), 16);
            } else {
                // Fallback: raw decimal integer (e.g. "16711680" for red)
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException e) {
            // Log the error (visible in game logs) and fall back to a safe default
            System.err.println("ToroHealth: Failed to parse color value '" + s + "', using default white (0xFFFFFF)");
            return 0xFFFFFF; // White as safe default; change if needed (e.g. 0xFF0000 for red)
        }
    }
}