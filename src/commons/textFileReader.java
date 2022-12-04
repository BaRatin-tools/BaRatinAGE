package commons;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class textFileReader {

	public static void main(String[] args) throws java.io.IOException {
		// if (args.length != 1) {
		// System.err.println("Usage: java TestDetectorFile FILENAME");
		// System.exit(1);
		// }
		// java.io.File file = new java.io.File(args[0]);
		// String encoding = UniversalDetector.detectCharset(file);
		// if (encoding != null) {
		// System.out.println("Detected encoding = " + encoding);
		// } else {
		// System.out.println("No encoding detected.");
		// }
	}

	static public String getFileInfo(File f) throws java.io.IOException {
		System.out.println(f);
		String encoding = UniversalDetector.detectCharset(f);
		// if (encoding != null) {
		// System.out.println("Detected encoding = " + encoding);
		// } else {
		// System.out.println("No encoding detected.");
		// }
		return encoding;
	}

	static public Scanner createScanner(File f) throws IOException {
		// StandardCharsets.ISO_8859_1;
		String encoding = textFileReader.getFileInfo(f);
		Charset cs = StandardCharsets.UTF_8;
		if (encoding == "US-ASCII") {
			cs = StandardCharsets.UTF_8;
		} else if (encoding == "WINDOWS-1252") {
			cs = StandardCharsets.ISO_8859_1;
		}
		// System.out.println("Using charset " + cs);
		return new Scanner(f, cs);
	}
}
