package org.grails.plugins.mybatis.install

import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.junit.Ignore

import grails.test.GrailsUnitTestCase

class InstallOnNewAppTests extends GrailsUnitTestCase {
  def pluginManager

  static transactional = false

  def ant = new AntBuilder()
  def temp = System.getProperty('java.io.tmpdir') as File
  def testDir = new File(temp, InstallOnNewAppTests.class.getName()) as File
  def appName = "myapp"
  def grailsHome = System.getProperty('grails.home')
  def executable = System.getProperty('os.name').contains('Win') ? 'grails.bat' : 'grails'

  void setUp() {
    super.setUp()
    runGrails('package-plugin', '.' as File)
    if (testDir.exists()) {
      def dir = testDir.deleteDir()
      assert dir : "Failed to delete existing directory ${testDir}"
    }
    def madeDirs = testDir.mkdirs()
    assert madeDirs : "Failed to create directory ${testDir}"
  }

  @Ignore
  void testExercisePluginInNewApp() {
    assert pluginManager instanceof GrailsPluginManager

    def grailsPlugin = pluginManager.getGrailsPlugin('mybatis')
    def pluginFullName = grailsPlugin.fullName
    def pluginName = grailsPlugin.name
    def pluginVersion = grailsPlugin.version

    assert pluginFullName

    assert pluginName == 'mybatis'

    runGrails("create-app ${appName}", testDir)

    def projectDir = new File(testDir, appName as String)
    assert projectDir.exists() : 'Failed to create Grails project'
    def homeParam = "-Dgrails.work.dir=${projectDir}/work"

    def pluginFile = "grails-${pluginFullName}.zip" as File
    assert pluginFile.exists()
    runGrails("${homeParam} install-plugin ${pluginFile.absolutePath}", projectDir)

    def res = runGrails("${homeParam} list-plugins -installed", projectDir)
    assert res.contains(pluginName) : "Expected ibatis plugin to be installed: ${res}"
    assert res.contains(pluginVersion) : "Expected ibatis plugin version to be ${pluginVersion}"
    def gatewaysDir = new File(projectDir, "grails-app/gateways")
    assert gatewaysDir.isDirectory()

    runGrails("${homeParam} create-gateway no-package", projectDir)
    assert new File(gatewaysDir, "${appName}/no-package.xml").exists()
    assert new File(gatewaysDir, "${appName}/NoPackageGateway.groovy").exists()

    runGrails("${homeParam} create-gateway com.example.installonnewapp.some-package", projectDir)
    assert new File(gatewaysDir, 'com/example/installonnewapp/some-package.xml').exists()
    assert new File(gatewaysDir, 'com/example/installonnewapp/SomePackageGateway.groovy').exists()

    runGrails("${homeParam} create-gateway com.example.installonnewapp.CamelCaseGateway", projectDir)
    assert new File(gatewaysDir, 'com/example/installonnewapp/camel-case.xml').exists()
    assert new File(gatewaysDir, 'com/example/installonnewapp/CamelCaseGateway.groovy').exists()

    runGrails("${homeParam} war", projectDir)
    assert new File("${projectDir}/target" as String, "${appName}-0.1.war" as String).exists()
  }

  private def runGrails(command, dir) {
    Process process = "${grailsHome}/bin/${executable} ${command} --non-interactive".execute(null, dir)
    def outText = new StringBuffer(), errText = new StringBuffer()
    process.waitForProcessOutput(outText, errText)
    println outText
    println errText
    assert process.exitValue() == 0
    return outText.toString()
  }

  void tearDown() {
    //testDir.deleteDir()
  }
}
