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
package org.grails.hbase.finders

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.GrailsDomainClass

import org.grails.hbase.api.finders.FinderFilterList
import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.api.finders.Operator
import org.grails.hbase.util.HBaseFinderUtils
/**
 * Build a FinderFilterList from a dynamic finder method name and its args
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 
 */
class FinderFilterListBuilder {

    protected FinderFilterListBuilder(GrailsDomainClass domainClass) {
        LOG.debug("Builder created for: ${domainClass.name}")
        this.domainClass = domainClass
    }

    protected void setMethodName(String methodName) {
        LOG.debug("Builder method name set to: ${methodName}")
        this.methodName = methodName
    }

    protected void setMethodArgs(Object[] methodArgs) {
        LOG.debug("Builder method args set to: ${methodArgs}")
        this.methodArgs = methodArgs
    }

    def getFinderFilters() {
        LOG.debug("Builder.getFinderFilters() invoked")

        TokenizerStrategy tokenizer
        if (HBaseFinderUtils.hasQuery(this.methodName, this.methodArgs)) 
             tokenizer = new QueryStringTokenizer(this.domainClass, this.methodName, this.methodArgs)

        else tokenizer = new MethodNameTokenizer(this.methodName, this.methodArgs)

        tokenizer.tokenize()
        String[] tokens = tokenizer.getTokens()
        this.methodArgs = tokenizer.getMethodArgs()

        handler.processToken(this, tokens, this.methodArgs);
        this.finderFilters = logicalBuilder.getFinderFilters()

        LOG.debug("Builder.getFinderFilters() returning: ${this.finderFilters}")
        return this.finderFilters
    }

    def getMethodArgs() {
        LOG.debug("Builder.getMethodArgs() invoked")
        if (!this.finderFilters) this.getFinderFilters()
        return this.methodArgs
    }

    protected String[] reduceArray(String[] orig, int reduceBy) {
        LOG.debug ("Removing first $reduceBy element(s) from $orig")
        String[] reduced = new String[orig.length - reduceBy]
        int origIndex = reduceBy
        for (int i = 0; origIndex < orig.length; origIndex++) {
            reduced[i] = orig[origIndex];
            i++
        }
        LOG.debug ("Reduced tokens: $reduced")
        return reduced
    }

    protected void addFinderFilter(filter) {
        LOG.debug("Filter to be added : $filter")        
        logicalBuilder.addFilter(filter)
        this.finderFilters = logicalBuilder.finderFilters
    }

    protected void setOperatorOnCurrentFilter(Operator op) {
        logicalBuilder.setOperatorOnLastFilter(op)
    }

    protected void checkArgs(remainingMethodNameTokens, remainingMethodArgs) {
        if (remainingMethodNameTokens) return
        
        if (remainingMethodArgs.length > 1 ||
            (remainingMethodArgs.length == 1 && !(methodArgs[0] instanceof Map)))
        throw new MissingMethodException(this.methodName, this.domainClass.clazz, new Object[0])
    }

    protected void startChild() {
        logicalBuilder.startChild()
    }

    protected void endChild() {
        logicalBuilder.endChild()
    }

    String methodName
    Object[] methodArgs = new Object[0]
    String finderName
    GrailsDomainClass domainClass
    DynamicFinderMethodHandler handler = new FinderNameHandler()
    
    def logicalBuilder = new LogicalOrFilterListBuilder(this)
    def finderFilters

    private static final Log LOG = LogFactory.getLog(FinderFilterListBuilder.class)
}

