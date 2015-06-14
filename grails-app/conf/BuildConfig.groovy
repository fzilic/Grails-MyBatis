grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
  inherits("global") {
  }

  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
  repositories {
    grailsCentral()
    mavenCentral()
  }

  dependencies {
    compile 'org.mybatis:mybatis:3.1.1'
    compile('org.mybatis:mybatis-spring:1.1.1') { transitive = false }
  }

  plugins {
    build ':release:3.1.1', { export = false }
  }
}
