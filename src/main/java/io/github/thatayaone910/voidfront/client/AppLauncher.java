package io.github.thatayaone910.voidfront.client;

import java.io.IOException;
import java.util.Arrays;
import static javafx.application.Application.launch;

public class AppLauncher {
    public static void main(String[] args) {

        // Evaluate arguments on startup
        if( Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase( "--gui" )) ){
            System.out.println("## Running the GUI...");
            String[] applicationArgs = Arrays.stream(args)
                    .filter(arg -> !arg.equalsIgnoreCase("--gui"))
                    .toArray(String[]::new);
            launch(VoidfrontGui.class, applicationArgs);
        }
        else{
            try {
                ClientMain.main(args);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
