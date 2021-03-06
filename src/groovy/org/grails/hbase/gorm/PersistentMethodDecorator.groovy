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
package org.grails.hbase.gorm

/**
 * Wrapper to specific persistence method implementations 
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:46:59 AM
 */

public class PersistentMethodDecorator implements PersistentMethod {

    public PersistentMethodDecorator(PersistentMethod persistentMethodImpl) {
        this.persistentMethodImpl = persistentMethodImpl
    }

    def invoke(target, String methodName, Object[] arguments) {
        this.persistentMethodImpl.invoke(target, methodName, arguments)
    }

    private PersistentMethod persistentMethodImpl
}