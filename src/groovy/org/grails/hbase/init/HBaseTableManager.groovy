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
package org.grails.hbase.init

import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.HColumnDescriptor
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.tableindexed.IndexedTableAdmin
import org.apache.hadoop.hbase.client.tableindexed.IndexedTableDescriptor
import org.apache.hadoop.hbase.client.tableindexed.IndexSpecification
import org.apache.hadoop.hbase.client.tableindexed.IndexKeyGenerator
import org.apache.hadoop.hbase.client.tableindexed.SimpleIndexKeyGenerator

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.util.HBaseIndexUtils
import org.grails.hbase.store.Constants

/**
 * Creates, deletes store and associated columns
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 1, 2009
 * Time: 2:32:37 PM
 */

public class HBaseTableManager {

    def createDomainTable(domainClass, createIndexedTables) {
        def tableDesc = new HTableDescriptor(HBaseNameUtils.getDomainTableName(domainClass))

        Constants.DEFAULT_FAMILIES.each {familyName ->
            def columnDesc = new HColumnDescriptor(familyName)
            columnDesc.setMaxVersions(1)
            tableDesc.addFamily(columnDesc)
        }

        this.delete(tableDesc.name)

        IndexedTableDescriptor indexDesc = new IndexedTableDescriptor(tableDesc)
        def fieldNames = HBaseNameUtils.getPersistentPropertiesNames(domainClass)

        if (createIndexedTables) fieldNames.each {fieldName ->
            def prop = domainClass?.getPropertyByName(fieldName)

            if (!prop.isAssociation() && HBaseIndexUtils.isTypeIndexed(prop.getType())) {
                def columnName = Bytes.toBytes(Constants.DEFAULT_DATA_FAMILY_STRING +
                    Constants.DEFAULT_FAMILY_NAME_DELIMITER +
                    HBaseNameUtils.getDomainColumnName(fieldName))
                byte[][] keyCol = [columnName] as byte[][]
                byte[][] otherCols = HBaseNameUtils.getColumnNamesArray(fieldNames?.minus(fieldName))
                IndexKeyGenerator indexKeyGenerator = new SimpleIndexKeyGenerator(columnName)
                IndexSpecification colIndex = new IndexSpecification(
                    HBaseNameUtils.getPersistentPropertyAsTableName(fieldName),
                    keyCol, otherCols, indexKeyGenerator)
                indexDesc.addIndex(colIndex)

                byte[] idxTableName = colIndex.getIndexedTableName(tableDesc.name)
                this.delete(idxTableName)
            }
        }

        LOG.info("Creating HBase table named ${Bytes.toString(tableDesc.name)} with data family '${Constants.DEFAULT_DATA_FAMILY_STRING}'")
        def idxAdmin = new IndexedTableAdmin(conf)
        idxAdmin.createIndexedTable(indexDesc)

        // Add a sequence number to the sequence number table used to generate associations class id's
        String sequenceTableName = HBaseNameUtils.getSequenceTableName()
        HTable table = new HTable(this.conf, sequenceTableName)
        Put p = new Put(tableDesc.name)
        long value = 1L
        p.add(Constants.DEFAULT_SEQUENCE_FAMILY,
            Constants.DEFAULT_SEQUENCE_QUALIFIER,
            Bytes.toBytes(value))
        table.put(p)
        LOG.debug "New sequence counter created for table ${Bytes.toString(tableDesc.name)}"
    }


    def createSequenceTable() {
        String tableName = HBaseNameUtils.getSequenceTableName()
        def tableDesc = new HTableDescriptor(tableName)
        def columnDesc = new HColumnDescriptor(Constants.DEFAULT_SEQUENCE_FAMILY_STRING)
        columnDesc.setMaxVersions(1)
        tableDesc.addFamily(columnDesc)

        this.delete(tableName)

        LOG.debug("Creating HBase table named $tableName, related IndexedTables created automatically by HBase")
        admin.createTable(tableDesc)
        admin.enableTable(tableName)
    }


    def createReferenceTable() {
        referenceTable.createTable(admin, this)
    }

    
    def delete(String tableName) {
        this.delete(Bytes.toBytes(tableName))
    }

    
    def delete(byte[] tableName) {
        if (admin.tableExists(tableName)) {
            LOG.info("Deleting HBase table named ${Bytes.toString(tableName)}")
            admin.disableTable(tableName)
            admin.deleteTable(tableName)
        }
    }


    def getTableNames() {
        def tableNames = []
        admin.listTables().each {table ->
            tableNames << table.getNameAsString()
        }
        return tableNames
    }

    def conf
    def admin
    def referenceTable
    
    private static final Log LOG = LogFactory.getLog(HBaseTableManager.class)

}