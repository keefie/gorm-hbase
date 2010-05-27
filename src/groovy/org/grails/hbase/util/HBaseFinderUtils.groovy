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

import org.apache.commons.lang.math.NumberUtils

import org.apache.hadoop.hbase.filter.Filter

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.FinderFilterList

import org.codehaus.groovy.grails.commons.GrailsDomainClass
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

    public static boolean hasQuery(String methodName, Object[] args) {
        if (methodName != "find" && methodName != "findAll") return false
        if (args && args[0] instanceof String) return true
        if (args && args[0] instanceof GString) return true

        return false
    }

    public static hasNativeHBaseFilter(methodName, Object[] args) {
        if (methodName != "find" && methodName != "findBy") return false
        if (args && args?.length > 0 && args[0] instanceof Filter) return true
        return false
    }

    public static Object refineNumber(GrailsDomainClass domainClass, String propertyName, String propertyValue) {
        def property = domainClass.getPropertyByName(propertyName)
        Class type = property.getType()
            
        if (type.isPrimitive()) {
            String className = type?.name.substring(0, 1).toUpperCase() + type?.name.substring(1)
            // Integers don't follow the same concention as other primitives ... grrr
            if (type.name.equals("int")) type = Class.forName("java.lang.Integer")
            else type = Class.forName("java.lang.$className")
        }
        
        def objectValue = type.newInstance([propertyValue] as Object[])

        // Integers don't follow the same concention as other primitives ... grrr
        if (type?.name.equals("java.lang.Integer")) return objectValue?.intValue()

        // Don't manipulate any java.math numbers any further
        if (type?.name.startsWith("java.math") ) return objectValue

        def primitiveName = HBaseFinderUtils.getUnqualifiedClassName(type).toLowerCase()

        objectValue?."${primitiveName}Value"()
    }

    public static boolean isNumber(value) {
        value instanceof Number
    }

    public static boolean isDate(GrailsDomainClass domainClass, String propertyName) {
        def property = domainClass.getPropertyByName(propertyName)
        Class type = property.getType()

        if (type?.name.equals('java.util.Date')
            || type?.name.equals('java.sql.Date')
            || type?.name.equals('java.sql.Time')
            || type?.name.equals('java.sql.Timestamp')) return true
        return false
    }

    public static boolean isString(value) {
        value instanceof org.codehaus.groovy.runtime.GStringImpl || value instanceof String
    }

    public static String getUnqualifiedClassName(Class type) {
        def i = type?.name.lastIndexOf('.') + 1
        type.name.substring(i)
    }
}

