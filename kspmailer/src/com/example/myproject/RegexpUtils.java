package com.example.myproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpUtils {
	
	public static String regexGetMatchGroup(String text, String regex, int groupIndex, boolean matchCase) {
		Pattern p = regexGetPattern(regex, matchCase);
		Matcher m = p.matcher(text);
		if (m.find()) {
			return m.group(groupIndex);
		} else {
			return null;
		}
	}
	
	/**
	 * This method creates a RegExp pattern.
	 * @param regex pattern in a string
	 * @param matchCase should be matching case sensitive?
	 */
	private static Pattern regexGetPattern(String regex, boolean matchCase) {
		int flags;
		if (matchCase) {
			flags = 0;
		} else {
			flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
		return Pattern.compile(regex, flags);
	}
}
