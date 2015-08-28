import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class AsymAlgo {
	
	public AsymAlgo() {}
	
	private Base64.Decoder decoder = Base64.getDecoder();
	private Base64.Encoder encoder = Base64.getEncoder();
	
	private PublicKey publicKey = null;
	private PrivateKey privateKey = null;

    private byte[] 	encryptedBytes = null,
    				decryptedBytes = null;
    
    protected void getKey(){
        try{
        	System.out.print("\nSetting up RSA encryption key");
        	
        	File f = new File("private_key.der");
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] bKey = new byte[(int)f.length()];
            dis.readFully(bKey);
            dis.close();
            
        //	byte[] bKey = decoder.decode(bKey);
            PKCS8EncodedKeySpec PKCS8privateKey = new PKCS8EncodedKeySpec(bKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(PKCS8privateKey);
            
        	System.out.println("...done.\n");
        //	System.out.println(encoder.encodeToString(privateKey.getEncoded()));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    protected void generateKeys() {

	    // Generate key pair for 1024-bit RSA encryption
    	System.out.print("Generating RSA keypair\n");
	    try {
	        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	        kpg.initialize(1024);
	        KeyPair kp = kpg.genKeyPair();
	        publicKey = kp.getPublic();
	        privateKey = kp.getPrivate();
	    //	System.out.print("PUBLIC KEY: " + publicKey.toString() + "\n");
	    //	System.out.print("PRIVATE KEY: " + privateKey.toString() + "\n");
	    } catch (Exception e) {
	    	System.out.print("RSA keypair error\n");
	    }
    }

    protected byte[] encrypt(byte[] data){
	    try {
	        Cipher c = Cipher.getInstance("RSA/ECB/NoPadding");
	        c.init(Cipher.ENCRYPT_MODE, publicKey);
	        encryptedBytes = c.doFinal(data);
	    } catch (Exception e) {
	    	System.out.print("RSA encryption error\n");
	        e.printStackTrace();
	    }
	    return encryptedBytes;
    }
    
    protected byte[] decrypt(byte[] data){
	    try {
	        Cipher c = Cipher.getInstance("RSA/ECB/NoPadding");
	        c.init(Cipher.DECRYPT_MODE, privateKey);
	        decryptedBytes = c.doFinal(data);
	    } catch (Exception e) {
	    	System.out.print("RSA decryption error\n");
	        e.printStackTrace();
	    }
	    return decryptedBytes;
    }
}