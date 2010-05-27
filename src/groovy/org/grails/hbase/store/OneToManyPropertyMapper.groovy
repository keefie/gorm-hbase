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
import org.grails.hbase.associations.IdSet
/**
 * One-to-Many property types are persisted to HBase with this strategy
 * @author Keith Thomas, keith.thomas@gmail.com
 * created on April 9th, 2010
 */
class OneToManyPropertyMapper implements DomainPropertyMapper {

    protected OneToManyPropertyMapper(SaveCounters counters, ByteArrayConverter byteArrayConverter, ReferenceTable referenceTable ) {
        this.counters = counters
        this.byteArrayConverter = byteArrayConverter
        this.referenceTable = referenceTable
    }

    public SaveCounters updateDatabaseRow(Put p, Delete d, Object instance, GrailsDomainClassProperty prop, Object row) {
        def fieldName = prop.name
        
        def assocIdListFieldName = "${fieldName}__association__id__list__"
        Set rowValue = row?."${assocIdListFieldName}"
        def assocIdListFieldValue = [] as Set
        Class referenceeType = prop?.getReferencedDomainClass()?.clazz

        // Save new associations as references
        Set persistedAssociations = [] as Set
        if (rowValue) persistedAssociations = new HashSet(rowValue)

        instance."${assocIdListFieldName}".each { setItem ->
            def assocId

            if (setItem?.class == referenceeType && setItem.id) assocId = setItem.id
            else assocId = setItem

            if (assocId?.class != referenceeType) {
                assocIdListFieldValue.add(assocId)
                def savedId = assocId

                if (!persistedAssociations?.remove(savedId)) {
                    counters.updates++
                    byte[] byteReferenceId = byteArrayConverter.getValueToPersist(assocId, fieldName)
                    referenceTable.saveReference(instance, prop, byteReferenceId)
                }
            }
        }

        // Anything left in the Set of id's read from the database is no longer required
        persistedAssociations.each { oldAssocId ->
            counters.updates++
            byte[] oldByteValue = byteArrayConverter.getValueToPersist(oldAssocId, fieldName)
            referenceTable.removeReference(instance, prop, oldByteValue)
        }

        if (counters.updates) {
            byte[] byteValue = byteArrayConverter.getValueToPersist(new IdSet(assocIdListFieldValue))
            def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
            p.add(Constants.DEFAULT_ASSOCIATION_FAMILY, Bytes.toBytes(columnName), byteValue)
        }

        return this.counters
    }

    private SaveCounters counters
    private ByteArrayConverter byteArrayConverter
    private ReferenceTable referenceTable

    private static final Log LOG = LogFactory.getLog(OneToManyPropertyMapper.class)
}

