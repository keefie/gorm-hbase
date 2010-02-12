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

package org.grails.hbase.store

import org.codehaus.groovy.grails.commons.GrailsDomainClass

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.simpleframework.xml.transform.CurrencyTransform
import org.simpleframework.xml.transform.GregorianCalendarTransform
import org.simpleframework.xml.transform.LocaleTransform
import org.simpleframework.xml.transform.TimeZoneTransform
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

import org.apache.hadoop.hbase.util.Bytes

import org.grails.hbase.error.PropertyPersistRestoreException
import org.grails.hbase.error.PropertyPersistenceException
import org.grails.hbase.associations.IdSet

/**
 * Convert data to/from byte[] format
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 17, 2009
 * Time: 12:28:55 PM
 */

public class ByteArrayConverter {

    public byte[] getValueToPersist(fieldValue) {
        Serializer ser = new Persister()
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ser.write(fieldValue, bos)
        Bytes.toBytes(bos.toString())
    }

   
    public byte[] getValueToPersist(fieldValue, String fieldName) {
        LOG.debug("getValueToPersist() invoked with args $fieldValue and $fieldName of type ${fieldValue.class.name}")
        byte[] valueToPersist

        switch (fieldValue) {
            case boolean:
            case Boolean:
            case long:
            case Long:
            case short:
            case Short:
            case int:
            case Integer:
            case float:
            case Float:
            case double:
            case Double:
            case String:
               valueToPersist = Bytes.toBytes(fieldValue)
               break
            case char:
               case Character:
               char[] c = [fieldValue] as char[]
               valueToPersist = Bytes.toBytes(new String(c))
               break
            case byte:
               case Byte:
               valueToPersist = [fieldValue] as byte[]
               break
            case java.util.Date:
            case java.sql.Date:
            case java.sql.Time:
            case java.sql.Timestamp:
               valueToPersist = Bytes.toBytes(fieldValue.time)
               break
            case BigDecimal:
               valueToPersist = Bytes.toBytes(fieldValue.toString())
               break
            case BigInteger:
               valueToPersist = fieldValue.toByteArray()
               break
            case byte[]:
            case Byte[]:
               valueToPersist = fieldValue
               break
            case char[]:
            case Character[]:
               valueToPersist = Bytes.toBytes(new String(fieldValue))
               break
            case URI:
            case URL:
               valueToPersist = Bytes.toBytes(fieldValue.toString())
               break
            case Locale:
               LocaleTransform transform = new LocaleTransform()
               String stringValue = transform.write(fieldValue)
               valueToPersist = Bytes.toBytes(stringValue)
               break
            case GregorianCalendar:
               GregorianCalendarTransform transform = new GregorianCalendarTransform()
               String stringValue = transform.write(fieldValue)
               valueToPersist = Bytes.toBytes(stringValue)
               break
            case Currency:
               CurrencyTransform transform = new CurrencyTransform()
               String stringValue = transform.write(fieldValue)
               valueToPersist = Bytes.toBytes(stringValue)
               break
            case TimeZone:
               TimeZoneTransform transform = new TimeZoneTransform()
               String stringValue = transform.write(fieldValue)
               valueToPersist = Bytes.toBytes(stringValue)
               break
            case java.sql.Blob:
            case java.sql.Clob:
            case Serializable:
               LOG.warn("Persisting data as serializable data: ${fieldValue?.class?.name}")
               valueToPersist = this.toByteArray(fieldValue, fieldName)
               break
            default:
               throw new PropertyPersistenceException("Unable to persist type ${fieldValue?.class.name}")
        }

        return valueToPersist
    }

       
    public Object getPersistedValue(byte[] colBytes, Class dataType) {
        // TODO Inject persister
        LOG.debug("getPersistedValue() invoked for data type of $dataType")
        String xml = Bytes.toString(colBytes)
        Serializer ser = new Persister()
        ser.read(dataType, xml)   
    }


