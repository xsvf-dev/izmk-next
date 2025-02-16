/*
 * Copyright 2023 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.parsers.json.gson;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Adapts a quilt-parsers' {@link org.quiltmc.parsers.json.JsonWriter} to be used with {@link com.google.gson.Gson}
 */
public class GsonWriter extends JsonWriter {
    private final org.quiltmc.parsers.json.JsonWriter delegate;

    public GsonWriter(org.quiltmc.parsers.json.JsonWriter writer) {
        super(Writer.nullWriter());
        this.delegate = writer;
    }

    public org.quiltmc.parsers.json.JsonWriter getDelegate() {
        return delegate;
    }

    @Override
    public JsonWriter beginArray() throws IOException {
        delegate.beginArray();
        return this;
    }

    @Override
    public JsonWriter endArray() throws IOException {
        delegate.endArray();
        return this;
    }

    @Override
    public JsonWriter beginObject() throws IOException {
        delegate.beginObject();
        return this;
    }

    @Override
    public JsonWriter endObject() throws IOException {
        delegate.endObject();
        return this;
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        delegate.name(name);
        return this;
    }

    @Override
    public JsonWriter value(String value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriter jsonValue(String value) throws IOException {
        delegate.jsonValue(value);
        return this;
    }

    @Override
    public JsonWriter nullValue() throws IOException {
        delegate.nullValue();
        return this;
    }

    @Override
    public JsonWriter value(boolean value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriter value(Boolean value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriter value(float value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriter value(double value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriter value(long value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public JsonWriter value(Number value) throws IOException {
        delegate.value(value);
        return this;
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        super.close();
    }
}
