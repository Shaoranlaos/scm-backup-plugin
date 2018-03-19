package de.shaoranlaos.scm_backup_plugin;

import sonia.scm.security.CipherHandler;

final class CipherUtil {

	private static CipherHandler cipher;
	
	public static void setCipherHandler(CipherHandler cipher) {
		CipherUtil.cipher = cipher;
	}

	public static String decode(String encodedString) {
		if (encodedString.isEmpty()) {
			return "";
		} else {
			return cipher.decode(encodedString);
		}
	}

	public static String encode(String decodedString) {
		if (decodedString.isEmpty()) {
			return "";
		} else {
			return cipher.encode(decodedString);
		}
	}
}
