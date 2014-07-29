
package com.liferay.test.jsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.liferay.portal.kernel.util.StringUtil;

public class Strip {

	private String content;
	private StringWriter trimmed;

	public Strip(String content) {
		this.content = content;
		stripped = new StringWriter(content.length());
		trimmed = new StringWriter(content.length());
	}

	public void strip() throws IOException {
		BufferedReader r = new BufferedReader(new StringReader(content));
		BufferedWriter strippedWriter = new BufferedWriter(stripped);
		BufferedWriter trimmedWriter = new BufferedWriter(trimmed);
		String line;
		while ((line = r.readLine()) != null) {
			String trim = StringUtil.trim(line);
			if (!trim.isEmpty()) {
				strippedWriter.write(line);
				strippedWriter.newLine();
				trimmedWriter.write(trim);
			}
		}
		strippedWriter.flush();
		trimmedWriter.flush();
	}

	private StringWriter stripped;

	public String stripped() {
		return stripped.toString();
	}

	public String trimmed() {
		return trimmed.toString();
	}

}