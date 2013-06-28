package com.farpost.logwatcher;

import org.testng.annotations.Test;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.farpost.logwatcher.TestUtils.checksum;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ChecksumTest {

	@Test
	public void createChecksumFromMd5String() {
		Checksum c = fromHexString("A1B8e7b98f6c6274bf7a454ec8bae6d4");
		assertThat(c, equalTo(checksum(0xa1, 0xb8, 0xe7, 0xb9, 0x8f, 0x6c, 0x62, 0x74, 0xbf, 0x7a, 0x45, 0x4e, 0xc8, 0xba,
			0xe6, 0xd4)));
	}

	@Test
	public void checksumToString() {
		Checksum c = checksum(1, 16, 255, 89);
		assertThat(c.toString(), is("0110ff59"));
	}
}
