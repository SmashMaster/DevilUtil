package com.samrj.devil.model.nodes;

import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.BlendFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts a Blender Nodes material into a GLSL shader.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class NodesToGLSL
{
    private static final int NODE_LINK_FLAG_MUTED = 1 << 4;

    public static NodesToGLSL of(BlendFile.Pointer bMat)
    {
        if (bMat.getField("use_nodes").asByte() == 0) return null;

        BlendFile.Pointer bNodeTree = bMat.getField("nodetree").dereference();
        if (bNodeTree == null) return null;

        //Build node directed acyclic graph
        VarNames varNames = new VarNames();
        DAG<Node> nodes = new DAG<>();
        Map<BlendFile.Pointer, InputNodeSocket> inSockets = new HashMap<>();
        Map<BlendFile.Pointer, OutputNodeSocket> outSockets = new HashMap<>();
        Node outputNode = null;

        for (BlendFile.Pointer bNode : bNodeTree.getField("nodes").asList("bNode"))
        {
            Node node = new Node(varNames, bNode);
            for (InputNodeSocket socket : node.getInputs()) inSockets.put(socket.ptr, socket);
            for (OutputNodeSocket socket : node.getOutputs()) outSockets.put(socket.ptr, socket);

            if (node.isMainOutput) outputNode = node;
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
        builder.append("""
                #version 140
                
                uniform float u_z_far;
                uniform vec2 u_vel_factor;
                """);
        for (String imgName : varNames.imageNames.values()) builder.append("uniform sampler2D " + imgName + ";\n");
        builder.append("""
                
                in vec3 v_obj_pos;
                in vec3 v_world_pos;
                in vec3 v_incoming;
                in vec3 v_prev_view_pos;
                in vec3 v_view_pos;
                in vec3 v_normal;
                in vec3 v_tangent;
                in vec2 v_uv;
                
                out vec3 out_albedo;
                out vec3 out_material;
                out vec3 out_normal;
                out vec2 out_velocity;
                out vec3 out_radiance;
                
                void main()
                {
                    //Motion blur
                    vec2 p0 = (-v_prev_view_pos.xy/v_prev_view_pos.z)*0.5 + 0.5;
                    vec2 p1 = (-v_view_pos.xy/v_view_pos.z)*0.5 + 0.5;
                    vec2 vel = (p1 - p0)*u_vel_factor;
                    float speed = length(vel);
                    if (speed > 1.0) vel /= speed;
                                    
                """);
        for (Node node : nodes) if (node.isUsed()) node.generateCode(builder);
        builder.append("""
                    
                    out_velocity = vel*0.5 + 0.5;
                    gl_FragDepth = -v_view_pos.z/u_z_far;
                }
                """);

        return new NodesToGLSL(builder.toString(), varNames);
    }

    public final String source;
    public final Map<String, String> images;

    private NodesToGLSL(String source, VarNames varNames)
    {
        this.source = source;
        images = Collections.unmodifiableMap(varNames.imageNames);
    }
}
