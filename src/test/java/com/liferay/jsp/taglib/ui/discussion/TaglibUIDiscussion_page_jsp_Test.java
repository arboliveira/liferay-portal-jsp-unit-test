
package com.liferay.jsp.taglib.ui.discussion;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.ModelHintsUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.SubscriptionLocalService;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
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
import com.liferay.portlet.ratings.service.RatingsEntryLocalService;
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil;
import com.liferay.portlet.ratings.service.RatingsStatsLocalService;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;
import com.liferay.portlet.trash.util.Trash;
import com.liferay.portlet.trash.util.TrashUtil;
import com.liferay.taglib.ui.UserDisplayTagTestSetUp;
import com.liferay.taglib.util.IncludeTagSetUp;
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
	SessionClicks.class,
	MBMessageLocalServiceUtil.class, MBDiscussionPermission.class,
	SubscriptionLocalServiceUtil.class,
	RatingsEntryLocalServiceUtil.class, RatingsStatsLocalServiceUtil.class,
	WorkflowDefinitionLinkLocalServiceUtil.class,
	UserLocalServiceUtil.class, ModelHintsUtil.class
})
@RunWith(PowerMockRunner.class)
public class TaglibUIDiscussion_page_jsp_Test
	implements HttpServletRequestPrepare {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		engine.setResourceBase("/Users/arbo/git/liferay-portal/portal-web/docroot");
		engine.setURI("/html/taglib/ui/discussion/page.jsp");

		liferayJSP.setUp();

		new Discussion_page_jsp_TestSetUp().setUp();

		userDisplayTagTestSetUp = new UserDisplayTagTestSetUp(liferayJSP.user);
		userDisplayTagTestSetUp.setUp();

		new IncludeTagSetUp().setUp();

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

		setUpRatingsEntryLocalServiceUtil();
		setUpRatingsStatsLocalServiceUtil();
	}

	@Override
	public void prepare(HttpServletRequest request) {
		liferayJSP.prepareRequest(request);
		userDisplayTagTestSetUp.setUpRequest(request);
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
		when_getThreadMessagesCount(1);

		ResponseContent response = engine.execute();

		response.assertContains("be-the-first");
	}

	@Test
	public void testAddComment() throws Exception {
		when_getThreadMessagesCount(2);

		ResponseContent response = engine.execute();

		response.assertContains(
			"<span class=\"taglib-text \">add-comment</span>");
	}

	@Test
	public void testMessagesRendered() throws Exception {
		MBMessage m1 = newMBMessage(), m2 = newMBMessage();

		String body = RandomTestUtil.randomString();

		when(m2.getBody()).thenReturn(body);

		List<MBMessage> messages = Arrays.asList(m1, m2);

		int messagesCount = messages.size();
		when_getThreadMessagesCount(messagesCount);

		when(mbMessageLocalService.getThreadRepliesMessages(
			anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(
			messages);

		ResponseContent response = engine.execute();

		String randomNamespace = find_randomNamespace(response);

		response.assertContains("<textarea class=\"field form-control\"  " +
			"id=\"" + randomNamespace + "editReplyBody" + messagesCount +
			"\"  " +
			"name=\"editReplyBody" + messagesCount + "\"     " +
			"title=\"reply-body\"  wrap=\"soft\" style=\"height: 100px;\" >" +
			body +
			"</textarea>");

		response.assertLookingAt(".*" +
			Pattern.quote(
				"<a href=\"javascript:if (confirm(" +
					"&#039;are-you-sure-you-want-to-delete-this&#039;" +
					")) { " +
					randomNamespace +
					"deleteMessage(" +
					messagesCount +
					"); } else { self.focus(); }\" " +
					"class=\" taglib-icon\" id=\"") +
			"........" +
			Pattern.quote(
				"\" >" +
					"<i class=\"icon-remove\"></i>" +
					"<span class=\"taglib-text \">delete</span>"));
	}

	protected String find_randomNamespace(ResponseContent response) {
		String regex =
			Pattern.quote(
				"<input  class=\"field form-control\"  "
					+ "id=\"randomNamespace\"    "
					+ "name=\"randomNamespace\"     "
					+ "type=\"hidden\" value=\"")
				+
				"(.....)"
				+ Pattern.quote("\"   />");

		Matcher matcher =
			Pattern.compile(regex)
				.matcher(response.content());
		matcher.find();
		return matcher.group(1);
	}

	protected MBMessage newMBMessage() {
		MBMessage m = mock(MBMessage.class);
		when(m.getModifiedDate()).thenReturn(new Date());
		return m;
	}

	protected void when_getThreadMessagesCount(int value) {
		when(
			mbMessageLocalService.getThreadMessagesCount(
				anyLong(), anyInt())).thenReturn(value);
	}

	private @Mock
	MBMessageLocalService mbMessageLocalService;

	private @Mock
	MBMessageDisplay mbMessageDisplay;

	private @Mock
	SubscriptionLocalService subscriptionLocalService;

	@Mock
	RatingsEntryLocalService ratingsEntryLocalService;

	@Mock
	RatingsStatsLocalService ratingsStatsLocalService;

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
		mockStatic(AssetEntryServiceUtil.class);
		mockStatic(AssetTagLocalServiceUtil.class);
		mockStatic(AssetTagServiceUtil.class);
		mockStatic(AssetVocabularyServiceUtil.class);
		mockStatic(AssetCategoryServiceUtil.class);
	}

	private void setUpRatingsStatsLocalServiceUtil() {
		mockStatic(
			RatingsStatsLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			RatingsStatsLocalServiceUtil.class, "getService")).toReturn(
			ratingsStatsLocalService);
	}

	private void setUpRatingsEntryLocalServiceUtil() {
		mockStatic(
			RatingsEntryLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			RatingsEntryLocalServiceUtil.class, "getService")).toReturn(
			ratingsEntryLocalService);
	}

	protected void setupMocks() throws Exception {
		ServletTester tester = engine.getServletTester();

		tester.setAttribute("portletDisplay", portletDisplay);
		when(portletDisplay.getId()).thenReturn(RandomTestUtil.randomString());

		new TrashUtil().setTrash(trash);

		new HttpUtil().setHttp(mock(Http.class));
	}

	JSPTestEngine engine = new JSPTestEngine(this);

	LiferayJSPTestSetUp liferayJSP = new LiferayJSPTestSetUp(engine);

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

	private UserDisplayTagTestSetUp userDisplayTagTestSetUp;

}
