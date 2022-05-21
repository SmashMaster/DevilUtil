package com.samrj.devil.model.nodes;

import com.samrj.devil.model.BlendFile;

import java.util.function.Supplier;

//Output sockets are expressions which refer to and manipulate the node's inputs.
class OutputNodeSocket implements Socket
{
    private final VarNames varNames;
    final Node node;
    final BlendFile.Pointer ptr;
    final String name;
    final int type;

    private boolean isUsed;

    private String varName;
    Supplier<String> expression;

    OutputNodeSocket(VarNames varNames, Node node, BlendFile.Pointer ptr)
    {
        this.varNames = varNames;
        this.node = node;
        this.ptr = ptr;
        name = ptr.getField("name").asString();
        type = ptr.getField("type").asShort()&0xFFFF;
    }

    void buildExpressions()
    {
        isUsed = true;
        node.buildExpressions();
    }

    boolean isUsed()
    {
        return isUsed;
    }

    String varName()
    {
        if (varName == null) varName = varNames.newVarName();
        return varName;
    }
}
