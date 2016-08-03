package dekk.pw.pokemate.tasks;

import POGOProtos.Data.PokedexEntryOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.NoSuchItemException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;
import dekk.pw.pokemate.util.StringConverter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_FLEE;
import static POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS;

/**
 * Created by TimD on 7/21/2016.
 */
class CatchPokemon extends Task implements Runnable {

    CatchPokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        //System.out.println("[CatchPokemon] Starting Loop");
        try {

            List<CatchablePokemon> pokemon = context.getMap().getCatchablePokemon().stream()
                    .filter(this::shouldIgnore)
                    .collect(Collectors.toList());

            if (pokemon.size() == 0) {
                return;
            }

            Pokeball pokeball;
            CatchResult catchResult;
            for (CatchablePokemon target : pokemon) {

                Walking.setLocation(context);
                EncounterResult encounterResult = target.encounterPokemon();
                if (!encounterResult.wasSuccessful()) {
                    continue;
                }


                //get pokedex entry.
                PokedexEntryOuterClass.PokedexEntry entry = context.getApi().getInventories().getPokedex().getPokedexEntry(target.getPokemonId());

                //if pokemon exist is registered in pokedex
                if (entry != null) {
                    //need to get pokeball for each target in case there are no balls left of the last pokeball we used.
                    Item ball = itemBag().getItem(getItemForId(Config.getPreferredBall()));
                    if (ball != null && ball.getCount() > 0) {
                        pokeball = getBallForId(Config.getPreferredBall());
                    } else {
                        pokeball = target.getItemBall();
                    }
                    catchResult = target.catchPokemon(pokeball);
                } else {
                    catchResult = target.catchPokemonWithBestBall();
                }

                if (catchResult.getStatus() != CATCH_SUCCESS) {
                    context.setConsoleString("CatchPokemon", target.getPokemonId() + " fled.");
                    continue;
                }

                try {
                    final String targetId = target.getPokemonId().name();

                    pokemons().stream()
                        .filter(pkmn -> pkmn.getPokemonId().name().equals(targetId))
                        .sorted((a, b) -> Long.compare(b.getCreationTimeMs(), a.getCreationTimeMs()))
                        .findFirst()
                        .ifPresent(p -> {
                            String output = null;
                            try {
                                output = String.format("Caught a %s [CP: %d] [IV: %d] [Candy: %d]", StringConverter.titleCase(targetId), p.getCp(), getIvRatio(p), p.getCandy());
                            } catch (LoginFailedException | RemoteServerException e) {
                                e.printStackTrace();
                            }

                            if (p.getCp() > Config.getMinimumCPForMessage()) {
                                PokeMateUI.toast(output, Config.POKE + "mon caught!", "icons/" + target.getPokemonId().getNumber() + ".png");
                            } else {
                                log(output + " [IV: " + getIvRatio(p) + "%]");
                            }
                            context.setConsoleString("CatchPokemon", output + " [IV: " + getIvRatio(p) + "%]");
                        });
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            //e.printStackTrace();
            System.out.println("[CatchPokemon] Exceeded Rate Limit");
        } catch (NoSuchItemException e) {
            context.setConsoleString("CatchPokemon","Out of Pokeballs.");
        } finally {
            context.addTask(new CatchPokemon(context));
        }
    }


    private boolean shouldIgnore(final CatchablePokemon p) {
        return !Config.getIgnoreCatchingPokemon().contains(p.getPokemonId());
    }

    private List<Pokemon> pokemons() {
        return context.getInventories().getPokebank().getPokemons();
    }

    private ItemBag itemBag() {
        return context.getInventories().getItemBag();
    }

    private void log(final String message) {
        final String formattedDate = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.printf("[%s] - %s\n", formattedDate, message);
        PokeMateUI.addMessageToLog(message);
    }

    private int getIvRatio(Pokemon pokemon) {
        return (pokemon.getIndividualAttack() + pokemon.getIndividualDefense() + pokemon.getIndividualStamina()) * 100 / 45;
    }

    private ItemId getItemForId(final int id) {
        return ItemId.forNumber(id);
    }

    private Pokeball getBallForId(int id) throws NoSuchItemException {
        switch (id) {
            case ItemId.ITEM_GREAT_BALL_VALUE:
                return Pokeball.GREATBALL;
            case ItemId.ITEM_ULTRA_BALL_VALUE:
                return Pokeball.ULTRABALL;
            case ItemId.ITEM_MASTER_BALL_VALUE:
                return Pokeball.MASTERBALL;
            case ItemId.ITEM_POKE_BALL_VALUE:
                return Pokeball.POKEBALL;
            default:
                throw new NoSuchItemException();
        }
    }


}
