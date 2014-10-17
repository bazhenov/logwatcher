package com.farpost.logwatcher;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.*;

public class JavaStackTraceParser {

	private String allowedPackagePrefix;
	private Pattern pattern;

	public JavaStackTraceParser() {
		rebuildRegexpPattern();
	}

	public void setAllowedPackagePrefix(String allowedPackagePrefix) {
		this.allowedPackagePrefix = allowedPackagePrefix;
		rebuildRegexpPattern();
	}

	private void rebuildRegexpPattern() {
		String classNamePattern = "(?:[\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*";
		String methodNamePattern = "[\\p{L}_$][\\p{L}\\p{N}_$]*";
		String fileNamePattern = "[\\p{L}_$][\\p{L}\\p{N}_$]*\\.[a-z]{2,5}";
		String modulePattern = "[^]]+";

		if (!isNullOrEmpty(allowedPackagePrefix))
			classNamePattern = quote(allowedPackagePrefix) + "\\." + classNamePattern;

		pattern = compile("^\\s*at\\s+(" + classNamePattern + ")\\.(" + methodNamePattern + ")" +
			"(?:\\((" + fileNamePattern + "):([0-9]+)\\))(?:\\s+~?\\[" + modulePattern + "\\])??$", MULTILINE);
	}

	public List<StackTraceLine> parse(String stacktrace) {
		Matcher matcher = pattern.matcher(stacktrace);
		List<StackTraceLine> result = newArrayList();
		while (matcher.find()) {
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String fileName = optionalGroup(matcher, 3, null);
			int lintNo = safeParseInt(optionalGroup(matcher, 4, null), 0);
			result.add(new StackTraceLine(className, methodName, fileName, lintNo));
		}
		return result;
	}

	private int safeParseInt(String s, int def) {
		try {
			return parseInt(s);
		} catch (RuntimeException e) {
			return def;
		}
	}

	private static String optionalGroup(Matcher matcher, int groupNo, String def) {
		return matcher.groupCount() >= groupNo ? matcher.group(groupNo) : def;
	}
}
