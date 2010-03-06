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
package org.grails.hbase.util

import org.apache.hadoop.hbase.filter.Filter

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.FinderFilterList
/**
 * Utility class for simple logic related to finders and their filters
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Oct 30, 2009
 * Time: 6:11:25 PM
 */

class HBaseFinderUtils {

    public static boolean hasFilter(Object[] args) {
        // Native HBase filters
        if (args && args[0] instanceof Filter) return true

        // Gorm-hbase plugin filters
        if (args && (args[0] instanceof FinderFilter || args[0] instanceof FinderFilterList)) return true

        // No filters
        return false
    }

    public static hasNativeHBaseFilter(methodName, Object[] args) {
        if (methodName != "find" && methodName != "findBy") return false
        if (args && args?.length > 0 && !(args[0] instanceof Filter)) return true
        return false
    }
}

