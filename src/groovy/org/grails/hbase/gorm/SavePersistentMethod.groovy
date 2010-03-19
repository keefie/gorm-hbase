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

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.RowLock
import org.apache.hadoop.hbase.client.tableindexed.IndexedTable
import org.apache.hadoop.hbase.HConstants

import org.springframework.validation.Errors
import org.springframework.validation.BindException

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.store.Constants
import org.grails.hbase.error.RowNotFoundException
import org.grails.hbase.error.OptimisticLockingException

/**
 * Domain class save() method support. The save() method both creates new rows and
 * updates existing rows.
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:02:56 AM
 */

public class SavePersistentMethod implements PersistentMethod {

    def invoke(instance, String methodName, Object[] arguments) {
        try {
            LOG.debug("Method ${instance.class.name}.${methodName}(${arguments}) invoked")
            if (instance?.id) this.update(instance)
            else this.create(instance)
        }
        catch (Exception ex) {
            String errmsg = ex.message
            if (!ex.message) errmsg = "Unexpected error: ${ex.class.name}"

            Errors errors = new BindException(instance, "${instance.id}")
            errors.reject(errmsg, ex.message)
            instance.errors = errors

            LOG.error(errmsg, ex)

            return null
        }
    
        // TODO check what should really be returned here
        return instance
    }


    private void create(instance) {
        LOG.debug("Creating new instance of ${instance.class.name}")

        def domainTableName = HBaseNameUtils.getDomainTableName(instance.class.name)

        // TODO See if HTable instances can be reused, pooled or whatever helps performance
        IndexedTable domainTable = new IndexedTable(conf, Bytes.toBytes(domainTableName))

        // Get the next free id for the associations table
        instance.id = rowIdGenerator.getNextId(domainTableName)

        // Add the associations instance record to the database
        byte[] row = Bytes.toBytes(rowIdGenerator.toPersistentForm(instance.id))
        RowLock rowLock = domainTable.lockRow(row)
        Put p = new Put(row, rowLock)

        long version = 0 // Versioning for optimistic locking
        instance.version = version
        
        p.add(Constants.DEFAULT_CONTROL_FAMILY,
            Constants.DEFAULT_VERSION_QUALIFIER,
            Bytes.toBytes(version))

        instanceMapper.createDatabaseRow(p, instance)
        domainTable.put(p)
        domainTable.unlockRow(rowLock)
        domainTable.close()
    }


    private void update(instance) {
        LOG.debug("Updating instance of ${instance.class.name} with id = ${instance.id}")
                                    
        def domainTableName = HBaseNameUtils.getDomainTableName(instance.class.name)

        // TODO See if HTable instances can be reused, pooled or whatever helps performance
        IndexedTable domainTable = new IndexedTable(conf, Bytes.toBytes(domainTableName))

        // Lock the table and make sure nobody else has updated it first
        // TODO this logic is also used in DeletePersistentMethod, need to pull out and re-use
        byte[] row = Bytes.toBytes(rowIdGenerator.toPersistentForm(instance.id))
        RowLock rowLock = domainTable.lockRow(row)
        Get g = new Get(row, rowLock)

        Result r = domainTable.get(g)
        if (r.isEmpty()) {
            domainTable.unlockRow(rowLock)
            domainTable.close()
            throw new RowNotFoundException(
              "Database row in table ${domainTableName} with id = ${instance.id} not found")
        }

        // If we have got here then we have found a record so we will create
        // a new instance of the associations class requested then populate it with data
        def rowInstance = instanceMapper.createDomainClass(r, instance.class)

        // Compare row versions to make sure we can perform the update requested
        LOG.debug("InstanceVersion=${instance.version}, RowVersion=${rowInstance.version}")
        if (instance.version != rowInstance.version) {
            domainTable.unlockRow(rowLock)
            domainTable.close()
            throw new OptimisticLockingException(
              "Requested update conflicts with another user's update, please retry")
        }

        // Update the version number to indicate the data has changed
        instance.version = instance.version + 1

        Put p = new Put(row, rowLock)
        Delete d = new Delete(row, HConstants.LATEST_TIMESTAMP, rowLock)
        p.add(Constants.DEFAULT_CONTROL_FAMILY,
            Constants.DEFAULT_VERSION_QUALIFIER,
            Bytes.toBytes(instance.version.longValue()))
        LOG.debug("New Row Version=${instance.version}, type=${instance.version.class.name}")

        // Loop through each field updating the database only where changes have occured
        def saveCounters = instanceMapper.updateDatabaseRow(p, d, instance, rowInstance)

        // Only make the update if some data has changed
        // TODO Make this more efficient by only doing put and/or delete as necessary
        if(saveCounters.updates) domainTable.put(p)
        if(saveCounters.deletes) domainTable.delete(d)

        domainTable.unlockRow(rowLock)
        domainTable.close()
    }

    def conf
    def instanceMapper
    def rowIdGenerator
    
    private static final Log LOG = LogFactory.getLog(SavePersistentMethod.class)
}