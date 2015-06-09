eventCreateWarStart = { warName, stagingDir ->
    //println "Inside the Plugin Script"
    ant.copy(todir: "${stagingDir}/WEB-INF/classes") {
        fileset(dir:"grails-app/gateways",includes:"**/*.xml")
    }
}
