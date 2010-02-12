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

import org.grails.hbase.api.finders.Operator

/**
 * Look for the name of of a comparison operator (Equal, Greater, etc) at the
 * start of a string
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Jan-2010
 */
class ComparisonOperatorParser implements DynamicFinderMethodParser {

    def parse(FinderFilterListBuilder builder, String[] methodNameTokens, Object[] methodArgs) {
        LOG.debug("Finder tokens received: $methodNameTokens")
        LOG.debug("Finder args received : $methodArgs")

        builder.parser = nextParser

        Operator operator
        StringBuffer operatorNameBuffer = new StringBuffer()
        int tokensConsumed

        // Keep looping through the tokens until we find the longest valid operator name
        for (int i = 0; i < methodNameTokens.length; i++) {
            operatorNameBuffer << methodNameTokens[i]
            String operatorName = operatorNameBuffer.toString()

            LOG.debug("Examining candidate comparison operator name: $operatorName")
            Operator comparisonOperator = comparisonOperators.get(operatorName)
            if (comparisonOperator) {
                tokensConsumed = i + 1
                operator =  comparisonOperator
            }
        }

        LOG.debug("Exiting loop with operator set to: $operator")
        String[] remainingMethodNameTokens
        if (operator) {
            builder.getCurrentFilter().operator = operator
            LOG.debug("FinderFilter built: ${builder.finderFilters}")
            remainingMethodNameTokens = builder.reduceArray(methodNameTokens, tokensConsumed)
        }
        else remainingMethodNameTokens = methodNameTokens

        builder.checkArgs(remainingMethodNameTokens, methodArgs)
        
        if (remainingMethodNameTokens) builder.parser.parse(builder, remainingMethodNameTokens, methodArgs)
    }

    def comparisonOperators = [
        'LessThan':Operator.LESS,
        'LessThanEquals':Operator.LESS_OR_EQUAL,
        'GreaterThan':Operator.GREATER,
        'GreaterThanEquals':Operator.GREATER_OR_EQUAL,
        'NotEqual':Operator.NOT_EQUAL
    ]

    private static final DynamicFinderMethodParser nextParser = new LogicalOperatorParser();
    private static final Log LOG = LogFactory.getLog(ComparisonOperatorParser.class)
}

