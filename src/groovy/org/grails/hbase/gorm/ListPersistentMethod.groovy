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
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.client.tableindexed.IndexedTable
import org.apache.hadoop.hbase.filter.FilterList
import org.apache.hadoop.hbase.filter.BinaryComparator
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.filter.RowFilter
import org.apache.hadoop.hbase.filter.CompareFilter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.Operator
import org.grails.hbase.store.Constants
import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.util.HBaseFinderUtils

/**
 * Domain class list() method support
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:02:56 AM
 */

public class ListPersistentMethod implements PersistentMethod {

    def invoke(clazz, String methodName, Object[] arguments) {
        LOG.debug("Method ${clazz.name}.${methodName}(${arguments}) invoked")

        // TODO Figure out why I need this kludge and get rid of it
        if (arguments.size() == 1 && arguments[0] instanceof Object[]) arguments = arguments[0]

        def list = []

        try {
            long max = 100
            long offset = 0
            String sort = null

            // If the first arg is a FinderFilter then create the HBase Filter equaivalent
            def filter = this.getFilter(arguments)

            // If the last arg is a map it probably contains max, sort, offset values
            if (arguments && arguments[arguments.size() - 1] instanceof Map) {
                def params = arguments[arguments.size() - 1]
                if (params.max) max = new Long("${params.max}").longValue()
                if (params.offset) offset = new Long("${params.offset}").longValue()
                if (params.sort) sort = params?.sort
            }
            
            LOG.debug("About to execute list() job for ${clazz.name} with params sort=${sort} max=${max}, offset=${offset}")

            def table = this.getTable(clazz, sort)
            def scanner = this.getScanner(table, clazz, filter, sort)

            // TODO Find a more efficient way to paginate
            def resultOffset = 0
            def rowsCollected = 0

            Result res = scanner.next()
            while (res && rowsCollected < max) {
                if (resultOffset >= offset) {
                    def instance = instanceMapper.createDomainClass(res, clazz)
                    // TODO find a more efficient way of doing this
                    if (this.isSorted(sort)) instance = clazz.get(instance.id)
                    LOG.debug("Found instance: $instance")
                    list << instance
                    rowsCollected++
                }
                res = scanner.next()
                resultOffset++
            }

            table.close()
        }
        catch (Exception ex) {
            LOG.error(ex.message, ex)
            // TODO revist this decision to just return an empty list
            return []
        }

        return list
    }

    def getSortFilter(clazz, filter, sortProperty) {
        def table = new HTable(conf, HBaseNameUtils.getDomainTableName(clazz.name))

        def s = new Scan()
        s.setFilter(filter)

        def scanner = table.getScanner(s)
        def sortFilterList = new FilterList(Operator.OR.value, [] as ArrayList)
        def res = scanner.next()

        while (res) {
            def columnName = HBaseNameUtils.getDomainColumnName(sortProperty)
            def row  = res.getRow()
            def columnValue = res.getValue(Constants.DEFAULT_DATA_FAMILY, Bytes.toBytes(columnName))
            // Key construction is consistent with org.apache.hadoop.hbase.client.tableindexed.SimpleIndexKeyGenerator
            def indexKey = Bytes.add(columnValue, row)
            def sortFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(indexKey))
            sortFilterList.addFilter(sortFilter)
            res = scanner.next()
        }
        table.close()

        return sortFilterList
    }

    def getTable(clazz, sort) {
        if (!this.isSorted(sort)) return new HTable(conf, HBaseNameUtils.getDomainTableName(clazz.name))
        return new IndexedTable(conf, Bytes.toBytes(HBaseNameUtils.getDomainTableName(clazz.name)))
    }

    def getScanner(table, clazz, filter, sort) {
        if (this.isSorted(sort)) return this.getSortScanner(table, clazz, filter, sort)
        return this.getScanner(table, clazz, filter)
    }

    def getSortScanner(table, clazz, filter, sort) {
        def sortFilter = new FilterList()

        if (filter instanceof FilterList) {
            if (filter.getFilters().size() != 0) sortFilter = this.getSortFilter(clazz, filter, sort)
        }
        else  sortFilter = this.getSortFilter(clazz, filter, sort)

        table.getIndexedScanner(HBaseNameUtils.getPersistentPropertyAsTableName(sort),
            null, null, null, sortFilter, null)
    }

    def getScanner(table, clazz, filter) {
        def s = new Scan()
        s.setFilter(filter)
        table.getScanner(s)
    }

    def getFilter(arguments) {
        def filter  = new FilterList()
        if (HBaseFinderUtils.hasFilter(arguments)) filter = filterFactory.createHBaseFilter(arguments[0])
        return filter
    }

    def isSorted(sort) {
        sort && !sort.toUpperCase().equals("ID")
    }


    def conf
    def instanceMapper
    def filterFactory

    private static final Log LOG = LogFactory.getLog(ListPersistentMethod.class)
}