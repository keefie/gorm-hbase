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
package org.grails.hbase.gorm

import org.apache.hadoop.hbase.client.tableindexed.IndexedTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.RowLock
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HConstants

import org.springframework.dao.DataIntegrityViolationException

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.util.HBaseNameUtils

/**
 * Domain class delete() method support
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:02:56 AM
 */

public class DeletePersistentMethod implements PersistentMethod {

    def invoke(target, String methodName, Object[] arguments) {
        if (!target || !target.id) return
        
        LOG.debug("Method ${target?.class.name}.${methodName}(${arguments}) invoked")

        byte[] tableName = Bytes.toBytes(HBaseNameUtils.getDomainTableName(target.class.name))
        IndexedTable domainTable = new IndexedTable(conf, tableName)

        // Make sure we have the latest copy increase new associations have been created
        // that we don't know about'
        RowLock rowLock = domainTable.lockRow(Bytes.toBytes(rowIdGenerator.toPersistentForm(target.id)))
        def instance = this.getLatestCopy(target, domainTable, rowLock)
        if(!instance) {
            throw new DataIntegrityViolationException(
                     "Unable to find ${target.class.name} with id ${target.id}")
        } 

        try {
            // Delete the entire row with the key object passed in as the target arg
            Delete d = new Delete(Bytes.toBytes(rowIdGenerator.toPersistentForm(instance.id)), HConstants.LATEST_TIMESTAMP, rowLock)

            if (referenceTable.isReferenced(instance)) {
                domainTable.unlockRow(rowLock)
                domainTable.close()
                throw new DataIntegrityViolationException(
                     "Foreign key constraint violation trying to delete ${instance?.class.name} with id ${instance.id}")
            }
            else {
                referenceTable.removeAllReferences(instance)
                LOG.debug("About to delete row ${instance.id} from indexed domain table ${Bytes.toString(tableName)}")
                domainTable.delete(d)
                LOG.debug("Row ${instance.id}  deleted from ${Bytes.toString(tableName)}")
            }

            domainTable.unlockRow(rowLock)
            domainTable.close()
        }
        catch (DataIntegrityViolationException ex) {
            LOG.error(ex.message)
            throw ex
        }
        catch (Exception ex) {
            String errmsg = th.message
            if (!th.message) errmsg = "Unexpected error: ${ex.class.name}"
            LOG.error(errmsg, ex)
            throw new DataIntegrityViolationException(errmsg)
        }

        return null
    }


    // TODO re-work this with locking enabled, i.e. fix HBASE-1869 available
    private Object getLatestCopy(Object instance, IndexedTable domainTable, RowLock rowLock) {
        LOG.debug("Getting latest copy of ${instance.class.name} with id = ${instance.id}")

        // Lock the table and make sure nobody else has updated it first
        // TODO this logic is also used in removeAllReferences, need to pull out and re-use
        byte[] row = Bytes.toBytes(rowIdGenerator.toPersistentForm(instance.id))
        Get g = new Get(row, rowLock)

        Result r = domainTable.get(g)
        if (r.isEmpty()) {
            domainTable.unlockRow(rowLock)
            domainTable.close()
            LOG.debug("Cannot find hbase record with row = ${instance.id}")
            return null
        }

        // If we have got here then we have found a record so we will create
        // a new instance of the associations class requested then populate it with data
        def latestCopy = instanceMapper.createDomainClass(r, instance.class)

        return latestCopy
    }

    def conf
    def instanceMapper
    def referenceTable
    def rowIdGenerator

    private static final Log LOG = LogFactory.getLog(DeletePersistentMethod.class)
}