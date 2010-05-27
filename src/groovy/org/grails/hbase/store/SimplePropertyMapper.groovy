/**
 * Copyright 2009 Keith Thomas
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

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Delete

import org.grails.hbase.util.HBaseNameUtils
/**
 * Basic property types are persisted to HBase with this strategy
 * @author Keith Thomas, keith.thomas@gmail.com
 * created on April 9th, 2010
 */
class SimplePropertyMapper implements DomainPropertyMapper {

    protected SimplePropertyMapper(SaveCounters counters, ByteArrayConverter byteArrayConverter) {
        this.counters = counters
        this.byteArrayConverter = byteArrayConverter
    }

    public SaveCounters updateDatabaseRow(Put p, Delete d, Object instance, GrailsDomainClassProperty prop, Object row) {
        def fieldName = prop.name
        def fieldValue = instance."$fieldName"
        def rowValue = row?."$fieldName"
        
        if ((fieldValue == true || fieldValue == false || fieldValue) && fieldValue != rowValue) {
            this.counters.updates++
            byte[] byteValue = byteArrayConverter.getValueToPersist(fieldValue, fieldName)
            def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
            p.add(Constants.DEFAULT_DATA_FAMILY, Bytes.toBytes(columnName), byteValue)
        }
        else if (!fieldValue && rowValue) {
            this.counters.deletes++
            def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
            d?.deleteColumn(Constants.DEFAULT_DATA_FAMILY, Bytes.toBytes(columnName))
        }

        return this.counters
    }

    private SaveCounters counters 
    private ByteArrayConverter byteArrayConverter
}

