package com.liferay.jsp.taglib.ui.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.liferay.portal.kernel.util.FastDateFormatFactory;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;

public class Discussion_page_jsp_TestSetUp {
	
	public void setUp() {
		setUpFastDateFormatFactoryUtil();
	}

	private void setUpFastDateFormatFactoryUtil() {
		FastDateFormatFactory fastDateFormatFactory = 
			mock(FastDateFormatFactory.class);
		
		when(
			fastDateFormatFactory.getDateTime(
				(Locale)any(), (TimeZone)any())
		).thenReturn(DateFormat.getInstance());
		
		new FastDateFormatFactoryUtil().setFastDateFormatFactory(
				fastDateFormatFactory);
	}
	
}
