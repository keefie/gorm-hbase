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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder

import org.apache.hadoop.hbase.filter.Filter

/**
 * Do not expose HBase filters to people just wishing to use GORM, wrap them in
 * a Groovy Grails class, this class
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 09-Dec-2009 at 8:06 PM PST
 */
public class FinderFilterList implements DynamicFinderFilter {

    FinderFilterList(Operator operator, List finderFilters) {
        if (operator != Operator.AND && operator != Operator.OR)
             throw new IllegalArgumentException("Invalid passed to FinderFilterList ctor: $operator")
        this.operator = operator
        this.filters = finderFilters
    }

    FinderFilterList(Operator operator, DynamicFinderFilter finderFilter) {
        if (operator != Operator.AND && operator != Operator.OR)
             throw new IllegalArgumentException("Invalid passed to FinderFilterList ctor: $operator")
        this.operator = operator
        this.filters = [finderFilter]
    }
    
    FinderFilterList(Operator operator) {
        if (operator != Operator.AND && operator != Operator.OR)
             throw new IllegalArgumentException("Invalid passed to FinderFilterList ctor: $operator")
        this.operator = operator
        this.filters = []
    }

    FinderFilterList() {
        this.operator = Operator.EQUAL
        this.filters = []
    }

    def addFilter(DynamicFinderFilter finderFilter) {
        this.filters << finderFilter
    }

    def setFilters(List filters) {
        this.filters = filters
    }

    public void accept(FinderFilterVisitor filterVisitor) {
        LOG.debug("Visitor: ${filterVisitor}, Visited: ${this}")
        filterVisitor.visit(this)
    }

    public String toString() {
        "${this.class.name}: operator:${operator}, list:${filters}"
    }

    public boolean equals(Object o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (o instanceof FinderFilterList) return new EqualsBuilder().
            append(operator, o.operator).
            append(filters, o.filters).
            isEquals()

        return false
    }

    public int hashCode() {
        return new HashCodeBuilder(19, 66).
            append(operator).
            append(filters).
            toHashCode();
    }

    Operator operator
    List filters

    private static final Log LOG = LogFactory.getLog(FinderFilterList.class)
}

