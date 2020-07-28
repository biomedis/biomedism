/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.biomedis.biomedismair3.utils.OS;

public class OSValidator {
 
	private static String OS = System.getProperty("os.name").toLowerCase();
 
	public static String getOS()
        {
 
		
 
		if (isWindows()) {
			return "win";
		} else if (isMac()) {
			return "mac";
		} else if (isUnix()) {
			return "nix";
		} else if (isSolaris()) {
			return "sunos";
		} else {
			return "other";
		}
	}
 
	public static boolean isWindows() {
 
		return (OS.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		return (OS.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
 
	public static boolean isSolaris() {
 
		return (OS.indexOf("sunos") >= 0);
 
	}

	public static String osAlt(){
		if(isMac()) return "OS X";
		else if(isWindows()) return "Windows";
		else return "Linux";
	}
 
}
