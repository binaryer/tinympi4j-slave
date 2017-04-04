# tinympi4j-slave
slave side of tinympi4j

生成可运行的jar文件
```bash
mvn package assembly:single
```

运行
```bash
java -jar tinympi4j-slave-0.1.jar {port}
```

```
[root@vbox-centos7-64 ~]# java -jar tinympi4j-slave-0.1.jar 1234
Apr 04, 2017 5:56:40 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-nio-1234"]
Apr 04, 2017 5:56:41 PM org.apache.tomcat.util.net.NioSelectorPool getSharedSelector
INFO: Using a shared selector for servlet write/read
Apr 04, 2017 5:56:41 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service Tomcat
Apr 04, 2017 5:56:41 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet Engine: Apache Tomcat/8.0.42
Apr 04, 2017 5:58:05 PM org.apache.catalina.util.SessionIdGeneratorBase createSecureRandom
INFO: Creation of SecureRandom instance for session ID generation using [SHA1PRNG] took [84,588] milliseconds.
Apr 04, 2017 5:58:05 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-nio-1234"]
Apr 04, 2017 5:58:05 PM lcy.tinympi4j.slave.AppSlave main
INFO: slave started at port 1234
```

 更多信息见 [https://github.com/binaryer/tinympi4j-master](https://github.com/binaryer/tinympi4j-master)