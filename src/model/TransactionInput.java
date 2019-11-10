package model;

/**
 * This class will be used to reference TransactionOutputs that have not yet been spent. 
 * The transactionOutputId will be used to find the relevant TransactionOutput, allowing miners to check your ownership
 * @author Osman Can Ornek
 *
 */
public class TransactionInput {

	public String transactionOutputId; // Reference to TransactionOutputs -> transactionId
	public TransactionOutput UTXO; // contains the Unspent Transaction Output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
