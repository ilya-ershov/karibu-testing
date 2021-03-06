[![Build Status](https://travis-ci.org/mvysny/karibu-testing.svg?branch=master)](https://travis-ci.org/mvysny/karibu-testing)
[![Join the chat at https://gitter.im/vaadin/vaadin-on-kotlin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/vaadin/vaadin-on-kotlin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)

# Vaadin Browserless Testing

Vaadin-on-Kotlin promotes a testing technique called *browserless testing*. With this approach, it's not the browser you issue
testing instructions to: we bypass the browser and the JavaScript-Server bridge, and talk directly to the server Vaadin component API.

This approach has the following advantages:

* *Fast*: Browserless tests are typically 100x faster than Selenium-based tests and run in 5-60 milliseconds,
  depending on their complexity.
* *Reliable*: We don't need arbitrary sleeps since we're server-side and we can await until the request is fully processed.
  We don't use Selenium drivers which are known to fail randomly.
* *Headless*: The tests run headless since there's no browser.

With this technique you can run 600 UI tests in 7 seconds, as opposing to 1-2 hours with Selenium-based approach.
Because of the speed, you can let the UI tests run after every commit by your continuous integration server.

Since we're bypassing the browser and talking to Vaadin server API directly, you don't even need to start the servlet container -
you can just add the server jars onto testing classpath and call Vaadin server API which will in turn invoke your server logic.

A 15-minute [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) explains everything behind the browserless testing technique.

## The Testing library is Standalone

The browserless testing is an *approach* rather than a product, although it's backed by this support library.

You don't have to use Vaadin-on-Kotlin nor Karibu-DSL to use this approach; you don't even need to write your app in Kotlin.
You can just plug this library in into your Java+Vaadin-based project as a test dependency, and write only the test code in Kotlin.

## Full Documentation

For the Getting Started documentation and for the full API documentation:

* If you are using Vaadin 8, head to [Testing with Vaadin 8](karibu-testing-v8).
* If you are using Vaadin 10, head to [Testing with Vaadin 10](karibu-testing-v10).

## More Resources

* The [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) which explains everything behind the browserless testing technique.
* The [browserless web testing](http://mavi.logdown.com/posts/3147601) article describes this technique in more depth.

# License

Licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

Copyright 2017-2018 Martin Vysny

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
