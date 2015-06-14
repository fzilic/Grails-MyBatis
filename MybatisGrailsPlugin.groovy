
import org.apache.commons.logging.LogFactory
import org.apache.ibatis.mapping.DefaultDatabaseIdProvider
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.grails.plugins.mybatis.MappingSupport
import org.grails.plugins.mybatis.GatewayArtefactHandler
import org.grails.plugins.mybatis.TypeHandlerArtefactHandler
import org.grails.plugins.mybatis.locking.OptimisticLockingInterceptor

class MybatisGrailsPlugin {
  def version = "0.0.3"
  def grailsVersion = "2.0 > *"
  def dependsOn = [:]
  def pluginExcludes = [
    "grails-app/views/**",
    "grails-app/gateways/**/*",
    "src/gateways/**/*"
  ]

  def title = "Grails Mybatis Plugin"
  def author = "Franjo Žilić"
  def authorEmail = "frenky666@gmail.com"
  def description = '''\
The MyBatis plugin enables Grails integration with MyBatis ORM framework
'''

  // URL to the plugin's documentation
  def documentation = "http://fzilic.github.com/Grails-MyBatis/"

  // Extra (optional) plugin metadata

  // License: one of 'APACHE', 'GPL2', 'GPL3'
  def license = "APACHE"

  // Details of company behind the plugin (if there is one)
  // def organization

  // Any additional developers beyond the author specified above.
  def developers = [
    [name: "Damir Murat", email: "dmurat@croz.net" ]
  ]

  // Location of the plugin's issue tracker.
  def issueManagement = [ system: "github", url: "https://github.com/fzilic/Grails-MyBatis/issues" ]

  // Online location of the plugin's browseable source code.
  def scm = [ url: "https://github.com/fzilic/Grails-MyBatis" ]

  def artefacts = [
    GatewayArtefactHandler,
    TypeHandlerArtefactHandler
  ]

  def watchedResources = [
    "file:grails-app/gateways/**/*",
    "file:grails-app/typeHandlers/**/*"
  ]

  def log = LogFactory.getLog(MybatisGrailsPlugin)
  def mappingSupport = new MappingSupport()

  def doWithWebDescriptor = { xml ->
  }

  def doWithSpring = {
    // register gateway artefacts
    log.debug "Looking for artefacts of type ${GatewayArtefactHandler.TYPE}"

    def gatewayArtefacts = application.getArtefacts(GatewayArtefactHandler.TYPE)

    log.debug "Looking for artefacts of type ${TypeHandlerArtefactHandler.TYPE}"

    def typeHandlerArtefacts = application.getArtefacts(TypeHandlerArtefactHandler.TYPE)

    gatewayArtefacts.each { artefact ->
      log.debug "Found gateway artefact $artefact of type ${artefact.clazz}; will register as ${artefact.shortName[0].toLowerCase()}${artefact.shortName[1..-1]}"

      //check artefact for mapper resource
      def mapperFile = mappingSupport.getArtefactMapperResource(artefact)

      if (mapperFile && mapperFile.exists()){
        "${artefact.shortName[0].toLowerCase() + artefact.shortName[1..-1]}"(artefact.clazz){ bean ->
          bean.singleton = true
          bean.autowire = "byName"
        }
      }
      else {
        log.warn "No mapping file found for artefact ${artefact.clazz}"
      }
    }

    def dataSourcesNames = application.config.mybatis.dataSourceNames ?: ['dataSource']

    //create myBatis SqlSessionFactoryBeans for each dataSource
    dataSourcesNames.each { dataSourceName ->
      log.debug "Registering $TypeHandlerArtefactHandler.TYPE instances for dataSource: $dataSourceName ..."

      def typeHandlerTypes = typeHandlerArtefacts.findAll { artefact ->
        dataSourceName ==  GrailsClassUtils.getStaticPropertyValue(artefact.clazz, 'dataSourceName') ?: dataSourceName
      }

      typeHandlerTypes.each { log.debug "Found $TypeHandlerArtefactHandler.TYPE $it.clazz" }

      "sqlSessionFactoryBean_$dataSourceName"(org.mybatis.spring.SqlSessionFactoryBean){
        dataSource = ref(dataSourceName)
        // register mapper locations per each gateway artefact
        mapperLocations = gatewayArtefacts.findAll { artefact ->
          dataSourceName == GrailsClassUtils.getStaticPropertyValue(artefact.clazz, 'dataSourceName') ?: 'dataSource'
        }.collect { artefact ->
          mappingSupport.getArtefactMapperResource(artefact)
        }

        typeHandlers = typeHandlerTypes.collect { artefactClass ->
          artefactClass.newInstance()
        }

        def enabledPlugins = []

        if (application.config.mybatis.optimisticLocking ?: true) {
          enabledPlugins << new OptimisticLockingInterceptor()
        }

        plugins = enabledPlugins

        if (application.config.mybatis.multivendor.enabled ?: false) {
          def provider = new DefaultDatabaseIdProvider()
          provider.properties = application.config.mybatis.multivendor.mapping

          databaseIdProvider = provider
        }
      }

      "sqlSessionTemplate_$dataSourceName"(org.mybatis.spring.SqlSessionTemplate, ref("sqlSessionFactoryBean_$dataSourceName"))
    }
  }

  def doWithDynamicMethods = { ctx ->
    GrailsClass[] gateways = application.getArtefacts(GatewayArtefactHandler.TYPE)
    log.debug "Gateways length is ${gateways?.length}"
    gateways.each { mappingSupport.registerMappings(it, application) }
  }

  def doWithApplicationContext = { applicationContext ->
  }

  def onChange = { event ->
  }

  def onConfigChange = { event ->
  }

  def onShutdown = { event ->
  }
}
