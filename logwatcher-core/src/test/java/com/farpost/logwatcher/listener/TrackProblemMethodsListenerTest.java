package com.farpost.logwatcher.listener;

import com.farpost.logwatcher.JavaStackTraceParser;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.StackTraceLine;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Set;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static com.farpost.logwatcher.listener.TrackProblemMethodsListener.ClusterReference;
import static com.google.common.collect.Iterables.getFirst;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TrackProblemMethodsListenerTest {

	private TrackProblemMethodsListener listener;
	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeMethod
	public void setUp() throws Exception {
		JavaStackTraceParser parser = new JavaStackTraceParser();
		parser.setAllowedPackagePrefix("com.farpost");
		listener = new TrackProblemMethodsListener(parser);
	}

	@Test
	public void listenerShouldTrackProblemMethods() {
		LogEntry entry = entry()
			.applicationId("search")
			.causedBy(new RuntimeException("foo"))
			.create();
		listener.onEntry(entry);

		Set<ClusterReference> refs = listener.getTrackedClusterReferences();
		assertThat(refs, hasSize(1));
		@SuppressWarnings("ConstantConditions")
		StackTraceLine line = getFirst(refs, null).getStackTraceLine();
		assertThat(line.getClassName(), is("com.farpost.logwatcher.listener.TrackProblemMethodsListenerTest"));
		assertThat(line.getMethodName(), is("listenerShouldTrackProblemMethods"));
	}

	@Test
	public void clusterReferenceShouldBeSerializableToJson() throws IOException {
		ClusterReference ref = new ClusterReference(new StackTraceLine("foo", "bar", null, 0), fromHexString("ab"));

		JsonNode t1 = mapper.readTree("{\"file\":null,\"class\":\"foo\",\"method\":\"bar\",\"cluster\":\"ab\",\"line\":0}");
		JsonNode t2 = mapper.readTree(mapper.writeValueAsString(ref));
		assertThat(t1, equalTo(t2));
	}
}