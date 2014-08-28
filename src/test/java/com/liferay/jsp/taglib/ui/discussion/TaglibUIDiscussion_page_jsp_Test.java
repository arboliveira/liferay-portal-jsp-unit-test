
package com.liferay.jsp.taglib.ui.discussion;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.replace;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
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
import org.powermock.reflect.Whitebox;

import com.liferay.portal.kernel.comment.CommentManager;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.ModelHintsUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
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
import com.liferay.portlet.messageboards.comment.MBCommentManagerImpl;
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
import com.liferay.test.junit.StringLookingAt;
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
	SearchContainer.class, SessionClicks.class,
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

		commentManager = createCommentManager();
	}

	@Override
	public void prepare(HttpServletRequest request) {
		liferayJSP.prepareRequest(request);
		userDisplayTagTestSetUp.setUpRequest(request);

		/*
		 * request.setAttribute(
		 * "liferay-ui:discussion:discussionViewHelperFactory", new
		 * DiscussionViewHelperFactoryImpl(commentManager));
		 */

		request.setAttribute(
			"liferay-ui:discussion:hideControls",
			String.valueOf(_hideControls));
	}

	@Test
	public void test_canViewDiscussion_true_MessagesCount() throws Exception {
		setUpDiscussionPageMessageCount(2);

		removePermission(ActionKeys.VIEW);

		render();

		assertContains("<div class=\"taglib-discussion\"");
	}

	@Test
	public void test_canViewDiscussion_true_Permission() throws Exception {
		setUpDiscussionPageMessageCount(1);

		render();

		assertContains("<div class=\"taglib-discussion\"");
	}

	@Test
	public void test_canViewDiscussion_false() throws Exception {
		setUpDiscussionPageMessageCount(1);

		removePermission(ActionKeys.VIEW);

		render();

		c.assertNotContains("<div class=\"taglib-discussion\"");
	}

	@Test
	public void test_getThreadId() throws Exception {
		setUpDiscussionPageMessageCount(2);

		long threadId = RandomTestUtil.randomLong();

		when(this.thread.getThreadId()).thenReturn(threadId);

		render();

		c.assertContains(
			"<input  class=\"field form-control\"  " +
				"id=\"threadId\"    name=\"threadId\"     " +
				"type=\"hidden\" value=\"" + threadId + "\"   />");
	}

	@Test
	public void test_canViewDiscussionControls_false_Hide()
		throws Exception {

		setUpDiscussionPage();

		_hideControls = true;

		render();

		assertDiscussionControlsVisible(false);
	}

	@Test
	public void test_canViewDiscussionControls_false_Permission()
		throws Exception {

		setUpDiscussionPage();

		_hideControls = false;

		removePermission(ActionKeys.ADD_DISCUSSION);

		render();

		assertDiscussionControlsVisible(false);
	}

	@Test
	public void test_canViewDiscussionControls_true()
		throws Exception {

		setUpDiscussionPage();

		_hideControls = false;

		render();

		assertDiscussionControlsVisible(true);
	}

	@Test
	public void test_getRootCommentMessageId() throws Exception {

		setUpDiscussionPage();

		long messageId = RandomTestUtil.randomLong();
		when(rootMessage.getMessageId()).thenReturn(messageId);

		render();

		String randomNamespace = find_randomNamespace();

		assertContains("<div id=\"" +
			randomNamespace +
			"messageScroll" +
			messageId +
			"\">" +
			"<input  class=\"field form-control\"  id=\"messageId0\"    name=\"messageId0\"     type=\"hidden\" value=\"" +
			messageId +
			"\"   />" +
			"<input  class=\"field form-control\"  id=\"parentMessageId0\"    name=\"parentMessageId0\"     type=\"hidden\" value=\"" +
			messageId +
			"\"   />" +
			"</div>");
	}

	@Test
	public void test_isInTrash() throws Exception {
		setUpDiscussionPage();

		when(trash.isInTrash(anyString(), anyLong())).thenReturn(true);

		render();

		assertContains("commenting-is-disabled-because-this-entry-is-in-the-recycle-bin");
	}

	@Test
	public void test_noCommentsYet_true() throws Exception {
		setUpNoCommentsYet();

		render();

		assertContains("be-the-first");
	}

	@Test
	public void test_noCommentsYet_false() throws Exception {
		setUpCommentedAlready();

		render();

		assertContains("<span class=\"taglib-text \">add-comment</span>");
	}

	@Test
	public void test_hasCommentsToView_false_DiscussionPage() throws Exception {
		setUpDiscussionPageMessageCount(0);

		render();

		assert_hasCommentsToView(false);
	}

	@Test
	public void test_hasPermissionToEdit_true() throws Exception {
		setUpDiscussionPageDiscussionWithSingleComment();

		String singleCommentBody = setUpSingleCommentBody();

		render();

		assertEditReplyBody(true, singleCommentBody);
	}

	@Test
	public void test_hasPermissionToEdit_false() throws Exception {
		setUpDiscussionPageDiscussionWithSingleComment();

		String singleCommentBody = setUpSingleCommentBody();

		removePermission(ActionKeys.UPDATE_DISCUSSION);

		render();

		assertEditReplyBody(false, singleCommentBody);
	}

	@Test
	public void test_hasPermissionToDelete_true() throws Exception {
		setUpDiscussionPageDiscussionWithSingleComment();

		render();

		assert_deleteMessage(true);
	}

	@Test
	public void test_hasPermissionToDelete_false() throws Exception {
		setUpDiscussionPageDiscussionWithSingleComment();

		removePermission(ActionKeys.DELETE_DISCUSSION);

		render();

		assert_deleteMessage(false);
	}

	@Test
	public void test_hasCommentsToView_true_DiscussionPage() throws Exception {
		setUpDiscussionPageDiscussionWithSingleComment();

		render();

		assert_hasCommentsToView(true);
	}

	@Test
	public void test_hasCommentsToView_false_DiscussionTree() throws Exception {
		setUpDiscussionTree();

		setUpTreeNoChildren();

		render();

		assert_hasCommentsToView(false);
	}

	@Test
	public void test_hasCommentsToView_true_DiscussionTree() throws Exception {
		setUpDiscussionTreeDiscussionWithSingleComment();

		setUpTreeNoChildren();

		render();

		assert_hasCommentsToView(true);
	}

	@Test
	public void test_getTopmostThreadedReplies_oneChild() throws Exception {
		setUpDiscussionTreeDiscussionWithSingleComment();

		when_getChildrenRange(rootMessage, 1, 2);
		when_getChildrenRange(singleComment, 0, 0);

		// when_getChildren(rootMessage, singleComment);

		long childMessageId = RandomTestUtil.randomLong();
		when(singleComment.getMessageId()).thenReturn(childMessageId);

		render();

		assertContains(vmth.messageCell(0, "l", childMessageId));
	}

	@Test
	public void test_getTopmostThreadedReplies_twoChildren() throws Exception {
		setUpDiscussionTree();

		long childAMessageId = RandomTestUtil.randomLong();
		long childBMessageId = RandomTestUtil.randomLong();

		setUpDiscussionWithTwoComments(childAMessageId, childBMessageId);

		render();

		assertContains(vmth.messageCell(0, "t", childAMessageId));
		assertContains(vmth.messageCell(0, "l", childBMessageId));
	}

	@Test
	public void test_canViewSearchPaginator_false_Delta() throws Exception {
		int previous = SearchContainer.DEFAULT_DELTA;

		Whitebox.setInternalState(
			SearchContainer.class, "DEFAULT_DELTA", 9);
		try {
			setUpDiscussionForSearchPaginator();

			render();

			assertSearchPaginatorVisible(false);
		}
		finally {
			Whitebox.setInternalState(
				SearchContainer.class, "DEFAULT_DELTA", previous);
		}
	}

	@Test
	public void test_canViewSearchPaginator_false_Tree() throws Exception {
		long childAMessageId = RandomTestUtil.randomLong();
		long childBMessageId = RandomTestUtil.randomLong();

		setUpDiscussionWithTwoComments(childAMessageId, childBMessageId);

		setUpDiscussionTree();

		render();

		assertSearchPaginatorVisible(false);
	}

	@Test
	public void test_canViewSearchPaginator_true() throws Exception {
		setUpDiscussionForSearchPaginator();

		render();

		assertSearchPaginatorVisible(true);
	}

	@Test
	public void test_canViewThreadedReplies_true() throws Exception {
		setUpDiscussionTreeDiscussionWithSingleComment();

		setUpTreeNoChildren();

		render();

		assertThreadedRepliesVisible(true);
	}

	@Test
	public void test_canViewThreadedReplies_false() throws Exception {
		setUpDiscussionPageDiscussionWithSingleComment();

		render();

		assertThreadedRepliesVisible(false);
	}

	private void render() throws Exception {
		this.c = engine.execute();
	}

	private void assertContains(String s) {
		c.assertContains(s);
	}

	private void assertContains(boolean yes, String s) {
		org.hamcrest.Matcher<String> matcher = containsString(s);
		assertThatContent(yes, matcher);
	}

	private void assertThatContent(
		boolean yes, org.hamcrest.Matcher<String> matcher) {
		c.assertThatContent(yes ? matcher : not(matcher));
	}

	private void assert_hasCommentsToView(boolean visible) {
		String randomNamespace = find_randomNamespace();

		String evidence =
			"<a name=\"" + randomNamespace + "messages_top\"></a>";

		assertContains(visible, evidence);
	}

	private void assertDiscussionControlsVisible(boolean visible) {
		assertContains(visible, "<fieldset class=\"fieldset add-comment\" ");
	}

	private void assertEditReplyBody(
		boolean visible, String singleCommentBody) {

		int singleCommentSeq = getMessageCount();
		String randomNamespace = find_randomNamespace();

		String evidence = "<textarea class=\"field form-control\"  " +
			"id=\"" + randomNamespace + "editReplyBody" + singleCommentSeq +
			"\"  " +
			"name=\"editReplyBody" + singleCommentSeq + "\"     " +
			"title=\"reply-body\"  wrap=\"soft\" style=\"height: 100px;\" >" +
			singleCommentBody +
			"</textarea>";

		assertContains(visible, evidence);
	}

	private void assert_deleteMessage(boolean visible) {
		int singleCommentSeq = getMessageCount();
		String randomNamespace = find_randomNamespace();

		String href = HtmlUtil.escapeAttribute(
			"javascript:if (confirm(" +
				"'are-you-sure-you-want-to-delete-this'" +
				")) { " +
				randomNamespace +
				"deleteMessage(" +
				singleCommentSeq +
				"); } else { self.focus(); }");

		StringLookingAt evidence = new StringLookingAt(".*" +
			Pattern.quote(
				"<a href=\"" + href + "\" class=\" taglib-icon\" id=\"") +
			"........" +
			Pattern.quote(
				"\" >" +
					"<i class=\"icon-remove\"></i>" +
					"<span class=\"taglib-text \">delete</span>"));

		assertThatContent(visible, evidence);
	}

	private void assertSearchPaginatorVisible(boolean visible) {
		assertContains(
			visible,
			"<div class=\"taglib-page-iterator\" id=\"ocerSearchContainerPageIterator\">");
	}

	private void assertThreadedRepliesVisible(boolean visible) {
		assertContains(
			visible,
			"<table class=\"table table-bordered" +
				" table-hover table-striped tree-walker\">");
	}

	private MBCommentManagerImpl createCommentManager() {
		MBCommentManagerImpl mbCommentManagerImpl = new MBCommentManagerImpl();
		mbCommentManagerImpl.setMBMessageLocalService(mbMessageLocalService);
		return mbCommentManagerImpl;
	}

	private String find_randomNamespace() {
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

	private int getMessageCount() {
		return messages.size();
	}

	private MBMessage mockMBMessage() {
		MBMessage m = mock(MBMessage.class);
		when(m.getModifiedDate()).thenReturn(new Date());
		return m;
	}

	private void removePermission(final String actionId) {
		removedPermissions.add(actionId);
	}

	private void setMessages(MBMessage... messages) {
		this.messages = Arrays.asList(messages);
		when(mbTreeWalker.getMessages()).thenReturn(this.messages);
	}

	private void setUpCommentedAlready() throws Exception {
		setUpDiscussionPageMessageCount(2);
	}

	private void setUpNoCommentsYet() throws Exception {
		setUpDiscussionPageMessageCount(1);
	}

	private String setUpSingleCommentBody() {
		String singleCommentBody = RandomTestUtil.randomString();
		when(singleComment.getBody()).thenReturn(singleCommentBody);
		return singleCommentBody;
	}

	private void setUpDiscussionForSearchPaginator() throws Exception {
		long childAMessageId = RandomTestUtil.randomLong();
		long childBMessageId = RandomTestUtil.randomLong();

		setUpDiscussionWithTwoComments(childAMessageId, childBMessageId);

		setUpDiscussionPageMessageCountAuto();
	}

	private void setUpDiscussionWithSingleComment() {
		discussionPlaceholder = mockMBMessage();
		singleComment = mockMBMessage();
		setMessages(discussionPlaceholder, singleComment);
	}

	private void setUpDiscussionWithTwoComments(
		long childAMessageId, long childBMessageId) {

		setUpDiscussionWithSingleComment();

		MBMessage parent = rootMessage;
		MBMessage childA = singleComment;
		MBMessage childB = mockMBMessage();

		setMessages(parent, childA, childB);

		when_getChildrenRange(parent, 1, 3);
		when_getChildrenRange(childA, 0, 0);
		when_getChildrenRange(childB, 0, 0);

		// when_getChildren(parent, childA, childB);

		when(childA.getMessageId()).thenReturn(childAMessageId);
		when(childB.getMessageId()).thenReturn(childBMessageId);
	}

	private void setUpDiscussionPageDiscussionWithSingleComment()
		throws Exception {

		setUpDiscussionWithSingleComment();

		setUpDiscussionPageMessageCountAuto();

		when(mbMessageLocalService.getThreadRepliesMessages(
			anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(
			messages);
	}

	private void setUpDiscussionPageMessageCountAuto() throws Exception {
		int messagesCount = messages.size();
		setUpDiscussionPageMessageCount(messagesCount);
	}

	private void setUpDiscussionTreeDiscussionWithSingleComment() {
		setUpDiscussionTree();

		setUpDiscussionWithSingleComment();
	}

	private void setUpDiscussionTree() {
		when(mbMessageDisplay.getTreeWalker()).thenReturn(mbTreeWalker);
		when(mbTreeWalker.getRoot()).thenReturn(rootMessage);
	}

	private void setUpTreeNoChildren() {
		when_getChildrenRange(rootMessage, 0, 0);

		// when_getChildren(rootMessage);
	}

	private void setUpDiscussionPageMessageCount(int value) throws Exception {
		setUpDiscussionPage();

		when(
			mbMessageLocalService.getThreadMessagesCount(
				anyLong(), anyInt())).thenReturn(value);
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

	private void setUpMBDiscussionPermission() {
		mockStatic(MBDiscussionPermission.class, Mockito.CALLS_REAL_METHODS);

		InvocationHandler contains = new MBDiscussionPermission_contains();

		replace(method(
			MBDiscussionPermission.class, "contains",
			PermissionChecker.class, Long.TYPE, Long.TYPE,
			String.class, Long.TYPE, Long.TYPE,
			String.class)).with(contains);

		replace(method(
			MBDiscussionPermission.class, "contains",
			PermissionChecker.class, Long.TYPE, Long.TYPE,
			String.class, Long.TYPE, Long.TYPE, Long.TYPE,
			String.class)).with(contains);
	}

	class MBDiscussionPermission_contains implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

			String actionId = (String)args[args.length - 1];
			return !removedPermissions.contains(actionId);
		}

	}

	private void setUpDiscussionMessageDisplay() throws PortalException {
		when(
			mbMessageLocalService.getDiscussionMessageDisplay(
				anyLong(), anyLong(), anyString(),
				anyLong(), anyInt(), anyString()))
			.thenReturn(mbMessageDisplay);

		when(mbMessageDisplay.getThread()).thenReturn(thread);
	}

	private void setUpMBMessageLocalService() {
		mockStatic(MBMessageLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);
		stub(method(
			MBMessageLocalServiceUtil.class, "getService")).toReturn(
			mbMessageLocalService);
	}

	private void setUpDiscussionPage() throws PortalException {
		long rootMessageId = RandomTestUtil.randomLong();

		when(thread.getRootMessageId()).thenReturn(rootMessageId);

		when(mbMessageLocalService.getMessage(rootMessageId)).thenReturn(
			rootMessage);
	}

	private void setUpBlogsEntryLocalServiceUtil() throws PortalException {
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

	private void setupMocks() throws Exception {
		ServletTester tester = engine.getServletTester();

		tester.setAttribute("portletDisplay", portletDisplay);
		when(portletDisplay.getId()).thenReturn(RandomTestUtil.randomString());

		new TrashUtil().setTrash(trash);

		new HttpUtil().setHttp(mock(Http.class));
	}

	private void when_getChildrenRange(MBMessage message, int... childrenRange) {
		when(mbTreeWalker.getChildrenRange(message)).thenReturn(childrenRange);
	}

	/*
	 * private void when_getChildren(MBMessage message, MBMessage... children) {
	 * when(mbTreeWalker.getChildren(message)).thenReturn(
	 * Arrays.asList(children)); }
	 */

	final HashSet<String> removedPermissions = new HashSet<String>();

	CommentManager commentManager;

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

	private boolean _hideControls;
}