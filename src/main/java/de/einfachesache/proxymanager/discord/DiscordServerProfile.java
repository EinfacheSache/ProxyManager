package de.einfachesache.proxymanager.discord;

import java.util.Objects;

public class DiscordServerProfile {

    private final String name;
    private final String guildId;
    private final String joinRoleId;
    private final String welcomeChannelId;
    private final String whitelistedRoleId;
    private final String countingChannelId;
    private final String giveawayChannelId;
    private final String whitelistChannelId;

    private String inviteLogChannelId;
    private String betaTesterRoleId;
    private String ticketCategoryId;
    private String staffRoleId;
    private String logChannelId;


    public DiscordServerProfile(String guildId,
                                String name,
                                String joinRoleId,
                                String staffRoleId,
                                String betaTesterRoleId,
                                String whitelistedRoleId,
                                String logChannelId,
                                String welcomeChannelId,
                                String ticketCategoryId,
                                String countingChannelId,
                                String giveawayChannelId,
                                String whitelistChannelId,
                                String inviteLogChannelId) {
        this.name = name;

        this.guildId = guildId;
        this.joinRoleId = joinRoleId;
        this.staffRoleId = staffRoleId;
        this.betaTesterRoleId = betaTesterRoleId;
        this.whitelistedRoleId = whitelistedRoleId;

        this.logChannelId = logChannelId;
        this.welcomeChannelId = welcomeChannelId;
        this.ticketCategoryId = ticketCategoryId;
        this.countingChannelId = countingChannelId;
        this.giveawayChannelId = giveawayChannelId;
        this.whitelistChannelId = whitelistChannelId;
        this.inviteLogChannelId = inviteLogChannelId;
    }


    public DiscordServerProfile() {
        this.name = null;
        this.guildId = null;

        this.joinRoleId = null;
        this.betaTesterRoleId = null;
        this.whitelistedRoleId = null;

        this.welcomeChannelId = null;
        this.countingChannelId = null;
        this.giveawayChannelId = null;
        this.whitelistChannelId = null;
    }

    public String getName() {
        return name;
    }


    public String getJoinRoleId() {
        return joinRoleId;
    }

    public String getWhitelistedRoleId() {
        return whitelistedRoleId;
    }

    public String getStaffRoleId() {
        return staffRoleId;
    }

    public void setStaffRoleId(String staffRoleId) {
        this.staffRoleId = staffRoleId;
    }

    public String getBetaTesterRoleId() {
        return betaTesterRoleId;
    }

    public void setBetaTesterRoleId(String betaTesterRoleId) {
        this.betaTesterRoleId = betaTesterRoleId;
    }


    public String getTicketCategoryId() {
        return ticketCategoryId;
    }

    public void setTicketCategoryId(String ticketCategoryId) {
        this.ticketCategoryId = ticketCategoryId;
    }

    public String getLogChannelId() {
        return logChannelId;
    }

    public void setLogChannelId(String logChannelId) {
        this.logChannelId = logChannelId;
    }

    public String getWelcomeChannelId() {
        return welcomeChannelId;
    }

    public String getCountingChannelId() {
        return countingChannelId;
    }

    public String getGiveawayChannelId() {
        return giveawayChannelId;
    }

    public String getWhitelistChannelId() {
        return whitelistChannelId;
    }

    public String getInviteLogChannelId() {
        return inviteLogChannelId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(guildId);
    }

    @Override
    public String toString() {
        return "DiscordServerProfile{" +
                "name='" + name + '\'' +
                ", guildId='" + guildId + '\'' +
                ", joinRoleId='" + joinRoleId + '\'' +
                ", ticketCategoryId='" + ticketCategoryId + '\'' +
                ", staffRoleId='" + staffRoleId + '\'' +
                ", logChannelId='" + logChannelId + '\'' +
                ", welcomeChannelId='" + welcomeChannelId + '\'' +
                ", countingChannelId='" + countingChannelId + '\'' +
                ", giveawayChannelId='" + giveawayChannelId + '\'' +
                ", whitelistChannelId='" + whitelistChannelId + '\'' +
                ", betaTesterRoleId='" + betaTesterRoleId + '\'' +
                ", whitelistedRoleId='" + whitelistChannelId + '\'' +
                '}';
    }
}