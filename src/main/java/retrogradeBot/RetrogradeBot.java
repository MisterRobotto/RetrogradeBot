package retrogradeBot;

import sx.blah.discord.api.*;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.util.*;
import java.io.*;

/**
 * Created by ConnorFlynn on 8/26/2016.
 */
public class RetrogradeBot {

    //The token of the bot to use
    public static String BotToken = "MjE4Nzk0MzIxODk2NDA3MDQy.CqIXmg.cJj9D-2M3bMsVmTjVODx5EYBdr4";

    //The file location of the text document containing the bot's messages
    public static String ConfigLocation = "C:\\Users\\ConnorFlynn\\Desktop\\RetrogradeBotMessages.txt";

    //ArrayList of messages
    public static ArrayList<String> Messages = new ArrayList<String>();

    //Client of bot
    public static IDiscordClient client;

    public static void main(String[] args) { // Main method

        RetrogradeBot INSTANCE = login(BotToken); // Creates the bot instance and logs it in.
        client.getDispatcher().registerListener(new EventHandler()); // Create event listener so bot can respond to events

    }

    /**
     * Refresh the messages ArrayList from the text file
     */
    public static void getMessages(){

        //Clear the ArrayList
        Messages.clear();

        try {

            //Create BufferedReader
            BufferedReader reader = new BufferedReader(new FileReader(new File(ConfigLocation)));

            boolean canRead = true;

            String line;
            String message = null;

            do{

                try{

                    //Read next line
                    line = reader.readLine();
                    //If next line has text
                    if(line != null) {
                        //If next line starts with "~", it's the end of the message
                        if(line.startsWith("~")) {
                            //Add completed message to Messages
                            Messages.add(message);
                            message = null;
                        }
                        //Otherwise...
                        else{
                            //If the message is empty, set it as the current line
                            if(message == null){
                                message = line;
                            }
                            //Otherwise, add the current line as a new line
                            else {
                                message = message + "\n" + line;
                            }
                        }
                    }
                    //If the line has no text, exit the loop
                    else{
                        canRead = false;
                    }
                //If there is an error, exit the loop
                }catch(IOException e){

                    canRead = false;

                }

            }while(canRead);

        }catch(FileNotFoundException e){

            //If the system can't find the file, print an error
            System.out.println("ERROR: CONFIG FILE AT LOCATION "+ConfigLocation+" NOT FOUND.");

        }
    }

    public static void RoutineMessage(){

        int interval = 2; //Interval between messages in minutes

        int msgCounter = 0;
        boolean msgSent = false;

        //Do forever (until bot is exited)
        do{
            //Every [interval] minutes...
            if(((Calendar.getInstance().get(Calendar.MINUTE) % interval) == 0)){
                //If message is not sent
                if(!msgSent) {

                    //Get next message from Messages and advance counter
                    String content = Messages.get(msgCounter);
                    msgCounter++;
                    //If the counter is equal to the size of Messages, reset it and refresh Messages
                    if (msgCounter == Messages.size()) {
                        msgCounter = 0;
                        getMessages();
                    }

                    //For every channel in the server
                    for(IChannel i : client.getChannels(false)) {
                        //If no role restrictions are set (ie. if it's a general channel)
                        if (i.getRoleOverrides().toString().compareTo("{}") == 0) {

                            try {
                                //Create new message and label and send it
                                new MessageBuilder(client).withChannel(i).withContent(content).build();
                                //Toggle msgSent, this keeps the bot from posting nonstop during this minute
                                msgSent = true;

                            } catch (RateLimitException e) {
                            } catch (DiscordException e) {
                            } catch (MissingPermissionsException e) {
                            }
                        }
                    }

                }
            }
            //If we are between minutes, reset msgSent
            else{
                msgSent = false;
            }


        }while(true);
    }

    /**
     * Log the program into the bot
     */
    public static RetrogradeBot login(String token) {
        RetrogradeBot bot = null;

        ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance
        builder.withToken(token); // Sets the bot token for the client
        try {
            client = builder.login(); // Builds the IDiscordClient instance and logs it in
            bot = new RetrogradeBot(client); // Creating the bot instance
        } catch (DiscordException e) { // Error occurred logging in
            System.err.println("Error occurred while logging in!");
            e.printStackTrace();
        }

        return bot;
    }

    public RetrogradeBot(IDiscordClient client){}

    /**
     * Handle events.
     */
    public static class EventHandler {

        //When the bot is ready to start, get the messages and start sending them.
        @EventSubscriber
        public void handle(ReadyEvent event) {

            getMessages();
            RoutineMessage();

        }
    }

}
