rem
rem Created by Mike O. 4/28/2024
rem Load Oxygen XML Editor for debugging with IntelliJ IDEA
rem Couldn't figure out how to include `oxygen.jar` in classpath when running from IntelliJ IDEA
rem OR make it available via maven as a local repository, so generated classpath from maven and then took
rem `oxygen.bat` script and modified it to use the generated classpath
rem Run this in IntelliJ IDEA and then click the button that appears in the terminal to attach the debugger

rem Add the `oxygen.jar` as the first item in the classpath, followed by the generated maven classpath
rem To generate the maven classpath, run the following command in the terminal:
rem `mvn dependency:build-classpath -DincludeScope=compile`
SET CP="C:\Program Files\Oxygen XML Editor 26\lib\oxygen.jar;C:\Users\MikeO\.m2\repository\com\google\guava\guava\33.1.0-jre\guava-33.1.0-jre.jar;C:\Users\MikeO\.m2\repository\com\google\guava\failureaccess\1.0.2\failureaccess-1.0.2.jar;C:\Users\MikeO\.m2\repository\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;C:\Users\MikeO\.m2\repository\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar;C:\Users\MikeO\.m2\repository\org\checkerframework\checker-qual\3.42.0\checker-qual-3.42.0.jar;C:\Users\MikeO\.m2\repository\com\google\errorprone\error_prone_annotations\2.26.1\error_prone_annotations-2.26.1.jar;C:\Users\MikeO\.m2\repository\com\google\j2objc\j2objc-annotations\3.0.0\j2objc-annotations-3.0.0.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-sdk\26.0.0.2\oxygen-sdk-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen\26.0.0.2\oxygen-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-slf4j\26.0.0.2\oxygen-patched-slf4j-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-token-markers\26.0.0.2\oxygen-token-markers-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-css-pretty-printer\26.0.0.2\oxygen-css-pretty-printer-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-jing\26.0.0.2\oxygen-patched-jing-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-trang\26.0.0.2\oxygen-patched-trang-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-resolver\26.0.0.2\oxygen-patched-resolver-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-xerces\26.0.0.2\oxygen-patched-xerces-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-saxon-6\26.0.0.2\oxygen-patched-saxon-6-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-saxon-12he\26.0.0.2\oxygen-patched-saxon-12he-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\org\xmlresolver\xmlresolver\5.2.1\xmlresolver-5.2.1.jar;C:\Users\MikeO\.m2\repository\org\apache\httpcomponents\client5\httpclient5\5.1.3\httpclient5-5.1.3.jar;C:\Users\MikeO\.m2\repository\org\apache\httpcomponents\core5\httpcore5-h2\5.1.3\httpcore5-h2-5.1.3.jar;C:\Users\MikeO\.m2\repository\org\apache\httpcomponents\core5\httpcore5\5.1.3\httpcore5-5.1.3.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-serializer\26.0.0.2\oxygen-patched-serializer-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-jna\26.0.0.2\oxygen-patched-jna-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-psychopath\26.0.0.2\oxygen-psychopath-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-sandbox\26.0.0.2\oxygen-sandbox-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-validation-api\26.0.0.2\oxygen-validation-api-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\dicts-hunspell\26.0.0.2\dicts-hunspell-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\dicts-autocorrect\26.0.0.2\dicts-autocorrect-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\native-hunspell\26.0.0.2\native-hunspell-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\native-jna\26.0.0.2\native-jna-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\native-windows-helper\26.0.0.2\native-windows-helper-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-basic-utilities\26.0.0.2\oxygen-basic-utilities-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-editor-variables-parser\26.0.0.2\oxygen-editor-variables-parser-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-truezip\26.0.0.2\oxygen-patched-truezip-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-annotations\26.0.0.2\oxygen-annotations-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-jeuclid-core\26.0.0.2\oxygen-patched-jeuclid-core-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\xml-apis\xml-apis-ext\1.3.04\xml-apis-ext-1.3.04.jar;C:\Users\MikeO\.m2\repository\org\apache\httpcomponents\httpclient\4.5.9\httpclient-4.5.9.jar;C:\Users\MikeO\.m2\repository\org\apache\httpcomponents\httpcore\4.4.11\httpcore-4.4.11.jar;C:\Users\MikeO\.m2\repository\commons-codec\commons-codec\1.14\commons-codec-1.14.jar;C:\Users\MikeO\.m2\repository\commons-io\commons-io\2.8.0\commons-io-2.8.0.jar;C:\Users\MikeO\.m2\repository\org\mozilla\rhino\1.7.14\rhino-1.7.14.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-batik\26.0.0.2\oxygen-patched-batik-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-patched-xmlgraphics\26.0.0.2\oxygen-patched-xmlgraphics-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\jidesoft\jide-oss\3.6.12\jide-oss-3.6.12.jar;C:\Users\MikeO\.m2\repository\net\java\dev\jna\jna-platform\5.7.0\jna-platform-5.7.0.jar;C:\Users\MikeO\.m2\repository\org\nokogiri\nekohtml\1.9.22.noko2\nekohtml-1.9.22.noko2.jar;C:\Users\MikeO\.m2\repository\edu\princeton\cup\java-cup\10k\java-cup-10k.jar;C:\Users\MikeO\.m2\repository\com\ibm\icu\icu4j\71.1\icu4j-71.1.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-jfx-components\26.0.0.2\oxygen-jfx-components-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-css-flute-parser\26.0.0.2\oxygen-css-flute-parser-26.0.0.2.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-markdown-converter-adapter\26.0.0.2\oxygen-markdown-converter-adapter-26.0.0.2-driver.jar;C:\Users\MikeO\.m2\repository\com\oxygenxml\oxygen-markdown-converter-adapter\26.0.0.2\oxygen-markdown-converter-adapter-26.0.0.2-backmapping.jar;C:\Users\MikeO\.m2\repository\com\sun\xml\bind\jaxb-impl\2.3.3\jaxb-impl-2.3.3.jar;C:\Users\MikeO\.m2\repository\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar;C:\Users\MikeO\.m2\repository\com\sun\activation\jakarta.activation\1.2.2\jakarta.activation-1.2.2.jar;C:\Users\MikeO\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.15.0\jackson-databind-2.15.0.jar;C:\Users\MikeO\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.15.0\jackson-annotations-2.15.0.jar;C:\Users\MikeO\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.15.0\jackson-core-2.15.0.jar;C:\Users\MikeO\.m2\repository\org\jetbrains\annotations\24.1.0\annotations-24.1.0.jar;C:\Users\MikeO\.m2\repository\org\apache\commons\commons-text\1.11.0\commons-text-1.11.0.jar;C:\Users\MikeO\.m2\repository\org\apache\commons\commons-lang3\3.14.0\commons-lang3-3.14.0.jar;C:\Users\MikeO\.m2\repository\org\apache\commons\commons-vfs2\2.9.0\commons-vfs2-2.9.0.jar;C:\Users\MikeO\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;C:\Users\MikeO\.m2\repository\org\apache\hadoop\hadoop-hdfs-client\3.3.1\hadoop-hdfs-client-3.3.1.jar;C:\Users\MikeO\.m2\repository\com\squareup\okhttp\okhttp\2.7.5\okhttp-2.7.5.jar;C:\Users\MikeO\.m2\repository\com\squareup\okio\okio\1.6.0\okio-1.6.0.jar;C:\Users\MikeO\.m2\repository\org\apache\logging\log4j\log4j-api\2.23.1\log4j-api-2.23.1.jar;C:\Users\MikeO\.m2\repository\org\apache\logging\log4j\log4j-core\2.23.1\log4j-core-2.23.1.jar;C:\Users\MikeO\.m2\repository\org\apache\tika\tika-core\2.9.0\tika-core-2.9.0.jar;C:\Users\MikeO\.m2\repository\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar"

