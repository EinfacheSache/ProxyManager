package de.cubeattack.proxymanager.discord;

import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.DataSourceProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class User {
    private final String name;
    private final UUID UUID;
    private final DataSourceProvider source;
    private String rang = "none";
    private int weight = 100;
    private int rangID = 1;
    private int coins = 0;
    private int playtime = 0;
    private boolean hasWebAccount = false;
    private boolean isBanned = false;
    private boolean isMuted = false;

    public User(UUID uuid, String name) {
        this.UUID = uuid;
        this.name = name;
        this.source = Core.getDatasource();
    }

    public void loadDataFromMySQL() {

        try (ResultSet query1 = source.query("SELECT * FROM core_players WHERE uuid='" + this.getUUID() + "';");
             ResultSet query2 = source.query("SELECT * FROM prime_perms_ranking WHERE uuid='" + this.getUUID() + "';")) {

            if (!query1.next()) return;
            if (!query2.next()) return;

            this.coins = query1.getInt("coins");
            this.playtime = query1.getInt("playtime");
            this.rangID = query2.getInt("group");
        } catch (SQLException ex) {
            Core.severe("Error while loading data from MySQL", ex);
        }

        try (ResultSet query1 = source.query("SELECT * FROM prime_perms_groups WHERE id='" + this.rangID + "';");
             ResultSet query2 = source.query("SELECT * FROM prime_bungee_mute WHERE uuid='" + this.getUUID() + "';");
             ResultSet query3 = source.query("SELECT * FROM prime_bungee_bans WHERE uuid='" + this.getUUID() + "';");
             ResultSet query4 = source.query("SELECT * FROM core_web_accounts WHERE player='" + this.getUUID() + "';")) {

            this.weight = query1.getInt("wei ght");
            this.rang = query1.getString("display_name");
            this.isMuted = query2.next();
            this.isBanned = query3.next();
            this.hasWebAccount = query4.next();
        } catch (SQLException ex) {
            Core.severe("Error while loading data from MySQL", ex);
        }
    }


    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return UUID;
    }

    public String getCoins() {
        return String.valueOf(coins);
    }

    public String getPlaytime() {
        int days = playtime / 60 / 24 % 60;
        int hours = playtime / 60 % 60;
        int min = playtime % 60;
        return days + " Tage " + hours + " Stunden " + min + " Minuten";
    }

    public String getRang() {
        return rang;
    }

    public String getWeight() {
        return String.valueOf(weight);
    }

    public String isMuted() {
        return String.valueOf(isMuted);
    }

    public String isBanned() {
        return String.valueOf(isBanned);
    }

    public String hasWebAccount() {
        return String.valueOf(hasWebAccount);
    }
}
