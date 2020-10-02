package com.samrj.devil.net;

import java.io.IOException;

@FunctionalInterface
interface PayloadConsumer
{
    public void accept(int type, byte[] data) throws IOException;
}
