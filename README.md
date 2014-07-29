Liferay Portal JSP Unit Test examples
=====================================

How to test actual rendered HTML from your Portal JSP pages and taglibs, without the need to start a web container.


How to use
==========

Your workspace must have a working "portal-master" project.

Eclipse, "Import",  "Existing Maven project".

Run the tests using the `liferay-portal-jsp-unit-test` launch configuration.


Under the hood
==============

If you ever need to configure a new Liferay Portal JSP Unit Test project from scratch.

## Eclipse project, Java Build Path

Required projects in the build path -> `portal-master`

Libraries -> `portlet.jar - portal-master/lib/global` 

## Launch configuration

Classpath, User Entries -> `Maven Dependencies` _before_ `liferay-portal-jsp-unit-test`

Source, Source Lookup Path -> `portal-master` _before_ `Default`


Acknowledgements
================

Adapted from work by Günther Enthaler, who wrote the original _Jetty JSP unit test simple example_.

https://github.com/genthaler/jetty-jsp-unit-test-simple/
