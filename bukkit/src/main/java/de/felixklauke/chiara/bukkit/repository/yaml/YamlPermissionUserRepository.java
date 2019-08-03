package de.felixklauke.chiara.bukkit.repository.yaml;

import com.google.common.collect.Maps;
import de.felixklauke.chiara.bukkit.model.PermissionUser;
import de.felixklauke.chiara.bukkit.repository.PermissionUserRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlPermissionUserRepository implements PermissionUserRepository {

  private final Path config;
  private final Logger logger;
  private final Map<UUID, PermissionUser> permissionUsers = Maps.newHashMap();

  public YamlPermissionUserRepository(Path config, Logger logger) {

    Objects.requireNonNull(config, "Config path cannot be null");
    Objects.requireNonNull(logger, "logger cannot be null");

    this.config = config;
    this.logger = logger;
    readUsers();
  }

  private void readUsers() {
    Yaml yaml = new Yaml();

    try (BufferedReader reader = Files.newBufferedReader(config)) {
      Map map = yaml.loadAs(reader, Map.class);
      Map<String, Object> users = (Map<String, Object>) map.get("users");

      users.forEach((key, value) -> {
        Map<String, Object> userMap = (Map<String, Object>) value;
        UUID uuid = UUID.fromString(key);
        PermissionUser permissionUser = userFromMap(uuid, userMap);
        permissionUsers.put(uuid, permissionUser);
      });

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error while reading users.", e);
    }
  }

  @Override
  public PermissionUser findUser(UUID uniqueId) {

    return permissionUsers.get(uniqueId);
  }

  @Override
  public void reloadUsers() {

    permissionUsers.clear();
    readUsers();
  }

  @Override
  public void writeUsers() {
    DumperOptions options = new DumperOptions();
    options.setPrettyFlow(true);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Yaml yaml = new Yaml(options);

    Map<String, Object> document = new HashMap<>();
    Map<String, Object> users = new HashMap<>();

    permissionUsers.forEach((uuid, permissionUser) -> {
      Map<String, Object> userMap = userToMap(permissionUser);
      users.put(uuid.toString(), userMap);
    });

    document.put("users", users);

    try (BufferedWriter writer = Files.newBufferedWriter(config)) {
      yaml.dump(document, writer);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error while writing users.", e);
    }

  }

  @Override
  public PermissionUser createUser(UUID uniqueId) {

    PermissionUser permissionUser = new PermissionUser(uniqueId, null, null, null);
    saveUser(permissionUser);
    return permissionUser;
  }

  @Override
  public void saveUser(PermissionUser permissionUser) {

    permissionUsers.put(permissionUser.getUniqueId(), permissionUser);
  }

  /**
   * Convert the given map into a user with the given name.
   *
   * @param uniqueId The unique id.
   * @param map The map.
   * @return The user.
   */
  private PermissionUser userFromMap(UUID uniqueId, Map<String, Object> map) {

    PermissionUser permissionUser = new PermissionUser();
    permissionUser.setUniqueId(uniqueId);
    permissionUser.setPermissions((Map<String, Boolean>) map.get("permissions"));
    permissionUser.setWorldPermissions((Map<String, Map<String, Boolean>>) map.get("worlds"));
    permissionUser.setGroups((List<String>) map.get("groups"));
    return permissionUser;
  }

  /**
   * Convert the given user into a map.
   *
   * @param permissionUser The user.
   * @return The map.
   */
  private Map<String, Object> userToMap(PermissionUser permissionUser) {

    Map<String, Object> map = new LinkedHashMap<>();
    map.put("permissions", permissionUser.getPermissions());
    map.put("worlds", permissionUser.getWorldPermissions());
    map.put("groups", permissionUser.getGroups());
    return map;
  }
}
