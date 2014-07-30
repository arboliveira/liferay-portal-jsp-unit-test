Liferay Portal JSP Unit Test examples
=====================================

How to test actual rendered HTML from your Portal JSP pages and taglibs, without the need to start a web container.


Highlights
==========

## JSP: assert rendered HTML

https://github.com/arboliveira/liferay-portal-jsp-unit-test/blob/7ad18550f37128dbba5e5f90ea86aff525aa417d/src/test/java/com/liferay/jsp/portlet/blogs/PortletBlogs_view_entry_content_Jsp_Test.java#L70-L92

## Taglibs: assert rendered HTML

https://github.com/arboliveira/liferay-portal-jsp-unit-test/blob/7ad18550f37128dbba5e5f90ea86aff525aa417d/src/test/java/com/liferay/jsp/taglib/ui/discussion/TaglibUIDiscussion_page_jsp_Test.java#L200-L222

## Portal page rendering: convenience mocks

https://github.com/arboliveira/liferay-portal-jsp-unit-test/blob/7ad18550f37128dbba5e5f90ea86aff525aa417d/src/test/java/com/liferay/test/portal/jsp/LiferayJSPTestSetUp.java#L194-L206

## Portal infrastructure: convenience mocks

https://github.com/arboliveira/liferay-portal-jsp-unit-test/blob/7ad18550f37128dbba5e5f90ea86aff525aa417d/src/test/java/com/liferay/test/portal/jsp/LiferayJSPTestSetUp.java#L77-L88


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
