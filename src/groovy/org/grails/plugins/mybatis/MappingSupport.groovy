package org.grails.plugins.mybatis

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource

class MappingSupport {
  def gatewaysPath = 'grails-app/gateways'

  private log = LogFactory.getLog(MappingSupport)

  def static allowDocTypeDeclaration(XmlSlurper xmlSlurper) {
    // three arguments XmlSluper constructor doesn't exist on Grails 2.0.x
    xmlSlurper.setFeature('http://apache.org/xml/features/disallow-doctype-decl', false)
  }

  def static allowHttpOnJava8OrHigher(XmlSlurper xmlSlurper) {
    // workaround for Java 8 and http method restriction on loading external DTDs
    def javaMajorVersion = (System.getProperty('java.version') =~ /\d\.(\d*)\..*/)[0][1] as Integer
    if (javaMajorVersion >= 8) {
      xmlSlurper.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false)
    }
  }

  def getArtefactMapperResource(artefact){
    def filename = getMappingFileFileName(artefact.fullName)
    def mappingFile = getResource(filename)

    return mappingFile
  }

  private Resource getResource(String name) {
    def resource = new FileSystemResource("$gatewaysPath/$name")

    if (!resource.exists()) {
      resource = new ClassPathResource(name)
    }

    return resource
  }

  def getOperationIds(mapping) {
    def operationIds = [:]

    ['select', 'insert', 'update', 'delete', 'procedure', 'statement'].each { opType ->
      operationIds[opType] = mapping."$opType".list().collect { it.@id.text() }
    }

    log.debug("Found mappings: " + operationIds)

    return operationIds
  }

  private invokeMybatis(id, opType, multiplicity, args, application, Class grailsClass) {
    def opName = "${opType}${multiplicity}"
    def dataSourceToUse = GrailsClassUtils.getStaticPropertyValue(grailsClass, 'dataSourceName') ?: 'dataSource'

    def sessionTemplate = application.mainContext.getBean("sqlSessionTemplate_$dataSourceToUse")

    return sessionTemplate."$opName"(id, args)
  }

  def registerMappings(GrailsClass grailsClass, GrailsApplication application) {
    log.debug("Registering mappings for class " + grailsClass)
    def operationIds = [:]
    def namespace
    String filename = getMappingFileFileName(grailsClass.fullName)

    try {
      def mappingXml = loadMappingFile(filename, grailsClass)
      namespace = mappingXml.@namespace.text()
      operationIds = getOperationIds(mappingXml)
    }
    catch (e) {
      log.error("Failed to load myBatis SQL map file ${filename}", e)
    }

    def mc = grailsClass.clazz.metaClass

    operationIds.each { String opType, ids ->
      ids.each {String id ->
        def myBatisOp = "${namespace}.$id"

        if (grailsClass.clazz.methods.find { it.name == id}) {
          id = "generated${id[0].toUpperCase()}${id.substring(1)}"
        }

        log.debug "Adding method $id to metaclass $mc"

        def postFix = (opType == 'select' ? (isListOp(grailsClass, id) ? "List" : "One") : "")

        mc."$id" = { args ->
          invokeMybatis(myBatisOp, opType, postFix, args, application, grailsClass.clazz)
        }
      }
    }
  }

  //List operation is one that ends with "List"
  private String shortenName(String name) {
    if (name.indexOf('Gateway') != -1) {
      name = name.substring(0, name.lastIndexOf('Gateway'))
    }
    return name
  }

  def isListOp(GrailsClass grailsClass, String operationId) {
    def listOps = GrailsClassUtils.getStaticPropertyValue(grailsClass.clazz, "forceAsListOps")

    if (listOps?.find {it == operationId}) {
      return true
    }

    return operationId.endsWith('List')
  }

  private loadMappingFile(String filename, GrailsClass grailsClass, boolean validating = true) {
    def mappingFileText
    def gatewaysDir = new File("./${gatewaysPath}")

    def mappingFile = new File(gatewaysDir, filename)
    def resourceUrl = grailsClass.clazz.getResource("/" + filename)

    if (gatewaysDir.exists() && mappingFile.exists()) {
      mappingFileText = mappingFile.text
    }
    else {
      mappingFileText = resourceUrl.text
    }

    def resolver = { publicId, systemId ->
      if (publicId == "-//mybatis.org//DTD Mapper 3.0//EN") {
        return new InputSource(grailsClass.getClass().classLoader.getResourceAsStream('org/apache/ibatis/builder/xml/mybatis-3-mapper.dtd'))
      }

      return null
    }

    XmlSlurper xmlSlurper = new XmlSlurper(validating, true)
    allowDocTypeDeclaration(xmlSlurper)
    allowHttpOnJava8OrHigher(xmlSlurper)
    xmlSlurper.entityResolver = resolver as EntityResolver

    return xmlSlurper.parseText(mappingFileText)
  }

  def getMappingFileFileName(String fullName) {
    def trimmed = shortenName(fullName)
    def packageName
    def className

    if (trimmed.indexOf('.') == -1) {
      packageName = ""
      className = trimmed
    }
    else {
      packageName = trimmed.subSequence(0, trimmed.lastIndexOf('.')+1)
      className = trimmed.substring(trimmed.lastIndexOf('.')+1)
    }

    def fileName = new StringBuilder()

    fileName << className[0].toLowerCase()
    className[1..-1].each { character ->
      char c2 = character
      fileName << (c2.isUpperCase() ? "-" + c2.toLowerCase() : character)
    }

    return packageName.replace('.', '/') + fileName + ".xml"
  }

}
