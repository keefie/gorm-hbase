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
package org.grails.hbase.error

/**
 * The type of a property in an instance of a associations class being persisted
 * is not (yet) supported by the Grails HBase plugin
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 7, 2009
 * Time: 8:59:06 AM
 */

public class PropertyPersistRestoreException extends RuntimeException {

    public PropertyPersistRestoreException() {
        super()
    }

    public PropertyPersistRestoreException(String message) {
        super(message)
    }

    public PropertyPersistRestoreException(Throwable cause) {
        super(cause)
    }

    public PropertyPersistRestoreException(String message, Throwable cause) {
        super(message, cause)
    }

}