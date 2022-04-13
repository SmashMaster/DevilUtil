/*
 * Copyright (c) 2022 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A general NIO serialization class. Can encode fields of various types. Supports all Java primitives, Strings, arrays,
 * and nested structs.
 *
 * @author Samuel Johnson (SmashMaster)
 */
public final class Struct<T extends Enum<T>> implements Bufferable
{
    private enum Type
    {
        BYTE("byte"), BYTE_ARRAY("byte[]"), SHORT("short"), SHORT_ARRAY("short[]"),
        INT("int"), INT_ARRAY("int[]"), LONG("long"), LONG_ARRAY("long[]"),
        FLOAT("float"), FLOAT_ARRAY("float[]"), DOUBLE("double"), DOUBLE_ARRAY("double[]"),
        BOOLEAN("boolean"), BOOLEAN_ARRAY("boolean[]"), CHAR("char"), CHAR_ARRAY("char[]"),
        STRING("String"), STRING_ARRAY("String[]"), STRUCT("Struct"), STRUCT_ARRAY("Struct[]");

        private final String java;

        private Type(String java)
        {
            this.java = java;
        }
    }

    private static class Field
    {
        private Type type;
        private Object value;

        private Field(Type type, Object value)
        {
            this.type = type;
            this.value = value;
        }
    }

    @FunctionalInterface
    private interface NameReader
    {
        int read(ByteBuffer buffer);
    }

    @FunctionalInterface
    private interface NameWriter
    {
        void write(ByteBuffer buffer, int name);
    }

    private static int stringSize(String string)
    {
        return 4 + string.getBytes(StandardCharsets.UTF_8).length;
    }

    private final Class<T> nameEnum;
    private final T[] names;
    private final Map<T, Field> fields;
    private final int nameSize;
    private final NameReader nameReader;
    private final NameWriter nameWriter;

    public Struct(Class<T> nameEnum)
    {
        this.nameEnum = nameEnum;
        if (!nameEnum.isEnum()) throw new IllegalArgumentException("Expected enum.");

        names = nameEnum.getEnumConstants();

        //Save space by writing the name as a byte, short, or int; depending on how many names there are.
        if (names.length <= 0xFF) //Can write names as bytes.
        {
            fields = new EnumMap<>(nameEnum);
            nameSize = 1;
            nameReader = b -> b.get() & 0xFF;
            nameWriter = (b, i) -> b.put((byte)i);
        }
        else if (names.length <= 0xFFFF) //Can write names as shorts.
        {
            fields = new HashMap<>(); //If there are lots of names, EnumMap takes up a lot of memory.
            nameSize = 2;
            nameReader = b -> b.getShort() & 0xFFFF;
            nameWriter = (b, i) -> b.putShort((short)i);
        }
        else //Write names as ints. Shouldn't ever really happen...
        {
            fields = new HashMap<>(); //If there are lots of names, EnumMap takes up a lot of memory.
            nameSize = 4;
            nameReader = ByteBuffer::getInt;
            nameWriter = ByteBuffer::putInt;
        }

        //Idea: Could bitpack type and name together to save space, since there are only 20 types, which fits in 5 bits.
        //Idea: Could multiply name by type count, and add type ordinal. To separate them, use division and modulus.
    }

    public Struct(Class<T> nameEnum, ByteBuffer buffer)
    {
        this(nameEnum);
        read(buffer);
    }

