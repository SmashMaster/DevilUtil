package com.samrj.devil.model.nodes;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.BlendFile;

import java.util.*;
import java.util.function.Supplier;

public class MaterialShader
{
    //Reading Blender's nodes:
    //Node type enums: https://github.com/blender/blender/blob/master/source/blender/blenkernel/BKE_node.h
    //Everything else: https://github.com/blender/blender/blob/master/source/blender/makesdna/DNA_node_types.h

    private static final int NODE_TYPE_SEPRGB_LEGACY = 120;
    private static final int NODE_TYPE_OUTPUT_MATERIAL = 124;
    private static final int NODE_TYPE_TEX_IMAGE = 143;
    private static final int NODE_TYPE_TEX_COORD = 155;
    private static final int NODE_TYPE_BSDF_PRINCIPLED = 193;

    private static final int NODE_LINK_FLAG_MUTED = 1 << 4;
    private static final int NODE_FLAG_DO_OUTPUT = 1 << 6;

    private static final int SOCKET_TYPE_FLOAT = 0;
    private static final int SOCKET_TYPE_VECTOR = 1;
    private static final int SOCKET_TYPE_RGBA = 2;
    private static final int SOCKET_TYPE_SHADER = 3;

    private static final int TEX_IMAGE_PROJ_FLAT = 0;
    private static final int TEX_IMAGE_PROJ_BOX = 1;
    private static final int TEX_IMAGE_PROJ_SPHERE = 2;
    private static final int TEX_IMAGE_PROJ_TUBE = 3;

    public static MaterialShader of(BlendFile.Pointer bMat)
    {
        if (bMat.getField("use_nodes").asByte() == 0) return null;

        BlendFile.Pointer bNodeTree = bMat.getField("nodetree").dereference();
        if (bNodeTree == null) return null;

        //Build node directed acyclic graph
        Names names = new Names();
        DAG<Node> nodes = new DAG<>();
        Map<BlendFile.Pointer, InputNodeSocket> inSockets = new HashMap<>();
        Map<BlendFile.Pointer, OutputNodeSocket> outSockets = new HashMap<>();
        Node outputNode = null;

        for (BlendFile.Pointer bNode : bNodeTree.getField("nodes").asList("bNode"))
        {
            Node node = new Node(names, bNode);
            for (InputNodeSocket socket : node.inputs.values()) inSockets.put(socket.ptr, socket);
            for (OutputNodeSocket socket : node.outputs.values()) outSockets.put(socket.ptr, socket);

            if (node.type == NODE_TYPE_OUTPUT_MATERIAL && node.isMainOutput) outputNode = node;
            nodes.add(node);
        }

        if (outputNode == null) return null;

        for (BlendFile.Pointer bNodeLink : bNodeTree.getField("links").asList("bNodeLink"))
        {
            boolean muted = (bNodeLink.getField("flag").asInt() & NODE_LINK_FLAG_MUTED) != 0;
            if (muted) continue;

            BlendFile.Pointer fromSockPtr = bNodeLink.getField("fromsock").dereference();
            BlendFile.Pointer toSockPtr = bNodeLink.getField("tosock").dereference();

            InputNodeSocket toSocket = inSockets.get(toSockPtr);
            if (toSocket != null)
            {
                OutputNodeSocket fromSocket = outSockets.get(fromSockPtr);
                toSocket.connectedFrom = fromSocket;
                nodes.addEdgeSafe(fromSocket.node, toSocket.node);
            }
        }

        //Traverse node graph, mark used nodes, and create expressions.
        outputNode.buildExpressions();

        StringBuilder builder = new StringBuilder();
        builder.append("#version 140\n\n");
        for (String imgName : names.imageNames.values()) builder.append("uniform sampler2D " + imgName + ";\n");
        builder.append("""
                    
                    in vec3 v_normal;
                    
                    out vec3 out_albedo;
                    out vec3 out_material;
                    out vec3 out_normal;
                    out vec3 out_radiance;
                    
                    void main()
                    {
                    """);
        for (Node node : nodes) if (node.isUsed) node.generateCode(builder);
        builder.append("}");

        return new MaterialShader(builder.toString(), names);
    }

    public final String source;
    public final Map<String, String> images;

    private MaterialShader(String source, Names names)
    {
        this.source = source;
        images = Collections.unmodifiableMap(names.imageNames);
    }

    //Output sockets are expressions which refer to and manipulate the node's inputs.
    private static class OutputNodeSocket
    {
        private final Names names;
        private final Node node;
        private final BlendFile.Pointer ptr;
        private final String name;
        private final int type;

        private boolean isUsed;

