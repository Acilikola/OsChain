package model;

import java.security.*;
import java.util.ArrayList;

import runtime.OsChain;
import util.StringUtil;

public class Transaction {

	public String transactionId; // also the hash of the transaction
	public PublicKey sender; // sender's public key/address
	public PublicKey receiver; // receiver's public key/address
	public float value;
	public byte[] signature; // to prevent others from spending funds in our wallet
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; // rough count of how many transactions have been generated
	
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.receiver = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	private String calculateHash() {
		sequence++; // increase the sequence to avoid two identical transactions having the same hash
		String calculatedHash =  StringUtil.encode(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(receiver) + 
				Float.toString(value) +
				sequence
				);
		return calculatedHash;
	}
	
	// signs all the data we don't want tampered with
	public void generateSignature(PrivateKey privateKey) {
		String transactionData = StringUtil.getStringFromKey(sender) + 
				StringUtil.getStringFromKey(receiver) +
				Float.toString(value);
		signature = StringUtil.applyECDSASignature(privateKey, transactionData);
	}
	
	// verifies that the data we signed has not been tampered with
	public boolean verifySignature() {
		String transactionData = StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(receiver) + 
				Float.toString(value);
		return StringUtil.verifyECDSASignature(sender, transactionData, signature);
	}
	
	// returns sum of inputs(UTXOs) values
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; // if Transaction can't be found skip it
			total += i.UTXO.value;
		}
		return total;
	}
	
	// returns sum of outputs
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	// returns true if new transaction could be created
	public boolean processTransaction() {
		if(verifySignature() == false) {
			System.out.println("#Transaction signature failed to verify");
			return false;
		}
		
		// gather transaction inputs (make sure they are unspent)
		for(TransactionInput i : inputs) {
			i.UTXO = OsChain.UTXOs.get(i.transactionOutputId);
		}
		
		// check if transaction is valid
		if(getInputsValue() < OsChain.MINIMUM_TRANSACTION) {
			System.out.println("#Transaction inputs to small: " + getInputsValue());
			return false;
		}
		
		// generate transaction outputs
		float leftOver = getInputsValue() - value;
		transactionId = calculateHash();
		outputs.add(new TransactionOutput(this.receiver, value, transactionId));
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
		
		// add outputs to Unspent list
		for(TransactionOutput o : outputs) {
			OsChain.UTXOs.put(o.id, o);
		}
		
		// remove transaction inputs from UTXO lists as spent
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; // if Transaction can't be found skip it
			OsChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
}
