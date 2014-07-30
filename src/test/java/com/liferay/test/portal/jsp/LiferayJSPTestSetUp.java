
package com.liferay.test.portal.jsp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Locale;

import javax.portlet.MimeResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.testing.ServletTester;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.mock.web.portlet.MockRenderResponse;

import com.liferay.portal.kernel.bean.BeanProperties;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.language.UnicodeLanguage;
import com.liferay.portal.kernel.language.UnicodeLanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletConfig;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.servlet.DirectRequestDispatcherFactory;
import com.liferay.portal.kernel.servlet.DirectRequestDispatcherFactoryUtil;
import com.liferay.portal.kernel.util.FriendlyURLNormalizer;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.HtmlImpl;
import com.liferay.portal.util.Portal;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portlet.PortletURLFactory;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.PortletURLUtil;
import com.liferay.test.jsp.JSPTestEngine;

public class LiferayJSPTestSetUp {

	public LiferayJSPTestSetUp(JSPTestEngine engine) {
		this.engine = engine;
	}

	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		engine.setResourceBase(PortalMasterProjectLocation.getDocroot());

		themeDisplay = new ThemeDisplay();
		themeDisplay.setUser(user);

		liferayPortletRequest = mock(LiferayPortletRequest.class,
			withSettings().extraInterfaces(RenderRequest.class));

		liferayPortletResponse = mock(LiferayPortletResponse.class,
			withSettings().extraInterfaces(RenderResponse.class));

		ServletTester tester = engine.getServletTester();

		tester.setAttribute("liferayPortletRequest", liferayPortletRequest);
		tester.setAttribute("liferayPortletResponse", liferayPortletResponse);
		tester.setAttribute("themeDisplay", themeDisplay);
		tester.setAttribute("locale", Locale.US);

		tester.setAttribute("portletPreferences", portletPreferences);

		setUpPortletURLUtil();

		tester.setAttribute("renderRequest", liferayPortletRequest);
		tester.setAttribute("renderResponse", new MockRenderResponse());

		setUpHtmlUtil();

		setUpDirectRequestDispatcherFactoryUtil();

		setUpPortalUtil();

		setUpLanguageUtil();
		setUpUnicodeLanguageUtil();

