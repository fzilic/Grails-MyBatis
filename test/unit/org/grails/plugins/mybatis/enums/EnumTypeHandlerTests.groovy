package org.grails.plugins.mybatis.enums

import grails.test.GrailsUnitTestCase

import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

enum SampleEnum {
  VALUE_ONE('ONE'), VALUE_TWO('TWO')

  String dbCode

  SampleEnum(dbCode) {
    this.dbCode = dbCode
  }
}

class SampleEnumTypeHandler extends AbstractEnumTypeHandler<SampleEnum> {
  SampleEnumTypeHandler() {
    super(SampleEnum, "dbCode")
  }
}

class EnumTypeHandlerTests extends GrailsUnitTestCase {

  void testGettingValue() {
    def hanlder = new SampleEnumTypeHandler()

    def resultSet = [getObject: { SampleEnum.VALUE_ONE.dbCode }] as ResultSet
    assert resultSet.getObject('test') == SampleEnum.VALUE_ONE.dbCode

    assert hanlder.getNullableResult(resultSet, "test") == SampleEnum.VALUE_ONE

    def statement = [getObject: { SampleEnum.VALUE_TWO.dbCode }] as CallableStatement
    assert statement.getObject(1) == SampleEnum.VALUE_TWO.dbCode

    assert hanlder.getNullableResult(statement, 1) == SampleEnum.VALUE_TWO
  }

  void testSettingValue() {
    def hanlder = new SampleEnumTypeHandler()
    def statement = [setObject: { index, parameter -> assert parameter == SampleEnum.VALUE_TWO.dbCode }] as PreparedStatement

    hanlder.setNonNullParameter(statement, 0, SampleEnum.VALUE_TWO, null)
  }
}
