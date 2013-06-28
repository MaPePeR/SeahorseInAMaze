package client;

import generated.WinMessageType;
import generated.WinMessageType.Winner;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WinStatistics {
	private static int totalGames = 0;
	private static String ourname = null;
	private static HashMap<String, HashMap<Integer, Integer>> playerNameHasWon = new HashMap<String, HashMap<Integer, Integer>>();
	private static HashMap<Integer, Integer> playerNumberhasWon = new HashMap<Integer, Integer>();

	public static void addStatistic(WinMessageType wmt, int myId) {
		totalGames += 1;
		Winner w = wmt.getWinner();
		if (!playerNameHasWon.containsKey(w.getValue())) {
			playerNameHasWon.put(w.getValue(), new HashMap<Integer, Integer>());
		}
		addOneTo(playerNameHasWon.get(w.getValue()), w.getId());
		addOneTo(playerNumberhasWon, w.getId());
		if (myId == w.getId()) {
			// I won
			ourname = w.getValue();
		} else {
			get(playerNumberhasWon, myId);
		}
	}

	private static <KeyType> void addOneTo(Map<KeyType, Integer> m, KeyType k) {
		if (m.containsKey(k)) {
			m.put(k, m.get(k) + 1);
		} else {
			m.put(k, 1);
		}
	}

	private static <KeyType> int get(Map<KeyType, Integer> m, KeyType k) {
		if (m.containsKey(k)) {
			return m.get(k);
		} else {
			m.put(k, 0);
			return 0;
		}
	}

	public static void printStatistic() {
		printStatistic(System.err);
	}

	private static final int MAXNAMEWIDTH = 10;

	public static void printStatistic(PrintStream out) {
		int totalGamesSlotLength = Math.max((int) Math.log10(totalGames) + 1, 4);

		out.format("%6s | %" + totalGamesSlotLength + "s ", "slot", "wins");
		for (String s : playerNameHasWon.keySet()) {
			int l = Math.min(MAXNAMEWIDTH, s.length());
			int w = Math.max(l, totalGamesSlotLength);
			boolean itsme = s.equals(ourname);
			out.format("|%c%" + w + "s%c", itsme ? '>' : ' ', s.substring(0, l), itsme ? '<' : ' ');
		}
		out.println();
		for (int i : playerNumberhasWon.keySet()) {
			out.format("%6d | %" + totalGamesSlotLength + "d", i, get(playerNumberhasWon, i));
			for (Entry<String, HashMap<Integer, Integer>> e : playerNameHasWon.entrySet()) {
				int l = Math.min(MAXNAMEWIDTH, e.getKey().length());
				int w = Math.max(l, totalGamesSlotLength);
				out.format(" | %" + w + "d", get(e.getValue(), i));
			}
			out.println();
		}
		out.format("%6s | %" + totalGamesSlotLength + "d", "Total:", totalGames);
		for (Entry<String, HashMap<Integer, Integer>> e : playerNameHasWon.entrySet()) {
			int l = Math.min(MAXNAMEWIDTH, e.getKey().length());
			int w = Math.max(l, totalGamesSlotLength);
			int s = 0;
			for (int i : e.getValue().values()) {
				s += i;
			}
			out.format(" | %" + w + "d", s);
		}
		out.println();
	}
}
