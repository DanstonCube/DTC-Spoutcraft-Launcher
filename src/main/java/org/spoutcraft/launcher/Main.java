/*
 * This file is part of Spoutcraft Launcher (http://wiki.getspout.org/).
 * 
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spoutcraft.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.UIManager;

import org.spoutcraft.launcher.gui.LauncherFrame;
import org.spoutcraft.launcher.gui.LoginForm;
import org.spoutcraft.launcher.logs.SystemConsoleListener;

import com.beust.jcommander.JCommander;

@SuppressWarnings("unused")
public class Main {

	static String[] args_temp;
	public static int build = -1;
	static File recursion;
	static File settingsDir;
	static File settingsFile;
	static SettingsHandler settings;

	public Main() throws Exception {
		main(new String[0]);
	}

	public static void reboot(String memory) {
		try {
			String pathToJar = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			ArrayList<String> params = new ArrayList<String>();
			if (PlatformUtils.getPlatform() == PlatformUtils.OS.windows) {
				params.add("javaw"); // Windows-specific
			} else {
				params.add("java"); // Linux/Mac/whatever
			}
			params.add(memory);
			params.add("-classpath");
			params.add(pathToJar);
			params.add("org.spoutcraft.launcher.Main");
			for (String arg : args_temp) {
				params.add(arg);
			}
			ProcessBuilder pb = new ProcessBuilder(params);
			Process process = pb.start();
			if (process == null)
				throw new Exception("!");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		try {
			new JCommander(options, args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		MinecraftUtils.setOptions(options);

		recursion = new File(PlatformUtils.getWorkingDirectory(), "rtemp");
		settingsDir = new File(PlatformUtils.getWorkingDirectory(), "spoutcraft");
		settingsFile = new File(settingsDir, "spoutcraft.properties");
		settings = new SettingsHandler("defaults/spoutcraft.properties", settingsFile);

		args_temp = args;
		boolean relaunch = false;

		try {
			if (!recursion.exists()) {
				relaunch = true;
			} else {
				recursion.delete();
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}

		if (!settingsFile.exists()) {
			settingsDir.mkdirs();
			settingsFile.createNewFile();
		}

		if (relaunch && settings.checkProperty("memory")) {
			if (settings.getPropertyInteger("memory") > 5) {
				settings.changeProperty("memory", "0");
			}
			int mem = 1 << (9 + settings.getPropertyInteger("memory"));
			recursion.createNewFile();
			reboot("-Xmx" + mem + "m");
		} 

		PlatformUtils.getWorkingDirectory().mkdirs();

		new File(PlatformUtils.getWorkingDirectory(), "spoutcraft").mkdir();

		SystemConsoleListener listener = new SystemConsoleListener();

		listener.initialize();

		System.out.println("------------------------------------------");
		System.out.println("Spoutcraft Launcher is starting....");
		System.out.println("Spoutcraft Launcher Build: " + getBuild());

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Warning: Can't get system LnF: " + e);
		}
		
		
		/* Miko */

		
		LoginForm login = new LoginForm();
		login.setVisible(true);
		
		if(options.getUser() != null && options.getPass() != null && options.getServer() != null)
		{
			login.doLogin(options.getUser(), options.getPass());		
		}
	}

	private static int getBuild() {
		if (build == -1) {
			File buildInfo = new File(PlatformUtils.getWorkingDirectory(), "launcherVersion");
			if (buildInfo.exists()) {
				try {
					BufferedReader bf = new BufferedReader(new FileReader(buildInfo));
					String version = bf.readLine();
					build = Integer.parseInt(version);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return build;
	}

}
