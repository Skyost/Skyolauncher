package fr.skyost.launcher.utils;

import java.io.File;

import fr.skyost.launcher.LauncherConstants;

public class SystemManager {

	private final Platform platform;

	public SystemManager() {
		final OS os;
		final String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) {
			os = OS.WINDOWS;
		}
		else if(osName.contains("mac")) {
			os = OS.MAC;
		}
		else {
			os = OS.LINUX;
		}
		final Arch arch;
		final String archName = System.getProperty("os.arch");
		if(archName.contains("64")) {
			arch = Arch.X64;
		}
		else {
			arch = Arch.X86;
		}
		platform = new Platform(os, arch);
	}

	public final Platform getPlatform() {
		return platform;
	}

	public final File getSkyostDirectory() {
		return new File(getUserDirectory() + File.separator + ".skyost" + File.separator);
	}

	public final File getApplicationDirectory() {
		return new File(getSkyostDirectory() + File.separator + LauncherConstants.LAUNCHER_NAME + File.separator);
	}

	public final File getUserDirectory() {
		final String dirName;
		switch(platform.getOS()) {
		case WINDOWS:
			dirName = System.getenv("APPDATA");
			break;
		case MAC:
			dirName = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support";
			break;
		default:
			dirName = System.getProperty("user.home");
			break;
		}
		return new File(dirName);
	}

	public final File getMinecraftDirectory() {
		return new File(getUserDirectory() + File.separator + (platform.getOS() == OS.MAC ? "" : ".") + "minecraft");
	}

	public enum OS {
		WINDOWS("Windows", "windows"),
		MAC("Mac OS X", "osx"),
		LINUX("Other (Linux ?)", "linux");

		private final String name;
		private final String minecraftName;

		OS(final String name, final String minecraftName) {
			this.name = name;
			this.minecraftName = minecraftName;
		}

		public final String getName() {
			return name;
		}

		public final String getMinecraftName() {
			return minecraftName;
		}
	}

	public enum Arch {
		X86("x86"),
		X64("x64");

		private final String name;

		Arch(final String name) {
			this.name = name;
		}

		public final String getName() {
			return name;
		}
	}

	public static class Platform {

		private final OS os;
		private final Arch arch;

		public Platform(final OS os, final Arch arch) {
			this.os = os;
			this.arch = arch;
		}

		public final OS getOS() {
			return os;
		}

		public final Arch getArch() {
			return arch;
		}
	}
	
}
