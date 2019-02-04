package de.noxafy.voc.fileManager;

/**
 * @author noxafy
 * @created 01.02.18
 */
public class TooShortLineException extends IllegalArgumentException {

	public TooShortLineException(String line) {
		super("Line has not enough arguments or is bad formatted: " + line);
	}
}
