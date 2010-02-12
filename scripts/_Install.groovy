//
// HBase requires a later, possibly less stable version, of the Commons CLI jar file, uninstalling
// this plug-in will restore the copy of the jar file distributed with Grails
//
def plugInCliJar = new File("${grailsHome}/lib/commons-cli-2.0-SNAPSHOT.jar")
if (!plugInCliJar.exists()) {
    println "Replacing supplied Grails commons cli jar file with later version"
    try {
        ant.move(file: "${grailsHome}/lib/commons-cli-1.0.jar",
            tofile: "${grailsHome}/lib/commons-cli-1.0.orig",
            overwrite: true)
    }
    catch (Exception ex) {
        println ex.message
    }
    try {
        ant.copy(file: "${pluginBasedir}/lib/commons-cli-2.0-SNAPSHOT.jar",
            tofile: "${grailsHome}/lib/commons-cli-2.0-SNAPSHOT.jar",
            overwrite: true)
    }
    catch (Exception ex) {
        println ex.message
    }
}

//
// The HBase config file is moved to ease editing when in app
// development by putting it in the default application dir structure
//
def plugInHBaseConfig = new File("${pluginBasedir}/grails-app/conf/hbase-site.xml")
if (plugInHBaseConfig.exists()) {
    println "Moving HBase config file from plug-in to application"
    try {
        ant.move(file: "${pluginBasedir}/grails-app/conf/hbase-site.xml",
            tofile: "${basedir}/grails-app/conf/hbase-site.xml",
            overwrite: false)
    }
    catch (Exception ex) {
        println ex.message
    }
}

