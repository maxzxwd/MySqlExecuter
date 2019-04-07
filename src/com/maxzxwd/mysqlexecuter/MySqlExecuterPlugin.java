package com.maxzxwd.mysqlexecuter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;

public class MySqlExecuterPlugin extends JavaPlugin {
  private FileConfiguration config = getConfig();
  private Connection connection;

  @Override
  public void onEnable() {
    getDataFolder().mkdir();

    config.addDefault("host", "localhost");
    config.addDefault("port", 3306);
    config.addDefault("username", "root");
    config.addDefault("password", "1234");
    config.options().copyDefaults(true);
    saveConfig();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equalsIgnoreCase("mysql") &&
        (sender.hasPermission(command.getPermission()) || sender instanceof ConsoleCommandSender)) {

      if (args.length == 0) {
        sender.sendMessage(command.getUsage());
        return true;
      }
      String query = String.join(" ", args).replace('`', '\'');

      BukkitRunnable r = new BukkitRunnable() {
        @Override
        public void run() {
          String result = "Affected records: ";
          try {
            openConnection();
            result += connection.createStatement().executeUpdate(query);
            connection.close();
          } catch(ClassNotFoundException | SQLException e) {
            e.printStackTrace();
          } finally {
            sender.sendMessage(result);
          }
        }
      };

      r.runTaskAsynchronously(this);

      return true;
    }

    sender.sendMessage(command.getPermissionMessage());
    return true;
  }

  public void openConnection() throws SQLException, ClassNotFoundException {
    if (connection != null && !connection.isClosed()) {
      return;
    }

    synchronized (this) {
      if (connection != null && !connection.isClosed()) {
        return;
      }
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("host") + ":" +
              config.getInt("port"), config.getString("username"), config.getString("password"));
    }
  }
}