rem FROM ORIGINAL oxygen.bat script
rem The command line parameters can be:
rem
rem  1. file paths of local files opened automatically in editor panels at startup
rem
rem  2. the following sequence to open a file with default schema association
rem		
rem      -instance pathToXMLFile -schema pathToSchemaFile -schemaType XML_SCHEMA|DTD_SCHEMA|RNG_SCHEMA|RNC_SCHEMA -dtName documentTypeName
rem
rem     where:
rem
rem       - pathToXMLFile: is the name of a local XML file
rem       - pathToSchemaFile: is the name of the schema which you want to associate to the XML file
rem       - schemaType: the four constants (XML_SCHEMA, DTD_SCHEMA, RNG_SCHEMA, RNC_SCHEMA) are the possible 
rem           schema types (W3C XML Schema, DTD, Relax NG schema in full syntax, Relax NG schema 
rem           in compact syntax)
rem       - dtName: The name of the document type automatically generated for association.

rem set OXYGEN_JAVA=java.exe
rem if exist "%JAVA_HOME%\bin\java.exe" set OXYGEN_JAVA="%JAVA_HOME%\bin\java.exe"
rem if exist "%~dp0\jre\bin\java.exe" set OXYGEN_JAVA="%~dp0\jre\bin\java.exe"
rem rem Set environment variables
rem call "%~dp0\env.bat"
rem %OXYGEN_JAVA% %OXYGEN_JAVA_OPTIONS% -Dcom.oxygenxml.app.descriptor=ro.sync.exml.EditorFrameDescriptor -Djava.security.manager=allow -XX:-OmitStackTraceInFastThrow -XX:SoftRefLRUPolicyMSPerMB=10 -Djavax.net.ssl.trustStoreType=Windows-ROOT -Dsun.java2d.noddraw=true -Dsun.awt.nopixfmt=true -Dsun.java2d.dpiaware=true -Dsun.io.useCanonCaches=true -Dsun.io.useCanonPrefixCache=true -Dsun.awt.keepWorkingSetOnMinimize=true -Dcom.oxygenxml.ApplicationDataFolder="%APPDATA%" -agentlib:jdwp=transport=dt_socket,address=59223,suspend=y,server=y -javaagent:C:\Users\MikeO\AppData\Local\JetBrains\IdeaIC2024.1\captureAgent\debugger-agent.jar -cp %CP% ro.sync.exml.Oxygen %*
rem END ORIGINAL oxygen.bat script

