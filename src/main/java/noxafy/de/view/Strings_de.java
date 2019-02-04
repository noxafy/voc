package noxafy.de.view;

/**
 * @author noxafy
 * @created 31.01.18
 */
public class Strings_de extends Strings {
	private static final String[] vibes = { "Gut!", "Großartig!", "Super!", "Wunderbar!", "Sagenhaft!", "Fantastisch!", "Bravo!",
			"Gute Arbeit!", "Gut gemacht!", "So geht's!" };

	@Override
	public String[] getGoodVibes() {
		return vibes;
	}

	@Override
	public String getFinal() {
		return "Alle Vokabeln für heute gelernt! :)";
	}

	@Override
	public String getFinalAndReset() {
		return "Alle Vokabeln für heute bereits gelernt. Möchstest du das zurücksetzen?";
	}

	@Override
	public String getVocsLeft() {
		return " bereits gefragte Vokabeln stehen noch aus.";
	}

	@Override
	public String getStatistics() {
		return "Statistik:";
	}

	@Override
	public String getNoVocFound() {
		return "Keine Vokabel gefunden!";
	}
}
