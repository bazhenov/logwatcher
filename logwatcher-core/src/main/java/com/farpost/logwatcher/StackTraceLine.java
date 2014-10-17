package com.farpost.logwatcher;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public final class StackTraceLine {

	@Nonnull
	private final String className;
	@Nonnull
	private final String methodName;

	@Nullable
	private final String fileName;
	private final int lineNo;

	public StackTraceLine(String className, String methodName, @Nullable String fileName, int lineNo) {
		checkArgument(!isNullOrEmpty(className), "Class name should not be empty");
		checkArgument(!isNullOrEmpty(methodName), "Method name should not be empty");
		checkArgument(lineNo >= 0, "Line number should not be negative");

		this.className = className;
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNo = lineNo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StackTraceLine)) return false;

		StackTraceLine that = (StackTraceLine) o;

		return lineNo == that.lineNo
			&& className.equals(that.className)
			&& !(fileName != null ? !fileName.equals(that.fileName) : that.fileName != null)
			&& methodName.equals(that.methodName);
	}

	@Nonnull
	public String getClassName() {
		return className;
	}

	@Nonnull
	public String getMethodName() {
		return methodName;
	}

	@Nullable
	public String getFileName() {
		return fileName;
	}

	public int getLineNo() {
		return lineNo;
	}

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + methodName.hashCode();
		result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
		result = 31 * result + lineNo;
		return result;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("className", className)
			.add("methodName", methodName)
			.add("fileName", fileName)
			.add("lineNo", lineNo)
			.toString();
	}
}