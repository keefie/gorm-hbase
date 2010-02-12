/**
 * Copyright 2009-2010 Keith Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.grails.hbase.store

import org.apache.hadoop.hbase.util.Bytes

/**
 * Constant default values
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 1, 2009
 * Time: 2:32:37 PM
 */

public class Constants {

    public static final String DEFAULT_CONTROL_FAMILY_STRING = "control"
    public static final byte[] DEFAULT_CONTROL_FAMILY = Bytes.toBytes(DEFAULT_CONTROL_FAMILY_STRING)

    public static final String DEFAULT_DATA_FAMILY_STRING = "data"
    public static final byte[] DEFAULT_DATA_FAMILY = Bytes.toBytes(DEFAULT_DATA_FAMILY_STRING)

    public static final String DEFAULT_ASSOCIATION_FAMILY_STRING = "associations"
    public static final byte[] DEFAULT_ASSOCIATION_FAMILY = Bytes.toBytes(DEFAULT_ASSOCIATION_FAMILY_STRING)

    public static final String DEFAULT_REFERENCE_FAMILY_STRING = "references"
    public static final byte[] DEFAULT_REFERENCE_FAMILY = Bytes.toBytes(DEFAULT_REFERENCE_FAMILY_STRING)

    public static final byte[] DEFAULT_REFERENCE_QUALIFIER = Bytes.toBytes("reference_map")

    public static final String DEFAULT_FAMILY_NAME_DELIMITER = ":"

    public static final String DEFAULT_SEQUENCE_FAMILY_STRING = "sequence"
    public static final byte[] DEFAULT_SEQUENCE_FAMILY = Bytes.toBytes(DEFAULT_SEQUENCE_FAMILY_STRING)
    public static final byte[] DEFAULT_SEQUENCE_QUALIFIER = Bytes.toBytes("next_id")

    public static final byte[] DEFAULT_VERSION_QUALIFIER = Bytes.toBytes("row_version")

    private static final List<String> DEFAULT_FAMILIES = [
        DEFAULT_CONTROL_FAMILY_STRING,
        DEFAULT_DATA_FAMILY_STRING,
        DEFAULT_ASSOCIATION_FAMILY_STRING
    ]
}