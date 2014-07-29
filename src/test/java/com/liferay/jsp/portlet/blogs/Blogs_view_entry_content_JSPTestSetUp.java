package com.liferay.jsp.portlet.blogs;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.testing.ServletTester;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

import com.liferay.jsp.taglib.ui.discussion.Discussion_page_jsp_TestSetUp;
import com.liferay.portal.kernel.comment.CommentManager;
import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.settings.SettingsFactoryUtil;
import com.liferay.portal.model.Layout;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.blogs.BlogsPortletInstanceSettings;
import com.liferay.portlet.blogs.BlogsSettings;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.permission.BlogsEntryPermission;
import com.liferay.portlet.blogs.util.BlogsUtil;
import com.liferay.test.jsp.JSPTestEngine;

class Blogs_view_entry_content_JSPTestSetUp {

	private JSPTestEngine engine;

	public Blogs_view_entry_content_JSPTestSetUp(JSPTestEngine engine) {
		this.engine = engine;
	}

	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		setUpBlogsEntryPermission();

		setupComments();
		
		new Discussion_page_jsp_TestSetUp().setUp();
	}

	void setUpHttpServletRequest(HttpServletRequest request) {
		request.setAttribute(com.liferay.portal.util.WebKeys.BLOGS_ENTRY, blogsEntry);
		
		request.setAttribute("view_entry_content.jsp-entry", blogsEntry);
		
		when(blogsEntry.isVisible()).thenReturn(true);
		when(blogsEntry.getDisplayDate()).thenReturn(new Date());
	}

	void setUpBlogsSettings() {
		ServletTester tester = engine.getServletTester();
		
		// because NPE Long null to long 
		tester.setAttribute("scopeGroupId", RandomTestUtil.randomLong());
		
		blogsSettings = mock(BlogsSettings.class);
		
		mockStatic(BlogsSettings.class, Mockito.CALLS_REAL_METHODS);
	
		PowerMockito.stub(PowerMockito.method(BlogsSettings.class, "getInstance", Long.TYPE)).toReturn(blogsSettings);
	}

	void setUpBlogsPortletInstanceSettings() throws Exception {
		// BlogsPortletInstanceSettings static initializer :-(
		new SettingsFactoryUtil().setSettingsFactory(mock(SettingsFactory.class));
		
		blogsPortletInstanceSettings = mock(BlogsPortletInstanceSettings.class);
		
		mockStatic(BlogsPortletInstanceSettings.class, Mockito.CALLS_REAL_METHODS);
		
		PowerMockito.stub(
			PowerMockito.method(
				BlogsPortletInstanceSettings.class, "getInstance",
				Layout.class, String.class
			)).toReturn(blogsPortletInstanceSettings);
		
		when(blogsPortletInstanceSettings.getDisplayStyle()).thenReturn(RandomTestUtil.randomString());
		when(blogsPortletInstanceSettings.isEnableComments()).thenReturn(true);
	}

	private void setupComments() throws Exception {
		mockStatic(BlogsUtil.class, Mockito.CALLS_REAL_METHODS);
		PowerMockito.stub(
				PowerMockito.method(BlogsUtil.class, "getCommentManager")
			).toReturn(
		commentManager);
	}
	
	private void setUpBlogsEntryPermission() {
		mockStatic(BlogsEntryPermission.class, Mockito.CALLS_REAL_METHODS);
		PowerMockito.stub(PowerMockito.method(
				BlogsEntryPermission.class, "contains",
						PermissionChecker.class, BlogsEntry.class, String.class
						)).toReturn(true);
	}
	
	private BlogsPortletInstanceSettings blogsPortletInstanceSettings;
	
	BlogsSettings blogsSettings;

	
	public @Mock BlogsEntry blogsEntry;
	
	public @Mock CommentManager commentManager; 
	
}
