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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.client.Result

import org.codehaus.groovy.grails.commons.GrailsDomainClass

import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.associations.IdSet
/**
 * Maps HBase data to Grails associations class instances and vice versa
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 4, 2009
 * Time: 2:51:20 PM
 */
public class DomainClassMapper {

    def createDomainClass(Result hbaseData, Class domainClazz) {
        GrailsDomainClass grailsClass = HBaseNameUtils.getDomainClass(domainClazz.name)

        def instance = domainClazz.newInstance()
        instance.id = rowIdGenerator.toInstanceForm(hbaseData.getRow())

        byte[] versionBytes = hbaseData.getValue(Constants.DEFAULT_CONTROL_FAMILY,
            Constants.DEFAULT_VERSION_QUALIFIER)
        long version = Bytes.toLong(versionBytes)
        instance.version = version

        hbaseData.getFamilyMap(Constants.DEFAULT_DATA_FAMILY).each {k, v ->
            def colName = Bytes.toString(k)
            def fieldName = HBaseNameUtils.getDomainFieldName(colName)
            def colValue = byteArrayConverter.getPersistedValue(v, colName, grailsClass, fieldName)
            instance."$fieldName" = colValue
        }

        hbaseData.getFamilyMap(Constants.DEFAULT_ASSOCIATION_FAMILY).each {k, v ->
            def colName = Bytes.toString(k)
            def fieldName = HBaseNameUtils.getDomainFieldName(colName)
            LOG.debug("Reading association column $colName and setting instance property $fieldName")
            def grailsProperty = grailsClass.getPropertyByName(fieldName)
            def propertyClass = grailsProperty.getReferencedDomainClass() 

            // TODO this String is coded in more than one place, refactor
            if (grailsProperty.isOneToOne()) {
                def colValue = byteArrayConverter.getPersistedValue(v, colName, propertyClass, 'id')
                instance."${fieldName}__association__id__" = colValue
            }
            else if (grailsProperty.isOneToMany()) {
                def colValue = byteArrayConverter.getPersistedValue(v, colName, grailsClass, fieldName)
                instance."${fieldName}__association__id__list__" = colValue
            }
        }
        
        return instance
    }


    def createDatabaseRow(Put p, Object instance) {
        this.updateDatabaseRow(p, null, instance, null)
    }

    public SaveCounters updateDatabaseRow(Put p, Delete d, Object instance, Object row) {
        GrailsDomainClass grailsClass = HBaseNameUtils.getDomainClass(instance.class.name)
        def counters = new SaveCounters()

        HBaseNameUtils.getPersistentPropertiesNames(instance.class.name).each {fieldName ->
            def prop = grailsClass?.getPropertyByName(fieldName)
            def mapper = mapperFactory.getMapper(prop, counters)
            counters = mapper.updateDatabaseRow(p, d, instance, prop, row)
        }

        return counters
    }

    def referenceTable
    def byteArrayConverter
    def rowIdGenerator
    def mapperFactory

    private static final Log LOG = LogFactory.getLog(DomainClassMapper.class)
}