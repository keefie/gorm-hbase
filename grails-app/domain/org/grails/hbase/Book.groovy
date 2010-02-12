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

/**
 * Domain Class used for integration testing
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:02:56 AM
 */

class Book {

    String title
    Publisher publisher

    public String toString() {
       "$id - $title"
    }

    static constraints = {
        title()
        publisher()
    }
}
