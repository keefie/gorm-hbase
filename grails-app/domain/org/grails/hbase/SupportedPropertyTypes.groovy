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
package org.grails.hbase

import org.grails.hbase.associations.Reference

/**
 * Domain class used by Integration tests to test persist/restore
 * of supported types
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Oct 22, 2009
 * Time: 7:55:32 PM
 */

class SupportedPropertyTypes {

    boolean myboolean
    long mylong
    short myshort
    int  myint
    byte  mybyte
    float  myfloat
    double  mydouble
    char  mychar
    Boolean  myBooleanClass // added 'Class' because Grails itself seems to get confused
    Long  myLong
    Short  myShort
    Integer  myInteger
    Byte  myByteClass
    Float  myFloat
    Double  myDouble
    Character  myCharacter
    String  myString
    java.util.Date  myUtilDate
    java.sql.Time myTime
    java.sql.Timestamp  myTimestamp
    java.sql.Date  mySqlDate
    BigDecimal  myBigDecimal
    BigInteger  myBigInteger
    Locale  myLocale
    Calendar  myCalendar
    GregorianCalendar  myGregorianCalendar
    Currency myCurrency
    TimeZone myTimeZone
    byte[]  mybytearray
    Byte[]  myByteArrayClass
    char[]  mychararray
    Character[] myCharacterArrayClass
    java.sql.Blob  myBlob
    java.sql.Clob  myClob
    Serializable mySerializable
    URI myUri
    URL myUrl
    Reference reference
    Reference[] references

    static constraints = {
    }
}
