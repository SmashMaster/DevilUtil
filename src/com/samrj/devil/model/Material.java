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
    private static final int NODE_TYPE_BSDF_PRINCIPLED = 193;

    private static final int NODE_LINK_FLAG_MUTED = 1 << 4;
    private static final int NODE_FLAG_DO_OUTPUT = 1 << 6;

    private static final int SOCKET_TYPE_FLOAT = 0;
    private static final int SOCKET_TYPE_RGBA = 2;

    public final int modelIndex;
    
    public final Vec3 baseColor;
    public final DataPointer<Image> baseColorImage;
    public final float metallic;
    public final DataPointer<Image> metallicImage;
    public final float specular;
    public final DataPointer<Image> specularImage;
    public final float roughness;
    public final DataPointer<Image> roughnessImage;
    
    Material(Model model, int modelIndex, BlendFile.Pointer bMat) throws IOException
    {
        super(model, bMat);
        this.modelIndex = modelIndex;

        //Default to legacy values if "Use Nodes" not enabled for this material.
        float r = bMat.getField("r").asFloat();
        float g = bMat.getField("g").asFloat();
        float b = bMat.getField("b").asFloat();

        Vec3 baseColor = new Vec3(r, g, b);
        float metallic = bMat.getField("metallic").asFloat();
        float specular = bMat.getField("spec").asFloat();
        float roughness = bMat.getField("roughness").asFloat();

        DataPointer<Image> baseColorImage = DataPointer.nullPointer(model);
        DataPointer<Image> metallicImage = DataPointer.nullPointer(model);
        DataPointer<Image> specularImage = DataPointer.nullPointer(model);
        DataPointer<Image> roughnessImage = DataPointer.nullPointer(model);


        /**
         * Node loading limitations: Only loads supported nodes. Only allows for the simplest possible node layout.
         * Ignores texture mapping information.
         */

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

                    NodeSocket baseColorSock = bsdf.inputs.get("Base Color");
                    Vec4 baseDefault = baseColorSock.asRGBA();
                    baseColor.set(baseDefault.x, baseDefault.y, baseDefault.z);
                    baseColorImage = baseColorSock.getImage(model);

                    NodeSocket metallicSock = bsdf.inputs.get("Metallic");
                    metallic = metallicSock.asFloat();
                    metallicImage = metallicSock.getImage(model);

                    NodeSocket specularSock = bsdf.inputs.get("Specular");
                    specular = specularSock.asFloat();
                    specularImage = specularSock.getImage(model);

                    NodeSocket roughnessSock = bsdf.inputs.get("Roughness");
                    roughness = roughnessSock.asFloat();
                    roughnessImage = roughnessSock.getImage(model);
                }
            }
        }

        this.baseColor = baseColor;
        this.baseColorImage = baseColorImage;
        this.metallic = metallic;
        this.metallicImage = metallicImage;
        this.specular = specular;
        this.specularImage = specularImage;
        this.roughness = roughness;
        this.roughnessImage = roughnessImage;
    }

    private static class NodeSocket<T>
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

        private Vec4 asRGBA()
        {
            if (type != SOCKET_TYPE_RGBA) throw new UnsupportedOperationException();
            return ptr.getField("default_value").cast("bNodeSocketValueRGBA").dereference().getField("value").asRGBA();
        }

        private float asFloat()
        {
            if (type != SOCKET_TYPE_FLOAT) throw new UnsupportedOperationException();
            return ptr.getField("default_value").cast("bNodeSocketValueFloat").dereference().getField("value").asFloat();
        }

        private DataPointer<Image> getImage(Model model)
        {
            if (connectedFrom == null || connectedFrom.node.type != NODE_TYPE_TEX_IMAGE || !connectedFrom.name.equals("Color"))
                return DataPointer.nullPointer(model);

            Node tex = connectedFrom.node;

            BlendFile.Pointer id = tex.ptr.getField("id").dereference();
            String imgName = id.getField("name").asString().substring(2);
            return new DataPointer<>(model, Type.IMAGE, imgName);
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
