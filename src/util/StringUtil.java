package util;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

import model.Transaction;

public class StringUtil {

	private final static String CRYPT_ALGO= "SHA-256";
	
	/**
	 * Encodes the given input with the <b>CRYPT_ALGO</b> algorithm and returns the hash result
	 * <br>
	 * <br>
	 * Default algorithm is SHA-256
	 * @param input - a String to encode
	 * @return String - the encoded input
	 */
	public static String encode(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance(CRYPT_ALGO);
			byte[] hash = md.digest(input.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer(); // contains hash as hexadecimal
			for(int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) sb.append('0');
				sb.append(hex);
			}
			return sb.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates the hash target for miner's to solve, based on difficulty
	 * @param difficulty - an integer to define number of 0s in target hash
	 * @return String - the hash target
	 */
	public static String generateHashTarget(int difficulty) {
		String target = new String(new char[difficulty]).replace('\0', '0');
		return target;
	}
	
}
