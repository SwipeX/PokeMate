package dekk.pw.pokemate.util;

import com.pokegoapi.auth.GoogleAuthJson;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.GoogleCredentialProvider;
import dekk.pw.pokemate.Context;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by TimD on 7/27/2016.
 */
public class AuthenticationListener implements GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener {
    @Override
    public void onInitialOAuthComplete(GoogleAuthJson googleAuthJson) {
        String copied;
        // Copy user code to clipboard
        try {
            StringSelection selection = new StringSelection(googleAuthJson.getUserCode());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);

            copied = " (copied to clipboard)";
        } catch (Exception ex) {
            copied = "";
        }

        System.out.println("-----------------------------------------");
        System.out.println("  Please go to the following URL");
        System.out.println("  URL: " + googleAuthJson.getVerificationUrl());
        System.out.println("  Code: " + googleAuthJson.getUserCode() + copied);
        System.out.println("-----------------------------------------");

        // Open default browser to authentication url
        try {
            Desktop.getDesktop().browse(new URI(googleAuthJson.getVerificationUrl()));
        } catch (Exception ex) {
            System.err.println("Failed to automatically open browser.");
        }
    }

    @Override
    public void onTokenIdReceived(GoogleAuthTokenJson googleAuthTokenJson) {
        System.out.println("Token received: " + googleAuthTokenJson);
        Path tokenPath = Paths.get("/tokens/");
        if(!Files.exists(tokenPath)){
            try {
                Files.createDirectory(tokenPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (PrintWriter p = new PrintWriter("tokens/" + Context.getUsernameHash() + ".txt")) {
            p.write(googleAuthTokenJson.getRefreshToken());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
