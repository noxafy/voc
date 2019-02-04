package de.noxafy.voc.cli;

import de.noxafy.utils.Log;
import de.noxafy.voc.Main;
import de.noxafy.voc.cli.lang.Lang;
import de.noxafy.voc.core.Settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static de.noxafy.utils.ANSI.bold;
import static de.noxafy.utils.ANSI.underline;
import static de.noxafy.voc.cli.CLUserInterface.TRAINING_WINDOW_DIMENSIONS;

/**
 * @author noxafy
 * @created 25.10.18
 */
public class CLArgsParser {

	private static final String usage = "Usage: " + bold("voc") + " -h | [-n|-t] [-l " + underline("lang") + "] [-v|-d] [-s] -f " + underline("csv");

	static boolean printNewlineAtExit = true;
	static {
		// Print newline after sigint
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (printNewlineAtExit) System.out.println();
		}));
	}

	public static void parse(String[] args) {
		try {
			parse0(args);
			if (Main.voc_file == null) {
				throw new IllegalArgumentException("Please give a vocabulary database to read from. See -h for more information.");
			}
		}
		catch (IllegalArgumentException e) {
			String message = e.getMessage();
			int exitCode = 1;

			if ("help".equals(message)) {
				message = createHelpMessage(Main.settings_file.getAbsolutePath());
				exitCode = 0;
			}

			System.out.print(message);
			System.exit(exitCode);
		}
	}

	private static void parse0(String[] args) throws IllegalArgumentException {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.charAt(0) == '-') {
				for (int j = 1; j < arg.length(); j++) {
					switch (arg.charAt(j)) {
						case '-':
							if ("help".equals(arg.substring(2))) {
								throw new IllegalArgumentException("help");
							}
							break;
						case 'h':
							throw new IllegalArgumentException("help");
						case 's':
							Settings.justSummarize = true;
							break;
						case 'v':
							Log.setLevel(Log.Level.VERBOSE);
							break;
						case 'd':
							Log.setLevel(Log.Level.DEBUG);
							break;
						case 't':
							Settings.TRAINING_MODE = true;
							break;
						case 'n':
							newWindow(args, i);
							break;
						case 'l':
							Settings.LANG = evalLang(args, ++i);
							break;
						case 'f':
							Main.voc_file = evalFile(args, ++i);
							break;
						default:
							throw new IllegalArgumentException("Wrong argument: " + args[i] + "\n" +
									usage + " -- " + "See -h for more help.");
					}
				}
			}
			else {
				throw new IllegalArgumentException("Wrong argument: " + args[i] + "\n" +
						usage + " -- See -h for more help.");
			}
		}
	}

	private static void newWindow(String[] args, int i) {
		try {
			args[i] = ""; // delete -n argument (prevent circularity)
			final String[] terminal_exec = { "osascript",
					"-e", "tell application \"Terminal\" to do script \"voc -t " + String.join(" ", args) + "\"",
					"-e", "tell application \"System Events\" to set frontmost of process \"Terminal\" to true"
			};
			final Process p = executeCmd(terminal_exec);
			System.exit(p.waitFor());
		}
		catch (IOException | InterruptedException e) {
			throw new IllegalArgumentException("New window not available in this mode");
		}
	}

	private static Process executeCmd(String[] cmd) throws IOException {
		final Process p = Runtime.getRuntime().exec(cmd);
		// print out possible error
		InputStream in = p.getErrorStream();
		int c;
		while ((c = in.read()) != -1) {
			System.err.print((char) c);
		}
		in.close();
		return p;
	}

	private static String createHelpMessage(String settings_file) {
		return "Asks vocabularies based on a rating algorithm.\n" +
				usage + "\n" +
				"\t" + bold("-h") + "\tDisplay this message and exit.\n" +
				"\t" + bold("-n") + "\tOpen a new window in training mode.\n" +
				"\t" + "\tIn training mode the shell window shrinks to " + TRAINING_WINDOW_DIMENSIONS + " and clears the screen after each voc.\n" +
				"\t" + bold("-t") + "\tStart training mode in this window.\n" +
				"\t" + bold("-l") + " " + underline("lang") + "\tSet an alternative interface language. Available: " + Lang.getAvailableString() + "\n" +
				"\t" + bold("-v") + "\tBe a bit verbose.\n" +
				"\t" + bold("-d") + "\tPrint very much debug information while asking. Not recommended in training mode.\n" +
				"\t" + bold("-s") + "\tShow current statistics as shown after learned all vocs for a day and exit.\n" +
				"\t" + bold("-f") + " " + underline("csv") + "\tRead vocabulary database from a specified " + underline("csv") + " file.\n" +
				"\n" +
				"Source of vocabularies is " + underline("csv") + " and is therefore obligatory.\n" +
				"Each entry there contains the following information:\n" +
				"\t1. word\n" +
				"\t2. meaning\n" +
				"\t3. further information for memorizing or pronouncing (mnemonic)\n" +
				"\t4. date when voc was added (unix time)\n" +
				"\t5. date when voc was last asked (unix time)\n" +
				"\t6. how often it has been asked\n" +
				"\t7. how often the user failed to answer\n" +
				"\t8. how often user succeeded in a row\n" +
				"The last four stats are used by rating calculation.\n" +
				"If 6. is not 0, the last stat (succeeded in a row) is used for forget time calculation, as follows:\n" +
				"\tLEVEL\t\tS. IN A ROW\tFORGET TIME\n" +
				"\tUnknown:\t0-2\t\tinstant\n" +
				"\tLevel 1:\t3-4\t\t1 day\n" +
				"\tLevel 2:\t5-6\t\t3 days\n" +
				"\tLevel 3:\t7-10\t\t1 week\n" +
				"\tLevel 4:\t11-13\t\t3 months\n" +
				"\tLevel 5:\t>13\t\t1 year\n" +
				"Each routine run guarantees each vocabulary has succeeded min. 3 times, so is min. Level 1.\n" +
				"Only if a run is interrupted premature, \"unknown\" vocabularies are possible.\n" +
				"\n" +
				"Further a settings file is used (defaults to " + settings_file + ").\n" +
				"There is defined:\n" +
				"\t- how many vocs should be asked each day (NUMBER_SIMUL_VOCS) and\n" +
				"\t- how many of them should be new ones (NUMBER_NEW_VOCS_AT_START).";
	}

	private static Lang evalLang(String[] args, int i) throws IllegalArgumentException {
		if (i < args.length) {
			final Lang lang = Lang.get(args[i].toUpperCase());
			if (lang == null) {
				throw new IllegalArgumentException("Please give an available language. Available: " + Lang.getAvailableString() + ".  See -h for more help.");
			}
			else {
				return lang;
			}
		}
		else {
			throw new IllegalArgumentException("Please give a language. Available: " + Lang.getAvailableString() + ".  See -h for more help.");
		}
	}

	private static File evalFile(String[] args, int i) throws IllegalArgumentException {
		if (i < args.length) {
			File voc_file = new File(args[i]);
			if (!voc_file.exists()) {
				throw new IllegalArgumentException("Please give an existing file to a csv with vocs.");
			}
			else if (!voc_file.canRead() || !voc_file.canWrite()) {
				throw new IllegalArgumentException("Please give a read- and writable file to a csv with vocs. See -h for more information.");
			}
			return voc_file;
		}
		else {
			throw new IllegalArgumentException("Please give a path to a csv with vocs. See -h for more information.");
		}
	}
}
