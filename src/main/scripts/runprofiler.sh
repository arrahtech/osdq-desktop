echo ==== Starting Profiler =======
echo Need java 1.8.x or above to run
echo 
java -version 
java -Xmx4096M -Xms4096M -classpath osdq-desktop-${project.version}.jar:lib/rowset.jar:lib/* org.arrah.gui.swing.Profiler $1
