/*
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
package org.grails.hbase.api.finders
/**
 * Visitor pattern to allow objects to navigate through a filter structure. Any
 * visitor has to implement both methods and the pass itself into an accept()
 * method on FinderFilter and FinderFilterList. For example,
 * <p>
 * def f = new FinderFilter('author', 'Dan Brown')<br/>
 * def v = new myVisitor()<br/>
 * f.accept(v)
 * </p>
 * The visitor will then be called back via its visit() method
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 05-Feb-2010
 * @see FinderFilter, FinderFilterList
 */
public interface FinderFilterVisitor {
    public void visit(FinderFilter filter)
    public void visit(FinderFilterList filterlist)
}

