
package com.liferay.taglib.util;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.liferay.portal.model.ModelHints;
import com.liferay.portal.model.ModelHintsUtil;

public class IncludeTagSetUp {

	public void setUp() {
		MockitoAnnotations.initMocks(this);

		setUpModelHintsUtil();
	}

	private void setUpModelHintsUtil() {
		mockStatic(ModelHintsUtil.class, Mockito.CALLS_REAL_METHODS);

		stub(
			method(ModelHintsUtil.class, "getModelHints")).toReturn(
			modelHints);
	}

	@Mock
	ModelHints modelHints;
}
