package com.farpost.logwatcher;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

public class JavaStackTraceParserTest {

	private JavaStackTraceParser parser;

	@BeforeMethod
	public void setUp() throws Exception {
		parser = new JavaStackTraceParser();
	}

	@Test
	public void shouldBeAbleToParseStackTraceElementsFromExceptions() {
		parser.setAllowedPackagePrefix("com.farpost");

		String stacktrace = "java.lang.RuntimeException: Fuck you that's why\n" +
			"\tat com.farpost.logwatcher.transport.CompositeEventListener.onEntry(CompositeEventListener.java:22) ~[classes/:na]\n" +
			"\tat com.farpost.logwatcher.transport.CompositeEventListenerTest.listenerShouldBeCalledEventIfPreviousListenerThrownAnException(CompositeEventListenerTest.java:33) [test-classes/:na]\n" +
			"\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.6.0_65]\n" +
			"\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39) ~[na:1.6.0_65]\n" +
			"\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) ~[na:1.6.0_65]\n" +
			"\tat java.lang.reflect.Method.invoke(Method.java:597) ~[na:1.6.0_65]\n" +
			"\tat org.testng.internal.MethodInvocationHelper.invokeMethod(MethodInvocationHelper.java:76) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.internal.Invoker.invokeMethod(Invoker.java:673) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.internal.Invoker.invokeTestMethod(Invoker.java:846) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.internal.Invoker.invokeTestMethods(Invoker.java:1170) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.internal.TestMethodWorker.invokeTestMethods(TestMethodWorker.java:125) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.internal.TestMethodWorker.run(TestMethodWorker.java:109) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.TestRunner.runWorkers(TestRunner.java:1147) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.TestRunner.privateRun(TestRunner.java:749) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.TestRunner.run(TestRunner.java:600) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.SuiteRunner.runTest(SuiteRunner.java:317) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.SuiteRunner.runSequentially(SuiteRunner.java:312) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.SuiteRunner.privateRun(SuiteRunner.java:274) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.SuiteRunner.run(SuiteRunner.java:223) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.SuiteRunnerWorker.runSuite(SuiteRunnerWorker.java:52) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.SuiteRunnerWorker.run(SuiteRunnerWorker.java:86) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.TestNG.runSuitesSequentially(TestNG.java:1039) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.TestNG.runSuitesLocally(TestNG.java:964) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.TestNG.run(TestNG.java:900) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.remote.RemoteTestNG.run(RemoteTestNG.java:110) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.remote.RemoteTestNG.initAndRun(RemoteTestNG.java:205) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.remote.RemoteTestNG.main(RemoteTestNG.java:174) [testng-6.0.1.jar:na]\n" +
			"\tat org.testng.RemoteTestNGStarter.main(RemoteTestNGStarter.java:125) [testng-plugin.jar:na]\n" +
			"\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.6.0_65]\n" +
			"\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39) ~[na:1.6.0_65]\n" +
			"\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) ~[na:1.6.0_65]\n" +
			"\tat java.lang.reflect.Method.invoke(Method.java:597) ~[na:1.6.0_65]\n" +
			"\tat com.intellij.rt.execution.application.AppMain.main(AppMain.java:134) [idea_rt.jar:na]\n";

		List<StackTraceLine> lines = parser.parse(stacktrace);

		assertThat(lines, hasSize(2));
		assertThat(lines, hasItems(
			new StackTraceLine("com.farpost.logwatcher.transport.CompositeEventListener", "onEntry",
				"CompositeEventListener.java", 22),
			new StackTraceLine("com.farpost.logwatcher.transport.CompositeEventListenerTest",
				"listenerShouldBeCalledEventIfPreviousListenerThrownAnException", "CompositeEventListenerTest.java", 33)
		));
	}

	@Test
	public void testComplexCase() {
		parser.setAllowedPackagePrefix("com.farpost");

		String stacktrace = "com.farpost.search.TimeoutException: Deadline has been reached\n" +
			"\tat com.farpost.search.web.PerformSearchCallable.call(PerformSearchCallable.java:36)\n" +
			"\tat com.farpost.search.web.PerformSearchCallable.call(PerformSearchCallable.java:16)\n" +
			"\tat com.farpost.MdcSafeCallable.call(MdcSafeCallable.java:42)\n" +
			"\tat com.farpost.search.web.IndexNodeRestController.lambda$propagateResult$0(IndexNodeRestController.java:151)\n" +
			"\tat com.farpost.search.web.IndexNodeRestController$$Lambda$50/1280963737.call(Unknown Source)\n" +
			"\tat com.farpost.concurrent.PriorityCallable.call(PriorityCallable.java:26)\n" +
			"\tat java.util.concurrent.FutureTask.run(FutureTask.java:266)\n" +
			"\tat com.farpost.concurrent.PriorityThreadPoolExecutor$ComparableRunnableFuture.run(PriorityThreadPoolExecutor.java:58)\n" +
			"\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\n" +
			"\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\n" +
			"\tat java.lang.Thread.run(Thread.java:745)";

		List<StackTraceLine> lines = parser.parse(stacktrace);
		assertThat(lines, hasSize(6));
		assertThat(lines, hasItem(new StackTraceLine("com.farpost.search.web.PerformSearchCallable", "call",
			"PerformSearchCallable.java", 36)));
		assertThat(lines, hasItem(new StackTraceLine("com.farpost.search.web.PerformSearchCallable", "call",
			"PerformSearchCallable.java", 16)));
		assertThat(lines, hasItem(new StackTraceLine("com.farpost.MdcSafeCallable", "call", "MdcSafeCallable.java", 42)));
		assertThat(lines, hasItem(new StackTraceLine("com.farpost.search.web.IndexNodeRestController",
			"lambda$propagateResult$0", "IndexNodeRestController.java", 151)));
		assertThat(lines, hasItem(new StackTraceLine("com.farpost.concurrent.PriorityCallable", "call",
			"PriorityCallable.java", 26)));
		assertThat(lines, hasItem(new StackTraceLine("com.farpost.concurrent.PriorityThreadPoolExecutor$ComparableRunnableFuture",
			"run", "PriorityThreadPoolExecutor.java", 58)));
	}
}