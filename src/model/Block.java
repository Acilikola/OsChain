package model;

import java.util.ArrayList;
import java.util.Date;

import util.StringUtil;

/*
 We should replace the useless data we had in our blocks with an ArrayList of transactions. 
 However there may be 1000s of transactions in a single block, too many to include in our hash calculation… 
 but don’t worry we can use the merkle root of the transactions 
 */

public class Block {

	public String hash; // holds our digital signature
	public String prevHash; // holds previous block's hash

	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public long timestamp; // as number of milliseconds since 1/1/1970
	public int nonce;

	// Constructor
	public Block(String prevHash) {
		this.prevHash = prevHash;
		this.timestamp = new Date().getTime();

		this.hash = calculateHash();
	}

	public String calculateHash() {
		String calculatedHash = StringUtil.encode(
				Long.toString(timestamp) + 
				Integer.toString(nonce) + 
				merkleRoot);
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
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		// Create a string with difficulty * "0"s
		String target = StringUtil.generateHashTarget(difficulty);
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
	}
	
	
	public boolean addTransaction(Transaction transaction) {
		// process transaction and check if valid, unless block is genesis block then ignore
		if(transaction == null) return false;
		
		if(prevHash != "0") {
			if(transaction.processTransaction() != true) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		
		transactions.add(transaction);
		System.out.println("Transaction successfully added to Block");
		return true;
	}
}
