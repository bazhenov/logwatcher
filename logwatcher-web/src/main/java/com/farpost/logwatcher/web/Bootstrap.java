package com.farpost.logwatcher.web;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.transport.LogEntryListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public class Bootstrap implements InitializingBean {

	private static final Logger log = getLogger(Bootstrap.class);

	private LogEntryListener entryListener;

	private boolean loadSampleDump = false;
	private File indexLocation;

	public void setEntryListener(LogEntryListener entryListener) {
		this.entryListener = entryListener;
	}

	public void setIndexLocation(File indexLocation) {
		this.indexLocation = indexLocation;
	}

	public void setLoadSampleDump(boolean loadSampleDump) {
		this.loadSampleDump = loadSampleDump;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (loadSampleDump) {
			deleteDirectory(indexLocation);
			checkState(indexLocation.mkdir());
			log.info("Loading sample dump...");
			new Thread(new DumpLoader()).start();
		}
	}

	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = checkNotNull(path.listFiles());
			for (File file : files) {
				if (file.isDirectory()) {
					checkState(deleteDirectory(file), "Unable to remove directory", file.getAbsolutePath());
				} else {
					checkState(file.delete(), "Unable to remove file: %s", file.getAbsolutePath());
				}
			}
		}
		return path.delete();
	}

	private void write(LogEntry entry) {
		entryListener.onEntry(entry);
	}

	private class DumpLoader implements Runnable {

		private Random rnd = new Random();
		private List<LogEntry> entries = newArrayList();

		@Override
		public void run() {
			Map<String, String> attributes = new HashMap<>();
			attributes.put("req.foo", "<b>Hello</b>");
			register(new LogEntryImpl(new Date(), "com.farpost.AdvertManager", "AdvertServiceException: Error Fetching http headers", Severity.error, "sum", "advertisement", attributes));
			Cause cause = new Cause("java.lang.RuntimeException", "Socket reading timeout",
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
					"\tat com.intellij.rt.execution.application.AppMain.main(AppMain.java:134) [idea_rt.jar:na]\n");

			for (int i = 0; i < 200; i++) {
				boolean isBot =  i > 100;
				register(new LogEntryImpl(new Date(), "com.farpost.AuditPolicy", "OverflowFundsException", Severity.warning, "sum2",
					"search", new HashMap<String, String>() {{
					put("url", "/some/foo/very/long/url/to/fit/in/screen");
					put("machine", "aux1.<b>srv</b>.loc\naux2.srv.loc");
					put("parameter", "some relatively long sampled parameter with value = " + Math.round(Math.random() * 20));
					if(isBot) {
						put("isBot", "true");
					}
				}}, cause));
			}

			register(new LogEntryImpl(new Date(), "", "Ooops", Severity.info, "sum4",
				"geocoder", null, cause));
			register(new LogEntryImpl(new Date(), "", "Ooops", Severity.debug, "sum4",
				"geocoder", null, cause));

			register(new LogEntryImpl(new Date(), "FarPost\\Geocoder\\ServiceImpl", "Ooops", Severity.trace, "sum4",
				"geocoder", null, cause));

			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very very long longvery very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));

			register(new LogEntryImpl(new Date(), "FarPost\\Geocoder\\AdvertServiceManager", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
				"frontend", null, null));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
				"frontend", null, null));

			while (!currentThread().isInterrupted()) {
				fireNewEvent();
				sleepUninterruptibly(1, SECONDS);
			}
		}

		private void fireNewEvent() {
			LogEntry e = entries.get(rnd.nextInt(entries.size()));
			LogEntry n = new LogEntryImpl(new Date(), e.getGroup(), e.getMessage(), e.getSeverity(), e.getChecksum(), e.getApplicationId(),
				e.getAttributes(), e.getCause());
			write(n);
		}

		private void register(LogEntry entry) {
			entries.add(entry);
		}
	}
}
