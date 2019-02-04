package de.noxafy.voc.core;

import de.noxafy.voc.view.lang.Lang;

/**
 * @author noxafy
 * @created 28.08.17
 */
public class Settings {
	// default lang for ui is "en"
	public static Lang LANG = Lang.EN;
	public static DEBUG_LEVEL DEBUG = DEBUG_LEVEL.NONE;
	// shrink window on start and clear after each voc
	public static boolean TRAINING_MODE = false;

	public final int NUMBER_SIMUL_VOCS;
	// should not be bigger than the previous two
	public final int NUMBER_NEW_VOCS_AT_START;

	public Settings(int number_simul_vocs, int number_new_vocs_at_start) {
		NUMBER_SIMUL_VOCS = number_simul_vocs;
		NUMBER_NEW_VOCS_AT_START = number_new_vocs_at_start;
	}

	public enum DEBUG_LEVEL {
		NONE, SHORT, LONG;

		public boolean is(DEBUG_LEVEL level) {
			return this.ordinal() >= level.ordinal();
		}
	}
}
