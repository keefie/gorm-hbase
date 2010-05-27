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

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder

import org.apache.hadoop.hbase.filter.CompareFilter
import org.apache.hadoop.hbase.filter.FilterList
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.filter.RowFilter
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.filter.RegexStringComparator
/**
 * Operator values used when construcing dynamic finder filters
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 10-Dec-2009
 *
 * @see FinderFiler, FinderFilterList
 */
class Operator {

    private Operator (value, toString, filterClass) {
        this.value = value
        this.toString = toString
        this.filterClass = filterClass
    }

    private Operator (value, toString) {
        this.value = value
        this.toString = toString
    }

    public boolean equals(Object o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (o instanceof Operator) return new EqualsBuilder().
            append(value, o.value).
            append(filterClass, o.filterClass).
            isEquals()

        return false
    }

    public int hashCode() {
        return new HashCodeBuilder(11, 19).
            append(value).
            append(filterClass).
            toHashCode();
    }

    public String toString() {
        this.toString
    }

    public static Map getComparisionOperators() {
        return comparisonOperators.clone()
    }

    private def value
    private String toString
    private def filterClass

    // Logical operators
    public static def AND = new Operator(FilterList.Operator.MUST_PASS_ALL, 'And')
    public static def OR  = new Operator(FilterList.Operator.MUST_PASS_ONE, 'Or')

    // Comparison operators
    public static def EQUAL = new Operator(CompareFilter.CompareOp.EQUAL, 'Equal', SingleColumnValueFilter.class)
    public static def NOT_EQUAL = new Operator(CompareFilter.CompareOp.NOT_EQUAL, 'Not_Equal', SingleColumnValueFilter.class)

    public static def GREATER = new Operator(CompareFilter.CompareOp.GREATER, 'Greater', SingleColumnValueFilter.class)
    public static def GREATER_OR_EQUAL = new Operator(CompareFilter.CompareOp.GREATER_OR_EQUAL, 'Greater_Or_Equal', SingleColumnValueFilter.class)

    public static def LESS = new Operator(CompareFilter.CompareOp.LESS, 'Less', SingleColumnValueFilter.class)
    public static def LESS_OR_EQUAL = new Operator(CompareFilter.CompareOp.LESS_OR_EQUAL, 'Less_Or_Equal', SingleColumnValueFilter.class)

    public static def LIKE = new Operator(CompareFilter.CompareOp.EQUAL, 'Like', RegexStringComparator.class)
    public static def NOT_LIKE = new Operator(CompareFilter.CompareOp.NOT_EQUAL, 'NotLike', RegexStringComparator.class)

    private static Map comparisonOperators = [
        'Equal':Operator.EQUAL,
        'eq':Operator.EQUAL,
        '=':Operator.EQUAL,
        'LessThan':Operator.LESS,
        'lt':Operator.LESS,
        '<':Operator.LESS,
        'LessThanEquals':Operator.LESS_OR_EQUAL,
        'le':Operator.LESS_OR_EQUAL,
        '<=':Operator.LESS_OR_EQUAL,
        'GreaterThan':Operator.GREATER,
        'gt':Operator.GREATER,
        '>':Operator.GREATER,
        'GreaterThanEquals':Operator.GREATER_OR_EQUAL,
        'ge':Operator.GREATER_OR_EQUAL,
        '>=':Operator.GREATER_OR_EQUAL,
        'NotEqual':Operator.NOT_EQUAL,
        'ne':Operator.NOT_EQUAL,
        '!=':Operator.NOT_EQUAL,
        'Like':Operator.LIKE,
        'NotLike':Operator.NOT_LIKE
    ]
}

