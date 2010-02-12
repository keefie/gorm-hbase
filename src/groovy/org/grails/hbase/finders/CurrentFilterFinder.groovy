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
 * Find the last filter in the structure
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 08-Feb-2010
 */
class CurrentFilterFinder implements FinderFilterVisitor {

    protected CurrentFilterFinder(FinderFilter origFilter) {
        this.origFilter = origFilter
    }

    protected CurrentFilterFinder(FinderFilterList origFilter) {
        this.origFilter = origFilter
    }

    def find() {
        this.origFilter.accept(this)
        return this.lastFilter
    }

    public void visit(FinderFilter filter) {
        this.lastFilter = filter
    }

    public void visit(FinderFilterList filterList) {
        this.lastFilter = filterList
        
        filterList.filters.each {f ->
            LOG.debug("Visiting : $f")
            f.accept(this)
        }
    }

    def lastFilter
    def origFilter

    private static final Log LOG = LogFactory.getLog(CurrentFilterFinder.class)
}