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
    compile 'org.mybatis:mybatis:3.0.6'
    compile('org.mybatis:mybatis-spring:1.0.2') { transitive = false }
  }

  plugins {
  }
}
