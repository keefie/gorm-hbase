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
import org.grails.hbase.api.finders.FinderFilterVisitor

/**
 * Add n instance of FinderFilterList to an existing filter
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 05-Feb-2010
 */
class FilterListAppender implements FinderFilterVisitor {

    protected FilterListAppender(FinderFilter origFilter) {
        this.origFilter = origFilter
    }

    protected FilterListAppender(FinderFilterList origFilter) {
        this.origFilter = origFilter
        this.lastOldFinderFilterList = origFilter
    }

    public void append(FinderFilter newFinderFilter) {
        LOG.debug("Append $newFinderFilter")
        this.finderFilters = new FinderFilterList(this.origFilter.operator)
        this.lastNewFinderFilterList = this.finderFilters
        origFilter.accept(this)
        this.lastNewFinderFilterList.addFilter(newFinderFilter)
    }
   
    public void append(FinderFilterList newFinderFilterList) {
        LOG.debug("Append $newFinderFilterList")
        this.newFinderFilterList = newFinderFilterList
        if (this.origFilter instanceof FinderFilter) {
            this.finderFilters = new FinderFilterList(this.newFinderFilterList.operator)
        }
        else this.finderFilters = new FinderFilterList(this.origFilter.operator)

        this.lastNewFinderFilterList = this.finderFilters

        LOG.debug("Filter asked to accept visitor $origFilter")
        origFilter.accept(this)

        if (this.lastOldFinderFilterList) {
            def firstFinderFilter = this.lastOldFinderFilterList.filters.first()
            def lastFinderFilter = this.lastOldFinderFilterList.filters.last()
            this.newFinderFilterList.addFilter(lastFinderFilter)
            this.lastNewFinderFilterList.setFilters([firstFinderFilter, this.newFinderFilterList] as List)
            return
        }
    }

    public void visit(FinderFilter filter) {
        LOG.debug("Visit: $filter")
        this.lastNewFinderFilterList.addFilter(filter)
    }

    public void visit(FinderFilterList filterList) {
        LOG.debug("Visit: $filterList")
        if (!filterList.equals(origFilter)) {
            def newList = new FinderFilterList(filterList.operator)
            this.lastNewFinderFilterList.addFilter(newList)
            this.lastNewFinderFilterList = newList
        }

        this.lastOldFinderFilterList = filterList
        
        filterList.filters.each {f ->
            LOG.debug("Visiting : $f")
            f.accept(this)
        }
    }

    def finderFilters
    def origFilter
    def lastNewFinderFilterList
    def lastOldFinderFilterList
    def newFinderFilterList

    private static final Log LOG = LogFactory.getLog(FilterListAppender.class)
}