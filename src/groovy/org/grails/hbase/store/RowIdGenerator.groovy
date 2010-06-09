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

        // Use the HBase builting feature to safely create row id's'
        return sequenceTable.incrementColumnValue(Bytes.toBytes(domainTableName),
            Constants.DEFAULT_SEQUENCE_FAMILY,
            Constants.DEFAULT_SEQUENCE_QUALIFIER, 1) - 1
    }

    def toPersistentForm(Long id) {
        id
    }

    def toPersistentForm(String id) {
        new Long(id)
    }

    def toInstanceForm(String id) {
        new Long(id)
    }

    def toInstanceForm(Long id) {
        id
    }

    def toInstanceForm(byte[] id) {
        Bytes.toLong(id)
    }

    def conf

    private static final Log LOG = LogFactory.getLog(RowIdGenerator.class)
}