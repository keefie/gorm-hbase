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
package org.grails.hbase.api.finders

import grails.test.GrailsUnitTestCase

import org.apache.hadoop.hbase.filter.RegexStringComparator
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.filter.FilterList

import org.grails.hbase.store.Constants
import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.Author
import org.grails.hbase.Publisher
/**
 * Integration Tests
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Feb 29th, 2010
 */
class FindAllWithHBaseFiltersTests extends GrailsUnitTestCase {
    
    protected void setUp() {
        super.setUp()
        pubs.each { n ->
            def p = new Publisher(name: n)
            p.save()          
        }
    }

    protected void tearDown() {
        super.tearDown()
        Publisher.list().each { p ->
            p.delete()
        }
    }

    void testFindWithSimpleFilter() {
        def columnName = Bytes.toBytes(HBaseNameUtils.getDomainColumnName('name'))
        def value = Bytes.toBytes(pubs.get(2))
        def filter = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY, columnName, CompareOp.EQUAL,  value)

        def publishersFound = Publisher.findAll(filter)

        assertEquals 3, publishersFound?.size()
        
        assertEquals pubs.get(2), publishersFound.get(0).name
        assertEquals pubs.get(4), publishersFound.get(1).name
        assertEquals pubs.get(5), publishersFound.get(2).name
    }
    
    void testFindWithSimpleFilterNoDataFound() {
        def columnName = Bytes.toBytes(HBaseNameUtils.getDomainColumnName('name'))
        def value = Bytes.toBytes("NoName")
        def filter = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY, columnName, CompareOp.EQUAL,  value)
        
        def publishersFound = Publisher.findAll(filter)

        assertEquals 0, publishersFound?.size()
    }
    
    void testFindWithSimpleFilterAndMaxAndOffset() {
        def columnName = Bytes.toBytes(HBaseNameUtils.getDomainColumnName('name'))
        def value = Bytes.toBytes(pubs.get(2))
        def filter = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY, columnName, CompareOp.NOT_EQUAL,  value)

        def publishersFound = Publisher.findAll(filter, [max:2, offset:1])

        assertEquals 2, publishersFound?.size()

        assertEquals pubs.get(1), publishersFound.get(0).name
        assertEquals pubs.get(3), publishersFound.get(1).name
    }

    void testFindWithRegexFilter() {
        def columnName = Bytes.toBytes(HBaseNameUtils.getDomainColumnName('name'))
        def value = new RegexStringComparator(".+(B|C)")
        def filter = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY, columnName, CompareOp.EQUAL,  value)

        def publishersFound = Publisher.findAll(filter, [ sort: "name"])

        assertEquals 2, publishersFound?.size()

        assertEquals pubs.get(1), publishersFound.get(0).name
        assertEquals pubs.get(0), publishersFound.get(1).name
    }
 
    void testFindWithMustPassOne() {
        def columnName1 = Bytes.toBytes(HBaseNameUtils.getDomainColumnName("name"))
        def value1 = Bytes.toBytes(pubs.get(3))
        def filter1 = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY, columnName1, CompareOp.EQUAL,  value1)

        def columnName2 = Bytes.toBytes(HBaseNameUtils.getDomainColumnName("name"))
        def value2 = Bytes.toBytes(pubs.get(1))
        def filter2 = new SingleColumnValueFilter(Constants.DEFAULT_DATA_FAMILY, columnName2, CompareOp.EQUAL,  value2)

        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE)
        filterList.addFilter(filter1)
        filterList.addFilter(filter2)

        def publishersFound = Publisher.findAll(filterList)

        assertEquals 2, publishersFound?.size()

        assertEquals pubs.get(1), publishersFound.get(0).name
        assertEquals pubs.get(3), publishersFound.get(1).name
    }

    List pubs = ["Pub C", "Pub B", "Pub A", "Pub D", "Pub A", "Pub A"]

    static transactional = false
}
