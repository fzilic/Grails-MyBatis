package org.grails.plugins.mybatis.test

import grails.test.GrailsUnitTestCase

import org.grails.plugins.mybatis.MappingSupport

import org.codehaus.groovy.grails.commons.DefaultGrailsClass

/** Superclass used for writing integration tests prior Grails 2.0.2 due to Grails bug GRAILS-8832
 *
 *  @deprecated since Grails version 2.0.2 use DI for gateway injection
 */

@Deprecated
abstract class GatewayIntegrationTest extends GrailsUnitTestCase {
  def grailsApplication
  private Object _gateway
  private mappingSupport = new MappingSupport()

  protected getGateway() {
    if (!_gateway) {
      _gateway = createGateway()
    }

    _gateway
  }

  private createGateway() {
    def gatewayClassName = getGatewayClassName(getClass())

    def gatewayClass = getClass().classLoader.loadClass(gatewayClassName)
    def grailsClass = new DefaultGrailsClass(gatewayClass)

    mappingSupport.registerMappings(grailsClass, grailsApplication)

    return gatewayClass.newInstance()
  }

  static String getGatewayClassName(clazz) {
    return clazz.name.substring(0, clazz.name.lastIndexOf('Test'))
  }
}
