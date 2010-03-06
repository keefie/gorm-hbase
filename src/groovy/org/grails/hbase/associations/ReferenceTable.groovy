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
package org.grails.hbase.associations

import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.HColumnDescriptor
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.RowLock
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.client.tableindexed.IndexedTable

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

import org.grails.hbase.init.HBaseTableManager
import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.store.Constants
import org.grails.hbase.error.UnsupportedFeatureException
/**
 * Creates, deletes store and associated columns
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 1, 2009
 * Time: 2:32:37 PM
 */
public class ReferenceTable {

    def createTable(HBaseAdmin hbAdmin, HBaseTableManager tableManager) {
        String tableName = HBaseNameUtils.getReferenceTableName()
        def tableDesc = new HTableDescriptor(tableName)
        def columnDesc = new HColumnDescriptor(Constants.DEFAULT_REFERENCE_FAMILY_STRING)
        columnDesc.setMaxVersions(1)
        tableDesc.addFamily(columnDesc)

        // Delete the table if it already exists
        tableManager.delete(tableName)

        LOG.info("Creating HBase table named $tableName with family '${Constants.DEFAULT_REFERENCE_FAMILY_STRING}'")
        hbAdmin.createTable(tableDesc)
        hbAdmin.enableTable(tableName)
    }


    def removeReference(Object referencerInstance,
        GrailsDomainClassProperty association, byte[] referenceeRow) {

        LOG.debug("removeReference() invoked with value ${referenceeRow}")

        if (association.isManyToOne()) {
            throw new UnsupportedFeatureException("Many to one associations not yet supported")
        }

        // Lock the referenced row so that we don't get conflicting updates
        String referencedClassName = association.getReferencedPropertyType().name
        IndexedTable referencedTable = new IndexedTable(conf, 
            Bytes.toBytes(HBaseNameUtils.getDomainTableName(referencedClassName)))
        RowLock referenceeRowLock = referencedTable.lockRow(referenceeRow)

        // TODO Make this a Spring Bean singleton to improve performance
        def refTableName = HBaseNameUtils.getReferenceTableName()
        HTable refTable = new HTable(conf, Bytes.toBytes(refTableName))
        byte[] row = HBaseNameUtils.getReferenceRowKey(referencedClassName, referenceeRow)

        // Lock the table and make sure nobody else  is updating it a the same time
        RowLock rowLock = refTable.lockRow(row)

        Get g = new Get(row, rowLock)

        Result r = refTable.get(g)
        if (r.isEmpty()) {
            LOG.error("Reference table entry not found, row=${row}")
            return
        }

        byte[] referencesBytes = r.getValue(Constants.DEFAULT_REFERENCE_FAMILY,
            Constants.DEFAULT_REFERENCE_QUALIFIER)
        def references = byteArrayConverter.getPersistedValue(referencesBytes, ReferenceSet.class)?.getSet() ?: new HashSet()
        references.each { rf ->
            LOG.debug("Reference found: $rf")
        }

        Reference ref = new Reference()
        ref.tableName = HBaseNameUtils.getDomainTableName(referencerInstance.class.name)
        ref.columnName = HBaseNameUtils.getDomainColumnName(association.name)
        ref.keyValue = byteArrayConverter.getValueToPersist(referencerInstance.id, "id")

        LOG.debug("About to remove reference $ref, list size = ${references?.size()}")
        def removed = references?.remove(ref)
        LOG.debug("Removed reference = $removed, list size now = ${references?.size()}")

        if (!references) {
            LOG.debug("About to delete reference row: $row")
            Delete d = new Delete(row, HConstants.LATEST_TIMESTAMP, rowLock)
            refTable.delete(d)
        }
        else {
            LOG.debug("About to update reference row: $row")
            Put p = new Put(row, rowLock)
            byte[] newValue = byteArrayConverter.getValueToPersist(new ReferenceSet(references))
            p.add(Constants.DEFAULT_REFERENCE_FAMILY, Constants.DEFAULT_REFERENCE_QUALIFIER, newValue)
            refTable.put(p)
        }

        refTable.unlockRow(rowLock)
        refTable.close()

        referencedTable.unlockRow(referenceeRowLock)
        referencedTable.close()

        LOG.debug("removeReference() complete")
    }


