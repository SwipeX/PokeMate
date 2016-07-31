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
import static dekk.pw.pokemate.util.Time.sleep;

/**
 * Created by TimD on 7/21/2016.
 */
public class CatchPokemon extends Task{

    CatchPokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        System.out.println("[CatchPokemon] Starting Loop");
        try {
            Pokeball pokeball = null;
            APIStartTime = System.currentTimeMillis();
            List<CatchablePokemon> pokemon = context.getApi().getMap().getCatchablePokemon().stream()
                .filter(this::shouldIgnore)
                .collect(Collectors.toList());

            APIElapsedTime = System.currentTimeMillis() - APIStartTime;
            if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
            }


            if (pokemon.size() == 0) {
                System.out.println("[CatchPokemon] Ending Loop - No Pokemon Found");
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
                System.out.println("[CatchPokemon] Ending Loop No Pokemon or No Pokeballs");
                return;
            }

            Walking.setLocation(context);
            EncounterResult encounterResult = target.encounterPokemon();
            if (!encounterResult.wasSuccessful()) {
                System.out.println("[CatchPokemon] Ending Loop - Caught Pokemon");
                return;
            }

            CatchResult catchResult = target.catchPokemon(pokeball);
            if (catchResult.getStatus() != CATCH_SUCCESS) {
                log(target.getPokemonId() + " fled.");
                System.out.println("[CatchPokemon] Ending Loop - Pokemon Ran Away");
                return;
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
                            output = String.format("Caught a %s [CP: %d] [Candy: %d]", StringConverter.titleCase(targetId), p.getCp(), p.getCandy());
                        } catch (LoginFailedException e) {
                            e.printStackTrace();
                        } catch (RemoteServerException e) {
                            e.printStackTrace();
                        }

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
            //e.printStackTrace();
            System.out.println("[CatchPokemon] Hit Rate Limited");
        }
        System.out.println("[CatchPokemon] Ending Loop");
    }


    private boolean shouldIgnore(final CatchablePokemon p) {
        return !Config.getIgnoreCatchingPokemon().contains(p.getPokemonId().getNumber());
    }

    private List<Pokemon> pokemons() {
        try {
            return context.getApi().getInventories().getPokebank().getPokemons();
        } catch (LoginFailedException e) {
            e.printStackTrace();
            return null;
        } catch (RemoteServerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ItemBag itemBag() {
        try {
            return context.getApi().getInventories().getItemBag();
        } catch (LoginFailedException e) {
            e.printStackTrace();
            return null;
        } catch (RemoteServerException e) {
            e.printStackTrace();
            return null;
        }
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
