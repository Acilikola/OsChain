package model;

import java.security.PublicKey;

import util.StringUtil;

/**
 * Transaction outputs will show the final amount sent to each party from the transaction.
 * These, when referenced as inputs in new transactions, act as proof that you have coins to send.
 * 
 * @author Osman Can Ornek
 *
 */
public class TransactionOutput {

	public String id;
	public PublicKey receiver; // also known as the new owner of these coins
	public float value; // amount of coins they own
	public String parentTransactionId; // id of the transaction this output was created in
	
	public TransactionOutput(PublicKey receiver, float value, String parentTransactionId) {
		this.receiver = receiver;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		
		this.id = StringUtil.encode(
				StringUtil.getStringFromKey(receiver) + 
				Float.toString(value) + 
				parentTransactionId
				);
	}
	
	// checks if coin belongs to you
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == receiver);
	}
}
