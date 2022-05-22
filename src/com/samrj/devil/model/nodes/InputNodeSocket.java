package com.samrj.devil.model.nodes;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;
import com.samrj.devil.model.BlendFile;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class InputNodeSocket implements Socket
{
    final Node node;
    final BlendFile.Pointer ptr;
    final String name;
    final int type;

    OutputNodeSocket connectedFrom;

    InputNodeSocket(Node node, BlendFile.Pointer ptr, String name)
    {
        this.node = node;
        this.ptr = ptr;
        this.name = name;
        type = ptr.getField("type").asShort()&0xFFFF;
    }

    private float getDefaultFloat()
    {
        if (type != Socket.TYPE_FLOAT) throw new UnsupportedOperationException();
        return ptr.getField("default_value").cast("bNodeSocketValueFloat").dereference().getField("value").asFloat();
    }

    private Vec3 getDefaultVector()
    {
        if (type != Socket.TYPE_VECTOR) throw new UnsupportedOperationException();
        return ptr.getField("default_value").cast("bNodeSocketValueVector").dereference().getField("value").asZUpVec3();
    }

    private Vec4 getDefaultRGBA()
    {
        if (type != Socket.TYPE_RGBA) throw new UnsupportedOperationException();
        return ptr.getField("default_value").cast("bNodeSocketValueRGBA").dereference().getField("value").asRGBA();
    }

    /**
     * If this node is connected, these get methods must generate an expression that converts from the output type
     * to this socket's input type. Otherwise, they may simply return this socket's default value.
     *
     * There is no need to convert the default value, as it will always be the correct type.
     */

    String getFloat()
    {
        if (type != Socket.TYPE_FLOAT) throw new UnsupportedOperationException();

        if (connectedFrom == null) return Float.toString(getDefaultFloat());

        return switch (connectedFrom.type)
        {
            case TYPE_FLOAT -> connectedFrom.varName();
            case TYPE_VECTOR -> //Average
                    "(0.3333*" + connectedFrom.varName() + ".x + " + "0.3333*" + connectedFrom.varName() + ".y + 0.3333*" + connectedFrom.varName() + ".z)";
            case TYPE_RGBA -> //Luminance, how Blender converts RGB to grayscale.
                    "(0.2126*" + connectedFrom.varName() + ".r + " + "0.7152*" + connectedFrom.varName() + ".g + 0.0722*" + connectedFrom.varName() + ".b)";
            default -> throw new UnsupportedOperationException();
        };
    }

    String getVector()
    {
        if (type != TYPE_VECTOR) throw new UnsupportedOperationException();

        if (connectedFrom == null)
        {
            Vec3 value = getDefaultVector();
            return "vec3(" + value.x + ", " + value.y + ", " + value.z + ")";
        }

        return switch (connectedFrom.type)
        {
            case TYPE_FLOAT -> "vec3(" + connectedFrom.varName() + ")";
            case TYPE_VECTOR -> connectedFrom.varName();
            default -> throw new UnsupportedOperationException();
        };
    }

    String getVector(int index)
    {
        if (type != TYPE_VECTOR) throw new UnsupportedOperationException();
        if (connectedFrom == null) return Float.toString(getDefaultVector().getComponent(index));

        return switch (connectedFrom.type)
        {
            case TYPE_FLOAT -> connectedFrom.varName();
            case TYPE_VECTOR -> connectedFrom.varName() + switch (index)
            {
                case 0 -> ".x";
                case 1 -> ".y";
                case 2 -> ".z";
                default -> throw new IllegalArgumentException();
            };
            default -> throw new UnsupportedOperationException();
        };
    }

    String getVectorX()
    {
        return getVector(0);
    }

    String getVectorY()
    {
        return getVector(1);
    }

    String getVectorZ()
    {
        return getVector(2);
    }

    String getRGBA()
    {
        if (type != TYPE_RGBA) throw new UnsupportedOperationException();
        if (connectedFrom == null)
        {
            Vec4 value = getDefaultRGBA();
            return "vec4(" + value.x + ", " + value.y + ", " + value.z + ", " + value.w + ")";
        }

        return switch (connectedFrom.type)
        {
            case TYPE_FLOAT -> "vec4(" + connectedFrom.varName() + ")";
            case TYPE_RGBA -> connectedFrom.varName();
            default -> throw new UnsupportedOperationException();
        };
    }

    String getRGBA(int index)
    {
        if (type != TYPE_RGBA) throw new UnsupportedOperationException();
        if (connectedFrom == null) return Float.toString(getDefaultRGBA().getComponent(index));

        return switch (connectedFrom.type)
        {
            case TYPE_FLOAT -> connectedFrom.varName();
            case TYPE_RGBA -> connectedFrom.varName() + switch (index)
            {
                case 0 -> ".r";
                case 1 -> ".g";
                case 2 -> ".b";
                case 3 -> ".a";
                default -> throw new IllegalArgumentException();
            };
            default -> throw new UnsupportedOperationException();
        };
    }

    String getRed()
    {
        return getRGBA(0);
    }

    String getGreen()
    {
        return getRGBA(1);
    }

    String getBlue()
    {
        return getRGBA(2);
    }

    String getAlpha()
    {
        return getRGBA(3);
    }
}
