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

import org.grails.hbase.api.finders.FinderFilterList
import org.grails.hbase.api.finders.Operator
import org.grails.hbase.util.HBaseLookupUtils
/**
 * Look for the name of of a logical operator (And / Or) at the start of a string
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Jan-2010
 */
class LogicalOperatorHandler implements DynamicFinderMethodHandler {

    def processToken(FinderFilterListBuilder builder, String[] methodNameTokens, Object[] methodArgs) {
        LOG.debug("Finder tokens received: $methodNameTokens")
        LOG.debug("Finder args received : $methodArgs")

        builder.handler = HBaseLookupUtils.getBean('hbase.gorm.finder.handler.leftParen')

        Operator operator = logicalOperators.get(methodNameTokens[0].toLowerCase())
        if (!operator) throw new MissingMethodException(builder.methodName, builder.domainClass.clazz, new Object[0])

        String[] remainingMethodNameTokens = builder.reduceArray(methodNameTokens, 1)
        if (!remainingMethodNameTokens) throw new MissingMethodException(builder.methodName, builder.domainClass.clazz, new Object[0])

        builder.addFinderFilter(new FinderFilterList(operator))

        builder.handler.processToken(builder, remainingMethodNameTokens, methodArgs)
    }

    def logicalOperators = [
        'and':Operator.AND,
        'or':Operator.OR
    ]

    private static final Log LOG = LogFactory.getLog(LogicalOperatorHandler.class)
}

