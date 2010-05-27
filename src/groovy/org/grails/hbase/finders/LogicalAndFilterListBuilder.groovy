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
class LogicalAndFilterListBuilder {

    LogicalAndFilterListBuilder(FinderFilterListBuilder mainBuilder, 
        LogicalOrFilterListBuilder parent, FinderFilterList finderFilters) {
        this.mainBuilder = mainBuilder
        this.parent = parent
        this.finderFilters = finderFilters
    }

    def addFilter(FinderFilter filter) {
        LOG.debug("Adding filter: $filter")
        this.finderFilters.addFilter(filter)
    }

    def addFilter(FinderFilterList filter) {
        LOG.debug("Adding filter: $filter")
        if (filter.operator == Operator.AND) return

        this.mainBuilder.setLogicalBuilder(parent)
    }

    def getFinderFilters() {
        return parent.finderFilters
    }

    def setOperatorOnLastFilter(Operator op) {
        finderFilters.filters.last().operator = op
    }

    def startChild() {
        LOG.debug("startChild() invoked")
        this.child = new LogicalOrFilterListBuilder(mainBuilder)
        this.mainBuilder.setLogicalBuilder(child)
        this.child.startAsChild(this)
    }

    def endChild() {
        LOG.debug("endChild() invoked")
        if (child) {
            this.finderFilters.addFilter(child.getFinderFilters())
            this.mainBuilder.setLogicalBuilder(this)
            this.child = null
            return
        }
        parent.endChild()
    }
    
    private FinderFilterListBuilder mainBuilder
    private FinderFilterList finderFilters
    private LogicalOrFilterListBuilder parent
    private LogicalOrFilterListBuilder child

    private static final Log LOG = LogFactory.getLog(FinderFilterListBuilder.class)
}

