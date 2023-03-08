# Vert.x Camel Bridge

[![Build Status (5.x)](https://github.com/vert-x3/vertx-camel-bridge/actions/workflows/ci-5.x.yml/badge.svg)](https://github.com/vert-x3/vertx-camel-bridge/actions/workflows/ci-5.x.yml)
[![Build Status (4.x)](https://github.com/vert-x3/vertx-camel-bridge/actions/workflows/ci-4.x.yml/badge.svg)](https://github.com/vert-x3/vertx-camel-bridge/actions/workflows/ci-4.x.yml)

This component lets Vert.x application to interact with Camel endpoints:

* event bus message can be propagated to Camel endpoints
* messages received from Camel endpoints can be sent on the event bus

See the [manual](src/main/asciidoc/java/index.adoc) for more details.

# FAQ

## Intellij issue

* Junit Test can't be launched - @{surefireArgLine}

  In intellij you need to go to "settings - build, execution, deployment - maven - Running Tests" and uncheck the argLine check box.
  It's because intellij does not support (yet) the late binding variable injection from surefire - see https://github.com/vert-x3/vertx-ext-parent/issues/7