        private String varNameUseMethodInstead;
        private Supplier<String> expression;

        private OutputNodeSocket(Names names, Node node, BlendFile.Pointer ptr)
        {
            this.names = names;
            this.node = node;
            this.ptr = ptr;
            name = ptr.getField("name").asString();
            type = ptr.getField("type").asShort() & 0xFFFF;
        }

        private void buildExpressions()
        {
            isUsed = true;
            node.buildExpressions();
        }

        private String varName()
        {
            if (varNameUseMethodInstead == null) varNameUseMethodInstead = names.newVarName();
            return varNameUseMethodInstead;
        }
//
//        private void getCodeRGBA(StringBuilder builder)
//        {
//            builder.append("\tvec4 " + variableName() + " = ");
//            if (connectedFrom != null) builder.append(connectedFrom.variableName() + ";\n");
//            else
//            {
//                Vec4 baseColor = asRGBA();
//                builder.append("vec4(" + baseColor.x + ", " + baseColor.y + ", " + baseColor.z + ", " + baseColor.w + ");\n");
//            }
//        }
//
//        private void getCodeFloat(StringBuilder builder)
//        {
//            builder.append("\tfloat " + variableName() + " = ");
//            if (connectedFrom != null)
//            {
//                if (connectedFrom.type == SOCKET_TYPE_FLOAT) builder.append(connectedFrom.variableName() + ";\n");
//                else if (connectedFrom.type == SOCKET_TYPE_RGBA) builder.append(connectedFrom.variableName() + ".r;\n");
//
//            }
//            else builder.append(asFloat() + ";\n");
//        }
    }

    /**
     * Handles variable names for textures and expressions.
     */
    private static class Names
    {
        private int nameCount;
        private Map<String, String> imageNames = new HashMap<>(4);

        private String newVarName()
        {
            return "v" + Integer.toString(nameCount++, Character.MAX_RADIX);
        }

        private String getImageName(String string)
        {
            String name = imageNames.get(string);
            if (name == null)
            {
                name = "u_image" + Integer.toString(imageNames.size(), Character.MAX_RADIX);
                imageNames.put(string, name);
            }
            return name;
        }
    }

    //Input sockets can be attached to an output socket, but if not they have a default value.
    private static class InputNodeSocket
    {
        private final Node node;
        private final BlendFile.Pointer ptr;
        private final String name;
        private final int type;

        private OutputNodeSocket connectedFrom;

        private InputNodeSocket(Node node, BlendFile.Pointer ptr)
        {
            this.node = node;
            this.ptr = ptr;
            name = ptr.getField("name").asString();
            type = ptr.getField("type").asShort() & 0xFFFF;
        }

        private float getDefaultFloat()
        {
            if (type != SOCKET_TYPE_FLOAT) throw new UnsupportedOperationException();
            return ptr.getField("default_value").cast("bNodeSocketValueFloat").dereference().getField("value").asFloat();
        }

        private Vec3 getDefaultVector()
        {
            if (type != SOCKET_TYPE_VECTOR) throw new UnsupportedOperationException();
            return ptr.getField("default_value").cast("bNodeSocketValueVector").dereference().getField("value").asVec3();
        }

        private Vec4 getDefaultRGBA()
        {
            if (type != SOCKET_TYPE_RGBA) throw new UnsupportedOperationException();
            return ptr.getField("default_value").cast("bNodeSocketValueRGBA").dereference().getField("value").asRGBA();
        }

        /**
         * If this node is connected, these get methods must generate an expression that converts from the output type
         * to this socket's input type. Otherwise, they may simply return this socket's default value.
         *
         * There is no need to convert the default value, as it will always be the correct type.
         */

        private String getFloat()
        {
            if (type != SOCKET_TYPE_FLOAT) throw new UnsupportedOperationException();

            if (connectedFrom == null) return Float.toString(getDefaultFloat());

            return switch (connectedFrom.type)
            {
                case SOCKET_TYPE_FLOAT -> connectedFrom.varName();
                case SOCKET_TYPE_VECTOR -> //Average
                        "(0.3333*" + connectedFrom.varName() + ".x + " + "0.3333*" + connectedFrom.varName() + ".y + 0.3333*" + connectedFrom.varName() + ".z)";
                case SOCKET_TYPE_RGBA -> //Luminance, how Blender converts RGB to grayscale.
                        "(0.2126*" + connectedFrom.varName() + ".r + " + "0.7152*" + connectedFrom.varName() + ".g + 0.0722*" + connectedFrom.varName() + ".b)";
                default -> throw new UnsupportedOperationException();
            };
        }

