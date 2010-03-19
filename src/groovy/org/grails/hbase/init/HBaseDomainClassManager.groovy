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
package org.grails.hbase.init

import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.util.HBaseLookupUtils

import groovy.text.SimpleTemplateEngine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Add persistence related methods to associations classes
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 10:32:24 AM
 */

public class HBaseDomainClassManager {

    def createQueryMethods(domainClass) {
        LOG.debug("Creating query methods for domain class ${domainClass.name}")

        def domainClassType = domainClass.clazz
        def domainMetaClass = domainClass.metaClass

        def countMethod = HBaseLookupUtils.getBean("hbase.gorm.count.method")
        domainMetaClass.static.count = {-> countMethod.invoke(domainClassType, "count", [] as Object[])}

        def getMethod =  HBaseLookupUtils.getBean("hbase.gorm.get.method")
        domainMetaClass.static.get = {id -> getMethod.invoke(domainClassType, "get", [id] as Object[])}

        def listMethod =  HBaseLookupUtils.getBean("hbase.gorm.list.method")
        domainMetaClass.static.list = {-> listMethod.invoke(domainClassType, "list", [] as Object[])}
        domainMetaClass.static.list = {Map args -> listMethod.invoke(domainClassType, "list", [args] as Object[])}
        domainMetaClass.static.list = {Object[] args -> listMethod.invoke(domainClassType, "list", args)}
    }