rem The command below is the same as the original but with a couple flags added for debugging in IntelliJ
rem The key change here is to add the debug agent to the command line parameters:
rem `-agentlib:jdwp=transport=dt_socket,address=59223,suspend=y,server=y`
rem Not sure if this is needed, but was part of the IntelliJ debug configuration ()that I tried to create)
rem so added it to make it easier for IntelliJ IDEA to attach the debugger (hopefully? maybe?)
rem `-javaagent:C:\Users\MikeO\AppData\Local\JetBrains\IdeaIC2024.1\captureAgent\debugger-agent.jar`
java -Dcom.oxygenxml.app.descriptor=ro.sync.exml.EditorFrameDescriptor -Djava.security.manager=allow -XX:-OmitStackTraceInFastThrow -XX:SoftRefLRUPolicyMSPerMB=10 -Djavax.net.ssl.trustStoreType=Windows-ROOT -Dsun.java2d.noddraw=true -Dsun.awt.nopixfmt=true -Dsun.java2d.dpiaware=true -Dsun.io.useCanonCaches=true -Dsun.io.useCanonPrefixCache=true -Dsun.awt.keepWorkingSetOnMinimize=true -Dcom.oxygenxml.ApplicationDataFolder="%APPDATA%" -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59223 -cp %CP% ro.sync.exml.Oxygen %*