        private String getVector(int index)
        {
            if (type != SOCKET_TYPE_VECTOR) throw new UnsupportedOperationException();
            if (connectedFrom == null) return Float.toString(getDefaultVector().getComponent(index));

            return switch (connectedFrom.type)
            {
                case SOCKET_TYPE_FLOAT -> connectedFrom.varName();
                case SOCKET_TYPE_VECTOR -> connectedFrom.varName() + switch(index) {
                    case 0 -> ".x";
                    case 1 -> ".y";
                    case 2 -> ".z";
                    default -> throw new IllegalArgumentException();
                };
                default -> throw new UnsupportedOperationException();
            };
        }

        private String getVectorX()
        {
            return getVector(0);
        }

        private String getVectorY()
        {
            return getVector(1);
        }

        private String getVectorZ()
        {
            return getVector(2);
        }

        private String getRGBA(int index)
        {
            if (type != SOCKET_TYPE_RGBA) throw new UnsupportedOperationException();
            if (connectedFrom == null) return Float.toString(getDefaultRGBA().getComponent(index));

            return switch (connectedFrom.type)
            {
                case SOCKET_TYPE_FLOAT -> connectedFrom.varName();
                case SOCKET_TYPE_RGBA -> connectedFrom.varName() + switch(index) {
                    case 0 -> ".r";
                    case 1 -> ".g";
                    case 2 -> ".b";
                    case 3 -> ".a";
                    default -> throw new IllegalArgumentException();
                };
                default -> throw new UnsupportedOperationException();
            };
        }

        private String getRed()
        {
            return getRGBA(0);
        }

        private String getGreen()
        {
            return getRGBA(1);
        }

        private String getBlue()
        {
            return getRGBA(2);
        }

        private String getAlpha()
        {
            return getRGBA(3);
        }
    }

    private static class Node
    {
        private final Names names;
        private final BlendFile.Pointer ptr;
        private final String name;
        private final boolean isMainOutput;
        private final int type;

        private final Map<String, InputNodeSocket> inputs = new HashMap<>();
        private final Map<String, OutputNodeSocket> outputs = new HashMap<>();

        private boolean isUsed;
        private List<Supplier<String>> innerExpressions = new ArrayList<>();

        private Node(Names names, BlendFile.Pointer ptr)
        {
            this.names = names;
            this.ptr = ptr;
            name = ptr.getField("name").asString();
            isMainOutput = (ptr.getField("flag").asInt() & NODE_FLAG_DO_OUTPUT) != 0;
            type = ptr.getField("type").asShort() & 0xFFFF;

            for (BlendFile.Pointer bNodeSocket : ptr.getField("inputs").asList("bNodeSocket"))
            {
                InputNodeSocket socket = new InputNodeSocket(this, bNodeSocket);
                inputs.put(socket.name, socket);
            }
            for (BlendFile.Pointer bNodeSocket : ptr.getField("outputs").asList("bNodeSocket"))
            {
                OutputNodeSocket socket = new OutputNodeSocket(names, this, bNodeSocket);
                outputs.put(socket.name, socket);
            }
        }

