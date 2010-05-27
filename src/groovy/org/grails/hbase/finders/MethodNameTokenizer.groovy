/**
 * Copyright 2010 Keith Thomas
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

import org.apache.commons.lang.StringUtils
/**
 * Takes a dynamic finder name and returns the finder inputs
 * in a common format
 *
 * @author Keith Thomas, keith.thomas@gmail.com
 * created on 25th March, 2009
 */
class MethodNameTokenizer implements TokenizerStrategy {

    protected MethodNameTokenizer(methodName, methodArgs) {
        this.methodName = methodName
        this.methodArgs = methodArgs
    }

    public void tokenize() {
        
    }

    public String[] getTokens() {
        return StringUtils.splitByCharacterTypeCamelCase(this.methodName)
    }

    public Object[] getMethodArgs() {
        return this.methodArgs
    }

    private String methodName
    private Object[] methodArgs
}



