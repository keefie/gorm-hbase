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
package org.grails.hbase.finders

import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.filter.FilterList
import org.apache.hadoop.hbase.filter.BinaryComparator
import org.apache.hadoop.hbase.filter.RowFilter
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.filter.RegexStringComparator
import org.apache.hadoop.hbase.util.Bytes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.FinderFilterList
import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.store.Constants
/**
 * Take the plugin specific api FinderFilter classes and create HBase
 * filters from them
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 14-Dec-2009 at 7:40pm GMT
 */
class FilterFactory {

    Filter createHBaseFilter(FinderFilter finderFilter) {
        LOG.debug ("Creating HBase Filter from $finderFilter")
        Filter filter
        if (finderFilter?.propertyName.toUpperCase().equals('ID')) filter = this.getRowFilter(finderFilter)
        else filter = this."get${finderFilter.operator.filterClass.simpleName}"(finderFilter)

        return filter
    }

    Filter createHBaseFilter(FinderFilterList finderFilterList) {
        LOG.debug ("Creating HBase FilterList from $finderFilterList")

        List<Filter> list = new ArrayList<Filter>()
        FilterList filterList = new FilterList(finderFilterList.operator.value, list)
        finderFilterList.filters.each { list << createHBaseFilter(it) }

        LOG.debug ("Created HBase FilterList as $filterList")
        return filterList
    }

    Filter createHBaseFilter(Filter filter) {
        LOG.debug ("Returning HBase filter unchanged: $filter")
        return filter
    }

    private Filter getRegexStringComparator(finderFilter) {
        LOG.debug ("Creating HBase RegexStringComparator from $finderFilter")
        def columnName = HBaseNameUtils.getDomainColumnName(finderFilter?.propertyName)
        def value = new RegexStringComparator(finderFilter?.value)

        def filter = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY,
            Bytes.toBytes(columnName),
            finderFilter?.operator?.value,
            value)

        filter.setFilterIfMissing(finderFilter.excludeIfColumnMissing)
        
        return filter
    }

    private Filter getSingleColumnValueFilter(finderFilter) {
        LOG.debug ("Creating HBase SingleColumnValueFilter from $finderFilter")
        def columnName = HBaseNameUtils.getDomainColumnName(finderFilter?.propertyName)

        def filter = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY,
            Bytes.toBytes(columnName),
            finderFilter?.operator?.value,
            byteArrayConverter.getValueToPersist(finderFilter?.value, finderFilter.propertyName))

        filter.setFilterIfMissing(finderFilter.excludeIfColumnMissing)

        return filter
    }

    private Filter getRowFilter(finderFilter) {
        LOG.debug ("Creating HBase RowFilter from $finderFilter")
        def id = byteArrayConverter.getValueToPersist(rowIdGenerator.toPersistentForm(finderFilter?.value), finderFilter.propertyName)
        def filter = new RowFilter(finderFilter?.operator?.value, new BinaryComparator(id))

        return filter
    } 

    def byteArrayConverter
    def rowIdGenerator

    final static FilterFactory factory = new FilterFactory()

    private static final Log LOG = LogFactory.getLog(FilterFactory.class)
}

