package org.grails.plugins.mybatis.datasources

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.mybatis.spring.SqlSessionFactoryBean

class MultipleDataSourceSupportTests extends GroovyTestCase {
  def grailsApplication

  @Override
  protected void setUp() {
    super.setUp()
  }

  void testCustomConfiguration(){
    assert grailsApplication
    assert grailsApplication instanceof GrailsApplication

    def config = grailsApplication.config

    assert config
    assert config instanceof ConfigObject

    def dataSourceNames = config.mybatis.dataSourceNames

    assert dataSourceNames
    assert dataSourceNames.size() == 2
    assert dataSourceNames.contains('dataSource')
    assert dataSourceNames.contains('dataSource_alternate')
  }

  void testMultipleSqlSessionFactoryBeansCreation(){
    assert grailsApplication.config.mybatis.dataSourceNames.size() == 2

    def springBeans = grailsApplication.mainContext.getBeansOfType(SqlSessionFactoryBean)
    def dataSourceNames = grailsApplication.config.mybatis.dataSourceNames

    assert springBeans
    assert springBeans.size() == 2

    dataSourceNames.each { name ->
      assert springBeans.find { key, value ->
        key.endsWith(name)
      }
    }
  }
}
