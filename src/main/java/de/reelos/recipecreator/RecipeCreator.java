package de.reelos.recipecreator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import de.reelos.recipecreator.util.CannotParseJsonException;
import de.reelos.recipecreator.util.RecipeReader;

public class RecipeCreator extends JavaPlugin {

    private final Path recipeFolder = Paths.get( "./recipes/" );

    @Override
    public void onEnable() {
        getLogger().log( Level.INFO, "RecipeCreator loaded." );

        if ( !Files.exists( this.recipeFolder ) ) {
            try {
                Files.createDirectories( this.recipeFolder );
            } catch ( IOException e ) {
                getLogger().log( Level.WARNING, "Could not create 'recipeFolder'.", e );
            }
        }
        try ( DirectoryStream<Path> directoryStream = Files.newDirectoryStream( this.recipeFolder, "*.json" ) ) {
            for ( Path path : directoryStream ) {
                try {
                    new RecipeReader( Files.newInputStream( path ) ).registerRecipe();
                } catch ( FileNotFoundException | CannotParseJsonException ex ) {
                    getLogger().log( Level.WARNING, "Could not load " + path, ex );
                }
            }
        } catch ( IOException ex ) {
            getLogger().log( Level.WARNING, ex.getMessage(), ex );
        }
    }

    @Override
    public void onDisable() {
        getLogger().log( Level.INFO, "RecipeCreator unloaded." );
    }

    public static void main( String[] args ) {
        for ( String file : Paths.get( "./recipes/" ).toFile().list() ) {
            System.out.println( file );
        }
    }
}
