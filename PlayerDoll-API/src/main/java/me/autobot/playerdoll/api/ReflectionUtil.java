package me.autobot.playerdoll.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

public abstract class ReflectionUtil {

    private static final String CB_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NM_PACKAGE = "net.minecraft";
    private static final String NMS_PACKAGE = "net.minecraft.server";

    private static final Class<?> FOLIA_REG_SERVER_CLASS;
    private static Object FOLIA_REG_SERVER_INSTANCE;
    private static final Object DEDI_SERVER_INSTANCE;
    private static final Class<?> CRAFT_SERVER_CLASS = getCBClass("CraftServer");
    private static final Object CRAFT_SERVER_INSTANCE;

    private static Method getBukkitPlayerMethod;
    private static final Method nmsToBukkitItemStackMethod;
    private static final Field craftHumanPermissiableBaseField;
    private static Method gameProfileIdMethod;
    private static Method gameProfileNameMethod;
    private static Method gameProfilePropertiesMethod;
    private static Constructor<?> gameProfileWithPropertiesConstructor;
    private static Constructor<?> propertyMapConstructor;
    private static Method immutableMultimapOfMethod;

    static {
        try {
            Server server = Bukkit.getServer();
            Field dedicatedServerField = server.getClass().getDeclaredField("console");
            dedicatedServerField.setAccessible(true);
            DEDI_SERVER_INSTANCE = dedicatedServerField.get(server);
            CRAFT_SERVER_INSTANCE = CRAFT_SERVER_CLASS.cast(server);

            // Folia things
            FOLIA_REG_SERVER_CLASS = getClass("io.papermc.paper.threadedregions.RegionizedServer");
            if (FOLIA_REG_SERVER_CLASS != null) {
                Method getRegionizedServerMethod = Arrays.stream(FOLIA_REG_SERVER_CLASS.getMethods())
                        .filter(method -> method.getReturnType() == FOLIA_REG_SERVER_CLASS && method.getName().equals("getInstance"))
                        .findFirst()
                        .orElseThrow();

                try {
                    FOLIA_REG_SERVER_INSTANCE = getRegionizedServerMethod.invoke(null);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            nmsToBukkitItemStackMethod = Arrays.stream(getCBClass("inventory.CraftItemStack").getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 1)
                    .filter(method -> method.getName().equals("asBukkitCopy"))
                    .findAny().orElseThrow();
            nmsToBukkitItemStackMethod.setAccessible(true);

            craftHumanPermissiableBaseField = Arrays.stream(getCBClass("entity.CraftHumanEntity").getDeclaredFields())
                    .filter(field -> field.getName().equals("perm"))
                    .findAny().orElseThrow();
            craftHumanPermissiableBaseField.setAccessible(true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static <T> T invokeStaticMethod(Class<T> clazz, Method method, Object... args) {
        return invokeMethod(clazz, method, null, args);
    }
    public static Object invokeStaticMethod(Method method, Object... args) {
        return invokeMethod(method, null, args);
    }

    public static <T> T invokeMethod(Class<T> clazz, Method method, Object instance, Object... args) {
        try {
            return clazz.cast(method.invoke(instance, args));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T getField(Class<T> clazz, Field field, Object instance) {
        try {
            return clazz.cast(field.get(instance));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static Object getField(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <C> C newInstance(Class<C> clazz, Constructor<?> constructor, Object... args) {
        return clazz.cast(newInstance(constructor, args));
    }
    public static Object newInstance(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean hasClass(String className) {
        return getClass(className) != null;
    }
    public static boolean hasAddonClass(String className, Addon addonMain) {
        return getAddonClass(className, addonMain) != null;
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public static Class<?> getAddonClass(String className, Addon addonMain) {
        try {
            return Class.forName(className, true, addonMain.getClass().getClassLoader());
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public static Class<?> getNMSClass(String shortName) {
        return getClass(NMS_PACKAGE + "." + shortName);
    }
    public static Class<?> getNMClass(String shortName) {
        return getClass(NM_PACKAGE + "." + shortName);
    }

    public static Class<?> getCBClass(String shortName) {
        return getClass(CB_PACKAGE + "." + shortName);
    }

    public static Object getDedicatedServerInstance() {
        return DEDI_SERVER_INSTANCE;
    }
    public static Class<?> getDedicatedServerClass() {
        return DEDI_SERVER_INSTANCE.getClass();
    }

    public static Class<?> getCraftServerClass() {
        return CRAFT_SERVER_CLASS;
    }
    public static Object getCraftServerInstance() {
        return CRAFT_SERVER_INSTANCE;
    }
    public static Object getFoliaRegoinizedServerInstance() {
        return FOLIA_REG_SERVER_INSTANCE;
    }
    public static Class<?> getFoliaRegoinizedServerClass() {
        return FOLIA_REG_SERVER_CLASS;
    }

    public static Player NMSToBukkitPlayer(Object nmsPlayer) {
        if (getBukkitPlayerMethod == null) {
            try {
                getBukkitPlayerMethod = nmsPlayer.getClass().getMethod("getBukkitEntity");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return invokeMethod(Player.class, getBukkitPlayerMethod, nmsPlayer);
    }

    public static Object bukkitToNMSPlayer(Player player) {
        try {
            return invokeMethod(player.getClass().getMethod("getHandle"), player);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack NMSToBukkitItemStack(Object itemStack) {
        return invokeStaticMethod(ItemStack.class, nmsToBukkitItemStackMethod, itemStack);
    }

//    public static Object getNMSEntity(Entity entity) {
//        try {
//            return invokeMethod(entity.getClass().getMethod("getHandle"), entity);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
    public static Entity getCraftEntity(Object nmsEntity) {
        try {
            return invokeMethod(Entity.class, nmsEntity.getClass().getMethod("getBukkitEntity"), nmsEntity);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID getGameProfileId(GameProfile profile) {
        if (gameProfileIdMethod == null) {
            gameProfileIdMethod = resolveNoArgMethod(GameProfile.class, "getId", "id");
        }
        return invokeMethod(UUID.class, gameProfileIdMethod, profile);
    }

    public static String getGameProfileName(GameProfile profile) {
        if (gameProfileNameMethod == null) {
            gameProfileNameMethod = resolveNoArgMethod(GameProfile.class, "getName", "name");
        }
        return invokeMethod(String.class, gameProfileNameMethod, profile);
    }

    public static PropertyMap getGameProfileProperties(GameProfile profile) {
        if (gameProfilePropertiesMethod == null) {
            gameProfilePropertiesMethod = resolveNoArgMethod(GameProfile.class, "getProperties", "properties");
        }
        return invokeMethod(PropertyMap.class, gameProfilePropertiesMethod, profile);
    }

    public static GameProfile createGameProfileWithTextures(UUID id, String name, String value, String signature) {
        try {
            if (gameProfileWithPropertiesConstructor == null) {
                gameProfileWithPropertiesConstructor = Arrays.stream(GameProfile.class.getConstructors())
                        .filter(c -> c.getParameterCount() == 3)
                        .filter(c -> c.getParameterTypes()[0] == UUID.class)
                        .filter(c -> c.getParameterTypes()[1] == String.class)
                        .findFirst()
                        .orElse(null);
            }

            if (gameProfileWithPropertiesConstructor != null) {
                if (immutableMultimapOfMethod == null) {
                    Class<?> immutableMultimapClass = Class.forName("com.google.common.collect.ImmutableMultimap");
                    immutableMultimapOfMethod = Arrays.stream(immutableMultimapClass.getMethods())
                            .filter(method -> method.getName().equals("of"))
                            .filter(method -> method.getParameterCount() == 2)
                            .findFirst()
                            .orElseThrow();
                    immutableMultimapOfMethod.setAccessible(true);
                }
                if (propertyMapConstructor == null) {
                    propertyMapConstructor = Arrays.stream(PropertyMap.class.getConstructors())
                            .filter(c -> c.getParameterCount() == 1)
                            .findFirst()
                            .orElseThrow();
                    propertyMapConstructor.setAccessible(true);
                }

                Object textureProperty = new com.mojang.authlib.properties.Property("textures", value, signature);
                Object multimap = immutableMultimapOfMethod.invoke(null, "textures", textureProperty);
                PropertyMap propertyMap = newInstance(PropertyMap.class, propertyMapConstructor, multimap);
                return newInstance(GameProfile.class, gameProfileWithPropertiesConstructor, id, name, propertyMap);
            }
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        GameProfile profile = new GameProfile(id, name);
        PropertyMap properties = getGameProfileProperties(profile);
        properties.clear();
        properties.put("textures", new com.mojang.authlib.properties.Property("textures", value, signature));
        return profile;
    }

    private static Method resolveNoArgMethod(Class<?> clazz, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = clazz.getMethod(methodName);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new IllegalStateException("No compatible method found in " + clazz.getName() + " for " + Arrays.toString(methodNames));
    }
    // Fix desync permissions when convertPlayer
    public static void setConvertPlayerPermBase(Player old, Player convert) {
        setField(craftHumanPermissiableBaseField, convert, getField(craftHumanPermissiableBaseField, old));
    }
//    public static Class<?> getPluginNMSClass(String className) {
//        return getClass("me.autobot.playerdoll." + PlayerDollAPI.getServerVersion().getName() + "." + className);
//    }
//    public static Class<?> getPluginClass(String className) {
//        return getClass("me.autobot.playerdoll.".concat(className));
//    }

}
