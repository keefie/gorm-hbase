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
 * Exception that is thrown when an error occurs related to optimistic locking
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 7, 2009
 * Time: 8:59:06 AM
 */

// TODO Handle 'expected' errors more elegantly than throwing this exception
public class ConstraintViolationException extends RuntimeException {

    public ConstraintViolationException() {
        super()
    }

    public ConstraintViolationException(String message) {
        super(message)
    }

    public ConstraintViolationException(Throwable cause) {
        super(cause)
    }

    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause)
    }

}