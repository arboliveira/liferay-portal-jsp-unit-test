
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
import com.liferay.portal.kernel.util.HtmlUtil;
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
import com.liferay.portlet.messageboards.MBSettings;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageDisplay;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBTreeWalker;
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
	MBSettings.class,
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

		engine.setURI("/html/taglib/ui/discussion/page.jsp");

		liferayJSP.setUp();
		vmth.setUp();

		new Discussion_page_jsp_TestSetUp().setUp();

		userDisplayTagTestSetUp = new UserDisplayTagTestSetUp(liferayJSP.user);
		userDisplayTagTestSetUp.setUp();

		new IncludeTagSetUp().setUp();

		setupMocks();

		setUpAssets();

		setUpBlogsEntryLocalServiceUtil();

		mockStatic(SessionClicks.class);

		setUpMBMessageLocalService();

		setUpDiscussionMessageDisplay();

		setUpMBDiscussionPermission();

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
	public void testInTrash() throws Exception {
		setUpMBMessageLocalServiceUtil();

		when(trash.isInTrash(anyString(), anyLong())).thenReturn(true);

		render();

		assertContains("commenting-is-disabled-because-this-entry-is-in-the-recycle-bin");
	}

	@Test
	public void testBeTheFirst() throws Exception {
		setUpNoCommentsYet();

		render();

		assertContains("be-the-first");
	}

	protected void setUpNoCommentsYet() throws Exception {
		setUpMessageCount(1);
	}

	@Test
	public void testAddComment() throws Exception {
		setUpCommentedAlready();

		render();

		assertContains("<span class=\"taglib-text \">add-comment</span>");
	}

	protected void setUpCommentedAlready() throws Exception {
		setUpMessageCount(2);
	}

	@Test
	public void testMBMessageLocalServiceCommentsNotVisible() throws Exception {
		setUpMBMessageLocalServiceUtil();

		setUpMessageCount(0);

		render();

		assertCommentsNotVisible();
	}

	@Test
	public void testMBMessageLocalServiceCommentsRendered() throws Exception {
		setUpMBMessageLocalServiceUtil();

		setUpMBMessageLocalServiceDiscussionWithSingleComment();

		when(singleComment.getBody()).thenReturn(RandomTestUtil.randomString());

		render();

		int singleCommentSeq = getMessageCount();
		String randomNamespace = find_randomNamespace();

		assertContains("<textarea class=\"field form-control\"  " +
			"id=\"" + randomNamespace + "editReplyBody" + singleCommentSeq +
			"\"  " +
			"name=\"editReplyBody" + singleCommentSeq + "\"     " +
			"title=\"reply-body\"  wrap=\"soft\" style=\"height: 100px;\" >" +
			singleComment.getBody() +
			"</textarea>");

		String href = HtmlUtil.escapeAttribute(
			"javascript:if (confirm(" +
				"'are-you-sure-you-want-to-delete-this'" +
				")) { " +
				randomNamespace +
				"deleteMessage(" +
				singleCommentSeq +
				"); } else { self.focus(); }");

		c.assertLookingAt(".*" +
			Pattern.quote(
				"<a href=\"" + href + "\" class=\" taglib-icon\" id=\"") +
			"........" +
			Pattern.quote(
				"\" >" +
					"<i class=\"icon-remove\"></i>" +
					"<span class=\"taglib-text \">delete</span>"));
	}

	@Test
	public void testMBMessageLocalServiceCommentsVisible() throws Exception {
		setUpMBMessageLocalServiceUtil();

		setUpMBMessageLocalServiceDiscussionWithSingleComment();

		render();

		assertCommentsVisible();
	}

	@Test
	public void testMBTreeWalkerCommentsNotVisible() throws Exception {
		setUpMBTreeWalker();

		when(mbTreeWalker.getMessages()).thenReturn(Arrays.<MBMessage> asList());

		setUpMBTreeWalkerNoChildren();

		render();

		assertCommentsNotVisible();
	}

	@Test
	public void testMBTreeWalkerCommentsVisible() throws Exception {
		setUpMBTreeWalker();

		setUpMBTreeWalkerDiscussionWithSingleComment();

		setUpMBTreeWalkerNoChildren();

		render();

		assertCommentsVisible();
	}

	@Test
	public void testMBTreeWalkerOneChild() throws Exception {
		setUpMBTreeWalker();

		setUpMBTreeWalkerDiscussionWithSingleComment();

		when_getChildrenRange(rootMessage, 1, 2);
		when_getChildrenRange(singleComment, 0, 0);

		long childMessageId = RandomTestUtil.randomLong();
		when(singleComment.getMessageId()).thenReturn(childMessageId);

		render();

		assertContains(vmth.messageCell(0, "l", childMessageId));
	}

	@Test
	public void testMBTreeWalkerTwoChildren() throws Exception {
		setUpMBTreeWalker();

		setUpMBTreeWalkerDiscussionWithSingleComment();

		MBMessage parent = rootMessage;
		MBMessage childA = singleComment;
		MBMessage childB = mockMBMessage();

		messages = Arrays.asList(parent, childA, childB);
		when(mbTreeWalker.getMessages()).thenReturn(messages);

		when_getChildrenRange(parent, 1, 3);
		when_getChildrenRange(childA, 0, 0);
		when_getChildrenRange(childB, 0, 0);

		long childAMessageId = RandomTestUtil.randomLong();
		when(childA.getMessageId()).thenReturn(childAMessageId);

		long childBMessageId = RandomTestUtil.randomLong();
		when(childB.getMessageId()).thenReturn(childBMessageId);

		render();

		assertContains(vmth.messageCell(0, "t", childAMessageId));
		assertContains(vmth.messageCell(0, "l", childBMessageId));
	}

	@Test
	public void testMBTreeWalkerTable() throws Exception {
		setUpMBTreeWalker();

		setUpMBTreeWalkerDiscussionWithSingleComment();

		setUpMBTreeWalkerNoChildren();

		render();

		assertContains("<table class=\"table table-bordered table-hover table-striped tree-walker\">");
	}

	private void render() throws Exception {
		this.c = engine.execute();
	}

	private void assertContains(String s) {
		c.assertContains(s);
	}

	protected void assertCommentsVisible() {
		c.assertContains(evidenceCommentsVisible());
	}

	protected void assertCommentsNotVisible() {
		c.assertNotContains(evidenceCommentsVisible());
	}

	protected String evidenceCommentsVisible() {
		String randomNamespace = find_randomNamespace();
		return "<a name=\"" + randomNamespace + "messages_top\"></a>";
	}

	protected void setUpDiscussionWithSingleComment() {
		discussionPlaceholder = mockMBMessage();
		singleComment = mockMBMessage();
		messages = Arrays.asList(discussionPlaceholder, singleComment);
	}

	protected void setUpMBMessageLocalServiceDiscussionWithSingleComment()
		throws Exception {
		setUpDiscussionWithSingleComment();

		int messagesCount = messages.size();
		setUpMessageCount(messagesCount);

		when(mbMessageLocalService.getThreadRepliesMessages(
			anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(
			messages);
	}

	protected void setUpMBTreeWalker() {
		when(mbMessageDisplay.getTreeWalker()).thenReturn(mbTreeWalker);
		when(mbTreeWalker.getRoot()).thenReturn(rootMessage);
	}

	protected void setUpMBTreeWalkerDiscussionWithSingleComment() {
		setUpDiscussionWithSingleComment();

		when(mbTreeWalker.getMessages()).thenReturn(messages);
	}

	protected void setUpMBTreeWalkerNoChildren() {
		when_getChildrenRange(rootMessage, 0, 0);
	}

	private void when_getChildrenRange(MBMessage message, int... childrenRange) {
		when(mbTreeWalker.getChildrenRange(message)).thenReturn(childrenRange);
	}

	protected String find_randomNamespace() {
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
				.matcher(c.content());
		matcher.find();
		return matcher.group(1);
	}

	protected MBMessage mockMBMessage() {
		MBMessage m = mock(MBMessage.class);
		when(m.getModifiedDate()).thenReturn(new Date());
		return m;
	}

	protected void setUpMessageCount(int value) throws Exception {
		setUpMBMessageLocalServiceUtil();

		when(
			mbMessageLocalService.getThreadMessagesCount(
				anyLong(), anyInt())).thenReturn(value);
	}

	private int getMessageCount() {
		return messages.size();
	}

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
		long rootMessageId = RandomTestUtil.randomLong();

		when(thread.getRootMessageId()).thenReturn(rootMessageId);

		when(mbMessageLocalService.getMessage(rootMessageId)).thenReturn(
			rootMessage);
	}

	protected void setUpMBDiscussionPermission() {
		mockStatic(MBDiscussionPermission.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			MBDiscussionPermission.class, "contains",
			PermissionChecker.class, Long.TYPE, Long.TYPE,
			String.class, Long.TYPE, Long.TYPE, Long.TYPE,
			String.class)).toReturn(true);
		stub(method(
			MBDiscussionPermission.class, "contains",
			PermissionChecker.class, Long.TYPE, Long.TYPE,
			String.class, Long.TYPE, Long.TYPE, String.class)).toReturn(true);
	}

	protected void setUpDiscussionMessageDisplay() throws PortalException {
		when(
			mbMessageLocalService.getDiscussionMessageDisplay(
				anyLong(), anyLong(), anyString(),
				anyLong(), anyInt(), anyString()))
			.thenReturn(mbMessageDisplay);

		when(mbMessageDisplay.getThread()).thenReturn(thread);
	}

	protected void setUpMBMessageLocalService() {
		mockStatic(MBMessageLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			MBMessageLocalServiceUtil.class, "getService")).toReturn(
			mbMessageLocalService);
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

	JSPTestEngine engine = new JSPTestEngine(this);

	private LiferayJSPTestSetUp liferayJSP = new LiferayJSPTestSetUp(engine);

	private TaglibUIDiscussion_view_message_thread_jsp_TestHelper vmth =
		new TaglibUIDiscussion_view_message_thread_jsp_TestHelper(liferayJSP);

	@Mock
	PortletDisplay portletDisplay;

	@Mock
	BlogsEntryLocalService blogsEntryLocalService;

	@Mock
	MBThread thread;

	@Mock
	private MBMessage rootMessage;

	private MBMessage discussionPlaceholder;

	private MBMessage singleComment;

	@Mock
	private Trash trash;

	private UserDisplayTagTestSetUp userDisplayTagTestSetUp;

	private ResponseContent c;

	@Mock
	private MBTreeWalker mbTreeWalker;

	private List<MBMessage> messages;
}
