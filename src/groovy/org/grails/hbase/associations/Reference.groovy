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

package org.grails.hbase.associations

import org.apache.hadoop.hbase.util.Bytes

import org.simpleframework.xml.Root
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementArray

/**
 * Stores an reference to/from another row
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 16, 2009
 * Time: 4:42:44 PM
 */
@Root
public class Reference implements Serializable {

    @Element
    String tableName
    @Element
    String columnName
    @ElementArray
    byte[] keyValue

    public boolean equals(Object other) {
        if (!other) return false
        if (!other instanceof Reference) return false

        Reference otherRef = (Reference)other

        if (!this.tableName?.equals(otherRef.tableName)) return false
        if (!this.columnName?.equals(otherRef.columnName)) return false
        if (this.keyValue?.length != otherRef.keyValue?.length) return false

        return Bytes.equals(this.keyValue, otherRef.keyValue)
    }


    public int hashCode() {
        int hash = 345
        hash = hash + this.columnName?.hashCode()
        hash = hash + this.columnName?.hashCode()
        hash = hash + Bytes.hashCode(this.keyValue)

        return hash
    }

    
    public String toString() {
       "$tableName : $columnName : $keyValue"
    }


    static final long serialVersionUID = -6294946617063040209L

}