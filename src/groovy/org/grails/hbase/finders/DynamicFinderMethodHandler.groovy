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
package org.grails.hbase.finders
/**
 * Break a method name apart into property names, comparison operators (Equal,
 * Greater, etc), and logical operators (and / or)
 *
 * @author Keith Thomas, redcoat.systems@gmail.com
 * created on 24-Jan-2010
 */
interface DynamicFinderMethodHandler {

    def processToken(FinderFilterListBuilder builder, String[] methodNameTokens, Object[] methodArgs)
    def handles(String token)
	
}

