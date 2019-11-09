package model;

import java.util.Date;

import util.StringUtil;

public class Block {

	public String hash; // holds our digital signature
	public String prevHash; // holds previous block's hash

	private String data; // data is a simple message for this example project
	private long timestamp; // as number of milliseconds since 1/1/1970
	private int nonce;

	// Constructor
	public Block(String data, String prevHash) {
		this.data = data;
		this.prevHash = prevHash;
		this.timestamp = new Date().getTime();

		this.hash = calculateHash();
	}

	public String calculateHash() {
		String calculatedHash = StringUtil.encode(
				prevHash + 
				Long.toString(timestamp) + 
				Integer.toString(nonce) + 
				data);
		return calculatedHash;
	}
	
	/**
	 * require miners to do proof-of-work by trying different variable values in the block until its hash starts with
	 * a certain number of 0's, identified by <b>difficulty</b> param
	 * <br>
	 * <br>
	 * Low difficulty like 1 or 2 can be solved nearly instantly; 4-6 works nice for testing; actual blockchain
	 * currencies have much larger numbers (Litecoin -> 442592)
	 * 
	 * @param difficulty
	 */
	public void mineBlock(int difficulty) {
		// Create a string with difficulty * "0"s
		String target = StringUtil.generateHashTarget(difficulty);
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
	}
}
