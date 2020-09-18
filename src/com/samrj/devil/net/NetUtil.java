package com.samrj.devil.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Utility methods used by this package.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class NetUtil
{
    static final int MAX_PACKET_SIZE = 1200;
    
    static final int CRYPT_HEADER_KEEPALIVE = 0;
    static final int CRYPT_HEADER_FINALIZE = 1;
    static final int CRYPT_HEADER_MESSAGE = 2;
    
    static final byte[]  FINALIZE = bytes("Finalize me plz.");
    static final byte[] KEEPALIVE = bytes("Plz no time out.");
    
    static byte[] bytes(String string)
    {
        if (string == null) return new byte[0];
        return string.getBytes(StandardCharsets.UTF_8);
    }
    
    static String string(byte[] bytes)
    {
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    private static byte[] counterIV(long counter)
    {
        return new byte[]{
                (byte)(counter >> 56),
                (byte)(counter >> 48),
                (byte)(counter >> 40),
                (byte)(counter >> 32),
                (byte)(counter >> 24),
                (byte)(counter >> 16),
                (byte)(counter >>  8),
                (byte)(counter      ), 0, 0, 0, 0};
    }
    
    static byte[] decrypt(ByteBuffer buffer, int[] header, Cipher cipher, SecretKey key, Sequence sequence) throws IOException
    {
        try
        {
            header[0] = Byte.toUnsignedInt(buffer.get());
            long sequenceNumber = buffer.getLong();
            byte[] iv = counterIV(sequenceNumber);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] decrypted = cipher.doFinal(ciphertext);

            //Check sequence only after successful decryption.
            if (!sequence.addIncoming(sequenceNumber)) throw new Exception("Duplicate or old packet.");
            return decrypted;
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }
    
    static void encrypt(byte[] plaintext, int header, ByteBuffer buffer, Cipher cipher, SecretKey key, Sequence sequence) throws IOException
    {
        try
        {
            long sequenceNumber = sequence.incrementOutgoing();
            byte[] ivArr = counterIV(sequenceNumber);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, ivArr));
            byte[] ciphertext = cipher.doFinal(plaintext);

            buffer.put((byte)header);
            buffer.putLong(sequenceNumber);
            buffer.put(ciphertext);
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }
    
    static void flipAndBufferChecksum(ByteBuffer buffer)
    {
        buffer.flip();
        buffer.position(4);
        CRC32 crc32 = new CRC32();
        crc32.update(buffer);
        buffer.putInt(0, (int)crc32.getValue());
        buffer.position(0);
    }
    
    static void checksumAndType(ByteBuffer buffer, int expectedType) throws IOException
    {
        int checksum = buffer.getInt();
        CRC32 crc32 = new CRC32();
        crc32.update(buffer);
        if (checksum != (int)crc32.getValue()) throw new IOException("Failed checksum");
        buffer.position(4);
        if (Byte.toUnsignedInt(buffer.get()) != expectedType) throw new IOException("Unexpected type.");
    }
}
