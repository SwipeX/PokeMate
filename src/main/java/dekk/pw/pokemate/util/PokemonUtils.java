package dekk.pw.pokemate.util;

import POGOProtos.Enums.PokemonMoveOuterClass;
import com.google.common.collect.ImmutableList;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.pokemon.PokemonMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kyle on 8/2/2016.
 */
public class PokemonUtils {


    public static boolean MoveSetOptimal(Pokemon pokemon) {
        // Grab the moves we currently have
        final PokemonMoveOuterClass.PokemonMove quickMove = pokemon.getMove1();
        final PokemonMoveOuterClass.PokemonMove mainMove = pokemon.getMove2();

        // Grab the possible moves for this pokemon
        final PokemonMeta pokemonMeta = pokemon.getMeta();
        final List<PokemonMoveOuterClass.PokemonMove> quickMoves = ImmutableList.copyOf(pokemonMeta.getQuickMoves());
        final List<PokemonMoveOuterClass.PokemonMove> mainMoves = ImmutableList.copyOf(pokemonMeta.getCinematicMoves());

        // Sort each move by DPS
        Collections.sort(quickMoves, dpsCompare);
        Collections.sort(mainMoves, dpsCompare);

        // See if the moves are optimal
        return MoveOptimal(quickMove, quickMoves) && MoveOptimal(mainMove, mainMoves);
    }

    private static boolean MoveOptimal(final PokemonMoveOuterClass.PokemonMove move, final List<PokemonMoveOuterClass.PokemonMove> moves)
    {
        // what???
        if(moves.size() < 1)
            return false;

        return moves.get(0).equals(move);
    }

    private static Comparator<PokemonMoveOuterClass.PokemonMove> dpsCompare = new Comparator<PokemonMoveOuterClass.PokemonMove>() {
        @Override
        public int compare(PokemonMoveOuterClass.PokemonMove a, PokemonMoveOuterClass.PokemonMove b) {

            final PokemonMoveMeta metaA = PokemonMoveMetaRegistry.getMeta(a);
            final PokemonMoveMeta metaB = PokemonMoveMetaRegistry.getMeta(b);

            final double dpsA = (metaA.getPower() * 1000) / (metaA.getTime());
            final double dpsB = (metaB.getPower() * 1000) / (metaB.getTime());

            if( dpsA > dpsB )
                return -1;
            else
                return 1;
        }
    };

}
