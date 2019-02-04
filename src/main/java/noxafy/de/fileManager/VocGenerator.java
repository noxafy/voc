package noxafy.de.fileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import noxafy.de.core.Settings;
import noxafy.de.core.Vocabulary;
import noxafy.de.view.UserInterface;

/**
 * @author noxafy
 * @created 30.08.17
 */
public class VocGenerator extends FileManager<String[]> {

	private static File from = null;
	private static File to = null;
	private static Exception convert_e = null;

	private static final UserInterface ui = UserInterface.getInstance();

	private VocGenerator(File file) {
		super(file);
	}

	public static void main(String[] args) throws IOException {
		if (!parseArgs(args)) return;
		Settings.DEBUG = true; // show all error messages

		if (from == null || to == null) {
			if (to == null && from != null) {
				ui.tellLn("Please give a csv where to write the generated items.");
				return;
			}
			else if (to != null) {
				ui.tellLn("Please give a csv where to read the items from.");
				return;
			}
			final String vok_dir = System.getProperty("user.home") + "/Dropbox/Sonstiges/Sprachen/Vokabeln/";
			from = new File(vok_dir + "vocs");
			to = new File(vok_dir + "Englisch.csv");
		}
		VocGenerator vocGenerator = new VocGenerator(from);
		String[] lines = vocGenerator.load();
		String res;
		int success = 0;
		for (int i = 0; i < lines.length; i++) {
			convert_e = null;
			Vocabulary voc = getVoc(lines[i]);
			if (voc == null) { // has error
				ui.tellLn("Failed to process line " + (i + 1) + ": " + lines[i]);
				ui.debug(convert_e.toString());
				continue;
			}
			res = getLine(voc);
			ui.tellLn("Processing " + res);
			vocGenerator.appendNewVoc(res);
			lines[i] = "";
			vocGenerator.write(lines);
			success++;
		}
		ui.tellLn(success + " new vocs added.");
	}

	private static boolean parseArgs(String[] args) throws IOException {
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-d":
					Settings.DEBUG = true;
					break;
				case "-f":
					from = evalFile(args, ++i);
					break;
				case "-t":
					to = ensureFile(args, ++i);
					break;
//				case "-h":
//				case "--help":
			}
		}
		return true;
	}

	private static File evalFile(String[] args, int i) {
		if (i < args.length) {
			File file = new File(args[i]);
			if (!file.exists()) {
				ui.tellLn("Please give an existing file to a csv with vocs.");
				System.exit(1);
			}
			else if (!file.canRead()) {
				ui.tellLn("Please give a readable file to a csv with vocs. See --help for more information.");
				System.exit(1);
			}
			else if (!file.canWrite()) {
				ui.tellLn("Please give a writable file to a csv with vocs. See --help for more information.");
				System.exit(1);
			}
			return file;
		}
		else {
			ui.tellLn("Please give a path to a csv with vocs. See --help for more information.");
			System.exit(1);
			return null;
		}
	}

	private static File ensureFile(String[] args, int i) throws IOException {
		if (i < args.length) {
			File file = new File(args[i]);
			file.createNewFile();
			return file;
		}
		else {
			ui.tellLn("Please give a path to a csv with vocs. See --help for more information.");
			System.exit(1);
		}
		return null;
	}

	private static Vocabulary getVoc(String org_line) {
		String line = org_line.substring(1, org_line.length() - 1);
		String[] args = line.split("\",\"");

		String word, meaning, mnemonic = "";
		long added = 0, lastAsked = 0;
		int asked = 0, failed = 0, succeeded_in_a_row = 0;

		try {
			switch (args.length) {
				default:
				case 8:
					succeeded_in_a_row = parseInt("succeeded_in_a_row", args[7]);
				case 7:
					failed = parseInt("failed", args[6]);
				case 6:
					asked = parseInt("asked", args[5]);
				case 5:
					if (!args[4].isEmpty()) {
						lastAsked = parseLong("lastAsked", args[4]);
					}
				case 4:
					added = parseLong("added", args[3]);
				case 3:
					mnemonic = args[2];
				case 2:
					meaning = args[1];
					if (meaning.isEmpty()) {
						convert_e = new IllegalArgumentException("Meaning field must not be empty.");
						return null;
					}
					word = args[0];
					if (word.isEmpty()) {
						convert_e = new IllegalArgumentException("Word field must not be empty.");
						return null;
					}
					break;
				case 1:
				case 0:
					convert_e = new TooShortLineException(org_line);
					return null;
			}
		}
		catch (NumberFormatException e) {
			convert_e = e;
			return null;
		}
		if (added == 0) added = System.currentTimeMillis();

		return new Vocabulary(word, meaning, mnemonic, added, lastAsked, asked, failed, succeeded_in_a_row);
	}

	private static String getLine(Vocabulary voc) {
		return quote(voc.getWord(), true) +
				quote(voc.getMeaning(), true) +
				quote(voc.getMnemonic(), true) +
				quote(voc.getAdded(), true) +
				quote((voc.getLastAsked() == 0) ? "" : voc.getLastAsked(), true) +
				quote(voc.getAsked(), true) +
				quote(voc.getFailed(), true) +
				quote(voc.getSucceeded_in_a_row(), false);
	}

	private static String quote(Object inner, boolean comma) {
		return "\"" + inner + ((comma) ? "\"," : "\"");
	}

	@Override
	String[] load() {
		String content = getStringFromFile();
		if (content == null) {
			return new String[0];
		}

		if (content.isEmpty()) {
			ui.tellLn("File " + getFilePath() + " is empty. No line processed.");
			return new String[0];
		}
		return content.split("\n");
	}

	@Override
	void write(String[] data) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String line : data) {
			if (line.isEmpty()) continue;
			sb.append(line).append("\n");
		}
		writeOutFile(sb.toString());
	}

	private void appendNewVoc(String line) throws IOException {
		line += "\n";
		try (FileOutputStream out = new FileOutputStream(to, true)) {
			out.write(line.getBytes());
		}
		catch (IOException e) {
			ui.tellLn("Writing a new line to " + to.getAbsolutePath() + " failed.");
			throw e;
		}
	}

	private static int parseInt(String type, String toParse) {
		int res;
		try {
			res = Integer.parseInt(toParse);
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Failed to parse \"" + type + "\": " + toParse);
		}
		return res;
	}

	private static long parseLong(String type, String toParse) {
		long res;
		try {
			res = Long.parseLong(toParse);
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Failed to parse \"" + type + "\": " + toParse);
		}
		return res;
	}
}
