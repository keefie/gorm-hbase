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
/**
 * Look for the name of the finder being invoked, i.e. findAll, findBy, findAllBy
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Jan-2010
 */
class FinderNameHandler implements DynamicFinderMethodHandler {

    def processToken(FinderFilterListBuilder builder, String[] methodNameTokens, Object[] methodArgs) {
        LOG.debug("Finder tokens received: $methodNameTokens")
        LOG.debug("Finder args received : $methodArgs")

        builder.handler = this.nextHandler
        
        int i = 0;
        StringBuffer finderNameBuffer =  new StringBuffer("");

        while (i < methodNameTokens.length && this.handles(methodNameTokens[i]))
        {
            finderNameBuffer << methodNameTokens[i]
            i++
        }

        builder.finderName = finderNameBuffer.toString()
        LOG.debug("Finder name: ${finderNameBuffer.toString()}")

        String[] remainingMethodNameTokens = builder.reduceArray(methodNameTokens, i)
        builder.handler.processToken(builder, remainingMethodNameTokens, methodArgs)
    }

    def handles(String token) {
        if (token.equals('find') || (token.equals('By') || token.equals('All'))) return true
        false
    }

    def nextHandler
    private static final Log LOG = LogFactory.getLog(FinderNameHandler.class)
}

