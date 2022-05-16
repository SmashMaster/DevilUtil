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

package com.samrj.devil.model;

import com.samrj.devil.math.*;
import com.samrj.devil.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Blender model loader. Capable of parsing the basic structure of a blend file,
 * but has no detailed knowledge about its inner workings.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class BlendFile
{
    private final ByteBuffer buffer;
    
    public final boolean pointer64Bit;
    public final boolean bigEndian;
    public final int majorVersion, minorVersion;
    
    public final List<Block> blocks;
    public final Map<String, List<Pointer>> libraries;
    
    private final String[] sdnaFieldNames;
    private final String[] sdnaTypeNames;
    private final int[] sdnaTypeLengths;
    private final StructDNA[] sdnaStructs;
    private final StructDNA[] sdnaTypesToStructs;
    private final HashMap<String, Integer> sdnaNamesToTypes;
    
    private final TreeMap<Long, Block> blockAddressMap;
    
    /**
     * Loads and parses the given blender file. The entire file is loaded into
     * a single native ByteBuffer, so it can't be larger than Integer.MAX_SIZE,
     * or around 2.15GB.
     */
    public BlendFile(Path path) throws IOException
    {
        //Buffer entire file into native memory.
        try (FileChannel channel = FileChannel.open(path))
        {
            long size = channel.size();
            if (size > Integer.MAX_VALUE) throw new IOException("Blend file sizes >2.15GB not supported.");
            
            buffer = memAlloc((int)size);
            channel.read(buffer);
            buffer.flip();
        }
        
        try
        {
            //Read file header.
            expect("BLENDER");

            switch (IOUtil.readString(buffer, 1))
            {
                case "_": pointer64Bit = false; break;
                case "-": pointer64Bit = true; break;
                default: throw new IOException("Illegal pointer size specified.");
            }

            switch (IOUtil.readString(buffer, 1))
            {
                case "v": bigEndian = false; break;
                case "V": bigEndian = true; break;
                default: throw new IOException("Illegal endianness specified.");
            }

            int version = Integer.parseInt(IOUtil.readString(buffer, 3));
            majorVersion = version/100;
            minorVersion = version%100;
            
            buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            
            //Read blocks.
            ArrayList<Block> blockList = new ArrayList<>();
            Block sdnaBlock = null;
            
            READ_BLOCKS: while (true)
            {
                Block block = new Block();
                blockList.add(block);
                buffer.position(buffer.position() + block.size);
                
                switch (block.identifier)
                {
                    case "ENDB": break READ_BLOCKS;
                    case "DNA1": sdnaBlock = block; break;
                }
            }
            blocks = Collections.unmodifiableList(blockList);
            
            //Read SDNA.
            if (sdnaBlock == null) throw new IOException("No SDNA block found.");
            buffer.position(sdnaBlock.start);
            expect("SDNA");
            
            expect("NAME");
            int fieldNameCount = buffer.getInt();
            sdnaFieldNames = new String[fieldNameCount];
            for (int i=0; i<fieldNameCount; i++) sdnaFieldNames[i] = IOUtil.readNullTermString(buffer);
            
            padding(4);
            expect("TYPE");
            int typeCount = buffer.getInt();
            sdnaTypeNames = new String[typeCount];
            for (int i=0; i<typeCount; i++) sdnaTypeNames[i] = IOUtil.readNullTermString(buffer);
            
            padding(4);
            expect("TLEN");
            sdnaTypeLengths = new int[typeCount];
            for (int i=0; i<typeCount; i++) sdnaTypeLengths[i] = Short.toUnsignedInt(buffer.getShort());
            
            padding(4);
            expect("STRC");
            int structCount = buffer.getInt();
            sdnaStructs = new StructDNA[structCount];
            sdnaTypesToStructs = new StructDNA[typeCount];
            for (int i=0; i<structCount; i++)
            {
                StructDNA struct = new StructDNA();
                sdnaStructs[i] = struct;
                sdnaTypesToStructs[struct.type] = struct;
            }
            
            //Allows us to get type index from name, so pointers can be cast.
            sdnaNamesToTypes = new HashMap<>();
            for (int i=0; i<typeCount; i++) sdnaNamesToTypes.put(sdnaTypeNames[i], i);
            
            //Allows us to find the block that contains a given address, so
            //pointers can be resolved.
            blockAddressMap = new TreeMap<>(Long::compareUnsigned);
            for (Block block : blocks) blockAddressMap.put(block.address, block);
            
            //Set up libraries, the usual entry point into a blend file.
            HashMap<String, List<Pointer>> libmap = new HashMap<>();
            for (Block block : blocks)
            {
                StructDNA structDNA = block.getStructDNA();
                if (structDNA.isLibraryStruct)
                {
                    String typeName = structDNA.getTypeName();
                    List<Pointer> list = libmap.get(typeName);
                    
                    if (list == null)
                    {
                        list = new ArrayList<>();
                        libmap.put(typeName, list);
                    }

                    for (Pointer pointer : block) list.add(pointer);
                }
            }
            
            for (Entry<String, List<Pointer>> entry : libmap.entrySet())
                entry.setValue(Collections.unmodifiableList(entry.getValue()));

            libraries = Collections.unmodifiableMap(libmap);
        }
        catch (BufferUnderflowException e)
        {
            throw new IOException("Reached unexpected end of file.", e);
        }
    }

    public BlendFile(File file) throws IOException
    {
        this(file.toPath());
    }

    public BlendFile(String path) throws IOException
    {
        this(Path.of(path));
    }
    
    private void expect(String string) throws IOException
    {
        String found = IOUtil.readString(buffer, string.length());
        if (!found.equals(string)) throw new IOException("Expected " + string + ", found " + found);
    }
    
    private void padding(int alignment)
    {
        int pos = buffer.position();
        int misalign = pos%alignment;
        if (misalign > 0) buffer.position(pos + alignment - misalign);
    }
    
    /**
     * Returns a pointer to every struct of the specified type in this file's
     * main library.
     */
    public List<Pointer> getLibrary(String typeName)
    {
        List<Pointer> result = libraries.get(typeName);
        if (result == null) return Collections.EMPTY_LIST;
        return result;
    }
    
    /**
     * Frees the native memory allocated for this file.
     */
    public void destroy()
    {
        memFree(buffer);
    }
    
    /**
     * Represents the location of data within a blend file. May point to any
     * type of data, including primitives, pointers, strings, arrays, or structs.
     */
    public final class Pointer
    {
        public final int position;
        public final StructDNA structDNA;
        public final int count;
        
        private final int type;
        
        private Pointer(int position, int type, StructDNA structDNA, int count)
        {
            this.position = position;
            this.structDNA = structDNA;
            this.type = type;
            this.count = count;
        }
        
        /**
         * Increments this pointer's position by the number of given bytes, and
         * returns the result as a new pointer with the same type.
         */
        public Pointer add(int bytes)
        {
            return new Pointer(position + bytes, type, structDNA, count);
        }
        
        /**
         * Returns a new, read-only ByteBuffer view of this pointer's blend file
         * with its position set to this pointer.
         */
        public ByteBuffer asBuffer()
        {
            return (ByteBuffer)buffer.asReadOnlyBuffer()
                    .order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN)
                    .position(position);
        }
        
        /**
         * Returns the name of the C type that this pointer references.
         */
        public String getTypeName()
        {
            return sdnaTypeNames[type];
        }
        
        /**
         * Returns the size, in bytes, of a single element of the C type this
         * pointer references.
         */
        public int getTypeLength()
        {
            return sdnaTypeLengths[type];
        }
        
        /**
         * Returns the total length, in bytes, of each element this references.
         */
        public int getLength()
        {
            return getTypeLength()*count;
        }
        
        /**
         * Returns whether this pointer references a C struct.
         */
        public boolean isStruct()
        {
            return structDNA != null;
        }
        
        /**
         * Casts this pointer to a byte and returns the value it references.
         */
        public byte asByte()
        {
            return buffer.get(position);
        }
        
        /**
         * Casts this pointer to a byte array and returns its value.
         */
        public byte[] asBytes(int count)
        {
            buffer.position(position);
            byte[] result = new byte[count];
            buffer.get(result);
            return result;
        }
        
        /**
         * Casts this pointer to a short and returns the value it references.
         */
        public short asShort()
        {
            return buffer.getShort(position);
        }
        
        /**
         * Casts this pointer to a short array and returns its value.
         */
        public short[] asShorts(int count)
        {
            buffer.position(position);
            short[] result = new short[count];
            for (int i=0; i<count; i++) result[i] = buffer.getShort();
            return result;
        }
        
        /**
         * Casts this pointer to an int and returns the value it references.
         */
        public int asInt()
        {
            return buffer.getInt(position);
        }
        
        /**
         * Casts this pointer to an int array and returns its value.
         */
        public int[] asInts(int count)
        {
            buffer.position(position);
            int[] result = new int[count];
            for (int i=0; i<count; i++) result[i] = buffer.getInt();
            return result;
        }
        
        /**
         * Casts this pointer to a long and returns the value it references.
         */
        public long asLong()
        {
            return buffer.getLong(position);
        }
        
        /**
         * Casts this pointer to a long array and returns its value.
         */
        public long[] asLongs(int count)
        {
            buffer.position(position);
            long[] result = new long[count];
            for (int i=0; i<count; i++) result[i] = buffer.getLong();
            return result;
        }
        
        /**
         * Casts this pointer to a float and returns the value it references.
         */
        public float asFloat()
        {
            return buffer.getFloat(position);
        }
        
        /**
         * Casts this pointer to a float array and returns its value.
         */
        public float[] asFloats(int count)
        {
            buffer.position(position);
            float[] result = new float[count];
            for (int i=0; i<count; i++) result[i] = buffer.getFloat();
            return result;
        }
        
        /**
         * Casts this pointer to a double and returns the value it references.
         */
        public double asDouble()
        {
            return buffer.getDouble(position);
        }
        
        /**
         * Casts this pointer to a double array and returns its value.
         */
        public double[] asDoubles(int count)
        {
            buffer.position(position);
            double[] result = new double[count];
            for (int i=0; i<count; i++) result[i] = buffer.getDouble();
            return result;
        }
        
        //Blender uses a different coordinate system than DevilUtil, so we need
        //to rearrange the components of vectors.
        
        /**
         * Casts this pointer to a 3d vector.
         */
        public Vec3 asVec3()
        {
            float[] v = asFloats(3);
            return new Vec3(v[1], v[2], v[0]);
        }

        /**
         * Casts this pointer to a 3d vector of signed shorts.
         */
        public Vec3 asNormalVec3()
        {
            short[] nrm = asShorts(3);
            return new Vec3(nrm[1], nrm[2], nrm[0]).div(32768.0f);
        }

        /**
         * Casts this pointer to an RGBA color.
         */
        public Vec4 asRGBA()
        {
            float[] v = asFloats(4);
            return new Vec4(v[0], v[1], v[2], v[3]);
        }

        /**
         * Casts this pointer to a quaternion.
         */
        public Quat asQuat()
        {
            float[] q = asFloats(4);
            return new Quat(q[0], q[2], q[3], q[1]);
        }
        
        /**
         * Casts this pointer to a 3x3 matrix.
         */
        public Mat3 asMat3()
        {
            buffer.position(position);
            float[][] m = new float[3][3];
            for (int i0=0; i0<3; i0++) for (int i1=0; i1<3; i1++) m[i0][i1] = buffer.getFloat();

            return new Mat3(m[1][1], m[1][2], m[1][0],
                            m[2][1], m[2][2], m[2][0],
                            m[0][1], m[0][2], m[0][0]);
        }
        
        /**
         * Casts this pointer to a 4x4 matrix.
         */
        public Mat4 asMat4()
        {
            buffer.position(position);
            float[][] m = new float[4][4];
            for (int i0=0; i0<4; i0++) for (int i1=0; i1<4; i1++) m[i0][i1] = buffer.getFloat();

            return new Mat4(m[1][1], m[1][2], m[1][0], m[1][3],
                            m[2][1], m[2][2], m[2][0], m[2][3],
                            m[0][1], m[0][2], m[0][0], m[0][3],
                            m[3][1], m[3][2], m[3][0], m[3][3]);
        }
        
        /**
         * Returns a new pointer with the same position but the specified type.
         */
        public Pointer cast(String typeName)
        {
            Integer castType = sdnaNamesToTypes.get(typeName);
            if (castType == null) return null;
            
            return new Pointer(position, castType, sdnaTypesToStructs[castType], 1);
        }
        
        /**
         * Returns the size of a pointer address, in bytes.
         */
        public int getAddressSize()
        {
            return pointer64Bit ? 8 : 4;
        }
        
        /**
         * Returns a new pointer whose indirection is one level lower than this
         * pointer. Basically, returns the pointer that this pointer references.
         */
        public Pointer dereference()
        {
            long address = pointer64Bit ? buffer.getLong(position) : Integer.toUnsignedLong(buffer.getInt(position));
            if (address == 0) return null;
            
            Entry<Long, Block> floor = blockAddressMap.floorEntry(address);
            if (floor == null) return null;
            
            Block block = floor.getValue();
            long offset = address - block.address;
            if (offset < 0 || offset >= block.count) return null;
            
            return new Pointer(block.start + (int)offset, type, structDNA, 1);
        }
        
        /**
         * Returns the null-terminated ASCII string that this references.
         */
        public String asString()
        {
            buffer.position(position);
            return IOUtil.readNullTermString(buffer);
        }
        
        /**
         * Returns the ASCII string that this references, with the given length.
         */
        public String asString(int length)
        {
            buffer.position(position);
            return IOUtil.readString(buffer, length);
        }
        
        /**
         * Treats this as a pointer to an array, and returns a pointer to the
         * specified element within that array. Does not do any bounds checks on
         * the given index.
         */
        public Pointer getElement(int index)
        {
            return new Pointer(position + getTypeLength()*index, type, structDNA, 1);
        }
        
        /**
         * Casts this pointer to an array with the given number of elements.
         */
        public Pointer[] asArray(int count)
        {
            Pointer[] result = new Pointer[count];
            for (int i=0; i<count; i++) result[i] = getElement(i);
            return result;
        }
        
        /**
         * Returns the number of fields in the struct this pointer references.
         */
        public int getFieldCount()
        {
            if (structDNA == null) return 0;
            return structDNA.fieldArray.length;
        }
        
        /**
         * Returns the nth field in the struct that this pointer references.
         */
        public Pointer getField(int index)
        {
            if (structDNA == null) throw new IllegalStateException("Not a struct.");
            
            FieldDNA fieldDNA = structDNA.fieldArray[index];
            if (fieldDNA == null) return null;
            
            return new Pointer(position + fieldDNA.offset, fieldDNA.type, sdnaTypesToStructs[fieldDNA.type], fieldDNA.count);
        }
        
        /**
         * If this points to a struct, returns a pointer to the specified field.
         */
        public Pointer getField(String name)
        {
            if (structDNA == null) throw new IllegalStateException("Not a struct.");
            
            FieldDNA fieldDNA = structDNA.fields.get(name);
            if (fieldDNA == null) return null;
            
            return new Pointer(position + fieldDNA.offset, fieldDNA.type, sdnaTypesToStructs[fieldDNA.type], fieldDNA.count);
        }
        
        /**
         * Treats this pointer as a Blender ListBase type, which is a linked
         * list, and returns a list of pointers to each element it contains.
         */
        public List<Pointer> asList(String type)
        {
            ArrayList<BlendFile.Pointer> result = new ArrayList<>();
            BlendFile.Pointer element = getField("first").cast(type).dereference();

            while (element != null)
            {
                result.add(element);
                element = element.getField("next").dereference();
            }

            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pointer pointer = (Pointer)o;
            return position == pointer.position && count == pointer.count && type == pointer.type && Objects.equals(structDNA, pointer.structDNA);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(position, structDNA, count, type);
        }

        @Override
        public String toString()
        {
            return "[pointer to " + count + "x " + getTypeName() + ", " + getLength() + " bytes @ " + position + "]";
        }
    }
    
    /**
     * Represents the header of a Blender file block. A block may contain any
     * number of C structs, each of the same type.
     */
    public final class Block implements Iterable<Pointer>
    {
        public final String identifier;
        public final int size;
        public final int count;
        public final int start;
        
        private final long address;
        private final int sdnaIndex;

        private Block()
        {
            identifier = IOUtil.readString(buffer, 4);
            size = buffer.getInt();
            address = pointer64Bit ? buffer.getLong() : Integer.toUnsignedLong(buffer.getInt());
            sdnaIndex = buffer.getInt();
            count = buffer.getInt();
            start = buffer.position();
        }
        
        /**
         * Returns the pointer to this block's element array.
         */
        public Pointer get()
        {
            if (count == 0) throw new ArrayIndexOutOfBoundsException();
            StructDNA structDNA = getStructDNA();
            return new Pointer(start, structDNA.type, structDNA, count);
        }
        
        /**
         * Returns a pointer to the C struct in this block at the given index.
         */
        public Pointer get(int index)
        {
            if (index < 0 || index >= count) throw new ArrayIndexOutOfBoundsException();
            
            StructDNA structDNA = getStructDNA();
            return new Pointer(start + structDNA.getLength()*index, structDNA.type, structDNA, 1);
        }
        
        /**
         * Returns the DNA which defines the type of C struct in this block.
         */
        public StructDNA getStructDNA()
        {
            return sdnaStructs[sdnaIndex];
        }
        
        @Override
        public Iterator<Pointer> iterator()
        {
            return new BlockIterator();
        }
        
        @Override
        public String toString()
        {
            return "[block:" + identifier + " " + getStructDNA().getTypeName() + " @ " + start + "]";
        }
        
        private final class BlockIterator implements Iterator<Pointer>
        {
            private int index;

            @Override
            public boolean hasNext()
            {
                return index < count;
            }

            @Override
            public Pointer next()
            {
                return get(index++);
            }
        }
    }
    
    public final class StructDNA implements Iterable<FieldDNA>
    {
        public final Map<String, FieldDNA> fields;
        public final boolean isLibraryStruct;
        
        private final int type;
        private final FieldDNA[] fieldArray;
        
        private StructDNA() throws IOException
        {
            type = Short.toUnsignedInt(buffer.getShort());
            
            int fieldCount = Short.toUnsignedInt(buffer.getShort());
            fieldArray = new FieldDNA[fieldCount];
            int offset = 0;
            
            if (fieldCount != 0)
            {
                HashMap<String, FieldDNA> fieldMap = new HashMap<>(fieldCount);
                
                for (int i=0; i<fieldCount; i++)
                {
                    FieldDNA field = new FieldDNA(offset);
                    fieldArray[i] = field;
                    fieldMap.put(field.name, field);
                    offset += field.length;
                }
                
                isLibraryStruct = fieldArray[0].getTypeName().equals("ID");
                fields = Collections.unmodifiableMap(fieldMap);
            }
            else
            {
                isLibraryStruct = false;
                fields = Collections.EMPTY_MAP;
            }
            
            if (offset != getLength())
                throw new RuntimeException("Struct type " + getTypeName() + " field lengths are incorrect.");
        }
        
        public String getTypeName()
        {
            return sdnaTypeNames[type];
        }
        
        public int getLength()
        {
            return sdnaTypeLengths[type];
        }
        
        public int getFieldCount()
        {
            return fieldArray.length;
        }
        
        public FieldDNA getField(int index)
        {
            return fieldArray[index];
        }

        @Override
        public Iterator<FieldDNA> iterator()
        {
            return Arrays.asList(fieldArray).iterator();
        }
        
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("struct ").append(getTypeName()).append(" {\n");
            for (FieldDNA field : fieldArray)
                builder.append("    ").append(field.toString()).append("\n");
            builder.append("};");
            return builder.toString();
        }
    }
    
    public final class FieldDNA
    {
        public final String name;
        public final String rawName;
        public final boolean isPointer;
        public final boolean isArray;
        public final int length;
        public final int count;
        public final int offset;
        
        private final int type;
        
        private FieldDNA(int offset) throws IOException
        {
            type = Short.toUnsignedInt(buffer.getShort());
            rawName = sdnaFieldNames[Short.toUnsignedInt(buffer.getShort())];
            
            String nameStr = rawName;
            if (rawName.startsWith("*"))
            {
                isPointer = true;
                nameStr = nameStr.substring(rawName.lastIndexOf('*') + 1); //Could be faster
            }
            else if (rawName.startsWith("(*"))
            {
                isPointer = true;
                nameStr = nameStr.substring(2, nameStr.indexOf(')'));
            }
            else isPointer = false;
            
            isArray = nameStr.endsWith("]");
            
            int cnt = 1;
            if (isArray)
            {
                int arrDimStart = nameStr.indexOf('[');
                String substr = nameStr.substring(arrDimStart + 1, nameStr.length() - 1);
                String[] dimStrs = substr.split("\\]\\[");
                for (String dimStr : dimStrs)
                    cnt *= Integer.parseInt(dimStr);
                
                nameStr = nameStr.substring(0, arrDimStart);
            }
            
            name = nameStr;
            
            if (isPointer) length = (pointer64Bit ? 8 : 4)*cnt;
            else length = sdnaTypeLengths[type]*cnt;
            
            this.count = cnt;
            this.offset = offset;
        }
        
        public String getTypeName()
        {
            return sdnaTypeNames[type];
        }
        
        public StructDNA getStructDNA()
        {
            return sdnaTypesToStructs[type];
        }
        
        @Override
        public String toString()
        {
            return getTypeName() + " " + rawName + ";";
        }
    }
}
