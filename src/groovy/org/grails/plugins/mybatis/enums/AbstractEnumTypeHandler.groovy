package org.grails.plugins.mybatis.enums

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

abstract class AbstractEnumTypeHandler<T extends Enum> extends BaseTypeHandler<T> {

  final Class<T> enumClass
  final String enumProperty

  AbstractEnumTypeHandler(Class<T> enumClass, String enumProperty) {
    this.enumClass = enumClass
    this.enumProperty = enumProperty
  }

  private enumFromProperty(value) {
    def foundEnum = enumClass.find {
      it[enumProperty] == value
    }
    return foundEnum
  }

  @Override
  public void setNonNullParameter(PreparedStatement statement, int index, T parameter, JdbcType jdbcType) throws SQLException {
    statement.setObject(index, parameter[enumProperty])
  }

  @Override
  public T getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
    def source = resultSet.getObject(columnName)
    return enumFromProperty(source);
  }

  @Override
  public T getNullableResult(CallableStatement statement, int columnIndex) throws SQLException {
    def source = statement.getObject(columnIndex)
    return enumFromProperty(source);
  }

}
