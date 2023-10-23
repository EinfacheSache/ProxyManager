package de.cubeattack.proxymanager.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cubeattack.proxymanager.discord.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class MinecraftAPI {

    public static User load(String name) {
        UUID uuid;
        String playerName;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()))){
            JsonObject response = JsonParser.parseReader(in).getAsJsonObject();
            uuid = UUID.fromString(response.get("id").getAsString().replaceAll("\"", "").replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            playerName = response.get("name").getAsString();

        } catch (Exception ex) {
            System.out.println("Unable to get UUID of: " + name + "! (" + ex + ")");
            return null;
        }
        return new User(uuid,playerName);
    }
}
