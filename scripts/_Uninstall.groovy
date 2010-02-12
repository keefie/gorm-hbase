//
// HBase requires a later, possibly less stable version, of the Commons CLI jar file, uninstalling
// this plug-in will restore the copy of the jar file distributed with Grails
//
// This uninstall script also removes the hbase conf file
//
try {
    ant.move(file: "${grailsHome}/lib/commons-cli-1.0.orig",
        tofile: "${grailsHome}/lib/commons-cli-1.0.jar",
        overwrite: true)
    ant.delete(file: "${grailsHome}/lib/commons-cli-2.0-SNAPSHOT.jar")
    ant.delete(file: "${basedir}/grails-app/conf/hbase-site.xml")
}
catch (Exception ex) {
    println ex.message
}

