package io.hypersistence.tsid;

import static org.junit.Assert.*;

import org.junit.Test;

import static io.hypersistence.tsid.TSID.BaseN.encode;

public class TsidFormatTest {

	@Test
	public void testFormat() {

		TSID tsid = TSID.fast();

		String[][] string = { //
				{ "HEAD", "TAIL" }, //
				{ "HEAD", "" }, //
				{ "", "TAIL" }, //
				{ "", "" } //
		};

		String format;
		String formatted;

		// '%S': upper case
		for (int i = 0; i < string.length; i++) {
			String head = string[i][0];
			String tail = string[i][1];

			// '%S': canonical string in upper case
			format = head + "%S" + tail;
			formatted = head + tsid.toString() + tail;
			assertEquals(formatted, tsid.format(format));

			// '%s': canonical string in lower case
			format = head + "%s" + tail;
			formatted = head + tsid.toLowerCase() + tail;
			assertEquals(formatted, tsid.format(format));

			// '%X': hexadecimal in upper case
			format = head + "%X" + tail;
			formatted = head + encode(tsid, 16) + tail;
			assertEquals(formatted, tsid.format(format));

			// '%x': hexadecimal in lower case
			format = head + "%x" + tail;
			formatted = head + encode(tsid, 16).toLowerCase() + tail;
			assertEquals(formatted, tsid.format(format));

			// '%d': base-10
			format = head + "%d" + tail;
			formatted = head + encode(tsid, 10) + tail;
			assertEquals(formatted, tsid.format(format));

			// '%z': base-62
			format = head + "%z" + tail;
			formatted = head + encode(tsid, 62) + tail;
			assertEquals(formatted, tsid.format(format));
		}
	}

	@Test
	public void testUnformat() {

		TSID tsid = TSID.fast();

		String[][] string = { //
				{ "HEAD", "TAIL" }, //
				{ "HEAD", "" }, //
				{ "", "TAIL" }, //
				{ "", "" } //
		};

		String format;
		String formatted;

		for (int i = 0; i < string.length; i++) {
			String head = string[i][0];
			String tail = string[i][1];

			// '%S': canonical string in upper case
			format = head + "%S" + tail;
			formatted = head + tsid.toString() + tail;
			assertEquals(tsid, TSID.unformat(formatted, format));

			// '%s': canonical string in lower case
			format = head + "%s" + tail;
			formatted = head + tsid.toLowerCase() + tail;
			assertEquals(tsid, TSID.unformat(formatted, format));

			// '%X': hexadecimal in upper case
			format = head + "%X" + tail;
			formatted = head + encode(tsid, 16) + tail;
			assertEquals(tsid, TSID.unformat(formatted, format));

			// '%x': hexadecimal in lower case
			format = head + "%x" + tail;
			formatted = head + encode(tsid, 16).toLowerCase() + tail;
			assertEquals(tsid, TSID.unformat(formatted, format));

			// '%d': base-10
			format = head + "%d" + tail;
			formatted = head + encode(tsid, 10) + tail;
			assertEquals(tsid, TSID.unformat(formatted, format));

			// '%z': base-62
			format = head + "%z" + tail;
			formatted = head + encode(tsid, 62) + tail;
			assertEquals(tsid, TSID.unformat(formatted, format));
		}
	}

	@Test
	public void testIllegalArgumentException() {

		{
			try {
				String string = TSID.fast().format("%z");
				TSID.unformat(string, "%z");
				// success
			} catch (IllegalArgumentException e) {
				fail();
			}
		}

		{
			try {
				TSID.fast().format((String) null);
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.fast().format("");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.fast().format("%");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.fast().format("%a");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.fast().format("INVALID");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.fast().format("INVALID%");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}
		}

		{
			try {
				TSID.unformat(null, "%s");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("", null);
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("", "");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("", "%s");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("INVALID", "%s");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}
		}
		{
			try {
				TSID.unformat("HEAD" + TSID.fast() + "TAIL", "HEAD%STOES");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}
			try {
				TSID.unformat("HEAD" + TSID.fast() + "TAIL", "BANG%STAIL");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("" + TSID.fast(), "%a");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("INVALID" + TSID.fast(), "INVALID%");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("HEADzzzTAIL", "HEAD%STAIL");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}

			try {
				TSID.unformat("HEADTAIL", "HEAD%STAIL");
				fail();
			} catch (IllegalArgumentException e) {
				// success
			}
		}
	}

	@Test
	public void testFormatAndUnformat() {

		TSID tsid = TSID.fast();

		String[][] string = { //
				{ "HEAD", "TAIL" }, //
				{ "HEAD", "" }, //
				{ "", "TAIL" }, //
				{ "", "" } //
		};

		String format;
		String formatted;

		for (int i = 0; i < string.length; i++) {

			String head = string[i][0];
			String tail = string[i][1];

			// '%S': canonical string in upper case
			format = head + "%S" + tail;
			formatted = head + tsid.toString() + tail;
			assertEquals(formatted, TSID.unformat(formatted, format).format(format));
			assertEquals(tsid, TSID.unformat(tsid.format(format), format));

			// '%s': canonical string in lower case
			format = head + "%s" + tail;
			formatted = head + tsid.toLowerCase() + tail;
			assertEquals(formatted, TSID.unformat(formatted, format).format(format));
			assertEquals(tsid, TSID.unformat(tsid.format(format), format));

			// '%X': hexadecimal in upper case
			format = head + "%X" + tail;
			formatted = head + encode(tsid, 16) + tail;
			assertEquals(formatted, TSID.unformat(formatted, format).format(format));
			assertEquals(tsid, TSID.unformat(tsid.format(format), format));

			// '%x': hexadecimal in lower case
			format = head + "%x" + tail;
			formatted = head + encode(tsid, 16).toLowerCase() + tail;
			assertEquals(formatted, TSID.unformat(formatted, format).format(format));
			assertEquals(tsid, TSID.unformat(tsid.format(format), format));

			// '%z': base-62
			format = head + "%z" + tail;
			formatted = head + encode(tsid, 62) + tail;
			assertEquals(formatted, TSID.unformat(formatted, format).format(format));
			assertEquals(tsid, TSID.unformat(tsid.format(format), format));

			// '%d': base-10
			format = head + "%d" + tail;
			formatted = head + encode(tsid, 10) + tail;
			assertEquals(formatted, TSID.unformat(formatted, format).format(format));
			assertEquals(tsid, TSID.unformat(tsid.format(format), format));
		}
	}
}
