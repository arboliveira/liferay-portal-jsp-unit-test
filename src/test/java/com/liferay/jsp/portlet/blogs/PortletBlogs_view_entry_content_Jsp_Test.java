
package com.liferay.jsp.portlet.blogs;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.PortletURLUtil;
import com.liferay.portlet.asset.service.AssetCategoryServiceUtil;
import com.liferay.portlet.asset.service.AssetTagServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyServiceUtil;
import com.liferay.portlet.blogs.BlogsPortletInstanceSettings;
import com.liferay.portlet.blogs.service.permission.BlogsEntryPermission;
import com.liferay.portlet.blogs.util.BlogsUtil;
import com.liferay.portlet.trash.util.Trash;
import com.liferay.portlet.trash.util.TrashUtil;
import com.liferay.test.jsp.JSPTestEngine;
import com.liferay.test.jsp.JSPTestEngine.HttpServletRequestPrepare;
import com.liferay.test.jsp.ResponseContent;
import com.liferay.test.portal.jsp.LiferayJSPTestSetUp;

@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({
	PortletURLUtil.class, BlogsUtil.class,
	BlogsEntryPermission.class, BlogsPortletInstanceSettings.class,
	AssetTagServiceUtil.class,
	AssetVocabularyServiceUtil.class, AssetCategoryServiceUtil.class
})
@PowerMockIgnore("javax.tools.*")
public class PortletBlogs_view_entry_content_Jsp_Test
	implements HttpServletRequestPrepare {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		engine.setURI("/html/portlet/blogs/view_entry_content.jsp");

		liferayJSP.setUp();
		blogsJSP.setUp();
		blogsJSP.setUpBlogsPortletInstanceSettings();

		setupMocks();

		setUpAssets();
	}

	private void setUpAssets() {
		PowerMockito.mockStatic(AssetVocabularyServiceUtil.class);
		PowerMockito.mockStatic(AssetCategoryServiceUtil.class);
		PowerMockito.mockStatic(AssetTagServiceUtil.class);
	}

	@Test
	public void test1Comment() throws Exception {
		testCommentCount(1, "1 comment");
	}

	@Test
	public void testNComments() throws Exception {
		testCommentCount(142857, "142857 comments");
	}

	protected void testCommentCount(int commentCount, String rendered)
		throws Exception {
		when(
			blogsJSP.commentManager.getCommentsCount(
				anyString(), anyLong())).thenReturn(commentCount);

		ResponseContent response = engine.execute();

		response.assertContains(
			"<a href=\"" +
				HtmlUtil.escapeAttribute("portletURL#blogsCommentsPanelContainer") +
				"\" >"
				+ rendered
				+ "</a>");
	}

	protected void setupMocks() throws Exception {
		new TrashUtil().setTrash(mock(Trash.class));
	}

	@Override
	public void prepare(HttpServletRequest request) {
		blogsJSP.setUpHttpServletRequest(request);

		prepareTaglibs(request);
	}

	private void prepareTaglibs(HttpServletRequest request) {
		liferayJSP.prepareRequest(request);

		LiferayPortletResponse liferayPortletResponse =
			liferayJSP.liferayPortletResponse;

		request.setAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE, liferayPortletResponse);

		when(liferayPortletResponse.getNamespace()).thenReturn(
			RandomTestUtil.randomString());
	}

	JSPTestEngine engine = new JSPTestEngine(this);

	LiferayJSPTestSetUp liferayJSP = new LiferayJSPTestSetUp(engine);

	private Blogs_view_entry_content_JSPTestSetUp blogsJSP =
		new Blogs_view_entry_content_JSPTestSetUp(engine);
}
