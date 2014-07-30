
package com.liferay.jsp.portlet.blogs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.settings.SettingsFactoryUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactory;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.PortletURLUtil;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.BlogsEntryService;
import com.liferay.portlet.blogs.service.BlogsEntryServiceUtil;
import com.liferay.portlet.blogs.service.permission.BlogsEntryPermission;
import com.liferay.portlet.blogs.util.BlogsUtil;
import com.liferay.portlet.trash.util.Trash;
import com.liferay.portlet.trash.util.TrashUtil;
import com.liferay.test.jsp.JSPTestEngine;
import com.liferay.test.jsp.JSPTestEngine.HttpServletRequestPrepare;
import com.liferay.test.jsp.ResponseContent;
import com.liferay.test.portal.jsp.LiferayJSPTestSetUp;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
	PortletURLUtil.class, FastDateFormatFactoryUtil.class,
	BlogsUtil.class, BlogsEntryPermission.class, BlogsEntryServiceUtil.class
})
@PowerMockIgnore("javax.tools.*")
public class PortletBlogsAggregator_view_jsp_Test
	implements HttpServletRequestPrepare {

	@Before
	public void setUp() throws Exception {
		engine.setURI("/html/portlet/blogs_aggregator/view.jsp");

		MockitoAnnotations.initMocks(this);

		liferayJSP.setUp();

		setupMocks();
	}

	@Test
	public void testNoBlogs() throws Exception {
		Mockito.when(
			blogsEntryService.getGroupEntries(
				Matchers.anyLong(), (Date)Matchers.any(), Matchers.anyInt(),
				Matchers.anyInt())
			).thenReturn(Collections.<BlogsEntry> emptyList());

		ResponseContent response = engine.execute();

		response.assertContains("there-are-no-blogs");
	}

	protected void setupMocks() throws Exception, PortalException,
		SystemException {
		ServletTester tester = engine.getServletTester();

		tester.setAttribute("scopeGroupId", RandomTestUtil.randomLong());

		mockStatic(BlogsEntryPermission.class);
		when(
			BlogsEntryPermission.contains(
				(PermissionChecker)any(), (BlogsEntry)any(), anyString())).thenReturn(
			true);

		new SettingsFactoryUtil().setSettingsFactory(mock(SettingsFactory.class));

		setUpFastDateFormatFactoryUtil();

		when(entry.isVisible()).thenReturn(true);
		when(entry.getDisplayDate()).thenReturn(new Date());

		new TrashUtil().setTrash(trash);

		when(liferayJSP.portletPreferences.getValue("organizationId", "0"))
			.thenReturn(String.valueOf(RandomTestUtil.randomLong()));

		when(liferayJSP.portletPreferences.getValue("selectionMethod", "users"))
			.thenReturn("groups");

		when(liferayJSP.liferayPortletRequest.getParameter("resetCur"))
			.thenReturn("true");

		when(liferayJSP.liferayPortletRequest.getWindowState())
			.thenReturn(WindowState.NORMAL);

		setUpBlogsEntryServiceUtil();

	}

	protected void setUpFastDateFormatFactoryUtil() {
		when(
			fastDateFormatFactory.getDateTime(
				(Locale)any(), (TimeZone)any())).thenReturn(
			DateFormat.getInstance());
		new FastDateFormatFactoryUtil().setFastDateFormatFactory(fastDateFormatFactory);
	}

	protected void setUpBlogsEntryServiceUtil() throws PortalException {
		mockStatic(BlogsEntryServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			BlogsEntryServiceUtil.class, "getService")).toReturn(
			blogsEntryService);
	}

	@Override
	public void prepare(HttpServletRequest request) {
		liferayJSP.prepareRequest(request);
		request.setAttribute("view_entry_content.jsp-entry", entry);
	}

	JSPTestEngine engine = new JSPTestEngine(this);

	LiferayJSPTestSetUp liferayJSP = new LiferayJSPTestSetUp(engine);

	@Mock
	BlogsEntry entry;

	@Mock
	FastDateFormatFactory fastDateFormatFactory;

	@Mock
	Trash trash;

	@Mock
	BlogsEntryService blogsEntryService;
}
