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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Delete

import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.associations.ReferenceTable
/**
 * One-to-One property types are persisted to HBase with this strategy
 * @author Keith Thomas, keith.thomas@gmail.com
 * created on April 9th, 2010
 */
class OneToOnePropertyMapper implements DomainPropertyMapper {

    protected OneToOnePropertyMapper(SaveCounters counters, ByteArrayConverter byteArrayConverter, ReferenceTable referenceTable ) {
        this.counters = counters
        this.byteArrayConverter = byteArrayConverter
        this.referenceTable = referenceTable
    }

    public SaveCounters updateDatabaseRow(Put p, Delete d, Object instance, GrailsDomainClassProperty prop, Object row) {
        def fieldName = prop.name
        
        // TODO remove hard coding of this String in multple places'
        def rowValue = row?."${fieldName}__association__id__"
        def assocIdFieldName = "${fieldName}Id"
        def assocIdFieldValue = instance."${assocIdFieldName}"
        instance."${fieldName}__association__id__" = assocIdFieldValue
                    
        LOG.debug("Old association id=${rowValue}, new association id=${assocIdFieldValue}")

        if (assocIdFieldValue != rowValue) {
            this.counters.updates++

            if (rowValue) {
                byte[] oldByteValue = byteArrayConverter.getValueToPersist(rowValue, fieldName)
                referenceTable.removeReference(instance, prop, oldByteValue)
            }
                        
            byte[] byteValue = byteArrayConverter.getValueToPersist(assocIdFieldValue, fieldName)
            referenceTable.saveReference(instance, prop, byteValue)
            def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
            p.add(Constants.DEFAULT_ASSOCIATION_FAMILY, Bytes.toBytes(columnName), byteValue)
        }

        return this.counters
    }

    private SaveCounters counters
    private ByteArrayConverter byteArrayConverter
    private ReferenceTable referenceTable

    private static final Log LOG = LogFactory.getLog(OneToOnePropertyMapper.class)
}

