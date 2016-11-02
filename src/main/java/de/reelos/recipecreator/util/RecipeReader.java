package de.reelos.recipecreator.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipeReader {

    private ItemStack craftedItem = new ItemStack( Material.AIR );
    private RecipeType recipeType = RecipeType.NONE;
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    private String[] recipe = null;

    public RecipeReader( final String target ) throws CannotParseJsonException, FileNotFoundException {
        this( new FileInputStream( target ) );
    }

    public RecipeReader( final InputStream inputStream ) throws CannotParseJsonException {
        final JsonObject jsonObject;
        try ( JsonReader reader = Json.createReader( inputStream ) ) {
            jsonObject = reader.readObject();
        }
        try {
            this.recipeType = RecipeType.valueOf( jsonObject.getString( "type", "NONE" ).toUpperCase() );
            switch ( this.recipeType ) {
                case SHAPED:
                case SHAPELESS:
                case FURNACE:
                    break;
                default:
                    throw new CannotParseJsonException( "Wrong or no RecipeType" );
            }
        } catch ( IllegalArgumentException ex ) {
            throw new CannotParseJsonException( "Could not read ", ex );
        }
        JsonObject tar = jsonObject.getJsonObject( "for" );
        {
            Material mat = Material.getMaterial( tar.getString( "name", "AIR" ).toUpperCase() );
            if ( mat.equals( Material.AIR ) ) {
                throw new CannotParseJsonException( "Wrong or no Material" );
            }
            int amount = tar.getInt( "amount", 1 );
            // short meta = ( short ) tar.getInt( "meta", 0 );
            String dName = tar.getString( "displayName", "" );
            this.craftedItem = new ItemStack( mat, amount );
            if ( !dName.matches( "" ) ) {
                ItemMeta iMeta = this.craftedItem.getItemMeta();
                iMeta.setDisplayName( dName );
                this.craftedItem.setItemMeta( iMeta );
            }
        }
        if ( this.recipeType.equals( RecipeType.SHAPED ) ) {
            JsonArray array = jsonObject.getJsonArray( "recipe" );
            List<String> swap = array.getValuesAs( JsonString.class ).stream().map( ( c ) -> c.getString() )
                .collect( Collectors.toList() );
            this.recipe = swap.toArray( new String[swap.size()] );
            if ( this.recipe == null ) {
                throw new CannotParseJsonException( "Wrong or no Recipe" );
            }
            array = jsonObject.getJsonArray( "ingredients" );
            array.getValuesAs( JsonObject.class ).forEach( c -> {
                Material mat = Material.getMaterial( c.getString( "name" ).toUpperCase() );
                char tag = c.getString( "tag", " " ).charAt( 0 );
                this.ingredients.add( new RecipeIngredient( tag, mat ) );
            } );
        } else {
            JsonArray array = jsonObject.getJsonArray( "ingredients" );
            List<JsonObject> valuesAsList = array.getValuesAs( JsonObject.class );
            valuesAsList.forEach( c -> {
                Material mat = Material.getMaterial( c.getString( "name" ).toUpperCase() );
                int amount = c.getInt( "amount", 1 );
                this.ingredients.add( new RecipeIngredient( amount, mat ) );
            } );
        }

    }

    public Recipe registerRecipe() {
        if ( this.recipeType == RecipeType.SHAPED ) {
            return createShaped();
        } else if ( this.recipeType == RecipeType.SHAPELESS ) {
            return createShapeless();
        } else {
            return null;
        }
    }

    private Recipe createShaped() {
        ShapedRecipe rec = new ShapedRecipe( this.craftedItem );
        rec.shape( this.recipe );
        for ( RecipeIngredient i : this.ingredients ) {
            rec.setIngredient( i.getTag(), i.getMat() );
        }
        return rec;
    }

    private Recipe createShapeless() {
        ShapelessRecipe rec = new ShapelessRecipe( this.craftedItem );
        for ( RecipeIngredient i : this.ingredients ) {
            rec.addIngredient( i.getAmount(), i.getMat() );
        }
        return rec;
    }

}
