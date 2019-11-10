package model;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import runtime.OsChain;

public class Wallet {
	public PrivateKey privateKey; // used to sign; keep SECRET
	public PublicKey publicKey; // address; OK to share with others

	// only UTXOs owned by this wallet
	public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

	public Wallet() {
		generateKeyPair();
	}

	public void generateKeyPair() {
		try {
			// Elliptic Curve with ECDSA signature cryptography algo
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			// SHA1 PRNG algo
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			// Elliptic Curve (EC) generation standard
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

			keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable security level
			KeyPair keyPair = keyGen.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// Returns balance and stores the UTXO's owned by this wallet in this.UTXOs
	public float getBalance() {
		float total = 0;
		for(Map.Entry<String, TransactionOutput> entry : OsChain.UTXOs.entrySet()) {
			TransactionOutput UTXO = entry.getValue();
			if(UTXO.isMine(publicKey)) { // if output belongs to me (if coins belong to me)
				UTXOs.put(UTXO.id, UTXO); // add it to our list of unspent transactions
				total += UTXO.value;
			}
		}
		return total;
	}
	
	// Generates and returns a new transaction from this wallet
	public Transaction sendFunds(PublicKey receiver, float value) {
		if(getBalance() < value) {
			System.out.println("#Not enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for(Map.Entry<String, TransactionOutput> entry : UTXOs.entrySet()) {
			TransactionOutput UTXO = entry.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, receiver, value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input : inputs) {
			UTXOs.remove(input.transactionOutputId);
		}
		return newTransaction;
	}
}
