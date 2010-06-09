//
// This uninstall script also removes the hbase conf file from your app
//
try {
    ant.delete(file: "${basedir}/grails-app/conf/hbase-site.xml")
}
catch (Exception ex) {
    println ex.message
}