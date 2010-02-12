/*
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
/**
 * Filter that maybe be passed as an argument to findAll() and find() dynamic
 * finder methods. Multiple filters maybe combined with instances of the
 * FinderFilterList class.
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 10-Dec-2009
 * @see FinderFiterList
 */
public class FinderFilter implements DynamicFinderFilter {

    /**
     * Create a new instance of FinderFilter with default operator of 'Equal'
     * <p>
     * e.g. author == 'Dan Brown' would be constructed as new FinderFilter('author', 'Dan Brown')
     * </p>
     * @param  propertyName name of the domain class property to be used in filter (e.g. 'author')
     * @param  value  value of the named property to be iltered upon  (e.g. 'Dan Brown')
     */
    FinderFilter(String propertyName, value) {
        this.propertyName = propertyName
        this.value = value
    }

    /**
     * Create a new instance of FinderFilter
     * <p>
     * e.g. author != 'Dan Brown' would be constructed as new FinderFilter('author', Operator.NOT_EQUAL, 'Dan Brown')
     * </p>
     * @param  propertyName name of the domain class property to be used in filter (e.g. 'author')
     * @param  operator comparison operator to be used in fiter (e.g. Operator.NOT_EQUAL)
     * @param  value  value of the named property to be iltered upon  (e.g. 'Dan Brown')  
     */
    FinderFilter(String propertyName, Operator operator, value) {
        if (operator == Operator.AND || operator == Operator.OR)
             throw new IllegalArgumentException("Invalid passed to FinderFilter ctor: $operator")
        this.propertyName = propertyName
        this.operator = operator
        this.value = value
    }

    /**
     * Accepts a FinderFilter visitor and calls back the visitor on
     * their visit() method
     *
     * @param  filterVisitor  The visitorrequesting to be called back
     */
    public void accept(FinderFilterVisitor filterVisitor) {
        LOG.debug("Visitor: ${filterVisitor}, Visited: ${this}")
        filterVisitor.visit(this)
    }

    public String toString() {
        "${this.class.name}: propertyName:${propertyName}, operator:${operator}, value:${value}"
    }

    public boolean equals(Object o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (o instanceof FinderFilter) return new EqualsBuilder().
            append(propertyName, o.propertyName).
            append(operator, o.operator).
            append(excludeIfColumnMissing, o.excludeIfColumnMissing).
            append(value, o.value).
            isEquals()
        return false
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 59).
            append(propertyName).
            append(operator).
            append(excludeIfColumnMissing).
            toHashCode();
    }

    /**
     * Controls whether rows are filtered out with the column upon which the
     * filter is based is missing. By default this value is set to true
     * which, for example, means all rows with no author would be excluded
     * new FinderFilter('author', 'Dan Brown')
     */
    boolean excludeIfColumnMissing = true

    private String propertyName
    private Operator operator = Operator.EQUAL
    private def value

    private static final Log LOG = LogFactory.getLog(FinderFilter.class)
}

