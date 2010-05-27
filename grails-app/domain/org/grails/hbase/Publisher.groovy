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

class Publisher {

    String name
    String city
    Date   published

    public String toString() {
        name
    }

    public boolean equals(Object obj) {
        if (!obj) return false
        boolean equals = false
        
        if (this.name == null && obj.name == null) equals = true
        else equals = this.name.equals(obj.name)
        if (!equals) return equals

        if (this.city == null && obj.city == null) equals = true
        else equals = this.city.equals(obj.city)
        if (!equals) return equals

        if (this.published == null && obj.published == null) equals = true
        else equals = this.published.equals(obj.published)
        return equals
    }

    // TODO implement hasCode() properly
    public int hashCode() {
        int hc = 123456
        if(name) hc = hc + name.hashCode()
        if(city) hc = hc + city.hashCode()
        if(published) hc = hc + published.hashCode()
        return hc
    }

    static constraints = {
    }
}
