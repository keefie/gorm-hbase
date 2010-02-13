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

import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.util.Bytes

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
    /*
    void testFindWithSimpleFilterNoDataFound() {
    FinderFilter filter = new FinderFilter("name", Operator.EQUAL, "NoName")
    def publishersFound = Publisher.findAll(filter)

    assertEquals 0, publishersFound?.size()
    }

    void testFindWithSimpleFilterAndMaxAndOffset() {
    FinderFilter filter = new FinderFilter("name", Operator.NOT_EQUAL, pubs.get(2))
    def publishersFound = Publisher.findAll(filter, [max:2, offset:1])

    assertEquals 2, publishersFound?.size()

    assertEquals pubs.get(1), publishersFound.get(0).name
    assertEquals pubs.get(3), publishersFound.get(1).name
    }

    void testFindWithIdFilters() {
    // Get our control publisher using means we know will work from
    // previous tests
    FinderFilter filter = new FinderFilter("name", Operator.EQUAL, pubs.get(3))
    def publisherFoundByName = Publisher.findAll(filter)
    assertEquals 1, publisherFoundByName?.size()
    assertEquals pubs.get(3), publisherFoundByName.get(0).name

    // Now create an id filter to get the same record
    filter = new FinderFilter("id", Operator.EQUAL, publisherFoundByName.get(0).id)
    def publisherFoundById = Publisher.findAll(filter)

    assertEquals 1, publisherFoundById?.size()
    assertEquals publisherFoundByName.get(0), publisherFoundById.get(0)
    }

    void testFindWithSimpleFilterAndSort() {
    FinderFilter filter = new FinderFilter("name", Operator.NOT_EQUAL, pubs.get(2))
    def publisherFoundByNotName = Publisher.findAll(filter, [max:2, offset:1, sort: "name"])

    assertEquals 2, publisherFoundByNotName?.size()

    assertEquals pubs.get(0), publisherFoundByNotName.get(0).name
    assertEquals pubs.get(3), publisherFoundByNotName.get(1).name
    }


    void testFindWithSimpleFilterAndSortNoDataFound() {
    FinderFilter filter = new FinderFilter("name", Operator.EQUAL, "No Name")
    def publisherFoundByNotName = Publisher.findAll(filter, [max:2, offset:1, sort: "name"])

    assertEquals 0, publisherFoundByNotName?.size()
    }

    void testFindWithAnd() {
    Publisher publisher = new Publisher(name:"Andy")
    publisher.save()

    FinderFilter filter1 = new FinderFilter("name", Operator.EQUAL, "Andy")
    FinderFilter filter2 = new FinderFilter("id", Operator.EQUAL, publisher.id)
    FinderFilterList filterList = new FinderFilterList(Operator.AND)
    filterList.addFilter(filter1)
    filterList.addFilter(filter2)

    def publisherFoundByNameAndId = Publisher.findAll(filterList)

    assertEquals 1, publisherFoundByNameAndId?.size()
    assertEquals publisher.name, publisherFoundByNameAndId.get(0).name
    assertEquals publisher.id, publisherFoundByNameAndId.get(0).id
    }

    void testFindWithAndAgain() {
    Publisher publisher = new Publisher(name:"Andy")
    publisher.save()

    FinderFilter filter1 = new FinderFilter("name", Operator.EQUAL, "Andy")
    FinderFilter filter2 = new FinderFilter("id", Operator.EQUAL, publisher.id)
    List<FinderFilter> filtersInAList = [filter1, filter2]
    FinderFilterList filterList = new FinderFilterList(Operator.AND, filtersInAList)

    def publisherFoundByNameAndId = Publisher.findAll(filterList)

    assertEquals 1, publisherFoundByNameAndId?.size()
    assertEquals publisher.name, publisherFoundByNameAndId.get(0).name
    assertEquals publisher.id, publisherFoundByNameAndId.get(0).id
    }

    void testNotFoundWithAnd() {
    Publisher publisher = new Publisher(name:"Not Andy")
    publisher.save()

    FinderFilter filter1 = new FinderFilter("name", Operator.EQUAL, "Andy")
    FinderFilter filter2 = new FinderFilter("id", Operator.EQUAL, publisher.id)
    List<FinderFilter> filtersInAList = [filter1, filter2]
    FinderFilterList filterList = new FinderFilterList(Operator.AND, filtersInAList)

    def publisherNotFoundByNameAndId = Publisher.findAll(filterList)

    assertEquals 0, publisherNotFoundByNameAndId?.size()
    }

    void testEmptyFinderFilterList() {
    FinderFilterList filterList = new FinderFilterList(Operator.AND)
    def publisherAll = Publisher.findAll(filterList)

    assertEquals Publisher.list().size(), publisherAll?.size()
    }

    void testFindWithOr() {
    FinderFilter filter1 = new FinderFilter("name", Operator.EQUAL, pubs.get(0))
    FinderFilter filter2 = new FinderFilter("name", Operator.EQUAL, pubs.get(3))
    List<FinderFilter> filtersInAList = [filter1, filter2]
    FinderFilterList filterList = new FinderFilterList(Operator.OR, filtersInAList)

    def publisherFoundByNameAndId = Publisher.findAll(filterList, [sort: "name"])

    assertEquals 2, publisherFoundByNameAndId?.size()
    assertEquals pubs.get(0), publisherFoundByNameAndId.get(0).name
    assertEquals pubs.get(3), publisherFoundByNameAndId.get(1).name
    }    

    void testNotFoundWithOr() {
    FinderFilter filter1 = new FinderFilter("name", Operator.EQUAL, "Andy")
    FinderFilter filter2 = new FinderFilter("name", Operator.EQUAL, "Pandy")
    List<FinderFilter> filtersInAList = [filter1, filter2]
    FinderFilterList filterList = new FinderFilterList(Operator.OR, filtersInAList)

    def publisherFoundByNameAndId = Publisher.findAll(filterList)

    assertEquals 0, publisherFoundByNameAndId?.size()
    }

    void testFindNestedOr() {
    FinderFilter filter1a = new FinderFilter("name", Operator.EQUAL, pubs.get(0))
    FinderFilter filter1b = new FinderFilter("name", Operator.EQUAL, pubs.get(1))
    List<FinderFilter> filtersInAList1 = [filter1a, filter1b]
    FinderFilterList filterList1 = new FinderFilterList(Operator.OR, filtersInAList1)

    FinderFilter filter2a = new FinderFilter("name", Operator.EQUAL, pubs.get(2))
    FinderFilter filter2b = new FinderFilter("name", Operator.EQUAL, pubs.get(3))
    List<FinderFilter> filtersInAList2 = [filter2a, filter2b]
    FinderFilterList filterList2 = new FinderFilterList(Operator.OR, filtersInAList2)

    FinderFilterList allFilters = new FinderFilterList(Operator.OR, filterList2)
    allFilters.addFilter(filterList1)

    def publisherAll = Publisher.findAll(allFilters, [sort: "name"])

    assertEquals 6, publisherAll?.size()
    assertEquals pubs.get(2), publisherAll.get(0).name
    assertEquals pubs.get(4), publisherAll.get(1).name
    assertEquals pubs.get(5), publisherAll.get(2).name
    assertEquals pubs.get(1), publisherAll.get(3).name
    assertEquals pubs.get(0), publisherAll.get(4).name
    assertEquals pubs.get(3), publisherAll.get(5).name
    }

    void testFindNestedAnd() {
    FinderFilter filter1a = new FinderFilter("name", Operator.NOT_EQUAL, pubs.get(0))
    FinderFilter filter1b = new FinderFilter("name", Operator.NOT_EQUAL, pubs.get(1))
    List<FinderFilter> filtersInAList1 = [filter1a, filter1b]
    FinderFilterList filterList1 = new FinderFilterList(Operator.AND, filtersInAList1)

    FinderFilter filter2 = new FinderFilter("name", Operator.EQUAL, pubs.get(3))

    FinderFilterList allFilters = new FinderFilterList(Operator.AND, filter2)
    allFilters.addFilter(filterList1)

    def publisherAll = Publisher.findAll(allFilters, [sort: "name"])

    assertEquals 1, publisherAll?.size()
    assertEquals pubs.get(3), publisherAll.get(0).name
    }


    void testFindGreaterThan() {
    FinderFilter filter = new FinderFilter("name", Operator.GREATER, pubs.get(1))
    def publishersFound = Publisher.findAll(filter, [sort:"name"])

    assertEquals 2, publishersFound?.size()

    assertEquals pubs.get(0), publishersFound.get(0).name
    assertEquals pubs.get(3), publishersFound.get(1).name
    }

    void testFindGreaterThanOrEqual() {
    FinderFilter filter = new FinderFilter("name", Operator.GREATER_OR_EQUAL, pubs.get(1))
    def publishersFound = Publisher.findAll(filter, [sort:"name"])

    assertEquals 3, publishersFound?.size()

    assertEquals pubs.get(1), publishersFound.get(0).name
    assertEquals pubs.get(0), publishersFound.get(1).name
    assertEquals pubs.get(3), publishersFound.get(2).name
    }

    void testFinderLessThan() {
    FinderFilter filter = new FinderFilter("name", Operator.LESS, pubs.get(0))
    def publishersFound = Publisher.findAll(filter, [sort:"name"])

    assertEquals 4, publishersFound?.size()

    assertEquals pubs.get(2), publishersFound.get(0).name
    assertEquals pubs.get(4), publishersFound.get(1).name
    assertEquals pubs.get(5), publishersFound.get(2).name
    assertEquals pubs.get(1), publishersFound.get(3).name
    }

    void testFinderLessThanOrEqual() {
    FinderFilter filter = new FinderFilter("name", Operator.LESS_OR_EQUAL, pubs.get(0))
    def publishersFound = Publisher.findAll(filter, [sort:"name"])

    assertEquals 5, publishersFound?.size()

    assertEquals pubs.get(2), publishersFound.get(0).name
    assertEquals pubs.get(4), publishersFound.get(1).name
    assertEquals pubs.get(5), publishersFound.get(2).name
    assertEquals pubs.get(1), publishersFound.get(3).name
    assertEquals pubs.get(0), publishersFound.get(4).name
    }

    void testFindByEqualsDate() {
    def d = new Date()
    def p = new Publisher(name:"Fred", published: d)
    p.save()

    FinderFilter filter = new FinderFilter("published", Operator.EQUAL, d)
    def publishers = Publisher.findAll(filter)

    assertEquals 1, publishers?.size()
    assertEquals p, publishers.get(0)
    }

    void testFindByEqualsDateIncludingMissingColumn() {
    def d = new Date()
    def p = new Publisher(name:"Zachary", published: d)
    p.save()

    FinderFilter filter = new FinderFilter("published", Operator.EQUAL, d)
    filter.excludeIfColumnMissing = false
    def publishers = Publisher.findAll(filter, [sort: "name"])

    assertEquals 1 + pubs.size(), publishers?.size()
    assertEquals p, publishers.get(pubs.size())
    }

    void testFindByLessDate() {
    def d = new Date()
    def p = new Publisher(name:"Fred", published: d)
    p.save()

    Thread.sleep(10)
    def dd = new Date()
    FinderFilter filter = new FinderFilter("published", Operator.LESS, dd)
    def publishers = Publisher.findAll(filter)

    assertEquals 1, publishers?.size()
    assertEquals p, publishers.get(0)
    }

    void testFindByGreaterDate() {
    def d = new Date()
    def p = new Publisher(name:"Fred", published: d)
    p.save()

    Thread.sleep(10)
    def dd = new Date()
    FinderFilter filter = new FinderFilter("published", Operator.GREATER, dd)

    def publishers = Publisher.findAll(filter)

    assertEquals 0, publishers?.size()
    }
     */
    List pubs = ["Pub C", "Pub B", "Pub A", "Pub D", "Pub A", "Pub A"]

    static transactional = false
}
