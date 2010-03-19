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

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.util.HBaseNameUtils

/**
 * Domain class get() method support
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:02:56 AM
 */

public class GetPersistentMethod implements PersistentMethod {

    def invoke(clazz, String methodName, Object[] arguments) {
        LOG.debug("Method ${clazz.name}.${methodName}(${arguments}) invoked")
        def instance

        try {
            // Construct the key from the first method arg
            if (arguments.length != 1 || !arguments[0]) return null

            def id = rowIdGenerator.toPersistentForm("${arguments[0]}")

            // Access the table containing the requested data
            HTable domainTable = new HTable(conf, HBaseNameUtils.getDomainTableName(clazz.name))

            // Get back the requested data
            Get g = new Get(Bytes.toBytes(id))
            Result r = domainTable.get(g)
            if (r.isEmpty()) return null

            // If we have got here then we have found a record so we will create
            // a new instance of the associations class requested then populate it with data
            instance = instanceMapper.createDomainClass(r, clazz)

            domainTable.close()
        }
        catch (Exception ex) {
            LOG.error(th.message, ex)
            return null
        }

        return instance
    }

    def conf
    def instanceMapper
    def rowIdGenerator

    private static final Log LOG = LogFactory.getLog(GetPersistentMethod.class)
}