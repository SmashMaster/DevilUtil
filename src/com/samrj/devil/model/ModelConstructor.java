package com.samrj.devil.model;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
interface ModelConstructor<T>
{
    T construct(Model model, DataInputStream in) throws IOException;
}