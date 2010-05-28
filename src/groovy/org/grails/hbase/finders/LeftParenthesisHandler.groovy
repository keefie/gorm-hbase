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
 * Look for a left parenthesis indicating a nested filter is starting
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 21-May-2010
 */
class LeftParenthesisHandler implements DynamicFinderMethodHandler {

    def processToken(FinderFilterListBuilder builder, String[] methodNameTokens, Object[] methodArgs) {
        LOG.debug("Finder tokens received: $methodNameTokens")
        LOG.debug("Finder args received : $methodArgs")

        builder.handler = this.nextHandler

        if (methodNameTokens && methodNameTokens[0].equals('(')) {
            builder.startChild()
            String[] remainingMethodNameTokens = builder.reduceArray(methodNameTokens, 1)
            if (remainingMethodNameTokens) {
                builder.handler.processToken(builder, remainingMethodNameTokens, methodArgs)
            }
            return
        }
        builder.handler.processToken(builder, methodNameTokens, methodArgs)

    }

    def handles(String token) {
        if (token?.equals('(') || nextHandler.handles(token)) return true
        false
    }

    def nextHandler
    private static final Log LOG = LogFactory.getLog(LeftParenthesisHandler.class)
}

