package runtime;

import java.util.ArrayList;

import com.google.gson.GsonBuilder;

import model.Block;
import util.StringUtil;

/*
 On the bitcoin network nodes share their blockchains and the longest valid chain is accepted by the network. 
 What’s to stop someone tampering with data in an old block then creating a whole new longer blockchain and presenting 
 that to the network ? Proof of work. 
 
 The hashcash proof of work system means it takes considerable time and computational power to create new blocks. 
 Hence the attacker would need more computational power than the rest of the peers combined.
 
 We will require miners to do proof-of-work by trying different variable values in the block until its hash starts with 
 a certain number of 0’s.
*/

public class OsChain {

	public static ArrayList<Block> blockChain = new ArrayList<Block>();
	public static final int DIFFICULTY = 5;
	
	public static void main(String[] args) {
		
		blockChain.add(new Block("Hi im the first block", "0"));
		System.out.println("Trying to Mine block 1... ");
		blockChain.get(0).mineBlock(DIFFICULTY);
		
		blockChain.add(new Block("Yo im the second block",blockChain.get(blockChain.size()-1).hash));
		System.out.println("Trying to Mine block 2... ");
		blockChain.get(1).mineBlock(DIFFICULTY);
		
		blockChain.add(new Block("Hey im the third block",blockChain.get(blockChain.size()-1).hash));
		System.out.println("Trying to Mine block 3... ");
		blockChain.get(2).mineBlock(DIFFICULTY);
		
		System.out.println("\nBlockchain is Valid: " + isChainValid());
		
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson);
	}
	
	public static Boolean isChainValid() {
		Block curBlock;
		Block prevBlock;
		String hashTarget = StringUtil.generateHashTarget(DIFFICULTY);
		
		for(int i = 1; i < blockChain.size(); i++) {
			curBlock = blockChain.get(i);
			prevBlock = blockChain.get(i-1);
			
			// compare registered hash vs calculated hash
			if(!curBlock.hash.equals(curBlock.calculateHash())) {
				System.out.println("Current Hashes not equal");
				return false;
			}
			
			// compare previous hash vs registered previous hash
			if(!prevBlock.hash.equals(curBlock.prevHash)) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			
			// check if hash is solved
			if(!curBlock.hash.substring(0, DIFFICULTY).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}
		}
		return true;
	}
}