    @Override
    public void read(ByteBuffer buffer)
    {
        int fieldCount = buffer.getInt();

        for (int fi=0; fi<fieldCount; fi++)
        {
            int name = nameReader.read(buffer);
            Type type = Type.values()[buffer.get() & 0xFF];

            Object value;

            switch (type) //Write value.
            {
                case BYTE: value = buffer.get(); break;
                case BYTE_ARRAY:
                {
                    int length = buffer.getInt();
                    byte[] array = new byte[length];
                    buffer.get(array);
                    value = array;
                    break;
                }
                case SHORT: value = buffer.getShort(); break;
                case SHORT_ARRAY:
                {
                    int length = buffer.getInt();
                    short[] array = new short[length];
                    for (int i=0; i<length; i++) array[i] = buffer.getShort();
                    value = array;
                    break;
                }
                case INT: value = buffer.getInt(); break;
                case INT_ARRAY:
                {
                    int length = buffer.getInt();
                    int[] array = new int[length];
                    for (int i=0; i<length; i++) array[i] = buffer.getInt();
                    value = array;
                    break;
                }
                case LONG: value = buffer.getLong(); break;
                case LONG_ARRAY:
                {
                    int length = buffer.getInt();
                    long[] array = new long[length];
                    for (int i=0; i<length; i++) array[i] = buffer.getLong();
                    value = array;
                    break;
                }
                case FLOAT: value = buffer.getFloat(); break;
                case FLOAT_ARRAY:
                {
                    int length = buffer.getInt();
                    float[] array = new float[length];
                    for (int i=0; i<length; i++) array[i] = buffer.getFloat();
                    value = array;
                    break;
                }
                case DOUBLE: value = buffer.getDouble(); break;
                case DOUBLE_ARRAY:
                {
                    int length = buffer.getInt();
                    double[] array = new double[length];
                    for (int i=0; i<length; i++) array[i] = buffer.getDouble();
                    value = array;
                    break;
                }
                case BOOLEAN: value = (buffer.get() != 0); break;
                case BOOLEAN_ARRAY:
                {
                    int numBits = buffer.getInt();
                    int numBytes = (numBits + 7)/8;
                    byte[] bytes = new byte[numBytes];
                    buffer.get(bytes);
                    BitSet bits = BitSet.valueOf(bytes);
                    boolean[] array = new boolean[numBits];
                    for (int i=0; i<numBits; i++) array[i] = bits.get(i);
                    value = array;
                    break;
                }
                case CHAR: value = buffer.getChar(); break;
                case CHAR_ARRAY:
                {
                    int length = buffer.getInt();
                    char[] array = new char[length];
                    for (int i=0; i<length; i++) array[i] = buffer.getChar();
                    value = array;
                    break;
                }
                case STRING: value = IOUtil.readUTF8(buffer); break;
                case STRING_ARRAY:
                {
                    int length = buffer.getInt();
                    String[] array = new String[length];
                    for (int i=0; i<length; i++) array[i] = IOUtil.readUTF8(buffer);
                    value = array;
                    break;
                }
                case STRUCT:
                {
                    Struct struct = new Struct(nameEnum);
                    struct.read(buffer);
                    value = struct;
                    break;
                }
                case STRUCT_ARRAY:
                {
                    int length = buffer.getInt();
                    Struct[] array = new Struct[length];
                    for (int i=0; i<length; i++)
                    {
                        Struct struct = new Struct(nameEnum);
                        struct.read(buffer);
                        array[i] = struct;
                    }
                    value = array;
                    break;
                }
                default: throw new RuntimeException("Unknown type: " + type);
            }

            fields.put(names[name], new Field(type, value));
        }
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        int fieldCount = fields.size();
        buffer.putInt(fieldCount);

        for (Map.Entry<T, Field> entry : fields.entrySet())
        {
            T name = entry.getKey();
            Field field = entry.getValue();
            Type type = field.type;
            Object value = field.value;

            nameWriter.write(buffer, name.ordinal()); //Write name.
            buffer.put((byte)type.ordinal()); //Write type.
            switch (type) //Write value.
            {
                case BYTE: buffer.put((byte)value); break;
                case BYTE_ARRAY:
                {
                    byte[] array = (byte[])value;
                    buffer.putInt(array.length);
                    buffer.put(array);
                    break;
                }
                case SHORT: buffer.putShort((short)value); break;
                case SHORT_ARRAY:
                {
                    short[] array = (short[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) buffer.putShort(array[i]);
                    break;
                }
                case INT: buffer.putInt((int)value); break;
                case INT_ARRAY:
                {
                    int[] array = (int[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) buffer.putInt(array[i]);
                    break;
                }
                case LONG: buffer.putLong((long)value); break;
                case LONG_ARRAY:
                {
                    long[] array = (long[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) buffer.putLong(array[i]);
                    break;
                }
                case FLOAT: buffer.putFloat((float)value); break;
                case FLOAT_ARRAY:
                {
                    float[] array = (float[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) buffer.putFloat(array[i]);
                    break;
                }
                case DOUBLE: buffer.putDouble((double)value); break;
                case DOUBLE_ARRAY:
                {
                    double[] array = (double[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) buffer.putDouble(array[i]);
                    break;
                }
                case BOOLEAN: buffer.put(((boolean)value) ? (byte)1 : (byte)0); break;
                case BOOLEAN_ARRAY:
                {
                    boolean[] array = (boolean[])value;
                    int byteCount = (array.length + 7)/8;
                    buffer.putInt(array.length);
                    BitSet bits = new BitSet(array.length);
                    for (int i=0; i<array.length; i++) if (array[i]) bits.set(i);
                    byte[] bytes = bits.toByteArray();
                    buffer.put(bytes);
                    for (int i=bytes.length; i<byteCount; i++) buffer.put((byte)0); //Must pad since BitSet chops off trailing zeroes.
                    break;
                }
                case CHAR: buffer.putChar((char)value); break;
                case CHAR_ARRAY:
                {
                    char[] array = (char[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) buffer.putChar(array[i]);
                    break;
                }
                case STRING: IOUtil.writeUTF8(buffer, (String)value); break;
                case STRING_ARRAY:
                {
                    String[] array = (String[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) IOUtil.writeUTF8(buffer, (String)array[i]);
                    break;
                }
                case STRUCT: ((Struct)value).write(buffer); break;
                case STRUCT_ARRAY:
                {
                    Struct[] array = (Struct[])value;
                    buffer.putInt(array.length);
                    for (int i=0; i<array.length; i++) ((Struct)array[i]).write(buffer);
                    break;
                }
            }
        }
    }

    @Override
    public int bufferSize()
    {
        int size = 4; //Field count

        for (Map.Entry<T, Field> entry : fields.entrySet())
        {
            T name = entry.getKey();
            Field field = entry.getValue();
            Type type = field.type;
            Object value = field.value;

            size += nameSize; //Name
            size++; //Type
            switch (type)
            {
                case BYTE: size++; break;
                case BYTE_ARRAY:
                {
                    byte[] array = (byte[])value;
                    size += 4 + array.length;
                    break;
                }
                case SHORT: size += 2; break;
                case SHORT_ARRAY:
                {
                    short[] array = (short[])value;
                    size += 4 + array.length*2;
                    break;
                }
                case INT: size += 4; break;
                case INT_ARRAY:
                {
                    int[] array = (int[])value;
                    size += 4 + array.length*4;
                    break;
                }
                case LONG: size += 8; break;
                case LONG_ARRAY:
                {
                    long[] array = (long[])value;
                    size += 4 + array.length*8;
                    break;
                }
                case FLOAT: size += 4; break;
                case FLOAT_ARRAY:
                {
                    float[] array = (float[])value;
                    size += 4 + array.length*4;
                    break;
                }
                case DOUBLE: size += 8; break;
                case DOUBLE_ARRAY:
                {
                    double[] array = (double[])value;
                    size += 4 + array.length*8;
                    break;
                }
                case BOOLEAN: size++; break;
                case BOOLEAN_ARRAY:
                {
                    boolean[] array = (boolean[])value;
                    int bytes = (array.length + 7)/8;
                    size += 4 + bytes;
                    break;
                }
                case CHAR: size += 2; break;
                case CHAR_ARRAY:
                {
                    char[] array = (char[])value;
                    size += 4 + array.length*2;
                    break;
                }
                case STRING: size += stringSize((String)value); break;
                case STRING_ARRAY:
                {
                    String[] array = (String[])value;
                    size += 4;
                    for (int i=0; i<array.length; i++) size += stringSize(array[i]);
                    break;
                }
                case STRUCT: size += ((Struct)value).bufferSize(); break;
                case STRUCT_ARRAY:
                {
                    Struct[] array = (Struct[])value;
                    size += 4;
                    for (int i=0; i<array.length; i++) size += ((Struct)array[i]).bufferSize();
                    break;
                }
            }
        }

        return size;
    }

    public boolean contains(T name)
    {
        return fields.containsKey(name);
    }

    public <TYPE> TYPE get(T name, TYPE defaultValue)
    {
        Field field = fields.get(name);
        Class<?> cls = defaultValue.getClass();
        if (field == null || !cls.isInstance(field.value)) return defaultValue;
        return (TYPE)field.value;
    }

    public <TYPE> TYPE get(T name, Class<TYPE> cls)
    {
        Field field = fields.get(name);
        if (field == null || !cls.isInstance(field.value)) return null;
        return (TYPE)field.value;
    }

    public Object get(T name)
    {
        Field field = fields.get(name);
        if (field == null) return null;
        return field.value;
    }

    private Field require(T name)
    {
        Field field = fields.get(name);
        if (field == null) throw new NoSuchElementException();
        return field;
    }

    public Struct putByte(T name, byte value)
    {
        fields.put(name, new Field(Type.BYTE, value));
        return this;
    }

    public byte requireByte(T name)
    {
        return (byte)require(name).value;
    }

    public Struct putByteArray(T name, byte[] value)
    {
        fields.put(name, new Field(Type.BYTE_ARRAY, value));
        return this;
    }

    public byte[] requireByteArray(T name)
    {
        return (byte[])require(name).value;
    }

    public Struct putShort(T name, short value)
    {
        fields.put(name, new Field(Type.SHORT, value));
        return this;
    }

    public short requireShort(T name)
    {
        return (short)require(name).value;
    }

    public Struct putShortArray(T name, short[] value)
    {
        fields.put(name, new Field(Type.SHORT_ARRAY, value));
        return this;
    }

    public short[] requireShortArray(T name)
    {
        return (short[])require(name).value;
    }

    public Struct putInt(T name, int value)
    {
        fields.put(name, new Field(Type.INT, value));
        return this;
    }

    public int requireInt(T name)
    {
        return (int)require(name).value;
    }

    public Struct putIntArray(T name, int[] value)
    {
        fields.put(name, new Field(Type.INT_ARRAY, value));
        return this;
    }

    public int[] requireIntArray(T name)
    {
        return (int[])require(name).value;
    }

    public Struct putLong(T name, long value)
    {
        fields.put(name, new Field(Type.LONG, value));
        return this;
    }

    public long requireLong(T name)
    {
        return (byte)require(name).value;
    }

    public Struct putLongArray(T name, long[] value)
    {
        fields.put(name, new Field(Type.LONG_ARRAY, value));
        return this;
    }

    public long[] requireLongArray(T name)
    {
        return (long[])require(name).value;
    }

    public Struct putFloat(T name, float value)
    {
        fields.put(name, new Field(Type.FLOAT, value));
        return this;
    }

    public float requireFloat(T name)
    {
        return (float)require(name).value;
    }

    public Struct putFloatArray(T name, float[] value)
    {
        fields.put(name, new Field(Type.FLOAT_ARRAY, value));
        return this;
    }

    public float[] requireFloatArray(T name)
    {
        return (float[])require(name).value;
    }

    public Struct putDouble(T name, double value)
    {
        fields.put(name, new Field(Type.DOUBLE, value));
        return this;
    }

    public double requireDouble(T name)
    {
        return (double)require(name).value;
    }

    public Struct putDoubleArray(T name, double[] value)
    {
        fields.put(name, new Field(Type.DOUBLE_ARRAY, value));
        return this;
    }

    public double[] requireDoubleArray(T name)
    {
        return (double[])require(name).value;
    }

    public Struct putBoolean(T name, boolean value)
    {
        fields.put(name, new Field(Type.BOOLEAN, value));
        return this;
    }

    public boolean requireBoolean(T name)
    {
        return (boolean)require(name).value;
    }

    public Struct putBooleanArray(T name, boolean[] value)
    {
        fields.put(name, new Field(Type.BOOLEAN_ARRAY, value));
        return this;
    }

    public boolean[] requireBooleanArray(T name)
    {
        return (boolean[])require(name).value;
    }

    public Struct putChar(T name, char value)
    {
        fields.put(name, new Field(Type.CHAR, value));
        return this;
    }

    public char requireChar(T name)
    {
        return (char)require(name).value;
    }

    public Struct putCharArray(T name, char[] value)
    {
        fields.put(name, new Field(Type.CHAR_ARRAY, value));
        return this;
    }

    public char[] requireCharArray(T name)
    {
        return (char[])require(name).value;
    }

    public Struct putString(T name, String value)
    {
        fields.put(name, new Field(Type.STRING, value));
        return this;
    }

    public String requireString(T name)
    {
        return (String)require(name).value;
    }

    public Struct putStringArray(T name, String[] value)
    {
        fields.put(name, new Field(Type.STRING_ARRAY, value));
        return this;
    }

    public String[] requireStringArray(T name)
    {
        return (String[])require(name).value;
    }

    public Struct putStruct(T name, Struct value)
    {
        fields.put(name, new Field(Type.STRUCT, value));
        return this;
    }

    public Struct requireStruct(T name)
    {
        return (Struct)require(name).value;
    }

    public Struct putStructArray(T name, Struct[] value)
    {
        fields.put(name, new Field(Type.STRUCT_ARRAY, value));
        return this;
    }

    public Struct[] requireStructArray(T name)
    {
        return (Struct[])require(name).value;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Struct {\n");
        for (Map.Entry<T, Field> entry : fields.entrySet())
        {
            builder.append("    ");
            builder.append(entry.getValue().type.java);
            builder.append(' ');
            builder.append(entry.getKey());
            builder.append(";\n");
        }
        builder.append('}');
        return builder.toString();
    }
}
