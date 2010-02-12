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
package org.grails.hbase.init

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
/**
 * If no data source is configured then default values are taken from
 * this class at startup
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Aug 29, 2009
 * Time: 8:11:29 AM
 */

class HBaseDefaults {

    HBaseDefaults() {
        LOG.debug("HBaseDefaults being created")
    }

    String dbCreate = "false"

    private static final Log LOG = LogFactory.getLog(HBaseDefaults.class)


}

