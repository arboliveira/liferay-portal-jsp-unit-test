
package com.liferay.jsp.taglib.ui.discussion;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import javax.portlet.ResourceURL;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.settings.SettingsFactoryUtil;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.messageboards.MBSettings;
import com.liferay.test.portal.jsp.LiferayJSPTestSetUp;

public class TaglibUIDiscussion_view_message_thread_jsp_TestHelper {

	public TaglibUIDiscussion_view_message_thread_jsp_TestHelper(
		LiferayJSPTestSetUp liferayJSP) {
		this.liferayJSP = liferayJSP;
	}

	void setUp() {
		MockitoAnnotations.initMocks(this);

		setUp_message_boards_init_jsp();

		setUpPathThemeImages();
	}

	protected void setUp_message_boards_init_jsp() {
		setUpMBSettings();
		setUpResourceURL();
	}

	String messageCell(
		int paddingLeft, String png, long messageId) {
		return depthEvidence(paddingLeft) + imgEvidence(png)
			+ hrefEvidence(messageURLEvidence("", messageId), "null");
	}

	static String depthEvidence(int expectedPaddingLeft) {
		return "<td class=\"table-cell\" style=\"padding-left: " +
			expectedPaddingLeft +
			"px; width: 90%\">";
	}

	static String hrefEvidence(String href, String anchor) {
		return "<a href=\"" + href + "\">" + anchor + "</a>";
	}

	String imgEvidence(String png) {
		return "<img alt=\"\" src=\"" + pathThemeImages +
			"/message_boards/" +
			png + ".png\" />";
	}

	static String messageURLEvidence(String namespace, long messageId) {
		return "#" + namespace + "message_" + messageId;
	}

	private void setUpMBSettings() {
		new SettingsFactoryUtil().setSettingsFactory(
			mock(SettingsFactory.class));

		mockStatic(MBSettings.class, CALLS_REAL_METHODS);

		MBSettings mbSettings = mock(MBSettings.class);

		stub(method(MBSettings.class, "getInstance",
			Long.TYPE
			)).toReturn(mbSettings);
	}

	private void setUpResourceURL() {
		when(liferayJSP.liferayPortletResponse.createResourceURL()).thenReturn(
			mock(ResourceURL.class));
	}

	private void setUpPathThemeImages() {
		pathThemeImages = RandomTestUtil.randomString();
		liferayJSP.themeDisplay.setPathThemeImages(pathThemeImages);
	}

	String pathThemeImages;

	private LiferayJSPTestSetUp liferayJSP;

	@Mock
	private SettingsFactory settingsFactory;
}
