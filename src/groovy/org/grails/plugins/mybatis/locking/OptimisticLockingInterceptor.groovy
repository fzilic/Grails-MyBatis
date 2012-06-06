package org.grails.plugins.mybatis.locking



import org.apache.ibatis.executor.Executor
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Plugin
import org.apache.ibatis.plugin.Signature
import org.apache.ibatis.session.RowBounds
import org.codehaus.groovy.grails.commons.GrailsClassUtils

/**
 * MyBatis Interceptor (plugin) used for optimistic locking
 *
 * Grails like use - convention over configuration
 *
 * Conventions (configuration options as static fields in object being persisted):
 *  - enable interceptor for persisted object (default: false)
 *   - def static useOptimisticLocking = true
 *  - identity property: 'id'
 *   - configurable: def static idProperty = 'identity'
 *  - version property: 'version' Long or Integer
 *   - configurable: def static versionProperty = 'ver'
 *  - version query (used to determine current object version in database): 'loadCurrentVersionOf${bean.class.simpleName}ById'
 *   - configurable: def static versionQuery = 'versionQuery'
 *   - mybatis namespace sensitive
 *
 * @author fzilic
 *
 */
@Intercepts([@Signature(type = Executor.class, method = "update", args = [ MappedStatement.class, Object.class ])])
class OptimisticLockingInterceptor implements Interceptor {

  /**
   * Loads current version for object from database
   *
   * Warning, some MyBatis magic
   *
   * @param executor target executor for MyBatis
   * @param object being updated
   * @return Long or Integer
   */
  def loadCurrentVersion(Executor executor, MappedStatement mappedStatement, Object object) {
    // get properties, with convention defaults
    def versionQuery = GrailsClassUtils.getStaticFieldValue(object.class, "versionQuery") ?: "loadCurrentVersionOf${object.class.simpleName}ById"
    def idProperty = GrailsClassUtils.getStaticFieldValue(object.class, "idProperty") ?: "id"

    def namespace = mappedStatement.id.substring(0, mappedStatement.id.lastIndexOf('.'))

    def versionQueryStatement = mappedStatement.configuration.mappedStatements.find { statement ->
      statement.id == "$namespace.$versionQuery"
    }

    if (!versionQueryStatement) {
      throw new OptimisticLockingConfigurationException("Unable to find object version query: $namespace.$versionQuery")
    }

    def currentVersion = executor.query(versionQueryStatement, object."$idProperty", RowBounds.DEFAULT, null)

    if (currentVersion == null || currentVersion.size() != 1) {
      throw new RecordDeletedException(
          """Record deleted by another user.
            Class: ${object.class}
            Object id: ${object."$idProperty"}
            Mapper namespace: $namespace
            Statement: $mappedStatement.id
            Version query: $versionQueryStatement.id""".stripIndent() as String
      )
    }
    else {
      currentVersion = currentVersion[0]
    }

    return currentVersion
  }

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // As per annotation first argument is statement, second argument is target object
    MappedStatement mappedStatement = invocation.args[0] as MappedStatement
    def object = invocation.args[1]

    def optimisticLockingEnabled = GrailsClassUtils.getStaticFieldValue(object.getClass(), "useOptimisticLocking") ?: false

    if (optimisticLockingEnabled) {
      Executor executor = invocation.target as Executor

      // Get object version property name
      def versionProperty = GrailsClassUtils.getStaticFieldValue(object.class, "versionProperty") ?: 'version'
      def idProperty = GrailsClassUtils.getStaticFieldValue(object.class, "idProperty") ?: "id"

      switch (mappedStatement.sqlCommandType) {

        case SqlCommandType.UPDATE:

          def databaseVersion = loadCurrentVersion(executor, mappedStatement, object)

          def objectVersion = object."$versionProperty"

          if (objectVersion != databaseVersion) {
            throw new RecordAlteredException(
                """Record alterd by another user.
                   Class: ${object.class}
                   Object id: ${object."$idProperty"}
                   Object version: ${object."$versionProperty"}
                   Database version: $databaseVersion
                   Statement: $mappedStatement.id
                """.stripIndent() as String
            )
          }

          object."$versionProperty" = objectVersion + 1

          break

        case SqlCommandType.DELETE:
          def databaseVersion = loadCurrentVersion(executor, mappedStatement, object)
          def objectVersion = object."$versionProperty"

          if (objectVersion != databaseVersion) {
            throw new OptimisticLockingException(
                """Record alterd by another user.
                   Class: ${object.class}      0
                   Object id: ${object."$idProperty"}
                   Object version: ${object."$versionProperty"}
                   Database version: $databaseVersion
                   Statement: $mappedStatement.id
                """.stripIndent() as String
            )
          }

          break

        case SqlCommandType.INSERT:
          object."$versionProperty" = 1
          break
      }
    }
    return invocation.proceed()
  }

  @Override
  public Object plugin(Object object) {
    return Plugin.wrap(object, this)
  }

  @Override
  public void setProperties(Properties properties) {

  }
}
