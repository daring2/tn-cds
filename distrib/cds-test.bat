@echo off

set JmxOpts=-Dcom.sun.management.jmxremote.port=33333 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
set JvmOpts=-Xmx128m %JmxOpts%

"%JAVA_CDS%/bin/java.exe" -cp "conf;lib/*" %JvmOpts% %* ru.antinform.cds.test.TestApp