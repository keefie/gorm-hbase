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

package org.grails.hbase.util

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Utility class for simple logic related to indexed HBase tables
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Oct 30, 2009
 * Time: 6:11:25 PM
 */

class HBaseIndexUtils {

    public static boolean isTypeIndexed(Class type) {        
        if (type instanceof java.sql.Clob
        || type instanceof java.sql.Clob) {
            LOG.debug ("Domain class property of type ${type.name} not indexed")
            return false
        }
        LOG.debug ("Domain class property of type ${type.name} being indexed for sorting features")
        return true
    }

    private static final Log LOG = LogFactory.getLog(HBaseIndexUtils.class)

}

