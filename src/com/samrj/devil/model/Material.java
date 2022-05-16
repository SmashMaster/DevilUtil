package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

import java.io.IOException;
import java.util.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Material extends DataBlock
{
    //Reading Blender's nodes:
    //Node type enums: https://github.com/blender/blender/blob/master/source/blender/blenkernel/BKE_node.h
    //Everything else: https://github.com/blender/blender/blob/master/source/blender/makesdna/DNA_node_types.h

    private static final int NODE_TYPE_OUTPUT_MATERIAL = 124;
    private static final int NODE_TYPE_TEX_IMAGE = 143;
    private static final int NODE_TYPE_NORMAL_MAP = 175;
    private static final int NODE_TYPE_BSDF_PRINCIPLED = 193;

    private static final int NODE_LINK_FLAG_MUTED = 1 << 4;
    private static final int NODE_FLAG_DO_OUTPUT = 1 << 6;

    private static final int SOCKET_TYPE_FLOAT = 0;
    private static final int SOCKET_TYPE_VECTOR = 1;
    private static final int SOCKET_TYPE_RGBA = 2;
    private static final int SOCKET_TYPE_SHADER = 3;
    private static final int SOCKET_TYPE_BOOLEAN = 4;
    private static final int SOCKET_TYPE_INT = 6;
    private static final int SOCKET_TYPE_STRING = 7;
    private static final int SOCKET_TYPE_OBJECT = 8;
    private static final int SOCKET_TYPE_IMAGE = 9;
    private static final int SOCKET_TYPE_GEOMETRY = 10;
    private static final int SOCKET_TYPE_COLLECTION = 11;
    private static final int SOCKET_TYPE_TEXTURE = 12;
    private static final int SOCKET_TYPE_MATERIAL = 13;

    public final int modelIndex;
    
    public final Vec3 diffuseColor;
    public final float specularIntensity;
    public final float roughness;
    public final float metallic;
    
    Material(Model model, int modelIndex, BlendFile.Pointer bMat) throws IOException
    {
        super(model, bMat);
        this.modelIndex = modelIndex;

        boolean useNodes = bMat.getField("use_nodes").asByte() != 0;
        Node outputNode = null;
        BlendFile.Pointer bNodeTree = bMat.getField("nodetree").dereference();
        if (useNodes && bNodeTree != null)
        {
            Map<BlendFile.Pointer, NodeSocket> sockets = new HashMap<>();

            for (BlendFile.Pointer bNode : bNodeTree.getField("nodes").asList("bNode"))
            {
                Node node = new Node(bNode);
                for (NodeSocket socket : node.inputs.values()) sockets.put(socket.ptr, socket);
                for (NodeSocket socket : node.outputs.values()) sockets.put(socket.ptr, socket);

                if (node.type == NODE_TYPE_OUTPUT_MATERIAL && node.isMainOutput) outputNode = node;
            }

            for (BlendFile.Pointer bNodeLink : bNodeTree.getField("links").asList("bNodeLink"))
            {
                boolean muted = (bNodeLink.getField("flag").asInt() & NODE_LINK_FLAG_MUTED) != 0;
                if (muted) continue;

                BlendFile.Pointer fromSockPtr = bNodeLink.getField("fromsock").dereference();
                BlendFile.Pointer toSockPtr = bNodeLink.getField("tosock").dereference();

                //An output socket may be connected to many input sockets, but an input socket may only be connected to 1 output socket.
                NodeSocket toSocket = sockets.get(toSockPtr);
                if (toSocket != null) toSocket.connectedFrom = sockets.get(fromSockPtr);
            }

            if (outputNode != null)
            {
                NodeSocket bsdfSock = outputNode.inputs.get("Surface").connectedFrom;
                if (bsdfSock != null && bsdfSock.node.type == NODE_TYPE_BSDF_PRINCIPLED && bsdfSock.name.equals("BSDF"))
                {
                    //Need to read possible textures & default values on input sockets Base Color, Metallic, Specular & Roughness.
                    //Might also need to handle a Separate RGB node that swizzles channels to each value?
                    //How does blender deal with RGB textures attached to 1-channel sockets? Guessing it just uses the red channel?

                    Node bsdf = bsdfSock.node;
                    NodeSocket baseColor = bsdf.inputs.get("Base Color");
                    Vec4 baseDefault = baseColor.asRGBA();
                    NodeSocket texSock = baseColor.connectedFrom;
                    if (texSock != null && texSock.node.type == NODE_TYPE_TEX_IMAGE && texSock.name.equals("Color"))
                    {
                        Node tex = texSock.node;

                        BlendFile.Pointer storage = tex.ptr.getField("storage").cast("NodeTexImage").dereference();
                        BlendFile.Pointer base = storage.getField("base"); //NodeTexBase
                        BlendFile.Pointer texMapping = base.getField("tex_mapping"); //TexMapping
                        BlendFile.Pointer colorMapping = base.getField("color_mapping"); //ColorMapping
                        BlendFile.Pointer imageUser = storage.getField("iuser"); //ImageUser

                        BlendFile.Pointer id = tex.ptr.getField("id").dereference();
                        String imgName = id.getField("name").asString().substring(2);
                        DataPointer<Image> image = new DataPointer<>(model, Type.IMAGE, imgName);
                    }
                }
            }
        }

        float r = bMat.getField("r").asFloat();
        float g = bMat.getField("g").asFloat();
        float b = bMat.getField("b").asFloat();
        
        diffuseColor = new Vec3(r, g, b);
        specularIntensity = bMat.getField("spec").asFloat();
        roughness = bMat.getField("roughness").asFloat();
        metallic = bMat.getField("metallic").asFloat();
    }

    private static class NodeSocket
    {
        private final Node node;
        private final BlendFile.Pointer ptr;
        private final String name;
        private final int type;

        private NodeSocket connectedFrom;

        private NodeSocket(Node node, BlendFile.Pointer ptr)
        {
            this.node = node;
            this.ptr = ptr;
            name = ptr.getField("name").asString();
            type = ptr.getField("type").asShort() & 0xFFFF;
        }

        private boolean isConnected()
        {
            return connectedFrom != null;
        }

        private Vec4 asRGBA()
        {
            if (type != SOCKET_TYPE_RGBA) throw new UnsupportedOperationException();
            return ptr.getField("default_value").cast("bNodeSocketValueRGBA").dereference().getField("value").asRGBA();
        }
    }

    private static class Node
    {
        private final BlendFile.Pointer ptr;
        private final String name;
        private final boolean isMainOutput;
        private final int type;

        private final Map<String, NodeSocket> inputs = new HashMap<>();
        private final Map<String, NodeSocket> outputs = new HashMap<>();

        private Node(BlendFile.Pointer ptr)
        {
            this.ptr = ptr;
            name = ptr.getField("name").asString();
            isMainOutput = (ptr.getField("flag").asInt() & NODE_FLAG_DO_OUTPUT) != 0;
            type = ptr.getField("type").asShort() & 0xFFFF;

            System.out.println(name + " " + type);

            for (BlendFile.Pointer bNodeSocket : ptr.getField("inputs").asList("bNodeSocket"))
            {
                NodeSocket socket = new NodeSocket(this, bNodeSocket);
                inputs.put(socket.name, socket);
            }
            for (BlendFile.Pointer bNodeSocket : ptr.getField("outputs").asList("bNodeSocket"))
            {
                NodeSocket socket = new NodeSocket(this, bNodeSocket);
                outputs.put(socket.name, socket);
            }
        }
    }
}
