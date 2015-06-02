package com.sasaug.shadowchat.security;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.sasaug.shadowchat.utils.SHA256;

public class Key {

	private PublicKey publicKey = null;
	private PrivateKey privateKey = null;
	private byte[] secret = null;
	private byte[] hash = null;
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	public byte[] getSecret(){
		if(secret == null){
			try{
				secret = SecurityCore.getInstance().getAgreementSecret(privateKey, publicKey);
			}catch(Exception ex){ex.printStackTrace();}
		}
		return secret;
	}
	
	public byte[] getHash(){
		if(hash == null){
			try{
				hash = SHA256.hash(getSecret());
			}catch(Exception ex){}
		}
		return hash;
	}

}
