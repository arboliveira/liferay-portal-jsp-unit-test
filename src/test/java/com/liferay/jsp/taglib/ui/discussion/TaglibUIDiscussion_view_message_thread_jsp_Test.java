
package com.liferay.jsp.taglib.ui.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequest;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.settings.SettingsFactoryUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactory;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.WebKeys;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.PortletURLUtil;
import com.liferay.portlet.messageboards.MBSettings;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBTreeWalker;
import com.liferay.test.jsp.JSPTestEngine;
import com.liferay.test.jsp.JSPTestEngine.HttpServletRequestPrepare;
import com.liferay.test.jsp.ResponseContent;
import com.liferay.test.portal.jsp.LiferayJSPTestSetUp;

@PowerMockIgnore("javax.tools.*")
@PrepareForTest({
	PortletURLUtil.class,
	MBSettings.class,
	FastDateFormatFactoryUtil.class
})
@RunWith(PowerMockRunner.class)
public class TaglibUIDiscussion_view_message_thread_jsp_Test
	implements HttpServletRequestPrepare {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		engine.setURI("/html/taglib/ui/discussion/view_message_thread.jsp");

		liferayJSP.setUp();

		setUpMBSettings();
		setUpResourceURL();
		setUpFastDateFormatFactoryUtil();
		setUpMBMessage();
		setUpPathThemeImages();
	}

	@Override
	public void prepare(HttpServletRequest request) {
		if (guardAgainstRecursion)
			return;

		liferayJSP.prepareRequest(request);

		request.setAttribute(
			WebKeys.MESSAGE_BOARDS_TREE_WALKER_LAST_NODE,
			Boolean.valueOf(_lastNode));

		request.setAttribute(
			WebKeys.MESSAGE_BOARDS_TREE_WALKER_DEPTH,
			Integer.valueOf(_depth));

		request.setAttribute(
			WebKeys.MESSAGE_BOARDS_TREE_WALKER_CUR_MESSAGE, mbMessage);

		request.setAttribute(
			WebKeys.MESSAGE_BOARDS_TREE_WALKER, mbTreeWalker);

		guardAgainstRecursion = true;
	}

	@Test
	public void testDepth0() throws Exception {
		testDepth(0, 0);
	}

	@Test
	public void testDepth2() throws Exception {
		testDepth(2, 20);
	}

	@Test
	public void testRootFalseLastNodeFalse() throws Exception {
		testRootFalseLastNode(false, "t");
	}

	@Test
	public void testRootFalseLastNodeTrue() throws Exception {
		testRootFalseLastNode(true, "l");
	}

	@Test
	public void testRootTrue() throws Exception {
		when(mbMessage.isRoot()).thenReturn(true);

		render();

		assertNotContains(pathThemeImages);
	}

	@Test
	public void testRowHREF() throws Exception {
		String rowHREF = prepareRowHREF();

		String body = StringUtil.randomString(60);

		when(mbMessage.getBody()).thenReturn(body);

		render();

		String shortened = body.substring(0, 47) + StringPool.TRIPLE_PERIOD;

		assertRowHREF(rowHREF, shortened);
	}

	@Test
	public void testAnonymousTrueUsernameNull() throws Exception {
		String username = null;
		String expectedAnchor = "anonymous";
		testAnonymousTrue(username, expectedAnchor);
	}

	@Test
	public void testAnonymousTrueUsernameNotNull() throws Exception {
		String username = RandomTestUtil.randomString();
		String expectedAnchor = username;
		testAnonymousTrue(username, expectedAnchor);
	}

	@Test
	public void testAnonymousFalse() throws Exception {
		String username = RandomTestUtil.randomString();
		String expectedAnchor = username;

		String rowHREF = prepareRowHREF();

		when(mbMessage.isAnonymous()).thenReturn(false);
		when(liferayJSP.portal.getUserName(mbMessage)).thenReturn(username);

		render();

		assertRowHREF(rowHREF, expectedAnchor);
	}

	@Test
	public void testModifiedDate() throws Exception {
		String rowHREF = prepareRowHREF();

		Date date = new Date();
		when(mbMessage.getModifiedDate()).thenReturn(date);

		Format format = mock(Format.class);
		whenGetDateTime(format);

		String formatted = RandomTestUtil.randomString();
		when(format.format(any(), (StringBuffer)any(), (FieldPosition)any()))
			.thenReturn(new StringBuffer(formatted));

		render();

		assertRowHREF(rowHREF, formatted);
	}

	@Test
	public void testNoChildren() throws Exception {
		whenGetChildrenRange(mbMessage, 0, 0);

		render();

		assertContains(depthEvidence(0));
		assertNotContains(depthEvidence(10));
	}

	@Test
	public void testOneChild() throws Exception {
		long parentMessageId = RandomTestUtil.randomLong();
		MBMessage parent = mockMBMessage(parentMessageId);

		long childMessageId = RandomTestUtil.randomLong();

		MBMessage child = mockMBMessage(childMessageId);

		when(mbTreeWalker.getMessages()).thenReturn(
			Arrays.asList(child));

		whenGetChildrenRange(parent, 0, 1);
		whenGetChildrenRange(child, 0, 0);

		render(parent);

		assertContains(messageCell(0, "t", parentMessageId));
		assertContains(messageCell(10, "l", childMessageId));
	}

	@Test
	public void testTwoChildren() throws Exception {
		long parentMessageId = RandomTestUtil.randomLong();
		MBMessage parent = mockMBMessage(parentMessageId);

		long childAMessageId = RandomTestUtil.randomLong();
		MBMessage childA = mockMBMessage(childAMessageId);

		long childBMessageId = RandomTestUtil.randomLong();
		MBMessage childB = mockMBMessage(childBMessageId);

		when(mbTreeWalker.getMessages()).thenReturn(
			Arrays.asList(childA, childB));

		whenGetChildrenRange(parent, 0, 2);
		whenGetChildrenRange(childA, 0, 0);
		whenGetChildrenRange(childB, 0, 0);

		render(parent);

		assertContains(messageCell(0, "t", parentMessageId));
		assertContains(messageCell(10, "t", childAMessageId));
		assertContains(messageCell(10, "l", childBMessageId));
	}

	@Test
	public void testNestedChild() throws Exception {
		long parentMessageId = RandomTestUtil.randomLong();
		MBMessage parent = mockMBMessage(parentMessageId);

		long childAMessageId = RandomTestUtil.randomLong();
		MBMessage childA = mockMBMessage(childAMessageId);

		long childBMessageId = RandomTestUtil.randomLong();
		MBMessage childB = mockMBMessage(childBMessageId);

		long childA1MessageId = RandomTestUtil.randomLong();
		MBMessage childA1 = mockMBMessage(childA1MessageId);

		when(mbTreeWalker.getMessages()).thenReturn(
			Arrays.asList(childA, childB, childA1));

		whenGetChildrenRange(parent, 0, 2);
		whenGetChildrenRange(childA, 2, 3);
		whenGetChildrenRange(childB, 0, 0);
		whenGetChildrenRange(childA1, 0, 0);

		render(parent);

		assertContains(messageCell(0, "t", parentMessageId));
		assertContains(messageCell(10, "t", childAMessageId));
		assertContains(messageCell(20, "l", childA1MessageId));
		assertContains(messageCell(10, "l", childBMessageId));
	}

	private void render() throws Exception {
		this.c = engine.execute();
	}

	private void render(MBMessage parent) throws Exception {
		mbMessage = parent;
		render();
	}

	private void whenGetMessageId(MBMessage message, long messageId) {
		when(message.getMessageId()).thenReturn(messageId);
	}

	private void whenGetChildrenRange(MBMessage message, int... childrenRange) {
		when(mbTreeWalker.getChildrenRange(message)).thenReturn(childrenRange);
	}

	private void whenGetDateTime(Format format) {
		when(
			fastDateFormatFactory.getDateTime(
				(Locale)any(), (TimeZone)any()
				)).thenReturn(format);
	}

	private void whenGetModifiedDate(MBMessage message, Date date) {
		when(message.getModifiedDate()).thenReturn(date);
	}

	private void testAnonymousTrue(String username, String expectedAnchor)
		throws Exception {
		String rowHREF = prepareRowHREF();

		when(mbMessage.isAnonymous()).thenReturn(true);
		when(mbMessage.getUserName()).thenReturn(username);

		render();

		assertRowHREF(rowHREF, expectedAnchor);
	}

	private void testDepth(int depth, int expectedPaddingLeft) throws Exception {
		_depth = depth;

		render();

		assertContains(depthEvidence(expectedPaddingLeft));
	}

	private void testRootFalseLastNode(boolean lastNode, String expectedPng)
		throws Exception {
		when(mbMessage.isRoot()).thenReturn(false);
		_lastNode = lastNode;

		render();

		assertContains(imgEvidence(expectedPng));
	}

	private MBMessage mockMBMessage(long messageId) {
		MBMessage child = mock(MBMessage.class);
		whenGetMessageId(child, messageId);
		whenGetModifiedDate(child, new Date());
		return child;
	}

	private String prepareRowHREF() {
		String namespace = RandomTestUtil.randomString();

		liferayJSP.renderResponse.setNamespace(namespace);

		long messageId = RandomTestUtil.randomLong();

		when(mbMessage.getMessageId()).thenReturn(messageId);

		String messageURL = messageURLEvidence(namespace, messageId);
		return messageURL;
	}

	private String messageURLEvidence(String namespace, long messageId) {
		return "#" + namespace + "message_" + messageId;
	}

	private void assertContains(String s) {
		c.assertContains(s);
	}

	private void assertNotContains(String substring) {
		c.assertThatContent(
			CoreMatchers.not(StringContains.containsString(substring)));
	}

	private void assertRowHREF(String rowHREF, String anchor) {
		assertContains(hrefEvidence(rowHREF, anchor));
	}

	private String depthEvidence(int expectedPaddingLeft) {
		return "<td class=\"table-cell\" style=\"padding-left: " +
			expectedPaddingLeft +
			"px; width: 90%\">";
	}

	private String hrefEvidence(String href, String anchor) {
		return "<a href=\"" + href + "\">" + anchor + "</a>";
	}

	private String imgEvidence(String png) {
		return "<img alt=\"\" src=\"" + pathThemeImages +
			"/message_boards/" +
			png + ".png\" />";
	}

	private String messageCell(int paddingLeft, String png, long messageId) {
		return depthEvidence(paddingLeft) + imgEvidence(png)
			+ hrefEvidence(messageURLEvidence("", messageId), "null");
	}

	private void setUpFastDateFormatFactoryUtil() {
		whenGetDateTime(DateFormat.getInstance());
		new FastDateFormatFactoryUtil().setFastDateFormatFactory(fastDateFormatFactory);
	}

	private void setUpPathThemeImages() {
		pathThemeImages = RandomTestUtil.randomString();
		liferayJSP.themeDisplay.setPathThemeImages(pathThemeImages);
	}

	private void setUpResourceURL() {
		when(liferayJSP.liferayPortletResponse.createResourceURL()).thenReturn(
			mock(ResourceURL.class));
	}

	private void setUpMBMessage() {
		whenGetModifiedDate(mbMessage, new Date());
		whenGetChildrenRange(mbMessage, 0, 0);
	}

	private void setUpMBSettings() {
		new SettingsFactoryUtil().setSettingsFactory(settingsFactory);

		mockStatic(MBSettings.class, Mockito.CALLS_REAL_METHODS);

		MBSettings mbSettings = Mockito.mock(MBSettings.class);

		stub(method(MBSettings.class, "getInstance",
			Long.TYPE
			)).toReturn(mbSettings);
	}

	private boolean guardAgainstRecursion;

	private int _depth;

	private JSPTestEngine engine = new JSPTestEngine(this);

	private LiferayJSPTestSetUp liferayJSP = new LiferayJSPTestSetUp(engine);

	@Mock
	private MBMessage mbMessage;

	@Mock
	private FastDateFormatFactory fastDateFormatFactory;

	private String pathThemeImages;

	private boolean _lastNode;

	@Mock
	private SettingsFactory settingsFactory;

	@Mock
	private MBTreeWalker mbTreeWalker;

	private ResponseContent c;
}