    public Object getPersistedValue(byte[] colBytes, String colName, GrailsDomainClass grailsClass, String fieldName) {
        def fieldType = grailsClass?.getPropertyByName(fieldName)?.type
        LOG.debug("getPersistedValue() determined property $fieldName to be of type $fieldType")

        def fieldValue = null

        switch (fieldType) {
            case boolean:
            case Boolean:
              fieldValue = Bytes.toBoolean(colBytes)
              break
            case long:
            case Long:
              fieldValue = Bytes.toLong(colBytes)
              break
            case short:
            case Short:
              fieldValue = Bytes.toShort(colBytes)
              break
            case int:
            case Integer:
              fieldValue = Bytes.toInt(colBytes)
              break
            case float:
            case Float:
              fieldValue = Bytes.toFloat(colBytes)
              break
            case double:
            case Double:
              fieldValue = Bytes.toDouble(colBytes)
              break
            case char:
            case Character:
              String s = Bytes.toString(colBytes)
              char[] c = s.toCharArray()
              fieldValue = c[0]
              break
            case byte:
            case Byte:
              fieldValue = colBytes[0]
              break
            case String:
              fieldValue = Bytes.toString(colBytes)
              break
            case java.sql.Date:
              long time = Bytes.toLong(colBytes)
              fieldValue = new java.sql.Date(time)
              break
            case java.sql.Time:
              long time = Bytes.toLong(colBytes)
              fieldValue = new java.sql.Time(time)
              break
            case java.sql.Timestamp:
              long time = Bytes.toLong(colBytes)
              fieldValue = new java.sql.Timestamp(time)
            break
              case java.util.Date:
              long time = Bytes.toLong(colBytes)
              fieldValue = new java.util.Date(time)
              break
            case BigDecimal:
              fieldValue = new BigDecimal(Bytes.toString(colBytes))
              break
            case BigInteger:
              fieldValue = new BigInteger(colBytes)
              break
            case byte[]:
            case Byte[]:
              fieldValue = colBytes
              break
            case char[]:
            case Character[]:
              String s = Bytes.toString(colBytes)
              fieldValue = s.toCharArray()
              break
            case URI:
              fieldValue = new URI(Bytes.toString(colBytes))
              break
            case URL:
              fieldValue = new URL(Bytes.toString(colBytes))
              break
            case Locale:
              LocaleTransform transform = new LocaleTransform()
              String stringValue = Bytes.toString(colBytes)
              fieldValue = transform.read(stringValue)
              break
            case Calendar:
            case GregorianCalendar:
              GregorianCalendarTransform transform = new GregorianCalendarTransform()
              String stringValue = Bytes.toString(colBytes)
              fieldValue = transform.read(stringValue)
              break
            case Currency:
              CurrencyTransform transform = new CurrencyTransform()
              String stringValue = Bytes.toString(colBytes)
              fieldValue = transform.read(stringValue)
              break
            case TimeZone:
              TimeZoneTransform transform = new TimeZoneTransform()
              String stringValue = Bytes.toString(colBytes)
              fieldValue = transform.read(stringValue)
              break  
            case java.sql.Blob:
            case java.sql.Clob:
            case Serializable:
              fieldValue = this.fromByteArray(colBytes, fieldName)
              break
            case Set:
              String xml = fieldValue = Bytes.toString(colBytes)
              Serializer ser = new Persister()
              IdSet setWrapper = ser.read(IdSet.class, xml)
              fieldValue = setWrapper.getSet()
              break
            default:
              throw new PropertyPersistRestoreException("Unable to restore type ${fieldType?.name}")
        }
        LOG.debug("getPersistedValue() returning value: $fieldValue")
        return fieldValue
    }


    public byte[] toByteArray(Object object, String fieldName) {
        LOG.debug("Invoking toByteArray() for $fieldName with value $object of type ${object.class.name}")
        try {
            def bos = new ByteArrayOutputStream()
            def oos = new ObjectOutputStream(bos)
            oos.writeObject(object)
            oos.flush()
            oos.close()
            bos.close()
            return bos.toByteArray()
        }
        catch (Exception ex) {
            throw new PropertyPersistRestoreException("Unable to persist property named $fieldName", ex)
        }
    }


    public Object fromByteArray(byte[] array, String fieldName) {
        // TODO Should test if serializable
        try {
            def ois = new org.apache.commons.io.input.ClassLoaderObjectInputStream(
                Thread.currentThread().getContextClassLoader(), new ByteArrayInputStream(array))
            def object = ois.readObject()
            ois.close()
            return object
        }
        catch (Exception ex) {
            throw new PropertyPersistRestoreException("Unable to read value for property named $fieldName from database", ex)
        }
    }


    private static final Log LOG = LogFactory.getLog(ByteArrayConverter.class)
}