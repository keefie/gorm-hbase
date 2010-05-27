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

import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.util.HBaseFinderUtils
/**
 * Look for the name of of a domain class property at the start of a string
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Jan-2010
 */
class PropertyNameParser implements DynamicFinderMethodParser {

    def parse(FinderFilterListBuilder builder, String[] methodNameTokens, Object[] methodArgs) {
        LOG.debug("Finder tokens received: $methodNameTokens")
        LOG.debug("Finder args received : $methodArgs")

        builder.parser = nextParser

        String propertyName
        StringBuffer propertyNameBuffer = new StringBuffer()
        int tokensConsumed

        // Keep looping through the tokens until we find the longest valid property name
        // TODO stop looping if we find an operator namee
        for (int i = 0; i < methodNameTokens.length; i++) {
            if (!propertyNameBuffer) propertyNameBuffer << methodNameTokens[i].substring(0, 1).toLowerCase() + methodNameTokens[i].substring(1)
            else propertyNameBuffer << methodNameTokens[i]

            LOG.debug("Examining candidate property name: ${propertyNameBuffer.toString()}")

            if (propertyNameBuffer.toString().equals('id') ||
                builder.domainClass.hasPersistentProperty(propertyNameBuffer.toString())) {
                tokensConsumed = i + 1
                propertyName =  propertyNameBuffer.toString()
            }
        }

        if (!propertyName ||!methodArgs.length)
        throw new MissingMethodException(builder.methodName, builder.domainClass.clazz, new Object[0])

        def value
        if (HBaseFinderUtils.isNumber(methodArgs[0])) {
             value = HBaseFinderUtils.refineNumber(builder.domainClass, propertyName, "${methodArgs[0]}")
        }
        else value = methodArgs[0]

        builder.addFinderFilter(new FinderFilter(propertyName, value))

        Object[] remainingMethodArgs = this.reduceArgs(methodArgs)
        builder.methodArgs = remainingMethodArgs

        String[] remainingMethodNameTokens = builder.reduceArray(methodNameTokens, tokensConsumed)

        builder.checkArgs(remainingMethodNameTokens, remainingMethodArgs)  

        if (remainingMethodNameTokens) {
            builder.parser.parse(builder, remainingMethodNameTokens, remainingMethodArgs)
        }
    }

    private Object[] reduceArgs(Object[] orig) {
        Object[] reduced = new Object[orig.length - 1]
        int origIndex = 1
        for (int i = 0; origIndex < orig.length; origIndex++) {
            reduced[i] = orig[origIndex];
            i++
        }
        return reduced
    }

    private static final DynamicFinderMethodParser nextParser = new ComparisonOperatorParser();
    private static final Log LOG = LogFactory.getLog(PropertyNameParser.class)	
}