    def createPersistenceMethods(domainClass) {
        LOG.debug("Creating persistence methods for domain class ${domainClass.name}")

        def domainMetaClass = domainClass.metaClass

        def deleteMethod =  HBaseLookupUtils.getBean("hbase.gorm.delete.method")
        domainMetaClass.delete = {id -> deleteMethod.invoke(delegate, "delete", [id] as Object[])}

        def saveMethod =  HBaseLookupUtils.getBean("hbase.gorm.save.method")
        domainMetaClass.save = {Boolean validate ->
            saveMethod.invoke(delegate, "save", [validate] as Object[])
        }
        domainMetaClass.save = {Map args ->
            saveMethod.invoke(delegate, "save", [args] as Object[])
        }
        domainMetaClass.save = {->
            saveMethod.invoke(delegate, "save", [] as Object[])
        }
    }

    
    def addLazyLoadingSupport(domainClass) {

        HBaseNameUtils.getPersistentPropertiesNames(domainClass).each {fieldName ->
            def prop = domainClass?.getPropertyByName(fieldName)
            
            if (prop?.isAssociation()) {
                def otherDomainClass = prop?.getReferencedDomainClass()
                def associationPropertyName = fieldName

                def getterName = "get${associationPropertyName.substring(0, 1).toUpperCase()}${associationPropertyName.substring(1)}"

                def getterNoArgs

                if (prop?.isOneToOne()) {
                    def id_fieldName = "${associationPropertyName}__association__id__"
                    domainClass.clazz.metaClass."${id_fieldName}" = domainClass.clazz.newInstance().id

                    Closure getterTwoArgs =  { assoc__class__,  assoc__id__field__ ->
                        LOG.debug("${assoc__id__field__} closure invoked for delegate ${delegate.class.name}")
                        assoc__class__.get(delegate."${assoc__id__field__}")
                    }
                    getterNoArgs = getterTwoArgs.curry(otherDomainClass.clazz, id_fieldName)
                    domainClass.clazz.metaClass."${getterName}" = getterNoArgs
                }

                else if (prop?.isOneToMany()) {
                    def id_list_fieldName = "${associationPropertyName}__association__id__list__"
                    domainClass.clazz.metaClass."${id_list_fieldName}" = [] as Set

                    def binding = [
                                    "domainClass": otherDomainClass.clazz.name,
                                    "id_list_fieldName": id_list_fieldName
                    ]
                    def engine = new SimpleTemplateEngine()
                    def template = engine.createTemplate(getterClosure).make(binding)
                    LOG.debug ("Closure created by script: $template")

                    def evalBinding = new Binding(['LOG':LOG])
                    def shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), evalBinding)
                    getterNoArgs = shell.evaluate(template.toString())
                    domainClass.clazz.metaClass."${getterName}" =  getterNoArgs

                    // Construct setter closure to accept new association set of values
                    def setterName = "set${getterName.substring(3)}"
                    binding = [
                                    "domainClass": domainClass.name,
                                    "setterName": setterName,
                                    "id_list_fieldName": id_list_fieldName
                    ]
                    engine = new SimpleTemplateEngine()
                    template = engine.createTemplate(setterClosure).make(binding)
                    LOG.debug ("Closure created by script: $template")
                    def setterOneArg = Eval.me('LOG', LOG, template.toString())
                    domainClass.clazz.metaClass."${setterName}" = setterOneArg

                    // Construct addTo closure to accept new association  to be added to existing set of values
                    def addToName = "addTo${getterName.substring(3)}"
                    binding = [
                                    "domainClass": domainClass.name,
                                    "addToName": addToName,
                                    "id_list_fieldName": id_list_fieldName
                    ]
                    engine = new SimpleTemplateEngine()
                    template = engine.createTemplate(addToClosure).make(binding)
                    LOG.debug ("Closure created by script: $template")
                    def addToOneArg = Eval.me('LOG', LOG, template.toString())
                    domainClass.clazz.metaClass."${addToName}" = addToOneArg

                    // Construct removeFrom closure to accept new association  to be added to existing set of values
                    def removeFromName = "removeFrom${getterName.substring(3)}"
                    binding = [
                                    "domainClass": domainClass.name,
                                    "removeFromName": removeFromName,
                                    "id_list_fieldName": id_list_fieldName
                    ]
                    engine = new SimpleTemplateEngine()
                    template = engine.createTemplate(removeFromClosure).make(binding)
                    LOG.debug ("Closure created by script: $template")
                    def removeFromOneArg = Eval.me('LOG', LOG, template.toString())
                    domainClass.clazz.metaClass."${removeFromName}" = removeFromOneArg
                }
            }
        }
    }

    def addDynamicFinders(domainClass) {
        LOG.debug("Adding dynamic finder support to ${domainClass.name}")
        def mc = domainClass.clazz.metaClass
        
        mc.static.invokeMethod = {String methodName, args ->
            def dynamicFinderManager = new org.grails.hbase.finders.DynamicFinderManager()
            def result
            if (dynamicFinderManager?.isValidDynamicFinderMethodNameFormat(methodName)) {
                result = dynamicFinderManager?.execute(delegate, methodName, args)
                return result
            }
            def metaMethod = delegate.metaClass.getStaticMetaMethod("$methodName", args)

            if (metaMethod) result = metaMethod.invoke(delegate, args)
            else throw new MissingMethodException(methodName, delegate, args)
            return result
        }
    }

    def getterClosure = '''\n
          Closure getterNoArgs = {
             LOG.debug("Getter closure for ${id_list_fieldName} invoked")
             def result = [] as Set
             delegate.${id_list_fieldName}.each { assoc__ ->
                if (assoc__ instanceof ${domainClass}) result.add(assoc__)
                else result.add(${domainClass}.get(assoc__))
              }
              return result
          }
          return getterNoArgs
       '''

    def setterClosure = '''\n
          Closure setterOneArg = { Set arg ->
             LOG.debug("Setter closure, ${setterName}(...), invoked for ${domainClass}")
             delegate.$id_list_fieldName = arg
          }
          return setterOneArg
       '''
    
    def addToClosure = '''\n
          Closure addToOneArg = {arg ->
             LOG.debug("AddTo closure, ${addToName}(...), invoked for ${domainClass}")
             if (!delegate.${id_list_fieldName}) delegate.${id_list_fieldName} = [] as Set
             if (arg.id) delegate.${id_list_fieldName}.add(arg.id)
             else delegate.${id_list_fieldName}.add(arg)
          }
          return addToOneArg
       '''

    def removeFromClosure = '''\n
          Closure removeFromOneArg = {arg ->
             LOG.debug("RemoveFrom closure, ${removeFromName}(...), invoked for ${domainClass}")

             // Either the associations class or just it's id might be stored, so try and remove both/either
             if (arg.id) delegate.${id_list_fieldName}.remove(arg.id)
             delegate.${id_list_fieldName}.remove(arg)
          }
          return removeFromOneArg
       '''

    private static final Log LOG = LogFactory.getLog(HBaseDomainClassManager.class)
}