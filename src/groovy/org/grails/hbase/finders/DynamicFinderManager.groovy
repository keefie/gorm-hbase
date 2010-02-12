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
import org.grails.hbase.api.finders.FinderFilter
import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.util.HBaseFinderUtils


/**
 * Takes care of dynamic finder support
 * 
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 19-Nov-2009
 */
class DynamicFinderManager {

    public boolean isValidDynamicFinderMethodNameFormat(methodName) {
        if (methodName ==~ finders) return true
        return false
    }

    def execute(domainClass, methodName, Object[] args) {
        LOG.debug("Execute() invoked as: ${methodName}(${args}) - ${args.class.name}}")

        if (!this.isValidDynamicFinderMethodNameFormat(methodName)) return null

        if (HBaseFinderUtils.hasFilter(args) || methodName.equals('findAll')) return domainClass.list(args)

        /*
        def finderFilterListBuilder = new FinderFilterListBuilderX()
        args = finderFilterListBuilder.createFinderFilters(domainClass, methodName, args)
        */

        def builder = new FinderFilterListBuilder(HBaseNameUtils.getDomainClass(domainClass.name))
        builder.methodName = methodName
        builder.methodArgs = args
        def finders = builder.finderFilters

        def reducedArgs = builder.methodArgs
        LOG.debug("Reduced args $reducedArgs")
        
        if (reducedArgs) args = [finders, reducedArgs[0]] as Object[]
        else args = [finders] as Object[]
        
        LOG.debug("List() args built: $args")

        if (!methodName.startsWith('findBy')) return domainClass.list(args)

        args = this.setMaxRecords(args, 1)
        def list = domainClass.list(args)
        if (!list.size()) return null
        return list.get(0)
    }


    def setMaxRecords(args, max) {
        if(args.size() && args[args.size() - 1] instanceof Map ) {
            args[args.size() - 1].'max' = max
            return args
        }
        
        def arguments = new Object[args.size() + 1]
        int i = 0
        args.each {a ->
            arguments[i] = a
            i++
        }
        arguments[i] = ['max':max]

        return arguments
    }

    def finders = /findAll||findAllBy([A-Z][a-z|0-9]+)+||findBy([A-Z][a-z|0-9]+)+/

    private static final Log LOG = LogFactory.getLog(DynamicFinderManager.class)
}

