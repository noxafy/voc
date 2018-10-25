package de.noxafy.voc.view.commandline;

/**
 * @author noxafy
 * @created 20.01.18
 */
public class ANSI {
	private static final String RESET = "\u001B[0m";
	private static final String BOLD = "\u001B[1m";
	private static final String TRANSPARENT = "\u001B[2m";
	private static final String UNDERLINE = "\u001B[4m";

	static final String TRAINING_WINDOW_DIMENSIONS = "5;100";
	static final String SHIRNK_WINDOW = "\u001B[8;" + TRAINING_WINDOW_DIMENSIONS + "t";
	// public static final String CLS = "\u001Bc";
	static final String CLEAR_WINDOW = "\u001B[2J\u001B[H"; // on other terminals also [3J

	static String bold(String bold) {
		return BOLD + bold + RESET;
	}

	static String transparent(String transparent) {
		return TRANSPARENT + transparent + RESET;
	}

	static String underline(String underline) {
		return UNDERLINE + underline + RESET;
	}
}