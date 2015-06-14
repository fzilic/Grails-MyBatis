package org.grails.plugins.mybatis.mapping

import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.commons.DefaultGrailsClass
import org.codehaus.groovy.grails.commons.GrailsClass
import org.grails.plugins.mybatis.MappingSupport

class MappingSupportTests extends GrailsUnitTestCase {
  def mappingSupport = new MappingSupport()

  void testGetMappingFileFileName() {
    def noPackage = mappingSupport.getMappingFileFileName('BareGateway')
    assert noPackage == 'bare.xml'
    def simple = mappingSupport.getMappingFileFileName('foo.bar.SimpleGateway')
    assert simple == 'foo/bar/simple.xml'
    def camel = mappingSupport.getMappingFileFileName('foo.bar.CamelCaseGateway')
    assert camel == 'foo/bar/camel-case.xml'
  }

  void testIsListOp() {
    GrailsClass clazz = new DefaultGrailsClass(PackageGateway)
    assert !mappingSupport.isListOp(clazz, 'pkgSingle')
    assert mappingSupport.isListOp(clazz, 'readPackageList')
    assert mappingSupport.isListOp(clazz, 'pkgListForced')
  }

  void testOperationNamespace() {
    GrailsClass c = new DefaultGrailsClass(PackageGateway)
    mappingSupport.registerMappings(c, null)
    def method = c.metaClass.pkgSingle
    assert method
  }

  void testXmlParsing() {
    def file = new File("test/unit/org/grails/plugins/mybatis/mapping/sample.xml")
    assert file.exists()

    def slurper = new XmlSlurper(false, true)
    MappingSupport.allowDocTypeDeclaration(slurper)
    MappingSupport.allowHttpOnJava8OrHigher(slurper)
    def xml = slurper.parseText(file.text)
    def mappingSupport = new MappingSupport()
    assert mappingSupport.getOperationIds(xml)['select'].size() == 3
    assert mappingSupport.getOperationIds(xml)['insert'].size() == 0
  }
}
