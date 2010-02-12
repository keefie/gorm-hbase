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

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.ApplicationHolder

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.NoSuchBeanDefinitionException
/**
 * Utility class for looking things up
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 9:19:25 AM
 */

public class HBaseLookupUtils {

    static GrailsApplication getGrailsApplication() {
        ApplicationContext applicationContext = ApplicationHolder.getApplication().getParentContext()
        applicationContext.getBean(GrailsApplication.APPLICATION_ID)
    }

    static Object getBean(String beanName) {
        try {
            ApplicationContext applicationContext = ApplicationHolder.getApplication().getMainContext()
            return applicationContext.getBean(beanName)
        }
        catch (NoSuchBeanDefinitionException ex) {
            LOG.error(ex.message, ex)
        }

        return null
    }

    private static final Log LOG = LogFactory.getLog(HBaseLookupUtils.class)

}