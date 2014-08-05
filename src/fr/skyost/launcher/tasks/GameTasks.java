package fr.skyost.launcher.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.ProfilesManager.LauncherProfile;
import fr.skyost.launcher.Skyolauncher;
import fr.skyost.launcher.UsersManager;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.tasks.AuthUser.Property;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;
import fr.skyost.launcher.utils.SystemManager.OS;
import fr.skyost.launcher.utils.SystemManager.Platform;
import fr.skyost.launcher.utils.Utils;

public class GameTasks extends Thread {

	private final LauncherProfile profile;
	private final JProgressBar downloadProgress;
	
	private static final List<GameTasksListener> listeners = new ArrayList<GameTasksListener>();

	public GameTasks(final LauncherProfile profile, final JProgressBar downloadProgress) {
		this.profile = profile;
		this.downloadProgress = downloadProgress;
	}

	@Override
	public void run() {
		for(final GameTasksListener listener : listeners) {
			listener.onGameTasksBegin();
		}
		final Platform platform = Skyolauncher.SYSTEM.getPlatform();
		final OS os = platform.getOS();
		String arch = platform.getArch().getName();
		LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Debug infos :");
		LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "OS : " + os.getName());
		LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Architecture : " + arch);
		LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Java version : " + System.getProperty("java.version"));
		final File libsDir = new File(profile.gameDirectory + LauncherConstants.LIBS_SUFFIX.replace("/", File.separator));
		final File verionsDir = new File(profile.gameDirectory + LauncherConstants.VERSIONS_SUFFIX.replace("/", File.separator));
		final File assetsDir = new File(profile.gameDirectory + LauncherConstants.ASSETS_SUFFIX.replace("/", File.separator));
		final File nativesDir = new File(verionsDir + File.separator + profile.version + File.separator + "natives");
		arch = arch.replace("x", "").replace("86", "32");
		if(!verionsDir.exists()) {
			verionsDir.mkdirs();
		}
		if(!assetsDir.exists()) {
			assetsDir.mkdir();
		}
		if(!nativesDir.exists()) {
			nativesDir.mkdirs();
		}
		final Gson gson = new Gson();
		try {
			final String osMinecraftName = os.getMinecraftName();
			final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			final String versionPath = profile.version + "/" + profile.version + ".json";
			final File versionJson = new File(verionsDir + File.separator + versionPath.replace("/", File.separator));
			if(!versionJson.exists() && !fixFile(versionJson, LauncherConstants.MINECRAFT_AWS_URL + LauncherConstants.VERSIONS_SUFFIX + "/" + versionPath, FileType.FILE, FixMode.MISSING)) {
				return;
			}
			final Game game = gson.fromJson(Utils.getFileContent(versionJson, null), Game.class);
			if(game.minimumLauncherVersion > LauncherConstants.CLIENT_VERSION) {
				LogUtils.log(Level.WARNING, LauncherConstants.GAME_TASKS_PREFIX + "THE REQUIERED LAUNCHER'S VERSION OF THIS GAME IS SUPERIOR THAN THE VERSION OF THIS LAUNCHER.");
			}
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Checking the local copy of the selected version...");
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Checking libraries...");
			final List<String> librariesPaths = new ArrayList<String>();
			for(final Library library : game.libraries) {
				if(library.isDisallowed(os)) {
					break;
				}
				final String[] libData = library.name.split(":");
				final String libName = libData[1];
				final String libVersion = libData[2];
				String libFileName = libName + "-" + libVersion;
				boolean hasNative = false;
				if(library.natives != null) {
					final String nativeOs = library.natives.get(osMinecraftName);
					if(nativeOs != null) {
						libFileName += "-" + library.natives.get(osMinecraftName).replace("${arch}", arch);
						hasNative = true;
					}
				}
				libFileName += ".jar";
				final String libPath = "/" + libData[0].replace(".", "/") + "/" + libName + "/" + libVersion + "/" + libFileName;
				final File libFile = new File(libsDir, libPath.replace("/", File.separator));
				final String libUrl = LauncherConstants.LIBS_URL + libPath;
				if(!libFile.exists() && !(library.natives != null && !hasNative) && !fixFile(libFile, libUrl, FileType.LIBRARY, FixMode.MISSING)) {
					return;
				}
				final File sha1File = new File(libFile + ".sha");
				if(!sha1File.exists() && !fixFile(sha1File, libUrl + ".sha1", FileType.HASH, FixMode.MISSING)) {
					LogUtils.log(Level.WARNING, LauncherConstants.GAME_TASKS_PREFIX + "Cannot download the SHA1 file, it has been ignored.");
				}
				if(sha1File.exists() && !Utils.getFileContent(sha1File, null).equals(Utils.getFileChecksum(libFile, sha1)) && !fixFile(libFile, libUrl, FileType.LIBRARY, FixMode.INVALID)) {
					return;
				}
				if(hasNative) {
					LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Unzipping native...");
					if(!Utils.unzipJar(nativesDir, libFile, library.extract.exclude)) {
						LogUtils.log(Level.SEVERE, LauncherConstants.GAME_TASKS_PREFIX + "Unable to unzip the native " + libFile.getName() + ". Please unzip it manually and place it into " + nativesDir.getName() + ".");
						notifyListeners(false);
						return;
					}
				}
				else {
					librariesPaths.add(libFile.getAbsolutePath());
				}
			}
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Done.");
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Checking assets...");
			final boolean isLegacy = !Utils.compareVersions(profile.version, "1.7.3");
			final String assetsIndexPath = LauncherConstants.ASSETS_INDEXES_SUFFIX + (isLegacy ? LauncherConstants.ASSETS_LEGACY_SUFFIX + ".json" : "/" + game.assets + ".json");
			final File assetsIndex = new File(assetsDir + assetsIndexPath.replace("/", File.separator));
			final File assetsIndexesDir = assetsIndex.getParentFile();
			if(!assetsIndexesDir.exists()) {
				assetsIndexesDir.mkdir();
			}
			if(!assetsIndex.exists() && !fixFile(assetsIndex, LauncherConstants.MINECRAFT_AWS_URL + assetsIndexPath, FileType.FILE, FixMode.MISSING)) {
				return;
			}
			final AssetsList assetsList = gson.fromJson(Utils.getFileContent(assetsIndex, null), AssetsList.class);
			final String assetsObjectsPath = isLegacy ? LauncherConstants.ASSETS_VIRTUAL_SUFFIX + LauncherConstants.ASSETS_LEGACY_SUFFIX : LauncherConstants.ASSETS_OBJECTS_SUFFIX;
			final File assetsObjectsDir = new File(assetsDir + assetsObjectsPath.replace("/", File.separator));
			if(!assetsObjectsDir.exists()) {
				assetsObjectsDir.mkdirs();
			}
			for(final Entry<String, Asset> entry : assetsList.objects.entrySet()) {
				final Asset asset = entry.getValue();
				final String hash = "/" + asset.hash.charAt(0) + asset.hash.charAt(1) + "/" + asset.hash;
				final File assetFile = new File(assetsObjectsDir + (isLegacy ? "/" + entry.getKey() : hash).replace("/", File.separator));
				if(!assetFile.exists() && !fixFile(assetFile, LauncherConstants.MINECRAFT_RES_URL + hash, FileType.ASSET, FixMode.MISSING)) {
					return;
				}
				if(!Utils.getFileChecksum(assetFile, sha1).equals(asset.hash) && !fixFile(assetFile, LauncherConstants.MINECRAFT_RES_URL + hash, FileType.ASSET, FixMode.INVALID)) {
					return;
				}
			}
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Done.");
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Launching Minecraft...");
			final String gameFilePath = LauncherConstants.VERSIONS_SUFFIX + "/" + game.id + "/" + game.id + ".jar";
			final File gameFile = new File(profile.gameDirectory + gameFilePath.replace("/", File.separator));
			if(!gameFile.exists() && !fixFile(gameFile, LauncherConstants.MINECRAFT_AWS_URL + gameFilePath, FileType.FILE, FixMode.MISSING)) {
				return;
			}
			else if(!Utils.isZipValid(gameFile) && !fixFile(gameFile, LauncherConstants.MINECRAFT_AWS_URL + gameFilePath, FileType.FILE, FixMode.INVALID)) {
				return;
			}
			final String pathSeparator = System.getProperty("path.separator");
			final List<String> command = new ArrayList<String>();
			command.add(Utils.getJavaDir());
			if(profile.arguments != null) {
				command.addAll(Arrays.asList(profile.arguments.split(" ")));
			}
			final User user = UsersManager.getUserByID(profile.user);
			command.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
			command.add("-cp");
			command.add(StringUtils.join(librariesPaths, pathSeparator) + pathSeparator + gameFile.getAbsolutePath());
			command.add(game.mainClass);
			command.addAll(getMinecraftArgs(game, user == null ? new User("Player", UUID.nameUUIDFromBytes(("OfflinePlayer:Player").getBytes(Charset.forName("UTF-8"))).toString().replace("-", ""), "Player", false, "0", new ArrayList<Property>()) : user, gson, assetsDir, assetsObjectsDir));
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Executing command : " + StringUtils.join(command, ' '));
			final Process process = new ProcessBuilder(command.toArray(new String[command.size()])).directory(profile.gameDirectory).start();
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Done.");
			if(profile.logMinecraft) {
				new StreamReader(LauncherConstants.MINECRAFT_OUTPUT_PREFIX, process.getInputStream()).start();
				new StreamReader(LauncherConstants.MINECRAFT_ERRORS_PREFIX, process.getErrorStream()).start();
			}
			notifyListeners(true);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private final List<String> getMinecraftArgs(final Game game, final User user, final Gson gson, final File assetsDir, final File assetsObjectsDir) {
		final HashMap<String, String> map = new HashMap<String, String>();
		final List<String> args = new ArrayList<String>(Arrays.asList(game.minecraftArguments.split(" ")));
		final HashMap<String, List<String>> properties = new HashMap<String, List<String>>();
		for(final Property property : user.properties) {
			properties.put(property.name, Arrays.asList(property.value));
		}
		map.put("auth_player_name", user.username);
		map.put("version_name", profile.version);
		map.put("game_directory", profile.gameDirectory.getPath());
		map.put("assets_root", assetsDir.getPath());
		map.put("assets_index_name", game.assets == null ? profile.version : game.assets);
		map.put("auth_uuid", user.uuid);
		map.put("auth_access_token", user.accessToken);
		map.put("user_properties", gson.toJson(properties));
		map.put("user_type", "mojang");
		map.put("game_assets", assetsObjectsDir.getPath());
		final StrSubstitutor substitutor = new StrSubstitutor(map);
		for(int i = 0; i != args.size(); i++) {
			args.set(i, substitutor.replace(args.get(i)));
		}
		if(LauncherConstants.MINECRAFT_SERVER_IP != null) {
			args.addAll(Arrays.asList("--server", LauncherConstants.MINECRAFT_SERVER_IP, "--port", String.valueOf(LauncherConstants.MINECRAFT_SERVER_PORT)));
		}
		return args;
	}

	private final boolean fixFile(final File file, final String fileUrl, final FileType type, final FixMode mode) throws IOException {
		LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + mode.name + " " + type.name + " : " + file.getPath() + ".");
		if(Skyolauncher.isOnline) {
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + (mode == FixMode.INVALID ? "Re-" : "") + "Downloading it.");
			if(type != FileType.FILE) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			initializeGuiComponents(file.getName());
			if(!ConnectionUtils.download(fileUrl, file, downloadProgress)) {
				LogUtils.log(Level.SEVERE, LauncherConstants.GAME_TASKS_PREFIX + "Failed !");
				file.delete();
				notifyListeners(false);
				resetGuiComponents();
				return false;
			}
			resetGuiComponents();
			return true;
		}
		else {
			if(mode == FixMode.INVALID) {
				file.delete();
			}
			notifyListeners(false);
			return false;
		}
	}
	
	private final void initializeGuiComponents(final String fileName) {
		downloadProgress.setVisible(true);
	}
	
	private final void resetGuiComponents() {
		downloadProgress.setString(null);
		downloadProgress.setVisible(false);
	}

	public final void notifyListeners(final boolean success) {
		if(!success && !Skyolauncher.isOnline) {
			LogUtils.log(Level.INFO, LauncherConstants.GAME_TASKS_PREFIX + "Please re-launch the selected version when you will be online.");
		}
		for(final GameTasksListener listener : listeners) {
			listener.onGameTasksFinished(success, profile);
		}
	}

	public static final void addListener(final GameTasksListener listener) {
		listeners.add(listener);
	}

	public interface GameTasksListener {

		public void onGameTasksBegin();
		public void onGameTasksFinished(final boolean success, final LauncherProfile profile);
		
	}

	public enum FileType {
		
		FILE("file"),
		LIBRARY("library"),
		ASSET("asset"),
		HASH("hash");

		public final String name;

		FileType(final String name) {
			this.name = name;
		}
		
	}

	public enum FixMode {
		
		MISSING("Missing"),
		INVALID("Invalid");

		public final String name;

		FixMode(final String name) {
			this.name = name;
		}
		
	}

	public class Game {

		public String id;
		public String time;
		public String releaseTime;
		public String type;
		public String minecraftArguments;
		public int minimumLauncherVersion;
		public String assets;
		public List<Library> libraries;
		public String mainClass;

	}

	public class Library {

		public String name;
		public HashMap<String, String> natives;
		public List<DLRule> rules;
		public ExtractRules extract;

		public final boolean isDisallowed(final OS os) {
			if(rules != null) {
				final String osName = os.getMinecraftName();
				for(final DLRule rule : rules) {
					if(rule.os != null) {
						final String currentOs = rule.os.get("name");
						if(rule.action.equals("disallow") && currentOs.equals(osName)) {
							return true;
						}
						else if(rule.action.equals("allow") && !currentOs.equals(osName)) { //TODO: Short this.
							return true;
						}
					}
				}
			}
			return false;
		}
		
	}

	public class DLRule {

		public String action;
		public HashMap<String, String> os;

	}

	public class ExtractRules {

		public List<String> exclude;
	}

	public class AssetsList {

		public boolean virtual;
		public HashMap<String, Asset> objects;

	}

	public class Asset {

		public String hash;
		public int size;

	}
	
}
