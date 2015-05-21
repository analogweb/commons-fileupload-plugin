Analog Web Framework Commons File Upload Plugin
===============================================

Enable uploading file to Analogweb's routing endpoint.
(*NOT needs Servlet API.*)

Example In Scala
=================

Add SBT dependency.([scala-plugin](https://github.com/analogweb/scala-plugin) includes this plugin on dependency.)

```scala
val scalaplugin = "org.analogweb" %% "analogweb-scala" % "0.9.6-SNAPSHOT"
```

Write them.

```scala
import org.analogweb.core.httpserver.HttpServers
import org.analogweb.scala.Analogweb

class Hello extends Analogweb {

  def upload = post("/upload") { implicit request => 
    multipart.as[java.io.File]("file").map { f =>
      // Read contents.
      ...
      Ok
    }.getOrElse(BadRequest)
  }

}
```

Example In Java
==========================

Add Maven dependency.

```xml
<dependency>
 <groupId>org.analogweb</groupId>
 <artifactId>analogweb-commons-fileupload</artifactId>
 <version>0.9.6-SNAPSHOT</version>
</dependency>
```

Write them.

```java
package org.analogweb.hello;

import org.analogweb.annotation.Route;
import org.analogweb.annotation.Post;
import org.analogweb.acf.MultipartParam;

@Route("/")
public class Hello {

  @Route
  @Post
  public String upload(@MultipartParam("file") File uploaded) {
    // Read contents.
    ...
    return "File received.";
  }

}
```
