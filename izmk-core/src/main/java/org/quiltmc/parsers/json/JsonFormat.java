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

package org.quiltmc.parsers.json;

/**
 * The JSON-family format to be used during parsing or writing.
 */
public enum JsonFormat {
    /**
     * The standard, <a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a> JSON.
     */
    JSON,
    /**
     * JSON, but with comments and trailing commas.
     */
    JSONC,
    /**
     * The <a href="https://json5.org/">JSON5</a> specification. Comments, bare keys,
     * more floating point numbers, and more.
      */
    JSON5
}
