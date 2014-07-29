
package com.liferay.test.junit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.core.SubstringMatcher;

public class StringLookingAt extends SubstringMatcher {

	public StringLookingAt(String substring) {
		super(substring);
	}

	@Override
	protected boolean evalSubstringOf(String s) {
		String regex = substring;
		String input = s;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		return m.lookingAt();
	}

	@Override
	protected String relationship() {
		return "looking at";
	}
}