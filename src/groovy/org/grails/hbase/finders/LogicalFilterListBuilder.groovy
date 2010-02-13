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

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.FinderFilterList
import org.grails.hbase.api.finders.Operator
/**
 * Manage the addition of new filters in a filter list with the logical operators 'or' & 'and'
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Feb-2010
 */
interface LogicalFilterListBuilder {
    def addFilter(FinderFilter filter)
    def addFilter(FinderFilterList filter)
    def getFinderFilters()
    def setOperatorOnLastFilter(Operator op)
}

