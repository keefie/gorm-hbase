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

import org.springframework.context.ApplicationContext

import org.grails.hbase.util.HBaseLookupUtils
import org.codehaus.groovy.grails.plugins.PluginManagerHolder

/**
 * Invoked when the plugin is started
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Aug 19, 2009
 * Time: 12:13:27 PM
 */
class HBasePluginSupport {

    static doWithSpring = {
        LOG.debug("Closure HBasePluginSupport.doWithSpring{} invoked")

       "hbase.configuration"(org.apache.hadoop.hbase.HBaseConfiguration) { bean ->
            bean.getBeanDefinition().setSingleton(true)
        }

       "hbase.byte.array.converter"(org.grails.hbase.store.ByteArrayConverter) { bean ->
            bean.getBeanDefinition().setSingleton(true)
        }

        "hbase.reference.table"( org.grails.hbase.associations.ReferenceTable) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
            byteArrayConverter = ref("hbase.byte.array.converter")
        }

        "hbase.admin"(org.apache.hadoop.hbase.client.HBaseAdmin,
            ref("hbase.configuration")) { bean ->
            bean.getBeanDefinition().setSingleton(true)
        }

        "hbase.table.manager"(org.grails.hbase.init.HBaseTableManager) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
            admin = ref("hbase.admin")
            referenceTable = ref("hbase.reference.table")
        }

        "hbase.row.id.generator"(org.grails.hbase.store.RowIdGenerator) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
        }

        "hbase.instance.mapper"(org.grails.hbase.store.InstanceMapper) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            referenceTable = ref("hbase.reference.table")
            byteArrayConverter = ref("hbase.byte.array.converter")
            rowIdGenerator = ref("hbase.row.id.generator")
        }

        "hbase.filter.factory"(org.grails.hbase.finders.FilterFactory) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            byteArrayConverter = ref("hbase.byte.array.converter")
            rowIdGenerator = ref("hbase.row.id.generator")
        }

       "hbase.gorm.count.impl"(org.grails.hbase.gorm.CountPersistentMethod) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
        }

       "hbase.gorm.delete.impl"(org.grails.hbase.gorm.DeletePersistentMethod) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
            instanceMapper = ref("hbase.instance.mapper")
            referenceTable = ref("hbase.reference.table")
            rowIdGenerator = ref("hbase.row.id.generator")
        }

        "hbase.gorm.get.impl"(org.grails.hbase.gorm.GetPersistentMethod) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
            instanceMapper = ref("hbase.instance.mapper")
            rowIdGenerator = ref("hbase.row.id.generator")
        }

        "hbase.gorm.list.impl"(org.grails.hbase.gorm.ListPersistentMethod) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
            instanceMapper = ref("hbase.instance.mapper")
            filterFactory = ref("hbase.filter.factory")
        }

        "hbase.gorm.save.impl"(org.grails.hbase.gorm.SavePersistentMethod) { bean ->
            bean.getBeanDefinition().setSingleton(true)
            conf = ref("hbase.configuration")
            instanceMapper = ref("hbase.instance.mapper")
            rowIdGenerator = ref("hbase.row.id.generator")
        }
        
        ["count", "delete", "get", "list", "save"].each { method ->
            "hbase.gorm.${method}.method"(org.grails.hbase.gorm.PersistentMethodDecorator,
                ref("hbase.gorm.${method}.impl")) { bean ->
                bean.getBeanDefinition().setSingleton(true)
            }
        }

    }

    static doWithApplicationContext = {ApplicationContext applicationContext ->
        LOG.debug("Closure HBasePluginSupport.doWithApplicationContext{} invoked with arg $applicationContext")

        assert !PluginManagerHolder.getPluginManager().hasGrailsPlugin("hibernate"),"hibernate plug-in conflicts with gorm-hbase plug-in"

        // Read data source configuration, setting defaults as required
        def dataSource = application.config.dataSource
        // TODO write tests for this <--- Even maybe figure out if this is ever invoked
        if (!dataSource) dataSource = new HBaseDefaults()

        def dbCreate = dataSource?.dbCreate
        if (!dbCreate) dbCreate = "create-drop"
        LOG.debug("Data Source configured with dbCreate set to $dbCreate")

        // TODO Complete dbCreate related processing
        if (dbCreate?.toUpperCase()?.equals("CREATE-DROP")) {
            def createIndexedTables = dataSource?.indexed
            LOG.debug ("Flag createIndexedTables set to $createIndexedTables")
            def tableManager = HBaseLookupUtils.getBean("hbase.table.manager")

            tableManager.createSequenceTable()
            tableManager.createReferenceTable()
      
            application.domainClasses.each {domainClass ->
                LOG.debug("Adding table for Domain Class $domainClass")
                tableManager.createDomainTable(domainClass, createIndexedTables)
            }

            LOG.debug("List of all store found :")
            tableManager.getTableNames().each {tn ->
                LOG.debug("- $tn")
            }
        }

        application.domainClasses.each {domainClass ->
            LOG.debug("Adding dbms related methods to Domain Class $domainClass")
            def domainClassManager = new HBaseDomainClassManager()
            domainClassManager.createQueryMethods(domainClass)
            domainClassManager.createPersistenceMethods(domainClass)
            domainClassManager.addLazyLoadingSupport(domainClass)
            domainClassManager.addDynamicFinders(domainClass)
        }
    }

    private static final Log LOG = LogFactory.getLog(HBasePluginSupport.class)
}

