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

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.RowLock
import org.apache.hadoop.hbase.util.Bytes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.util.HBaseNameUtils

/**
 * Each row has its own numeric key value, this class generates that value
 * 
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 7, 2009
 * Time: 7:09:59 AM
 */

// TODO convert to Spring Bean
public class RowIdGenerator {
    
    public long getNextId(String domainTableName) {
        LOG.debug("Method getNextId() invoked")

        def sequenceTableName = HBaseNameUtils.getSequenceTableName()

        // TODO See if HTable instances can be reused, pooled or whatever helps performance
        HTable sequenceTable = new HTable(conf, sequenceTableName)

        // Get back the next id for the requested associations class
        RowLock rowLock = sequenceTable.lockRow(Bytes.toBytes(domainTableName))
        Get g = new Get(Bytes.toBytes(domainTableName), rowLock)
        Result r = sequenceTable.get(g)
        byte[] value = r.getValue(Constants.DEFAULT_SEQUENCE_FAMILY,
            Constants.DEFAULT_SEQUENCE_QUALIFIER)
        long newId = Bytes.toLong(value)

        // TODO figure out why we get back a negative id
        if (newId < 0) newId = newId * -1

        // Update the sequence counter for the next client
        Put p = new Put(Bytes.toBytes(domainTableName), rowLock)
        p.add(Constants.DEFAULT_SEQUENCE_FAMILY,
            Constants.DEFAULT_SEQUENCE_QUALIFIER,
            Bytes.toBytes(newId + 1))
        sequenceTable.put(p)
        sequenceTable.unlockRow(rowLock)
        sequenceTable.close()
      
        return newId
    }

    def conf

    private static final Log LOG = LogFactory.getLog(RowIdGenerator.class)
}