
package com.liferay.taglib.ui;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceUtil;

public class UserDisplayTagTestSetUp {

	private User user;

	public UserDisplayTagTestSetUp(User user) {
		this.user = user;
	}

	public void setUp() {
		MockitoAnnotations.initMocks(this);
		setUpUserLocalServiceUtil();
	}

	public void setUpRequest(HttpServletRequest request) {
		request.setAttribute("liferay-ui:user-display:user", user);
	}

	private void setUpUserLocalServiceUtil() {
		mockStatic(UserLocalServiceUtil.class, Mockito.CALLS_REAL_METHODS);

		stub(
			method(UserLocalServiceUtil.class, "getService")).toReturn(
			userLocalService);
	}

	@Mock
	UserLocalService userLocalService;
}
