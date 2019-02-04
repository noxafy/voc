package de.noxafy.voc.view.lang;

import de.noxafy.voc.core.Settings;

/**
 * @author noxafy
 * @created 22.04.18
 */
public enum Lang {
	EN(new Strings_en()), DE(new Strings_de());

	private final Strings strings;

	Lang(Strings strings) {
		this.strings = strings;
	}

	public static Lang get(String lang) {
		try {
			return valueOf(lang);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static String getAvailableString() {
		final StringBuilder res = new StringBuilder();
		for (Lang lang : values()) {
			res.append("\"").append(lang.name().toLowerCase()).append("\"");
			if (lang == Settings.LANG) {
				res.append(" (default)");
			}
			res.append(", ");
		}
		if (res.length() > 1) {
			res.delete(res.length() - 2, res.length());
		}
		return res.toString();
	}

	public Strings getStrings() {
		return strings;
	}
}