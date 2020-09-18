package com.samrj.devil.net;

/**
 * Symmetrical peer that performs packet fragmentation/assembly, and provides
 * reliability through acks. Used by both Server and Client. The basic idea is
 * to keep re-sending packets until they are acknowledged, or the information
 * they contain is no longer useful.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class Peer
{
}
