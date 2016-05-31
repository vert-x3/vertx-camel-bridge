# Vert.x Camel Bridge

This component lets Vert.x application to interact with Camel endpoints:

* event bus message can be propagated to Camel endpoints
* messages received from Camel endpoints can be sent on the event bus

See the [manual](src/main/asciidoc/java/index.adoc) for more details.

# FAQ

## Intellij issue 

* Junit Test can't be launched - @{surefireArgLine}

  In intellij you need to go to "settings - build, execution, deployment - maven - Running Tests" and uncheck the argLine check box.
  It's because intellij does not support (yet) the late binding variable injection from surefire - see https://github.com/vert-x3/vertx-ext-parent/issues/7



