WorldEditUtil
---
An interface between different versions of WorldEdit/WorldGuard. 

The problem is that if you build your project against WE/WG v5, then at runtime, your plugin will fail 
if the server is running WE/WG v6 (and vice-versa).

The solution is to interface with WorldEdit/WorldGuard abstractly 
and let the specific implementation vary at runtime:

The WorldEditUtil project allows BattleArena to be backwards compatible with WorldEdit v5 & v6


```java
// The new way:
WorldGuardInterface wgi = WorldGuardInterface.newInstance(); // it's that easy

// The old way:
// Or use WorldGuardUtil static, legacy methods()
// which are powered by the WorldGuardInterface
```

These are all the available operations that you can perform:
```java
public abstract ProtectedRegion getRegion(String world, String id);

public abstract ProtectedRegion getRegion(World w, String id);

public abstract boolean hasRegion(ArenaRegion region);

public abstract boolean hasRegion(World world, String id);

public abstract boolean hasRegion(String world, String id);

public abstract ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception;

public abstract ProtectedRegion createProtectedRegion(Player p, String id) throws Exception;

public abstract void clearRegion(WorldGuardRegion region);

public abstract void clearRegion(String world, String id);

public abstract boolean isLeavingArea(final Location from, final Location to, final ArenaRegion region);

public abstract boolean isLeavingArea(final Location from, final Location to, final World w, String id);

public abstract boolean setFlag(WorldGuardRegion region, String flag, boolean enable);

public abstract Flag<?> getWGFlag(String flagString);

public abstract StateFlag getStateFlag(String flagString);

public abstract boolean setFlag(String worldName, String id, String flag, boolean enable);

public abstract boolean allowEntry(Player player, String regionWorld, String id);

public abstract boolean addMember(String playerName, WorldGuardRegion region);

public abstract boolean addMember(String playerName, String regionWorld, String id);

public abstract boolean removeMember(String playerName, WorldGuardRegion region);

public abstract boolean removeMember(String playerName, String regionWorld, String id);

public abstract void deleteRegion(String worldName, String id);

public abstract boolean contains(Location location, WorldGuardRegion region);

public abstract boolean hasPlayer(String playerName, WorldGuardRegion region);

public abstract boolean trackRegion(ArenaRegion region) throws RegionNotFound;

public abstract boolean trackRegion(String world, String id) throws RegionNotFound;

public abstract int regionCount();

public abstract WorldGuardRegion getContainingRegion(Location location);

public abstract boolean pasteSchematic(WorldGuardRegion region);

public abstract boolean pasteSchematic(String worldName, String id);

public abstract boolean pasteSchematic(CommandSender consoleSender, String worldName, String id);

public abstract boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world);

public abstract boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world);

public abstract boolean saveSchematic(Player p, String schematicName);
```

Maven Repository:
---

[http://rainbowcraft.sytes.net/maven/repository/] (http://rainbowcraft.sytes.net/maven/repository/ "Maven Repository")

If you use maven, put these declarations in your pom.xml:

~~**repositories section:**~~

Check to make sure this repository is still active. If not, you will have to install the project to your local ```~/.m2/repository```

```xml
<repository>
    <id>rainbowcraft-repo</id>
    <url>http://rainbowcraft.sytes.net/maven/repository/</url>
</repository>
```

**Installation to your local ~/.m2/repository**

***git latest version:***

* ```git clone https://github.com/Europia79/BukkitInterface.git```
* ```mvn clean install```

***git previous versions:***
* ```git clone https://github.com/Europia79/BukkitInterface.git```
* ```git log --format=oneline```
* ```git checkout <hash>```
* ```mvn clean install```
* ```git checkout master```

***file download & mvn install:***

* Or, you can download a jar and run the ```mvn install:install-file``` command.
* This is also helpful to install any dependencies that maven can't automatically download.
  * Arguments: 
    * ```-Dfile=``` : The name & location of the jar
    * ```-DgroupId=``` : Mine is ```mc.euro```
    * ```-DartifactId=``` : If you decompile or unzip the jar, then you can find this & other information inside the folder ```META-INF/maven/{groupId}.{artifactId}/pom.properties``` & ```pom.xml```
    * ```-Dversion``` : Also found in ```pom.properties``` & ```pom.xml```
    * ```Dpackaging=jar```
    * ```DcreateChecksum=true```
  * Example: ```mvn install:install-file -Dfile="C:\Users\Nikolai\Documents\lib\BukkitInterface\2.0.1\BukkitInterface.jar" -DgroupId=mc.euro -DartifactId=BukkitInterface -Dversion=2.0.1 -Dpackaging=jar -DcreateChecksum=true```

**dependencies section:**

```xml
<dependency>
    <groupId>mc.alk</groupId>
    <artifactId>worldeditutil</artifactId>
    <version>1.1.6</version>
    <scope>compile</scope>
</dependency>
```

**build-plugins section:**

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.4.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <artifactSet>
                                <includes>
                                    <include>mc.alk:worldeditutil</include>
                                </includes>
                            </artifactSet>
                            <minimizeJar>true</minimizeJar>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

Dependencies:
---

- **Bukkit API**
  * https://github.com/Bukkit/Bukkit
  * http://repo.bukkit.org/content/groups/public
- **WorldEdit**
  * http://dev.bukkit.org/bukkit-plugins/worldedit/
  * https://github.com/sk89q/worldedit
  * http://maven.sk89q.com/repo/
  * v5 & v6 are required for compilation
- **WorldGuard**
  * http://dev.bukkit.org/bukkit-plugins/worldguard/
  * https://github.com/sk89q/WorldGuard
  * http://maven.sk89q.com/repo/
  * v5 & v6 are required for compilation


Contact:
---

Nick at Nikolai.Kalashnikov@hotmail.com

Nicodemis79 on Skype
