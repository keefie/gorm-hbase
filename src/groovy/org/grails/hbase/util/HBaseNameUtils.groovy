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

import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import grails.util.GrailsNameUtils

import org.apache.hadoop.hbase.util.Bytes

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.store.Constants
/**
 * Utility class for converting different name types related to HBase persistences
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 9:19:25 AM
 */

public class HBaseNameUtils {

    // TODO Write tests

    /**
     * Convert a list of String fieldNames and convert them to byte[][] database column names
     */
    static byte[][] getColumnNamesArray(List fieldNames) {
        def colNames = []
        fieldNames.each {fieldName ->
            colNames <<  Bytes.toBytes(Constants.DEFAULT_DATA_FAMILY_STRING +
                Constants.DEFAULT_FAMILY_NAME_DELIMITER +
                HBaseNameUtils.getDomainColumnName(fieldName))
        }

        byte[][] cols = colNames as byte[][]

        return cols
    }


    static String getDomainTableName(domainClass) {
        if (domainClass instanceof java.lang.String) {
            def domainClassName = domainClass
            domainClass = HBaseNameUtils.getDomainClass(domainClassName)
        }

        def databaseMapping = GrailsClassUtils.getStaticPropertyValue(domainClass.getClazz(), "mapping")
        def tableName = databaseMapping?.mapping?.tableName

        if (!tableName) tableName = GrailsNameUtils.getNaturalName(domainClass.name)?.replace(' ', '_')?.toUpperCase()

        def grailsApplication = HBaseLookupUtils.getGrailsApplication()
        def applicationName = grailsApplication.metadata.'app.name'
        String qualifiedTableName = applicationName?.replace(' ', '_')?.replace('-', '_')?.toUpperCase() +
                                                                          "_$tableName"

        LOG.debug("Table name for ${domainClass.name} determined to be $qualifiedTableName")

        return qualifiedTableName
    }


    /**
     * Persistent property secondary index table name
     */
    static String getPersistentPropertyAsTableName(String propertyName) {
        return GrailsNameUtils.getNaturalName(propertyName)?.toUpperCase()?.replace(' ', '_')
    }


    /**
     * All persistent properties for a associations class
     */
    static List getPersistentPropertiesNames(domainClass) {
        if (domainClass instanceof java.lang.String) {
            def domainClassName = domainClass
            domainClass = HBaseNameUtils.getDomainClass(domainClassName)
        }
        def propNameList = []
        domainClass.getPersistentProperties().each {pp ->
            propNameList << pp?.name
        }
  
        return propNameList
    }


    /**
     * Pass in the camelcase format property name and get back the database column name, e.g
     * 'myLittleField' will begat 'my_little_field'
     */
    static String getDomainColumnName(String fieldName) {
        GrailsNameUtils.getNaturalName(fieldName)?.replace(' ', '_')?.toLowerCase()
    }


    /**
     * Pass in the database column name and get back the camelcase format property name, e.g
     * 'my_little_field'will begat 'myLittleField'
     */
    static getDomainFieldName(String columnName) {
        def fieldName = new StringBuffer()
        def firstToken = true
        columnName?.tokenize("_")?.each {t ->
            def f = t
            if (!firstToken) f = t?.substring(0, 1)?.toUpperCase() + t?.substring(1)
            fieldName << f
            firstToken = false
        }
        fieldName?.toString()
    }


    static String getSequenceTableName() {
        if (!HBaseNameUtils.sequenceTableName) {
            def grailsApplication = HBaseLookupUtils.getGrailsApplication()
            def applicationName = grailsApplication.metadata.'app.name'
            HBaseNameUtils.sequenceTableName = applicationName?.replace(' ', '_')?.replace('-', '_')?.toUpperCase() +
              "__SEQUENCES"
            LOG.debug("Sequence table name constructed as $sequenceTableName")
        }
        return HBaseNameUtils.sequenceTableName
    }


    static String getReferenceTableName() {
        if (!HBaseNameUtils.referenceTableName) {
            def grailsApplication = HBaseLookupUtils.getGrailsApplication()
            def applicationName = grailsApplication.metadata.'app.name'
            HBaseNameUtils.referenceTableName = applicationName?.replace(' ', '_')?.replace('-', '_')?.toUpperCase() +
              "__REFERENCES"
            LOG.debug("Reference table name constructed as $referenceTableName")
        }
        return HBaseNameUtils.referenceTableName
    }


    static byte[] getReferenceRowKey(String domainClassName, byte[] propertyValue) {
        LOG.debug("Creating reference row key from '${domainClassName}' and '${propertyValue}'")

        def grailsApplication = HBaseLookupUtils.getGrailsApplication()
        String applicationName = grailsApplication.metadata.'app.name'

        String keyPrefixString = applicationName?.replace(' ', '_')?.replace('-', '_')?.toUpperCase() + "_" +
        HBaseNameUtils.getDomainTableName(domainClassName) + "_"
        byte[] keyPrefix = Bytes.toBytes(keyPrefixString)

        byte[] row = new byte[keyPrefix.length + propertyValue.length]
        for (int i = 0; i < keyPrefix.length; i++) {
            row[i] = keyPrefix[i]
            if (i < propertyValue.length) row[i + keyPrefix.length] = propertyValue[i]
        }

        LOG.debug("Row key created = $row")
        return row
    }


    static GrailsDomainClass getDomainClass(String domainClassName) {
        def grailsApplication = HBaseLookupUtils.getGrailsApplication()
        // Class cast added to stop IDE warning message being generated
        (GrailsDomainClass) grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, domainClassName)
    }
    
    private static String sequenceTableName
    private static String referenceTableName
    private static final Log LOG = LogFactory.getLog(HBaseNameUtils.class)
}