package com.sasaug.shadowchat.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.sasaug.shadowchat.security.modules._Dummy;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.utils.Base64;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.Log;

public class SecurityCore {
	public final static String ID = "Security";
	final static String TAG = "SecurityCore";

	SecureRandom random;
	ArrayList<ISecurity> modules = new ArrayList<ISecurity>();

	private static SecurityCore instance;
	public static SecurityCore getInstance(){
		if(instance == null)
			instance = new SecurityCore();
		return instance;
	}

	public SecurityCore(){
		random = new SecureRandom();
		loadModules();
		instance = this;
	}
	
	public void loadModules(){
		modules.clear();
		
		try{
			ArrayList<Class<?>> list = ClassEnumerator.getClassesForPackage(_Dummy.class.getPackage());
			for(int i = 0; i < list.size(); i++){
				if(!list.get(i).getSimpleName().equals(_Dummy.class.getSimpleName())){
					try {
						ISecurity module = (ISecurity)list.get(i).newInstance();
						modules.add(module);
						module.onInit(this);
						Log.write(TAG, "Loaded module '" + list.get(i).getSimpleName() + "'");
					} catch (InstantiationException e) {
						Log.error(TAG, "Failed to initialise module '" + list.get(i).getSimpleName() +"'.");
					} catch (IllegalAccessException e) {
						Log.error(TAG, "Failed to access module '" + list.get(i).getSimpleName() +"'.");
					}
				}
			}
			Log.write(TAG, "Finish loading modules. " + modules.size() + " loaded.");
		}catch(Exception ex){
			ex.printStackTrace();
			Log.error(TAG, "Error loading modules. Modules not loaded.");
		}		
	}
	
	public String randomPassword(){
		return new BigInteger(130, random).toString(32);
	}
	
	public Key generateKey(TCPClient client) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException{
		KeyPair pair = generateECDHKey();
		Key key = new Key();
		key.setPublicKey(pair.getPublic());
		key.setPrivateKey(pair.getPrivate());
		client.setTag("key", key);
		return key;
	}
	
	private KeyPair generateECDHKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException{
		ECGenParameterSpec spec = new ECGenParameterSpec("secp224r1");
		Provider BC = new BouncyCastleProvider();
	    KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", BC);
	    g.initialize(spec, random);
	    KeyPair pair = g.generateKeyPair();
	    return pair;
	}
	
	public byte[] getAgreementSecret(PrivateKey myKey, PublicKey theirKey) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException{
		Provider BC = new BouncyCastleProvider();
		KeyAgreement agreement = KeyAgreement.getInstance("ECDH", BC);
		agreement.init(myKey);
		agreement.doPhase(theirKey, true);
		byte[] secret = agreement.generateSecret();
		return secret;
	}
	
	public PublicKey getECDHPublicKey(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		Provider BC = new BouncyCastleProvider();
		PublicKey publicKey = KeyFactory.getInstance("ECDH", BC).generatePublic(new X509EncodedKeySpec(data));
		return publicKey;
	}
	
	public PrivateKey getECDHPrivateKey(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
		Provider BC = new BouncyCastleProvider();
		PrivateKey key = KeyFactory.getInstance("ECDH", BC).generatePrivate(new PKCS8EncodedKeySpec(data));
		return key;
	}
	
	public byte[] encrypt(byte[] data, String password, byte[] salt) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{   
	    byte[] saltBytes = salt;
	    byte[] ivBytes = random.generateSeed(16);
	 
	    // Derive the key, given password and salt.
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 10, 128);
	 
	    SecretKey secretKey = factory.generateSecret(spec);
	    SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
	     
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(ivBytes));
	 
	    byte[] encrypted = cipher.doFinal(data);

	    ByteBuffer bb = ByteBuffer.allocate(encrypted.length + 16);
	    bb.put(ivBytes);
	    bb.put(encrypted);
	    return bb.array();
	}
	
	public byte[] decrypt(byte[] data, String password, byte[] salt) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		if(data.length <= 16)
			return data;
		
	    byte[] saltBytes = salt;
	    byte[] ivBytes = new byte[16];
	    byte[] encryptedTextBytes = new byte[data.length-16];

	    
	    ByteBuffer bb = ByteBuffer.wrap(data);
	    bb.get(ivBytes, 0, 16);
	    bb.get(encryptedTextBytes, 0, data.length-16);
	    
	    // Derive the key, given password and salt.
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	    PBEKeySpec spec = new PBEKeySpec( password.toCharArray(), saltBytes, 10, 128);
	 
	    SecretKey secretKey = factory.generateSecret(spec);
	    SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
	 
	    // Decrypt the message, given derived key and initialization vector.
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
	 
	    byte[] decryptedTextBytes = null;
	    decryptedTextBytes = cipher.doFinal(encryptedTextBytes);

	    return decryptedTextBytes;
	}
	
	public KeyPair generateRSA1024() throws NoSuchAlgorithmException{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024);
		return kpg.genKeyPair();
	}
	
	public byte[] encryptRSA(byte[] data, byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey pubKey = keyFactory.generatePublic(spec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		return cipher.doFinal(data);
	}
	
	public byte[] decryptRSA(byte[] data, byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(privateKey);
		PrivateKey privKey = keyFactory.generatePrivate(spec2);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		return cipher.doFinal(data);
	}
	
	public KeyPair generateECDSAKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException{
		Provider BC = new BouncyCastleProvider();
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", BC);
	    ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
	    keyGen.initialize(ecSpec, random);
	    return keyGen.generateKeyPair();
	}
	
	public byte[] signContent(byte[] privateKey, byte[] content) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, InvalidKeySpecException{
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(privateKey);
		PrivateKey key = keyFactory.generatePrivate(spec2);
		Provider BC = new BouncyCastleProvider();
	    Signature signature = Signature.getInstance("ECDSA", BC);
	    signature.initSign(key, random);
	    signature.update(content);
	    return signature.sign();
	}
	
	public boolean verifyContent(byte[] publicKey, byte[] content, byte[] sig) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException{
		Provider BC = new BouncyCastleProvider();
		PublicKey key = KeyFactory.getInstance("ECDSA", BC).generatePublic(new X509EncodedKeySpec(publicKey));
		Signature signature = Signature.getInstance("ECDSA", BC);
		signature.initVerify(key);
		signature.update(content);
		return signature.verify(Base64.decode(sig, Base64.DEFAULT));
	}
}
