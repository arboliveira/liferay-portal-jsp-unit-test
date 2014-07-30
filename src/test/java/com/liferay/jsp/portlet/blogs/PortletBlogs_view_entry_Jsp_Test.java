
package com.liferay.jsp.portlet.blogs;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.SubscriptionLocalService;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portal.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.service.WorkflowDefinitionLinkLocalServiceUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.util.SessionClicks;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.PortletURLUtil;
import com.liferay.portlet.asset.service.AssetCategoryServiceUtil;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetEntryServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyServiceUtil;
import com.liferay.portlet.blogs.BlogsPortletInstanceSettings;
import com.liferay.portlet.blogs.BlogsSettings;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.BlogsEntryLocalService;
import com.liferay.portlet.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.portlet.blogs.service.permission.BlogsEntryPermission;
import com.liferay.portlet.blogs.util.BlogsUtil;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageDisplay;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBMessageLocalService;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.permission.MBDiscussionPermission;
import com.liferay.portlet.trash.util.Trash;
import com.liferay.portlet.trash.util.TrashUtil;
import com.liferay.test.jsp.JSPTestEngine;
import com.liferay.test.jsp.JSPTestEngine.HttpServletRequestPrepare;
import com.liferay.test.jsp.ResponseContent;
import com.liferay.test.portal.jsp.LiferayJSPTestSetUp;

@PowerMockIgnore("javax.tools.*")
@PrepareForTest({
	AssetEntryLocalServiceUtil.class, AssetEntryServiceUtil.class,
	AssetTagLocalServiceUtil.class, AssetTagServiceUtil.class,
	AssetVocabularyServiceUtil.class,
	AssetCategoryServiceUtil.class,
	PortletURLUtil.class,
	BlogsUtil.class, BlogsPortletInstanceSettings.class, BlogsSettings.class,
	BlogsEntryPermission.class, BlogsEntryLocalServiceUtil.class,
	BeanParamUtil.class, SessionClicks.class,
	MBMessageLocalServiceUtil.class, MBDiscussionPermission.class,
	SubscriptionLocalServiceUtil.class,
	WorkflowDefinitionLinkLocalServiceUtil.class
})
@RunWith(PowerMockRunner.class)
public class PortletBlogs_view_entry_Jsp_Test
	implements HttpServletRequestPrepare {

	@Before
	public void setUp() throws Exception {
		engine.setURI("/html/portlet/blogs/view_entry.jsp");

		MockitoAnnotations.initMocks(this);

		liferayJSP.setUp();

		blogsJSP.setUp();
		blogsJSP.setUpBlogsPortletInstanceSettings();
		blogsJSP.setUpBlogsSettings();

		setupMocks();

		setUpAssets();

		setUpBlogsEntryLocalServiceUtil();

		mockStatic(SessionClicks.class);

		setUpMBMessageLocalServiceUtil();

		ServletTester tester = engine.getServletTester();
		tester.setAttribute("company", company);
		tester.setAttribute("user", mock(User.class));
		liferayJSP.themeDisplay.setCompany(company);

		setUpSubscriptionLocalServiceUtil();

		setUpWorkflowDefinitionLinkLocalServiceUtil();
	}

	@Test
	public void testDiscussionTaglib() throws Exception {
		engine.execute();
	}

	@Test
	public void testInTrash() throws Exception {
		when(trash.isInTrash(anyString(), anyLong())).thenReturn(true);

		ResponseContent response = engine.execute();

		response.assertContains(
			"commenting-is-disabled-because-this-entry-is-in-the-recycle-bin");
	}

	@Test
	public void testBeTheFirst() throws Exception {
		when(
			mbMessageLocalService.getThreadMessagesCount(anyLong(), anyInt()))
			.thenReturn(1);

		ResponseContent response = engine.execute();

		response.assertContains("be-the-first");
	}

	private @Mock
	MBMessageLocalService mbMessageLocalService;

	private @Mock
	MBMessageDisplay mbMessageDisplay;

	private @Mock
	SubscriptionLocalService subscriptionLocalService;

	@Mock
	WorkflowDefinitionLinkLocalService workflowDefinitionLinkLocalService;

	@Mock
	private Company company;

	private void setUpWorkflowDefinitionLinkLocalServiceUtil() {
		mockStatic(
			WorkflowDefinitionLinkLocalServiceUtil.class,
			Mockito.CALLS_REAL_METHODS);
		stub(method(
			WorkflowDefinitionLinkLocalServiceUtil.class, "getService")).toReturn(
			workflowDefinitionLinkLocalService);
	}

	private void setUpSubscriptionLocalServiceUtil() {
		mockStatic(
			SubscriptionLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			SubscriptionLocalServiceUtil.class, "getService")).toReturn(
			subscriptionLocalService);
	}

	protected void setUpMBMessageLocalServiceUtil() throws PortalException {
		mockStatic(MBMessageLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			MBMessageLocalServiceUtil.class, "getService")).toReturn(
			mbMessageLocalService);
		when(
			mbMessageLocalService.getDiscussionMessageDisplay(
				anyLong(), anyLong(), anyString(),
				anyLong(), anyInt(), anyString()))
			.thenReturn(mbMessageDisplay);

		when(mbMessageDisplay.getThread()).thenReturn(thread);

		when(mbMessageLocalService.getMessage(anyLong())).thenReturn(
			rootMessage);

		mockStatic(MBDiscussionPermission.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			MBDiscussionPermission.class, "contains",
			PermissionChecker.class, Long.TYPE, Long.TYPE,
			String.class, Long.TYPE, Long.TYPE, String.class)).toReturn(true);
	}

	protected void setUpBlogsEntryLocalServiceUtil() throws PortalException {
		mockStatic(BlogsEntryLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			BlogsEntryLocalServiceUtil.class, "getService")).toReturn(
			blogsEntryLocalService);
		when(
			blogsEntryLocalService.getEntriesPrevAndNext(Mockito.anyLong())).thenReturn(
			new BlogsEntry[3]);
	}

	private void setUpAssets() {
		mockStatic(AssetEntryLocalServiceUtil.class);
		PowerMockito.mockStatic(AssetEntryServiceUtil.class);
		PowerMockito.mockStatic(AssetTagLocalServiceUtil.class);
		PowerMockito.mockStatic(AssetTagServiceUtil.class);
		PowerMockito.mockStatic(AssetVocabularyServiceUtil.class);
		PowerMockito.mockStatic(AssetCategoryServiceUtil.class);
	}

	protected void setupMocks() throws Exception {
		ServletTester tester = engine.getServletTester();

		tester.setAttribute("portletDisplay", portletDisplay);
		when(portletDisplay.getId()).thenReturn(RandomTestUtil.randomString());

		PowerMockito.mockStatic(BeanParamUtil.class);

		new TrashUtil().setTrash(trash);
	}

	@Override
	public void prepare(HttpServletRequest request) {
		liferayJSP.prepareRequest(request);
		blogsJSP.setUpHttpServletRequest(request);
	}

	JSPTestEngine engine = new JSPTestEngine(this);

	LiferayJSPTestSetUp liferayJSP = new LiferayJSPTestSetUp(engine);

	private Blogs_view_entry_content_JSPTestSetUp blogsJSP =
		new Blogs_view_entry_content_JSPTestSetUp(engine);

	@Mock
	PortletDisplay portletDisplay;

	@Mock
	BlogsEntryLocalService blogsEntryLocalService;

	@Mock
	MBThread thread;

	@Mock
	MBMessage rootMessage;

	@Mock
	private Trash trash;
}
