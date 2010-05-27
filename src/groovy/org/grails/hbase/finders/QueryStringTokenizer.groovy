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

import java.io.Reader
import java.io.StringReader
import antlr.Token
import antlr.TokenStreamException
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.ghql.LexerRules
import org.grails.hbase.ghql.ParserRulesTokenTypes
import org.grails.hbase.util.HBaseFinderUtils

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import groovy.text.SimpleTemplateEngine
/**
 * Takes a finder with a query string and returns the finder inputs
 * in a common format
 *
 * @author Keith Thomas, keith.thomas@gmail.com
 * created on 25th March, 2009
 */
class QueryStringTokenizer implements TokenizerStrategy {
    
    protected QueryStringTokenizer(GrailsDomainClass domainClass, String methodName, Object[] methodArgs) {
        LOG.debug("Query string for ${domainClass} received as: ${methodArgs[0]}")
        this.domainClass = domainClass
        this.methodArgs = methodArgs
        this.methodName = methodName
    }

    public void tokenize() {
        List queryTokens = StringUtils.splitByCharacterTypeCamelCase(this.methodName) as List
        def reader = new StringReader("${this.methodArgs[0]}")
        def lexer  = new LexerRules(reader)
        def type = ParserRulesTokenTypes.WS
        int nested = 0
        String propertyName

        while(type != ParserRulesTokenTypes.EOF)
        {
            def token = lexer.nextToken();
            type = token.getType()

            switch (type) {
                case(ParserRulesTokenTypes.LPAREN):
                   LOG.debug("Left parenthesis found by Lexer")
                   queryTokens << token.getText()
                   nested++
                   break
                case(ParserRulesTokenTypes.RPAREN):
                   LOG.debug("Right parenthesis found by Lexer")
                   queryTokens << token.getText()
                   nested--
                   break
                case(ParserRulesTokenTypes.NUMBER):
                   LOG.debug("Number found by Lexer: ${token.getText()}")
                   def number = HBaseFinderUtils.refineNumber(this.domainClass, propertyName, token.getText())
                   newArgsStore << number
                   break
                case(ParserRulesTokenTypes.BYTE):
                   LOG.debug("Byte found by Lexer: ${token.getText()}")
                   String stringValue = token.getText()
                   def text = 'byte abyte = $value'
                   def binding = ["value": stringValue]
                   def engine = new SimpleTemplateEngine()
                   def template = engine.createTemplate(text).make(binding)
                   newArgsStore << Eval.me(template.toString())            
                   break
                case(ParserRulesTokenTypes.DATE):             
                   LOG.debug("Date found by Lexer: ${token.getText()}")
                   if (HBaseFinderUtils.isDate(this.domainClass, propertyName)) newArgsStore << this.getDateBasedInstance(this.domainClass, propertyName, token.getText())
                   else newArgsStore << token.getText()
                   break
                case(ParserRulesTokenTypes.STRING):
                   LOG.debug("String found by Lexer: ${token.getText()}")
                   newArgsStore << token.getText()
                   break
                case(ParserRulesTokenTypes.BOOLEAN):
                   LOG.debug("Boolean found by Lexer: ${token.getText()}")
                   newArgsStore << new Boolean(token.getText()).booleanValue()
                   break
                case (ParserRulesTokenTypes.COMPARISON_OPERATOR):
                case (ParserRulesTokenTypes.EQUAL):
                case (ParserRulesTokenTypes.NOT_EQUAL):
                case (ParserRulesTokenTypes.LT):
                case (ParserRulesTokenTypes.LE):
                case (ParserRulesTokenTypes.GT):
                case (ParserRulesTokenTypes.GE):
                   LOG.debug("Operator found by Lexer: ${token.getText()}")
                   queryTokens << token.getText()?.toLowerCase()
                   break        
                case(ParserRulesTokenTypes.EOF):
                   break
                case(ParserRulesTokenTypes.PROPERTY_NAME):
                   LOG.debug("Property Name value found by Lexer: ${token.getText()}")
                   propertyName = token.getText()
                   queryTokens << propertyName
                   break   
                case(ParserRulesTokenTypes.AND):
                case(ParserRulesTokenTypes.OR):
                   LOG.debug("Logical Operator value found by Lexer: ${token.getText()}")
                   queryTokens << token.getText()
                   break
                default:
                   LOG.debug("Invalid value found by Lexer: ${token.getText()}")
                   throw new MissingMethodException(this.methodName, this.domainClass.clazz, this.methodArgs[0])
            }
            if (nested < 0) throw new MissingMethodException(this.methodName, this.domainClass.clazz, this.methodArgs[0])
        }

        if (nested != 0) throw new MissingMethodException(this.methodName, this.domainClass.clazz, this.methodArgs[0])
        this.tokens = queryTokens.toArray(new String[0])
        this.insertFoundValuesIntoArgs()   
    }

    public String[] getTokens() {
        LOG.debug("Query string created tokens: ${this.tokens}")
        return this.tokens
    }

    public Object[] getMethodArgs() {
        LOG.debug("Query string created method args: ${this.methodArgs}")
        return this.methodArgs
    }

    // Returns number of tokens used to create the value, can be more than one
    private int storeValue(String value) {
        def typedValue
        def skipValues = 0

        if (value.startsWith("'")) {
            if (value.endsWith("'")) {
                if (value.length() == 2) typedValue = ''
                else typedValue = value.substring(1, value.length() - 1)
            }
        }

        newArgsStore << typedValue

        return skipValues
    }

    private void insertFoundValuesIntoArgs() {
        Object newArgs = new Object[methodArgs.size() + newArgsStore.size() - 1]

        int i = 0
        newArgsStore.each { na ->
            newArgs[i] = na
            i++
        }

        for (int j = 1; j < this.methodArgs.size(); j++) {
            newArgs[i] = this.methodArgs[j]
            i++
        }

        this.methodArgs = newArgs
    }

    private String extractQueryStringFromArgs() {
        String queryString = methodArgs[0]
        
        Object[] reduced = new Object[this.methodArgs.size() - 1]
        for (int i = 1; i < this.methodArgs.size(); i++) {
            reduced[i - 1] = this.methodArgs[i];
            i++
        }
        this.methodArgs = reduced

        return queryString
    }

    private Object getDateBasedInstance(GrailsDomainClass domainClass, String propertyName, String propertyValue) {
       def date = Date.parse(dateFormats.get(propertyValue?.trim().length()), propertyValue)

       def property = domainClass.getPropertyByName(propertyName)
       Class type = property.getType()
       LOG.debug("Date based type found, type=${type.name}")
       
       if (type == date.class) return date

       def instance = type.newInstance([date.getTime()] as Object[])
       LOG.debug("Date based instance created, type=${type.name}, value=${instance}}")
       return instance
    }

    private String methodName
    private Object[] methodArgs
    private String[] tokens = new String[0]
    private List newArgsStore = []
    private GrailsDomainClass domainClass
    private Map dateFormats = [10:'yyyy-MM-dd', 19:'yyyy-MM-dd HH:mm:ss', 23:'yyyy-MM-dd HH:mm:ss.SSS']

    private static final Log LOG = LogFactory.getLog(QueryStringTokenizer.class)
}



