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
	
	/**
	 * Returns encoded string from any key
	 * @param key - Key to extract the encoded string from
	 * @return String - encoded string
	 */
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	/**
	 * Takes in the senders private key and string input, signs it using ECDSA and returns an array of bytes
	 * @param privateKey - the PrivateKey to sign with
	 * @param input - the input to sign
	 * @return byte[] - signed input
	 */
	public static byte[] applyECDSASignature(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			dsa.update(input.getBytes());
			output = dsa.sign();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	/**
	 * Takes in the signature, public key and string data and returns true or false if the signature is valid ECDSA
	 * @param publicKey - PublicKey to use in verification
	 * @param data - data to be verified
	 * @param signature - byte[] signature to be verified
	 * @return boolean - verification result
	 */
	public static boolean verifyECDSASignature(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Takes in array of transactions and returns the merkle root
	 * @param transactions - ArrayList of transactions
	 * @return String - merkle root
	 */
	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();
		ArrayList<String> prevTreeLayer = new ArrayList<String>();
		for(Transaction tran : transactions) {
			prevTreeLayer.add(tran.transactionId);
		}
		ArrayList<String> treeLayer = prevTreeLayer;
		while(count > 1) {
			treeLayer = new ArrayList<String>();
			for(int i = 1; i < prevTreeLayer.size(); i++) {
				treeLayer.add(encode(prevTreeLayer.get(i-1) + prevTreeLayer.get(i)));
			}
			count = treeLayer.size();
			prevTreeLayer = treeLayer;
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}
}
