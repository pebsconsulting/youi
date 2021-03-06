# youi

[![Build Status](https://travis-ci.org/outr/youi.svg?branch=master)](https://travis-ci.org/outr/youi)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c0425ea823824cd7ab60659e8b9542dc)](https://www.codacy.com/app/matthicks/youi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=outr/youi&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/c0425ea823824cd7ab60659e8b9542dc)](https://www.codacy.com/app/matthicks/youi?utm_source=github.com&utm_medium=referral&utm_content=outr/youi&utm_campaign=Badge_Coverage)
[![Stories in Ready](https://badge.waffle.io/outr/youi.png?label=ready&title=Ready)](https://waffle.io/outr/youi)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/youi)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.youi/youi-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.youi/youi-core_2.12)
[![Latest version](https://index.scala-lang.org/outr/youi/youi-core/latest.svg)](https://index.scala-lang.org/outr/youi)

Next generation user interface and application development in Scala and Scala.js for web, mobile, and desktop.

## Status

We have completed migration from Hyperscala (http://github.com/outr/hyperscala) and the base functionality of NextUI
(https://github.com/outr/nextui) and we've added a lot of new functionality. We're currently being used in production
environments but are still in heavy development.

## Modules

YouI is broken out into modules of functionality to minimize the dependencies required for your specific usage:

* [app](app) - unification of client and server to write complete applications (Scala and Scala.js)
* [client](client) - HTTP client for asynchronous request/response and restful support (Scala)
* [communication](communication) - communication framework to provide type-safe communication between a client / server (Scala and Scala.js)
* [core](core) - core features generally useful for web and HTTP (Scala and Scala.js)
* [dom](dom) - features and functionality related to working with the browser's DOM (Scala.js)
* [server](server) - base functionality for a web server (Scala)
* [server-undertow](serverUndertow) - implementation of [server](server) using [Undertow](http://undertow.io/) (Scala)
* [stream](stream) - streaming functionality for on-the-fly processing and modification of any XML or HTML content (Scala)
* [template](template) - features for creating and managing templates for use in applications (Scala and Scala.js)
* [ui](ui) - functionality for user-interface creation and management (Scala.js)

## Features for 1.0.0 (In-Progress)

* [ ] Integration of basic HTML components for UI (Scala.js)
    * [X] Button
    * [X] Container
    * [X] ImageView
    * [X] Label
    * [X] TextInput
    * [X] TextArea
    * [ ] Full event support
    * [ ] Styling and Theme functionality
* [ ] Integration of Pixi.js for more advanced UI functionality (Scala.js)
    * [ ] Canvas / Integration with HTML
    * [ ] Shapes
    * [ ] Text
    * [ ] Image
    * [ ] Events
* [X] Animation and workflow functionality
    * [X] Task
    * [X] Action
    * [X] Temporal
    * [X] Parallel and Sequential
    * [X] Sleep
    * [X] Easings integration
    * [X] DSL
* [ ] Layout Managers
    * [X] Core Support
    * [X] BoxLayout
    * [X] GridLayout
    * [X] FlowLayout
    * [ ] SnapLayout
    * [ ] TableLayout
* [ ] Existing HTML, CSS, and JavaScript optimization, compression, and obfuscation for production use
* [ ] Image optimizer for production use supporting compile-time and run-time optimization
* [ ] HTML caching in Local Storage for offline and faster cached loading
* [ ] Convenience functionality for deploying native applications
    * [ ] Windows
    * [ ] Mac
    * [ ] Linux
    * [ ] iOS
    * [ ] Android
    
## Features for 0.3.0 (In-Progress)

* [ ] Major cleanup and simplification of writing a web application
    * [X] Simplify connectivity / communication functionality
    * [ ] Provide simple classes to extend for client, server, and shared
    * [ ] SBT plugin to simplify build setup (especially management and sharing of generated JS)
    * [ ] Ability to create an app with only three classes (client, server, and shared)
    * [ ] Create a template project (maybe Giter8)

## Features for 0.2.0 (Released 2017.02.27)

* [X] Complete SSL support (binding and proxying)
* [X] Ajax Request / Response framework
* [X] XML Content Modification Streams for any XML / HTML content
* [X] Compile-time HTML injection into Scala.js from existing template
* [X] Shared Var - shared state modifiable on client or server
* [X] History Management convenience functionality (Scala.js)
* [X] YouIApplication to simplify client/server functionality
    * [X] General WebSocket connectivity support
    * [X] Communication support
    * [X] Streaming convenience functionality
    * [X] Pages functionality (JVM)
    * [X] Filter support on HttpHandlers
    * [X] Screens functionality (Scala.js)
    * [X] Receive JavaScript errors on server for logging
    * [X] Client auto reload on server stop / restart
* [X] Template features
    * [X] Includes
    * [X] Server for faster testing
    * [X] Real-time updates in browser
    * [X] LESS and SASS support
* [X] HTTP Client
    * [X] Basic HTTP request / response using abstraction
    * [X] Fully and properly asynchronous and non-blocking
    * [X] Restful support for case class serialization and deserialization

## Features for 0.1.0 (Released 2016.12.22)

* [X] Scala JVM and JS support
* [X] URL implementation offering good parsing and flexibility
    * [X] compile-time interpolation
* [X] Server abstraction with HttpRequest and HttpResponse allowing for multiple implementations
    * [X] Undertow implementation
* [X] HttpHandler prioritization and flow to allow handlers to build upon each other
* [X] IPv4 and IPv6 wrapper classes
* [X] TestServerImplementation for easy unit testing
* [X] Full Cookie support
* [X] Wrapper for standard ContentTypes
* [X] WebSocket support through abstraction
* [X] Proxying support and optional ProxyingSupport trait to be mixed into Server
* [X] Session support
* [X] Communication implementation supporting client and server with JVM and JS support
