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

// TODO convert to Spring Bean
public class InstanceMapper {

    def createDomainClass(Result hbaseData, Class domainClazz) {
        GrailsDomainClass grailsClass = HBaseNameUtils.getDomainClass(domainClazz.name)

        def instance = domainClazz.newInstance()
        long id = Bytes.toLong(hbaseData.getRow())
        instance.id = id

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

    // TODO Refactor this method
    public SaveCounters updateDatabaseRow(Put p, Delete d, Object instance, Object row) {
        GrailsDomainClass grailsClass = HBaseNameUtils.getDomainClass(instance.class.name)
        int changeCount = 0
        int removeCount = 0

        HBaseNameUtils.getPersistentPropertiesNames(instance.class.name).each {fieldName ->
            def prop = grailsClass?.getPropertyByName(fieldName)

            if (prop?.isAssociation()) {
                if (prop?.isOneToOne()) {
                    // TODO remove hard coding of this String in multple places'
                    def rowValue = row?."${fieldName}__association__id__"
                    def assocIdFieldName = "${fieldName}Id"
                    def assocIdFieldValue = instance."${assocIdFieldName}"
                    instance."${fieldName}__association__id__" = assocIdFieldValue
                    
                    LOG.debug("Old association id=${rowValue}, new association id=${assocIdFieldValue}")

                    if (assocIdFieldValue != rowValue) {
                        changeCount++

                        if (rowValue) {
                            byte[] oldByteValue = byteArrayConverter.getValueToPersist(rowValue, fieldName)
                            referenceTable.removeReference(instance, prop, oldByteValue)
                        }
                        
                        byte[] byteValue = byteArrayConverter.getValueToPersist(assocIdFieldValue, fieldName)
                        referenceTable.saveReference(instance, prop, byteValue)
                        def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
                        p.add(Constants.DEFAULT_ASSOCIATION_FAMILY, Bytes.toBytes(columnName), byteValue)
                    }
                }
                else if (prop?.isOneToMany()) {
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
                                changeCount++
                                byte[] byteReferenceId = byteArrayConverter.getValueToPersist(assocId, fieldName)
                                referenceTable.saveReference(instance, prop, byteReferenceId)
                            }
                        }
                    }
                    
                    // Anything left in the Set of id's read from the database is no longer required
                    persistedAssociations.each { oldAssocId ->
                        changeCount++
                        byte[] oldByteValue = byteArrayConverter.getValueToPersist(oldAssocId, fieldName)
                        referenceTable.removeReference(instance, prop, oldByteValue)
                    }

                    if (changeCount) {
                        byte[] byteValue = byteArrayConverter.getValueToPersist(new IdSet(assocIdListFieldValue))
                        def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
                        p.add(Constants.DEFAULT_ASSOCIATION_FAMILY, Bytes.toBytes(columnName), byteValue)
                    }
                }
            }
            else {
                def fieldValue = instance."$fieldName"
                def rowValue = row?."$fieldName"
                if ((fieldValue == true || fieldValue == false || fieldValue) && fieldValue != rowValue) {
                    changeCount++
                    byte[] byteValue = byteArrayConverter.getValueToPersist(fieldValue, fieldName)
                    def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
                    p.add(Constants.DEFAULT_DATA_FAMILY, Bytes.toBytes(columnName), byteValue)
                }
                else if (!fieldValue && rowValue) {
                    removeCount++
                    def columnName = HBaseNameUtils.getDomainColumnName(fieldName)
                    d?.deleteColumn(Constants.DEFAULT_DATA_FAMILY, Bytes.toBytes(columnName))
                }
            }
        }

        return new SaveCounters(updates: changeCount, deletes: removeCount)
    }

    def referenceTable
    def byteArrayConverter

    private static final Log LOG = LogFactory.getLog(InstanceMapper.class)
}