		setUpFriendlyURLNormalizerUtil();
	}

	protected void setUpPortalUtil() {
		when(portal.getLiferayPortletResponse(
			(PortletResponse)any())).thenReturn(liferayPortletResponse);

		when(portal.stripURLAnchor(anyString(), anyString())).then(
			returnStripURLAnchor());

		when(portal.generateRandomKey(
			(HttpServletRequest)any(), anyString())).thenReturn(
			RandomTestUtil.randomString());

		new PortalUtil().setPortal(portal);
	}

	private Answer<String[]> returnStripURLAnchor() {
		return new Answer<String[]>() {

			@Override
			public String[] answer(InvocationOnMock invocation)
				throws Throwable {

				Object[] args = invocation.getArguments();

				return new String[] {
					String.valueOf(args[0]),
					String.valueOf(args[1])
				};
			}
		};
	}

	public void prepareRequest(HttpServletRequest request) {
		preparePortletContextForTag(request);

		request.setAttribute(
			PortletRequest.LIFECYCLE_PHASE, PortletRequest.RENDER_PHASE);

		when(liferayPortletRequest.getPreferences()).thenReturn(
			portletPreferences);
		when(liferayPortletRequest.getPortletSession()).thenReturn(
			mock(PortletSession.class));

		setUpLiferayPortletConfig(request);

		setUpBeanPropertiesUtil();

		request.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);
		themeDisplay.setLayout(mock(Layout.class));

		request.setAttribute(WebKeys.CTX, request.getServletContext());
	}

	private void setUpBeanPropertiesUtil() {
		BeanProperties beanProperties = mock(BeanProperties.class);
		new BeanPropertiesUtil().setBeanProperties(beanProperties);

		when(
			beanProperties.getStringSilent(any(), anyString())).then(
			returnArgumentAsIs(1));
		when(
			beanProperties.getStringSilent(any(), anyString(), anyString())).then(
			returnArgumentAsIs(2));
	}

	protected void setUpLiferayPortletConfig(HttpServletRequest request) {
		LiferayPortletConfig liferayPortletConfig =
			mock(LiferayPortletConfig.class);
		when(liferayPortletConfig.getPortletId()).thenReturn(
			RandomTestUtil.randomString());
		request.setAttribute(
			JavaConstants.JAVAX_PORTLET_CONFIG, liferayPortletConfig);
	}

	/**
	 * Prevent:
	 * "Render response is null because this tag is not being called within the context of a portlet"
	 */
	protected void preparePortletContextForTag(HttpServletRequest request) {
		request.setAttribute(
			JavaConstants.JAVAX_PORTLET_REQUEST, liferayPortletRequest);

		when(liferayPortletResponse.createLiferayPortletURL(
			anyLong(), anyString(), anyString())).thenReturn(portletURL);
	}

	private void setUpLanguageUtil() {
		Language language = mock(Language.class);

		when(
			language.get((HttpServletRequest)any(), anyString())).then(
			returnArgumentAsIs(1));

		when(
			language.get((Locale)any(), anyString())).then(
			returnArgumentAsIs(1));

		new LanguageUtil().setLanguage(language);
	}

	private void setUpUnicodeLanguageUtil() {
		UnicodeLanguage unicodeLanguage = mock(UnicodeLanguage.class);
		new UnicodeLanguageUtil().setUnicodeLanguage(unicodeLanguage);
		when(
			unicodeLanguage.get((HttpServletRequest)any(), anyString())).then(
			returnArgumentAsIs(1));
	}

	private Answer<String> returnArgumentAsIs(final int index) {
		return new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String)invocation.getArguments()[index];
			}
		};
	}

	protected void setUpDirectRequestDispatcherFactoryUtil() {
		DirectRequestDispatcherFactory directRequestDispatcherFactory =
			mock(DirectRequestDispatcherFactory.class);

		new DirectRequestDispatcherFactoryUtil().setDirectRequestDispatcherFactory(directRequestDispatcherFactory);

		when(
			directRequestDispatcherFactory.getRequestDispatcher(
				(ServletContext)any(), anyString())).then(returnItsOwn());
	}

	private Answer<RequestDispatcher> returnItsOwn() {
		return new Answer<RequestDispatcher>() {

			@Override
			public RequestDispatcher answer(InvocationOnMock invocation)
				throws Throwable {
				Object[] args = invocation.getArguments();
				ServletContext param = (ServletContext)args[0];
				String path = (String)args[1];
				return param.getRequestDispatcher(path);
			}

		};
	}

	protected void setUpHtmlUtil() {
		new HtmlUtil().setHtml(new HtmlImpl());
	}

	protected void setUpPortletURLUtil() {
		PropsUtil.setProps(props);

		mockStatic(PortletURLUtil.class, Mockito.CALLS_REAL_METHODS);

		PowerMockito.stub(PowerMockito.method(PortletURLUtil.class,
			"getCurrent",
			LiferayPortletRequest.class, LiferayPortletResponse.class)
			).toReturn(portletURL);

		PowerMockito.stub(PowerMockito.method(PortletURLUtil.class,
			"getCurrent",
			PortletRequest.class, MimeResponse.class)
			).toReturn(portletURL);

		when(portletURLFactory.create(
			(HttpServletRequest)any(), anyString(),
			anyLong(), anyString())).thenReturn(portletURL);

		new PortletURLFactoryUtil().setPortletURLFactory(portletURLFactory);
	}

	private void setUpFriendlyURLNormalizerUtil() {
		FriendlyURLNormalizer friendlyURLNormalizer =
			mock(FriendlyURLNormalizer.class);
		when(friendlyURLNormalizer.normalize(anyString())).then(
			returnArgumentAsIs(0));
		new FriendlyURLNormalizerUtil().setFriendlyURLNormalizer(friendlyURLNormalizer);
	}
	@Mock
	LiferayPortletURL portletURL;

	@Mock
	PortletURLFactory portletURLFactory;

	@Mock
	Props props;

	@Mock
	Portal portal;

	public LiferayPortletRequest liferayPortletRequest;

	public LiferayPortletResponse liferayPortletResponse;

	public ThemeDisplay themeDisplay;

	@Mock
	public PortletPreferences portletPreferences;

	@Mock
	public User user;

	private JSPTestEngine engine;

}
