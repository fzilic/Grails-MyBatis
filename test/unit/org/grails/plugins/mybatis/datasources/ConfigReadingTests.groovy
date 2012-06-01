package org.grails.plugins.mybatis.datasources

import grails.test.GrailsUnitTestCase

class ConfigReadingTests extends GrailsUnitTestCase {
  void testConfigurationMock(){
    def config = mockConfig('''
      mybatis {
        dataSourceNames = ['dataSource', 'dataSource_alternate']
      }
    ''')

    assert config.mybatis.dataSourceNames.size() == 2
    assert config.mybatis.dataSourceNames[0] == 'dataSource'
  }
}