        private void buildExpressions()
        {
            if (isUsed) return; //Prevent this from being called multiple times.
            isUsed = true;

            switch (type)
            {
                case NODE_TYPE_SEPRGB_LEGACY ->
                {
                }
                case NODE_TYPE_TEX_IMAGE ->
                {
                    InputNodeSocket vector = inputs.get("Vector");

                    BlendFile.Pointer id = ptr.getField("id").dereference();
                    String imgName = names.getImageName(id.getField("name").asString().substring(2));

                    BlendFile.Pointer storage = ptr.getField("storage").cast("NodeTexImage").dereference();
                    int projection = storage.getField("projection").asInt();
                    switch (projection)
                    {
                        case TEX_IMAGE_PROJ_FLAT ->
                        {
                            outputs.get("Color").expression = () -> "texture(" + imgName + ", vec2(" + vector.getVectorX() + ", " + vector.getVectorY() + "))";
                        }
                        case TEX_IMAGE_PROJ_BOX ->
                        {
                            float blendAmt = storage.getField("projection_blend").asFloat();
                            float invBlendAmt = blendAmt == 0.0f ? 1e9f : 1.0f/blendAmt;

                            //TODO: Box projection. Also, generate simplified code if blend = 0.
                            String blendName = names.newVarName();
                            innerExpressions.add(() -> "vec3 " + blendName + " = pow(abs(v_normal), vec3(" + invBlendAmt + "));");
                            innerExpressions.add(() -> blendName + " /= dot(" + blendName + ", vec3(1.0));");

                            String xName = names.newVarName(), yName = names.newVarName(), zName = names.newVarName();
                            innerExpressions.add(() -> "vec4 " + xName + " = texture(" + imgName + ", vec2(" + vector.getVectorZ() + ", " + vector.getVectorY() + "));");
                            innerExpressions.add(() -> "vec4 " + yName + " = texture(" + imgName + ", vec2(" + vector.getVectorX() + ", " + vector.getVectorZ() + "));");
                            innerExpressions.add(() -> "vec4 " + zName + " = texture(" + imgName + ", vec2(" + vector.getVectorX() + ", " + vector.getVectorY() + "));");

                            outputs.get("Color").expression = () -> xName + "*" + blendName + ".x + " + yName + "*" + blendName + ".y + " + zName + "*" + blendName + ".z";
                        }
                        case TEX_IMAGE_PROJ_SPHERE -> throw new UnsupportedOperationException();
                        case TEX_IMAGE_PROJ_TUBE -> throw new UnsupportedOperationException();
                    }
                }
                case NODE_TYPE_TEX_COORD ->
                {
                }
                case NODE_TYPE_BSDF_PRINCIPLED ->
                {
                    InputNodeSocket baseColor = inputs.get("Base Color");
                    InputNodeSocket metallic = inputs.get("Metallic");
                    InputNodeSocket specular = inputs.get("Specular");
                    InputNodeSocket roughness = inputs.get("Roughness");
                    InputNodeSocket emission = inputs.get("Emission");
                    InputNodeSocket emissionStrength = inputs.get("Emission Strength");

                    //InputNodeSocket should have a number of methods that return strings (or accept a StringBuilder)
                    //and basically fill in an expression

                    if (emissionStrength == null)
                    {
                        outputs.get("BSDF").expression = () -> "float[](" +
                                baseColor.getRed() + ", " + baseColor.getGreen() + ", " + baseColor.getBlue() + ", " +
                                metallic.getFloat() + ", " + specular.getFloat() + ", " + roughness.getFloat() + ", " +
                                "v_normal.x, v_normal.y, v_normal.z, " +
                                emission.getRed() + ", " + emission.getGreen() + ", " + emission.getBlue() + ")";
                    }
                    else outputs.get("BSDF").expression = () -> "float[](" +
                            baseColor.getRed() + ", " + baseColor.getGreen() + ", " + baseColor.getBlue() + ", " +
                            metallic.getFloat() + ", " + specular.getFloat() + ", " + roughness.getFloat() + ", " +
                            "v_normal.x, v_normal.y, v_normal.z, " +
                            emission.getRed() + "*" + emissionStrength.getFloat() + ", " + emission.getGreen() + "*" + emissionStrength.getFloat() + ", " + emission.getBlue() + "*" + emissionStrength.getFloat() + ")";
                }
            }

            for (InputNodeSocket socket : inputs.values())
                if (socket.connectedFrom != null) socket.connectedFrom.buildExpressions();
        }

        private void generateCode(StringBuilder builder)
        {
            builder.append("\t//" + name + "\n");

            for (Supplier<String> exp : innerExpressions)
            {
                builder.append('\t');
                builder.append(exp.get());
                builder.append('\n');
            }

            for (OutputNodeSocket socket : outputs.values()) if (socket.isUsed)
            {
                if (socket.expression != null)
                {
                    builder.append("\t");
                    switch (socket.type)
                    {
                        case SOCKET_TYPE_FLOAT:
                            builder.append("float ");
                            break;
                        case SOCKET_TYPE_RGBA:
                            builder.append("vec4 ");
                            break;
                        case SOCKET_TYPE_SHADER:
                            builder.append("float[] ");
                            break;
                    }
                    builder.append(socket.varName());
                    builder.append(" = ");
                    builder.append(socket.expression.get());
                    builder.append(";\n");
                }
            }

            if (type == NODE_TYPE_OUTPUT_MATERIAL)
            {
                OutputNodeSocket bsdf = inputs.get("Surface").connectedFrom;
                builder.append("\tout_albedo = vec3(" + bsdf.varName() + "[0], " + bsdf.varName() + "[1], " + bsdf.varName() + "[2]);\n");
                builder.append("\tout_material = vec3(" + bsdf.varName() + "[3], " + bsdf.varName() + "[4], " + bsdf.varName() + "[5]);\n");
                builder.append("\tout_normal = vec3(" + bsdf.varName() + "[6], " + bsdf.varName() + "[7], " + bsdf.varName() + "[8]);\n");
                builder.append("\tout_radiance = vec3(" + bsdf.varName() + "[9], " + bsdf.varName() + "[10], " + bsdf.varName() + "[11]);\n");
            }
            else builder.append('\n');
        }
    }
}
