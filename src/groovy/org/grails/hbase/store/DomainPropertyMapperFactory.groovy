/**
 * Copyright 2009 Keith Thomas
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

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

import org.grails.hbase.associations.ReferenceTable
import org.grails.hbase.error.UnsupportedFeatureException
/**
 * Creates instances of DomainPropertyMapper based upon the property type
 * @author Keith Thomas, keith.thomas@gmail.com
 * created on April 9th, 2010
 */
class DomainPropertyMapperFactory {

    public DomainPropertyMapper getMapper(GrailsDomainClassProperty prop, SaveCounters counters) {

        if (!prop?.isAssociation()) return new SimplePropertyMapper(counters, this.byteArrayConverter)

        if (prop?.isOneToOne()) 
                return  new OneToOnePropertyMapper(counters, this.byteArrayConverter, this.referenceTable)

         if (prop?.isOneToMany()) 
                return new OneToManyPropertyMapper(counters, this.byteArrayConverter, this.referenceTable)
                
         throw new UnsupportedFeatureException("Property ${prop.name} not a supported type")
        }

    def byteArrayConverter
    def referenceTable
}

