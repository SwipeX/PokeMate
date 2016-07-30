package dekk.pw.pokemate.util;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;

import java.util.List;

/**
 * created on 27.07.2016 by iDreamInBinary
 */
public class StringConverter {

	//return a pokemon name thats not all uppercase
	public static String convertPokename(String pokeName){
		return pokeName.substring(0, 1).toUpperCase() + pokeName.substring(1).toLowerCase();
	}

	//return an item name thats not all uppercase and doesn't have underscores
	public static String convertItem(String itemName){
		if ((itemName.length() - itemName.replace("_", "").length()) > 1) {
			String[] result = (itemName.substring(5,6).toUpperCase() + itemName.substring(6).replaceAll("_", " ").toLowerCase()).split("\\s+");
			return result[0] + " " + result[1].substring(0,1).toUpperCase() + result[1].substring(1);
		}
		else {
			String[] result = (itemName.substring(5,6).toUpperCase() + itemName.substring(6).replaceAll("_", " ").toLowerCase()).split("\\s+");
			return result[0].substring(0,1).toUpperCase() + result[0].substring(1);
		}
	}

	public static String convertItemAwards(List<ItemAwardOuterClass.ItemAward> itemAwards) {
		String retStr = "";
		int pokeBall = 0, greatBall = 0, ultraBall = 0, masterBall = 0, potion = 0, superPotion = 0, hyperPotion = 0, maxPotion = 0, razzBerry = 0, revive = 0, maxRevive = 0;
		for (ItemAwardOuterClass.ItemAward item : itemAwards) {
			switch (item.getItemId().name()) {
				case "ITEM_POKE_BALL":
					pokeBall++;
					break;
				case "ITEM_GREAT_BALL":
					greatBall++;
					break;
				case "ITEM_ULTRA_BALL":
					ultraBall++;
					break;
				case "ITEM_MASTER_BALL":
					masterBall++;
					break;
				case "ITEM_POTION":
					potion++;
					break;
				case "ITEM_SUPER_POTION":
					superPotion++;
					break;
				case "ITEM_HYPER_POTION":
					hyperPotion++;
					break;
				case "ITEM_MAX_POTION":
					maxPotion++;
					break;
				case "ITEM_REVIVE":
					revive++;
					break;
				case "ITEM_MAX_REVIVE":
					maxRevive++;
					break;
				case "ITEM_RAZZ_BERRY":
					razzBerry++;
					break;
			}
		}
		if (pokeBall > 0)
			retStr += " - Poke Ball (x" + pokeBall + ")";
		if (greatBall > 0)
			retStr += " - Great Ball (x" + greatBall + ")";
		if (masterBall > 0)
			retStr += " - Master Ball (x" + masterBall + ")";
		if (ultraBall > 0)
			retStr += " - Ultra Ball (x" + ultraBall + ")";
		if (potion > 0)
			retStr += " - Potion (x" + potion + ")";
		if (superPotion > 0)
			retStr += " - Super Potion (x" + superPotion + ")";
		if (hyperPotion > 0)
			retStr += " - Hyper Potion (x" + hyperPotion + ")";
		if (maxPotion > 0)
			retStr += " - Max Potion (x" + maxPotion + ")";
		if (revive > 0)
			retStr += " - Revive (x" + revive + ")";
		if (maxRevive > 0)
			retStr += " - Max Revive Potion (x" + maxRevive + ")";
		if (razzBerry > 0)
			retStr += " - Razz Berry (x" + razzBerry + ")";

		return retStr;
	}

}
