package com.gpiay.cpm.util;

import com.google.gson.stream.JsonReader;
import com.gpiay.cpm.util.function.ExceptionConsumer;
import com.gpiay.cpm.util.function.ExceptionRunnable;

import java.io.IOException;

public class JsonHelper {
    public static void readArray(JsonReader reader, ExceptionRunnable<IOException> consumer) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            consumer.run();
        }

        reader.endArray();
    }

    public static void readObject(JsonReader reader, ExceptionConsumer<String, IOException> consumer) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            consumer.accept(reader.nextName());
        }

        reader.endObject();
    }
}
