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

import org.simpleframework.xml.Root
import org.simpleframework.xml.ElementArray

/**
 * Sets of References are not persisted as a java.util.Set they are saved as
 * this class
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 11/08/2009
 */

@Root
public class ReferenceSet {

    public ReferenceSet() {
    }

    public ReferenceSet(Set refs) {
       this.refs = refs as Reference[]
    }

    Set getSet() {
      refs as HashSet
    }

    @ElementArray
    Reference[] refs = new Reference[0]
}

