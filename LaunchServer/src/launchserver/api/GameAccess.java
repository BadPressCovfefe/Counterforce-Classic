package launchserver.api;

import launch.game.LaunchServerGame;

// Allows the API to get the game context to know where to run commands to from discord.

public final class GameAccess {
    private static volatile LaunchServerGame GAME;

    private GameAccess() {}

    public static void set(LaunchServerGame g) { GAME = g; }
    public static LaunchServerGame get() { return GAME; }
}
