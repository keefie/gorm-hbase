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
import org.grails.hbase.init.HBasePluginSupport

class GormHbaseGrailsPlugin {
    // the plugin version
    def version = "0.2.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.1"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/**/*",
            "test/**/*",
            "target/**/*",
            "docs/**/*",
            "CodeNarcReport.html",
            "stacktrace.log"
    ]

    def author = "Keith Thomas"
    def authorEmail = "redcoat.systems@gmail.com"
    def title = "GORM-HBase Plug-in"
    def description = '''\\
A plug-in that emulates the behavior of the GORM-Hibernate plug-in against a Hadoop HDFS HBase 0.20.4 datasource
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/gorm-hbase"

    def doWithSpring = HBasePluginSupport.doWithSpring

    def doWithApplicationContext = HBasePluginSupport.doWithApplicationContext

    def doWithWebDescriptor = { xml ->
    }

    def doWithDynamicMethods = { ctx ->
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
    }
}
