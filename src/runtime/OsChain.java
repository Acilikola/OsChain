package runtime;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

import model.Block;
import model.Transaction;
import model.TransactionInput;
import model.TransactionOutput;
import model.Wallet;
import util.StringUtil;

/*
 On the bitcoin network nodes share their blockchains and the longest valid chain is accepted by the network. 
 What’s to stop someone tampering with data in an old block then creating a whole new longer blockchain and presenting 
 that to the network ? Proof of work. 
 
 The hashcash proof of work system means it takes considerable time and computational power to create new blocks. 
 Hence the attacker would need more computational power than the rest of the peers combined.
 
 We will require miners to do proof-of-work by trying different variable values in the block until its hash starts with 
 a certain number of 0’s.
 
 ---UPDATE---
 update OsChain class with:
    + A Genesis transaction which release 100 OsCoins to walletA.
    + An updated chain validity check that takes transactions into account.
    + Some test transactions to see that everything is working.
*/

public class OsChain {

	public static ArrayList<Block> blockChain = new ArrayList<Block>();
	// list of all unspent transactions
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	public static final int DIFFICULTY = 5;
	public static final float MINIMUM_TRANSACTION = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
	
	public static void main(String[] args) {
		// Setup Bouncy Castle as Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		// Create wallets
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet OsCoinBase = new Wallet();
		
		// Create genesis transaction, which sends 100 OsCoin to walletA
		// manually sign, set transaction id and add TransactionOutput of genesis transaction
		genesisTransaction = new Transaction(OsCoinBase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(OsCoinBase.privateKey);
		genesisTransaction.transactionId = "0";
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value,
				genesisTransaction.transactionId));
		
		// !!important to store our genesis transaction in the UTXOs list!!
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		System.out.println("Creating and Mining Genesis Block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		// testing
		Block block1 = new Block(genesis.hash);
		System.out.println("WalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletA is attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("WalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("WalletA is attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("WalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("WalletB is attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20f));
		addBlock(block3);
		System.out.println("WalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		boolean validChain = isChainValid();
		if(validChain) printChain();
	}
	
	public static Boolean isChainValid() {
		Block curBlock;
		Block prevBlock;
		String hashTarget = StringUtil.generateHashTarget(DIFFICULTY);
		// a temporary working list of unspent transactions at a given block state
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
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
			
			// loop through blockchain transactions
			TransactionOutput tempOutput;
			for(int j = 0; j < curBlock.transactions.size(); j++) {
				Transaction curTran = curBlock.transactions.get(j);
				
				if(!curTran.verifySignature()) {
					System.out.println("Signature on Transaction(" + j + ") is invalid");
					return false;
				}
				
				if(curTran.getInputsValue() != curTran.getOutputsValue()) {
					System.out.println("Inputs are not equal to outputs on Transaction(" + j + ")");
					return false;
				}
				
				for(TransactionInput curTranInput : curTran.inputs) {
					tempOutput = tempUTXOs.get(curTranInput.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("Referenced input on Transaction(" + j + ") is missing");
						return false;
					}
					
					if(curTranInput.UTXO.value != tempOutput.value) {
						System.out.println("Referened input Transaction(" + j + ") value is invalid");
						return false;
					}
					
					tempUTXOs.remove(curTranInput.transactionOutputId);
				}
				
				for(TransactionOutput curTranOutput : curTran.outputs) {
					tempUTXOs.put(curTranOutput.id, curTranOutput);
				}
				
				if(curTran.outputs.get(0).receiver != curTran.receiver) {
					System.out.println("Transaction(" + j + ") output receiver is not who it should be");
					return false;
				}
				
				if(curTran.outputs.get(1).receiver != curTran.sender) {
					System.out.println("Transaction(" + j + ") output 'change' is not sender");
					return false;
				}
			}
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void printChain() {
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson);
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(DIFFICULTY);
		blockChain.add(newBlock);
	}
}
