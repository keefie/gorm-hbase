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

