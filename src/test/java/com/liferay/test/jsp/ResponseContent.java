
package com.liferay.test.jsp;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;

import com.liferay.test.junit.StringLookingAt;

public class ResponseContent {

	public ResponseContent(String contentRaw) throws Exception {
		this.strip = new Strip(contentRaw);
		this.strip.strip();
		content = this.strip.trimmed();

		dump();
	}

	public String content() {
		return content;
	}

	public void assertThatContent(Matcher<? super String> matcher) {
		assertThat(content, matcher);
	}

	public void assertContains(String substring) {
		assertThat(content, containsString(substring));
	}

	public void assertNotContains(String substring) {
		assertThat(content, not(containsString(substring)));
	}

	public void assertLookingAt(String regex) {
		assertThat(content, new StringLookingAt(regex));
	}

	private void dump() throws Exception {
		if (SYSOUT_RESPONSE_CONTENT)
			System.out.println(strip.stripped());
	}

	private String content;

	private Strip strip;

	private static final boolean SYSOUT_RESPONSE_CONTENT = true;

}