    def saveReference(Object referencerInstance,
        GrailsDomainClassProperty association, byte[] referenceeRow) {

        LOG.debug("saveReference() invoked")

        if (association.isManyToOne()) {
            throw new UnsupportedFeatureException("Many to one associations not yet supported")
        }

        // TODO Make this a Spring Bean singleton to improve performance
        def refTableName = HBaseNameUtils.getReferenceTableName()
        HTable refTable = new HTable(conf, Bytes.toBytes(refTableName))

        String referencedClassName = association.getReferencedPropertyType().name
        byte[] row = HBaseNameUtils.getReferenceRowKey(referencedClassName, referenceeRow)

        // Lock the table and make sure nobody else  is updating it a the same time
        RowLock rowLock = refTable.lockRow(row)

        // Get back the existing data from the database
        Get g = new Get(row, rowLock)
        def references = new HashSet()

        Result r = refTable.get(g)
        if (!r.isEmpty()) {
            byte[] referencesBytes = r.getValue(Constants.DEFAULT_REFERENCE_FAMILY,
                Constants.DEFAULT_REFERENCE_QUALIFIER)
            references = byteArrayConverter.getPersistedValue(referencesBytes, ReferenceSet.class)?.getSet()
        }

        Reference ref = new Reference()
        ref.tableName = HBaseNameUtils.getDomainTableName(referencerInstance.class.name)
        ref.columnName = HBaseNameUtils.getDomainColumnName(association.name)
        ref.keyValue = byteArrayConverter.getValueToPersist(referencerInstance.id, "id")

        if (references.contains(ref)) {
            LOG.debug("Reference already persisted, no update required: ${ref}, saveReference() complete")
            return
        }
        
        LOG.debug("About to add reference: $ref")
        references.add(ref)
        LOG.debug("Reference add to list, size now=${references.size()}")
        Put p = new Put(row, rowLock)

        ReferenceSet refs = new ReferenceSet(references)
        byte[] newValue = byteArrayConverter.getValueToPersist(refs)
        p.add(Constants.DEFAULT_REFERENCE_FAMILY, Constants.DEFAULT_REFERENCE_QUALIFIER, newValue)

        refTable.put(p)
        refTable.unlockRow(rowLock)
        refTable.close()

        LOG.debug("saveReference() complete")
    }


    def removeAllReferences(Object domainInstance) {
        if (!domainInstance) return
        
        LOG.debug("removeAllReferences() invoked for: ${domainInstance.class.name} ${domainInstance.id}")

        def domainClass = HBaseNameUtils.getDomainClass(domainInstance.class.name)

        HBaseNameUtils.getPersistentPropertiesNames(domainInstance.class.name).each {fieldName ->
            def prop = domainClass?.getPropertyByName(fieldName)

            if (prop?.isAssociation()) {
                if (prop?.isOneToOne()) {
                    def assocIdFieldName = "${fieldName}__association__id__"
                    def assocIdFieldValue = domainInstance."${assocIdFieldName}"
                    if (assocIdFieldValue) {
                        // Using byteArrayConverter so we handle different types of keys
                        byte[] associationRow = byteArrayConverter.getValueToPersist(assocIdFieldValue, fieldName)
                        this.removeReference(domainInstance, prop, associationRow)
                    }
                }
                else if (prop?.isOneToMany()) {
                    def assocIdListFieldName = "${fieldName}__association__id__list__"
                    Class referenceeType = prop?.getReferencedDomainClass()?.clazz

                    domainInstance."${assocIdListFieldName}".each { assocId ->
                        if (assocId?.class != referenceeType) {
                            byte[] associationRow = byteArrayConverter.getValueToPersist(assocId, fieldName)
                            this.removeReference(domainInstance, prop, associationRow)
                        }
                    }
                }
                else if (association.isManyToOne()) {
                    throw new UnsupportedFeatureException("Many to one associations not yet supported")
                }
            }
        }
        LOG.debug("removeAllReferences() complete")
    }

    
    public boolean isReferenced(Object domainInstance) {
        LOG.debug("isReferenced() invoked for class ${domainInstance.class.name} with id ${domainInstance.id}")

        def isReferenced = true

        def refTableName = HBaseNameUtils.getReferenceTableName()
        HTable refTable = new HTable(conf, Bytes.toBytes(refTableName))

        byte[] row = HBaseNameUtils.getReferenceRowKey(domainInstance.class.name,
            byteArrayConverter.getValueToPersist(domainInstance.id, 'id'))

        // Lock the table and make sure nobody else  is updating it while we are making our query
        // TODO re-work with transactions etc to make atomic
        RowLock rowLock = refTable.lockRow(row)
        Get g = new Get(row, rowLock)
        Result r = refTable.get(g)
        
        if (r.isEmpty()) isReferenced = false

        refTable.unlockRow(rowLock)
        refTable.close()
        LOG.debug("isReferenced() complete, returning $isReferenced")

        return isReferenced
    }

    def conf
    def byteArrayConverter
    
    private static final Log LOG = LogFactory.getLog(ReferenceTable.class)
}