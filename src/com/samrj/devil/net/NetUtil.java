package com.samrj.devil.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.CRC32;

/**
 * Utility methods used by this package.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class NetUtil
{
    static final SecureRandom getCSPRNG()
    {
        return new SecureRandom();
    }
    
    static final MessageDigest getDigest()
    {
        try
        {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    static final int MAX_PACKET_SIZE = 1200;
    
    static byte[] bytes(String string)
    {
        if (string == null) return new byte[0];
        return string.getBytes(StandardCharsets.UTF_8);
    }
    
    static String string(byte[] bytes)
    {
        return new String(bytes, StandardCharsets.UTF_8);
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
    
    static boolean failedChecksum(ByteBuffer buffer)
    {
        int checksum = buffer.getInt();
        CRC32 crc32 = new CRC32();
        crc32.update(buffer);
        buffer.position(4);
        return checksum != (int)crc32.getValue();
    }
}
