package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;
import dekk.pw.pokemate.util.StringConverter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS;

/**
 * Created by TimD on 7/21/2016.
 */
public class CatchPokemon extends Task {

    CatchPokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            Pokeball pokeball = null;
            List<CatchablePokemon> pokemon = context.getApi().getMap().getCatchablePokemon().stream()
                    .filter(this::shouldIgnore)
                    .collect(Collectors.toList());

            if (pokemon.size() == 0) {
                return;
            }

            Item ball = itemBag().getItem(getItemForId(Config.getPreferredBall()));
            if (ball != null && ball.getCount() > 0) {
                pokeball = getBallForId(Config.getPreferredBall());
            } else {
                //find any pokeball we can.
                for (Pokeball pb : Pokeball.values()) {
                    ball = itemBag().getItem(pb.getBallType());
                    if (ball != null && ball.getCount() > 0) {
                        pokeball = pb;
                        break;
                    }
                }
            }

            CatchablePokemon target = pokemon.get(0);
            if (target == null || pokeball == null) {
                return;
            }

            Walking.setLocation(context);
            EncounterResult encounterResult = target.encounterPokemon();
            if (!encounterResult.wasSuccessful()) {
                return;
            }

            CatchResult catchResult = target.catchPokemon(pokeball);
            if (catchResult.getStatus() != CATCH_SUCCESS) {
                log(target.getPokemonId() + " fled.");
                return;
            }

            try {
                final String targetId = target.getPokemonId().name();

                pokemons().stream()
                        .filter(pkmn -> pkmn.getPokemonId().name().equals(targetId))
                        .sorted((a, b) -> Long.compare(b.getCreationTimeMs(), a.getCreationTimeMs()))
                        .findFirst()
                        .ifPresent(p -> {
                            String output = String.format("Caught a %s [CP: %d] [Candy: %d]", StringConverter.titleCase(targetId), p.getCp(), p.getCandy());

                            if (p.getCp() > Config.getMinimumCPForMessage()) {
                                PokeMateUI.toast(output, Config.POKE + "mon caught!", "icons/" + target.getPokemonId().getNumber() + ".png");
                            } else {
                                log(output + " [IV: " + getIvRatio(p) + "%]");
                            }
                        });
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldIgnore(final CatchablePokemon p) {
        return !Config.getIgnoreCatchingPokemon().contains(p.getPokemonId().getNumber());
    }

    private List<Pokemon> pokemons() {
        return context.getApi().getInventories().getPokebank().getPokemons();
    }

    private ItemBag itemBag() {
        return context.getApi().getInventories().getItemBag();
    }

    private void log(final String message) {
        final String formattedDate = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.printf("[%s] - %s\n", formattedDate, message);
        PokeMateUI.addMessageToLog(message);
    }

    private int getIvRatio(Pokemon pokemon) {
        return (pokemon.getIndividualAttack() + pokemon.getIndividualDefense() + pokemon.getIndividualStamina()) * 100 / 45;
    }

    private ItemIdOuterClass.ItemId getItemForId(final int id) {
        return ItemIdOuterClass.ItemId.forNumber(id);
    }

    private Pokeball getBallForId(int id) {
        switch (id) {
            case ItemIdOuterClass.ItemId.ITEM_GREAT_BALL_VALUE:
                return Pokeball.GREATBALL;
            case ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL_VALUE:
                return Pokeball.ULTRABALL;
            case ItemIdOuterClass.ItemId.ITEM_MASTER_BALL_VALUE:
                return Pokeball.MASTERBALL;
            default:
                return Pokeball.POKEBALL;
        }
    }


}
