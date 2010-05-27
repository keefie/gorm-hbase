/**
 * Copyright 2010 Keith Thomas
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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.FinderFilterList
import org.grails.hbase.api.finders.Operator
/**
 * Manage the addition of new filters in a filter list with the logical operator 'or'
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Feb-2010
 */
class LogicalOrFilterListBuilder implements LogicalFilterListBuilder {
    LogicalOrFilterListBuilder(FinderFilterListBuilder mainBuilder) {
        this.mainBuilder = mainBuilder
    }

    def addFilter(FinderFilter filter) {
        LOG.debug("Adding filter: $filter")
        this.finderFilters.addFilter(filter)
    }

    def addFilter(FinderFilterList filter) {
        LOG.debug("Adding filter: $filter")

        // If it is still an 'or' operator then do nothing'
        if (filter.operator == Operator.OR) return

        // Take this last filter in our list and add it to the new filter
        def lastFilter = finderFilters.filters.last()
        this.finderFilters.filters = this.finderFilters.filters.minus(lastFilter)
        filter.addFilter(lastFilter)
        this.finderFilters.addFilter(filter)

        // Until further notice process all filters as 'and' filters'
        def logicalBuilder = new LogicalAndFilterListBuilder(mainBuilder, this, filter)
        mainBuilder.setLogicalBuilder(logicalBuilder)
    }

    def getFinderFilters() {
        if (finderFilters.filters.size() == 1) {
            LOG.debug("Returning filter: ${finderFilters.filters.get(0)}")
            return finderFilters.filters.get(0)
        }

        LOG.debug("Returning filter: $finderFilters")
        return this.finderFilters
    }

    def setOperatorOnLastFilter(Operator op) {
        finderFilters.filters.last().operator = op
    }

    def startChild() {
        LOG.debug("startChild() invoked")
        def childFinderFilters = new FinderFilterList(Operator.OR)
        this.finderFilters.addFilter(childFinderFilters)
        this.finderStack.push(this.finderFilters)
        this.finderFilters = childFinderFilters
    }

    def endChild() {
        LOG.debug("endChild() invoked")
        if (parent) {
            parent.endChild()
            return
        }
        this.finderFilters = this.finderStack.pop()
    }

    def startAsChild(LogicalAndFilterListBuilder parent) {
        this.parent = parent
    }

    private FinderFilterListBuilder mainBuilder
    private FinderFilterList finderFilters = new FinderFilterList(Operator.OR)
    private List finderStack = [finderFilters]
    private LogicalAndFilterListBuilder parent

    private static final Log LOG = LogFactory.getLog(FinderFilterListBuilder.class)
}

