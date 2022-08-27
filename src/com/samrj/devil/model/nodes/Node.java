package com.samrj.devil.model.nodes;

import com.samrj.devil.math.Vec4;
import com.samrj.devil.model.BlendFile;

import java.util.*;
import java.util.function.Supplier;

class Node
{
    private static final int NODE_FLAG_DO_OUTPUT = 1 << 6;
    private static final String EMIT_BRIGHTNESS = Float.toString(1.0f/32.0f);

    //Reading Blender's nodes:
    //Node type enums: https://github.com/blender/blender/blob/master/source/blender/blenkernel/BKE_node.h
    //Everything else: https://github.com/blender/blender/blob/master/source/blender/makesdna/DNA_node_types.h

    private final VarNames varNames;
    private final BlendFile.Pointer ptr;
    private final String name;

    private final int type;
    final boolean isMainOutput;

    private final Map<String, InputNodeSocket> inputs = new HashMap<>();
    private final Map<String, OutputNodeSocket> outputs = new HashMap<>();

    private boolean isUsed;
    private List<Supplier<String>> innerExpressions = new ArrayList<>();

    Node(VarNames varNames, BlendFile.Pointer ptr)
    {
        this.varNames = varNames;
        this.ptr = ptr;
        name = ptr.getField("name").asString();
        type = ptr.getField("type").asShort()&0xFFFF;
        isMainOutput = type == TYPE_OUTPUT_MATERIAL && (ptr.getField("flag").asInt()&NODE_FLAG_DO_OUTPUT) != 0;

        //Need to loop through names to detect and disambiguate duplicate names.
        //Example: Rename [Vector, Vector, Vector] to [Vector1, Vector2, Vector3].
        Map<String, int[]> duplicateInputNames = new HashMap<>();
        for (BlendFile.Pointer bNodeSocket : ptr.getField("inputs").asList("bNodeSocket"))
        {
            String name = bNodeSocket.getField("name").asString();

            int[] count = duplicateInputNames.get(name);
            if (count == null)
            {
                count = new int[]{1};
                duplicateInputNames.put(name, count);
            }
            else count[0]++;
        }

        for (Iterator<Map.Entry<String, int[]>> it = duplicateInputNames.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, int[]> e = it.next();
            int[] count = e.getValue();
            if (count[0] < 2) it.remove(); //Ignore non-duplicates.
            else count[0] = 1; //Reset counter for renaming variables.
        }

        for (BlendFile.Pointer bNodeSocket : ptr.getField("inputs").asList("bNodeSocket"))
        {
            String name = bNodeSocket.getField("name").asString();
            int[] dupCount = duplicateInputNames.get(name);
            if (dupCount != null) name += dupCount[0]++;

            InputNodeSocket socket = new InputNodeSocket(this, bNodeSocket, name);
            inputs.put(socket.name, socket);
        }

        for (BlendFile.Pointer bNodeSocket : ptr.getField("outputs").asList("bNodeSocket"))
        {
            OutputNodeSocket socket = new OutputNodeSocket(varNames, this, bNodeSocket);
            outputs.put(socket.name, socket);
        }
    }

    Collection<InputNodeSocket> getInputs()
    {
        return Collections.unmodifiableCollection(inputs.values());
    }

    Collection<OutputNodeSocket> getOutputs()
    {
        return Collections.unmodifiableCollection(outputs.values());
    }

    private static final int TYPE_GROUP = 2;
    private static final int TYPE_RGB = 101;
    private static final int TYPE_VALUE = 102;
    private static final int TYPE_MIX_RGB = 103;
    private static final int TYPE_MATH = 115;
    private static final int TYPE_VECTOR_MATH = 116;
    private static final int TYPE_SEPRGB_LEGACY = 120;
    private static final int TYPE_OUTPUT_MATERIAL = 124;
    private static final int TYPE_MIX_SHADER = 128;
    private static final int TYPE_NEW_GEOMETRY = 141;
    private static final int TYPE_TEX_IMAGE = 143;
    private static final int TYPE_TEX_COORD = 155;
    private static final int TYPE_BRIGHTCONTRAST = 165;
    private static final int TYPE_NORMAL_MAP = 175;
    private static final int TYPE_SEPXYZ = 188;
    private static final int TYPE_BSDF_PRINCIPLED = 193;

    //Enum orders matter.
    private enum MixRGBOperation
    {
        BLEND, ADD, MULTIPLY, SUBTRACT, SCREEN, DIVIDE, DIFFERENCE, DARK, LIGHT, OVERLAY, DODGE, BURN, HUE, SAT, VAL, COLOR, SOFT, LINEAR;
    }

    private enum MathOperation
    {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, SINE, COSINE, TANGENT, ARCSINE, ARCCOSINE, ARCTANGENT, POWER, LOGARITHM, MINIMUM, MAXIMUM, ROUND,
        LESS_THAN, GREATER_THAN, MODULO, ABSOLUTE, ARCTAN2, FLOOR, CEIL, FRACTION, SQRT, INV_SQRT, SIGN, EXPONENT, RADIANS, DEGREES, SINH,
        COSH, TANH, TRUNC, SNAP, WRAP, COMPARE, MULTIPLY_ADD, PINGPONG, SMOOTH_MIN, SMOOTH_MAX
    }

    private enum VectorMathOperation
    {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, CROSS_PRODUCT, PROJECT, REFLECT, DOT_PRODUCT, DISTANCE, LENGTH, SCALE, NORMALIZE,
        SNAP, FLOOR, CEIL, MODULO, FRACTION, ABSOLUTE, MINIMUM, MAXIMUM, WRAP, SINE, COSINE, TANGENT, REFRACT, FACEFORWARD, MULTIPLY_ADD;
    }

    private enum TexImageProjection
    {
        FLAT, BOX, SPHERE, TUBE;
    }

    void buildExpressions()
    {
        if (isUsed) return; //Prevent this from being called multiple times.
        isUsed = true;

//        System.out.println(name + " " + type);

        switch (type)
        {
            case TYPE_GROUP ->
            {
                //TODO: Add support for node groups.
            }
            case TYPE_RGB ->
            {
                OutputNodeSocket valueSock = outputs.get("Color");
                Vec4 value = valueSock.ptr.getField("default_value").cast("bNodeSocketValueRGBA").dereference().getField("value").asRGBA();
                valueSock.expression = () -> "vec4(" + value.x + ", " + value.y + ", " + value.z + ", " + value.w + ")";
            }
            case TYPE_VALUE ->
            {
                OutputNodeSocket valueSock = outputs.get("Value");
                float value = valueSock.ptr.getField("default_value").cast("bNodeSocketValueFloat").dereference().getField("value").asFloat();
                valueSock.expression = () -> Float.toString(value);
            }
            case TYPE_MIX_RGB ->
            {
                InputNodeSocket fac = inputs.get("Fac");
                InputNodeSocket color1 = inputs.get("Color1");
                InputNodeSocket color2 = inputs.get("Color2");

                switch (MixRGBOperation.values()[ptr.getField("custom1").asShort() & 0xFFFF])
                {
                    case BLEND    -> outputs.get("Color").expression = () -> "mix(" + color1.getRGBA() + ", " + color2.getRGBA() + ", " + fac.getFloat() + ")";
                    case ADD      -> outputs.get("Color").expression = () -> color1.getRGBA() + " + " + color2.getRGBA() + "*" + fac.getFloat();
                    //Could optimize multiply if factor is a constant 0.0 or 1.0
                    case MULTIPLY -> outputs.get("Color").expression = () -> "mix(" + color1.getRGBA() + ", " + color1.getRGBA() + "*" + color2.getRGBA() + ", " + fac.getFloat() + ")";
                    //case BURN     -> outputs.get("Color").expression = () -> "1.0 - (1.0 - " + color2.getRGBA() + ")/" + color1.getRGBA();
                }
            }
            case TYPE_MATH ->
            {
                InputNodeSocket value1 = inputs.get("Value1");
                InputNodeSocket value2 = inputs.get("Value2");

                switch (MathOperation.values()[ptr.getField("custom1").asShort() & 0xFFFF])
                {
                    case MULTIPLY -> outputs.get("Value").expression = () -> value1.getFloat() + "*" + value2.getFloat();
                    case SINE     -> outputs.get("Value").expression = () -> "sin(" + value1.getFloat() + ")";
                    case ABSOLUTE -> outputs.get("Value").expression = () -> "abs(" + value1.getFloat() + ")";
                }
            }
            case TYPE_VECTOR_MATH ->
            {
                InputNodeSocket vector1 = inputs.get("Vector1");
                InputNodeSocket vector2 = inputs.get("Vector2");
                InputNodeSocket vector3 = inputs.get("Vector3");
                InputNodeSocket scale = inputs.get("Scale");

                switch (VectorMathOperation.values()[ptr.getField("custom1").asShort() & 0xFFFF])
                {
                    case ADD      -> outputs.get("Vector").expression = () -> vector1.getVector() + " + " + vector2.getVector();
                    case SUBTRACT -> outputs.get("Vector").expression = () -> vector1.getVector() + " - " + vector2.getVector();
                    case MULTIPLY -> outputs.get("Vector").expression = () -> vector1.getVector() + "*" + vector2.getVector();
                    case DIVIDE   -> outputs.get("Vector").expression = () -> vector1.getVector() + "/" + vector2.getVector();
                    case REFRACT  -> outputs.get("Vector").expression = () -> "refract(" + vector1.getVector() + ", " + vector2.getVector() + ", " + scale.getFloat() + ")";
                }
            }
            case TYPE_SEPRGB_LEGACY ->
            {
            }
            case TYPE_MIX_SHADER ->
            {
                InputNodeSocket mix = inputs.get("Fac");
                InputNodeSocket shader1 = inputs.get("Shader1");
                InputNodeSocket shader2 = inputs.get("Shader2");

                outputs.get("Shader").expression = () ->
                {
                    String exp = "float[](";

                    for (int i=0; i<12; i++)
                    {
                        exp += "mix(" + shader1.getShader(i) + ", " + shader2.getShader(i) + ", " + mix.getFloat() + ")";
                        if (i < 11) exp += ", ";
                        else exp += ")";
                    }

                    return exp;
                };
            }
            case TYPE_NEW_GEOMETRY ->
            {
                //Some of these may be impractical to implement.
                outputs.get("Position").expression = () -> "v_world_pos.zxy";
                outputs.get("Normal").expression = () -> "v_normal.zxy";
                outputs.get("Tangent").expression = null; //Should not actually return () -> "v_tangent.zxy" -- blender calculates this differently.
                outputs.get("True Normal").expression = null;
                outputs.get("Incoming").expression = () -> "normalize(v_incoming.zxy)";
                outputs.get("Parametric").expression = null;
                outputs.get("Backfacing").expression = null;
                outputs.get("Pointiness").expression = null;
                outputs.get("Random Per Island").expression = null;
            }
            case TYPE_TEX_IMAGE ->
            {
                InputNodeSocket vector = inputs.get("Vector");

                BlendFile.Pointer id = ptr.getField("id").dereference();
                String imgName = varNames.getImageName(id.getField("name").asString().substring(2));

                BlendFile.Pointer storage = ptr.getField("storage").cast("NodeTexImage").dereference();
                switch (TexImageProjection.values()[storage.getField("projection").asInt()])
                {
                    case FLAT ->
                    {
                        if (vector.connectedFrom == null) outputs.get("Color").expression = () -> "texture(" + imgName + ", v_uv)";
                        else outputs.get("Color").expression = () -> "texture(" + imgName + ", vec2(" + vector.getVectorX() + ", " + vector.getVectorY() + "))";
                    }
                    case BOX ->
                    {
                        String x = varNames.newVarName(), y = varNames.newVarName(), z = varNames.newVarName();
                        innerExpressions.add(() -> "vec4 " + x + " = v_normal.z > 0.0 ? texture(" + imgName + ", vec2("  + vector.getVectorY() + ", " + vector.getVectorZ() + ")) : texture(" + imgName + ", vec2(-" + vector.getVectorY() + ", " + vector.getVectorZ() + "));");
                        innerExpressions.add(() -> "vec4 " + y + " = v_normal.x > 0.0 ? texture(" + imgName + ", vec2(-" + vector.getVectorX() + ", " + vector.getVectorZ() + ")) : texture(" + imgName + ", vec2("  + vector.getVectorX() + ", " + vector.getVectorZ() + "));");
                        innerExpressions.add(() -> "vec4 " + z + " = v_normal.y > 0.0 ? texture(" + imgName + ", vec2(-" + vector.getVectorY() + ", " + vector.getVectorX() + ")) : texture(" + imgName + ", vec2("  + vector.getVectorY() + ", " + vector.getVectorX() + "));");

                        float blendAmt = storage.getField("projection_blend").asFloat();
                        if (blendAmt < 0.01f)
                        {
                            String n = varNames.newVarName();
                            innerExpressions.add(() -> "vec3 " + n + " = abs(v_normal.zxy);");
                            outputs.get("Color").expression = () ->  "(" + n + ".x > " + n + ".y) ? (" + n + ".x > " + n + ".z ? " + x + " : " + z + ") : (" + n + ".y > " + n + ".z ? " + y + " : " + z + ")";
                        }
                        else
                        {
                            float invBlendAmt = blendAmt == 0.0f ? 1e9f : 1.0f/blendAmt;
                            String b = varNames.newVarName(); //Blend
                            innerExpressions.add(() -> "vec3 " + b + " = pow(abs(v_normal.zxy), vec3(" + invBlendAmt + "));");
                            innerExpressions.add(() -> b + " /= dot(" + b + ", vec3(1.0));");

                            outputs.get("Color").expression = () -> x + "*" + b + ".x + " + y + "*" + b + ".y + " + z + "*" + b + ".z";
                        }
                    }
                    case SPHERE -> throw new UnsupportedOperationException();
                    case TUBE -> throw new UnsupportedOperationException();
                }
            }
            case TYPE_TEX_COORD -> //Must convert from DevilUtil's coordinate system to Blender.
            {
                outputs.get("Generated").expression = null; //TODO: Gives [0,1] in the range of the object's bounding box.
                outputs.get("Normal").expression = () -> "v_normal.zxy";
                outputs.get("UV").expression = () -> "vec3(v_uv, 0.0)";
                outputs.get("Object").expression = () -> "v_obj_pos.zxy";
                outputs.get("Camera").expression = null;
                outputs.get("Window").expression = null;
                outputs.get("Reflection").expression = null;
            }
            case TYPE_BRIGHTCONTRAST ->
            {
                InputNodeSocket color = inputs.get("Color");
                InputNodeSocket bright = inputs.get("Bright");
                InputNodeSocket contrast = inputs.get("Contrast");

                String a = varNames.newVarName(); //Normal
                String b = varNames.newVarName(); //Tangent

                innerExpressions.add(() -> "float " + a + " = 1.0 + " + contrast.getFloat() + ";");
                innerExpressions.add(() -> "float " + b + " = " + bright.getFloat() + " - " + contrast.getFloat() + "*0.5;");

                outputs.get("Color").expression = () -> "vec4(max(" + a + "*" + color.getRGB() + " + " + b + ", vec3(0.0)), " + color.getAlpha() + ")";
            }
            case TYPE_NORMAL_MAP ->
            {
                InputNodeSocket strength = inputs.get("Strength");
                InputNodeSocket color = inputs.get("Color");

                //TODO: Could optimize these kinds of expressions to be "global" and only declared once.
                //Normals and tangents probably not normalized.
                String n = varNames.newVarName(); //Normal
                String t = varNames.newVarName(); //Tangent
                String b = varNames.newVarName(); //Bitangent
                String tsn = varNames.newVarName(); //Tangent space normal
                innerExpressions.add(() -> "vec3 " + n + " = normalize(v_normal.zxy);");
                innerExpressions.add(() -> "vec3 " + t + " = normalize(v_tangent.zxy);");
                innerExpressions.add(() -> "vec3 " + b + " = normalize(cross(" + n + ", " + t + "));");
                innerExpressions.add(() -> "vec3 " + tsn + " = normalize(" + color.getRGB() + "*2.0 - 1.0);");

                outputs.get("Normal").expression = () -> "normalize(mix(" + n + ", " + tsn + ".x*" + t + " + " + tsn + ".y*" + b + " + " + tsn + ".z*" + n + ", " + strength.getFloat() + "))";
            }
            case TYPE_SEPXYZ ->
            {
                InputNodeSocket vector = inputs.get("Vector");
                outputs.get("X").expression = () -> vector.getVectorX();
                outputs.get("Y").expression = () -> vector.getVectorY();
                outputs.get("Z").expression = () -> vector.getVectorZ();
            }
            case TYPE_BSDF_PRINCIPLED ->
            {
                InputNodeSocket baseColor = inputs.get("Base Color");
                InputNodeSocket metallic = inputs.get("Metallic");
                InputNodeSocket specular = inputs.get("Specular");
                InputNodeSocket roughness = inputs.get("Roughness");
                InputNodeSocket emission = inputs.get("Emission");
                InputNodeSocket emissionStrength = inputs.get("Emission Strength");
                InputNodeSocket normal = inputs.get("Normal");

                if (emissionStrength == null || emissionStrength.getDefaultFloat() == 0.0f)
                {
                    outputs.get("BSDF").expression = () -> "float[](" +
                            baseColor.getRed() + ", " + baseColor.getGreen() + ", " + baseColor.getBlue() + ", " +
                            metallic.getFloat() + ", " + specular.getFloat() + ", " + roughness.getFloat() + ", " +
                            (normal.connectedFrom == null ? "v_normal.z, v_normal.x, v_normal.y, " : (normal.getVectorX() + ", " + normal.getVectorY() + ", " + normal.getVectorZ() + ", ")) +
                            emission.getRed() + ", " + emission.getGreen() + ", " + emission.getBlue() + ")";
                }
                else outputs.get("BSDF").expression = () -> "float[](" +
                            baseColor.getRed() + ", " + baseColor.getGreen() + ", " + baseColor.getBlue() + ", " +
                            metallic.getFloat() + ", " + specular.getFloat() + ", " + roughness.getFloat() + ", " +
                            (normal.connectedFrom == null ? "v_normal.z, v_normal.x, v_normal.y, " : (normal.getVectorX() + ", " + normal.getVectorY() + ", " + normal.getVectorZ() + ", ")) +
                            emission.getRed() + "*" + emissionStrength.getFloat() + ", " + emission.getGreen() + "*" + emissionStrength.getFloat() + ", " + emission.getBlue() + "*" + emissionStrength.getFloat() + ")";
            }
        }

        for (InputNodeSocket socket : inputs.values())
            if (socket.connectedFrom != null) socket.connectedFrom.buildExpressions();
    }

    boolean isUsed()
    {
        return isUsed;
    }

    void generateCode(StringBuilder builder)
    {
        builder.append("\t//" + name + "\n");

        for (Supplier<String> exp : innerExpressions)
        {
            builder.append('\t');
            builder.append(exp.get());
            builder.append('\n');
        }

        for (OutputNodeSocket socket : outputs.values()) if (socket.isUsed() && socket.expression != null)
        {
            builder.append("\t");
            switch (socket.type)
            {
                case Socket.TYPE_FLOAT  -> builder.append("float ");
                case Socket.TYPE_VECTOR -> builder.append("vec3 ");
                case Socket.TYPE_RGBA   -> builder.append("vec4 ");
                case Socket.TYPE_SHADER -> builder.append("float[] ");
            }
            builder.append(socket.varName());
            builder.append(" = ");
            builder.append(socket.expression.get());
            builder.append(";\n");
        }

        if (type == TYPE_OUTPUT_MATERIAL) //Must convert from Blender's coordinate system back to DevilUtil.
        {
            OutputNodeSocket bsdf = inputs.get("Surface").connectedFrom;
            builder.append("\tout_albedo = vec3(" + bsdf.varName() + "[0], " + bsdf.varName() + "[1], " + bsdf.varName() + "[2]);\n");
            builder.append("\tout_material = vec3(" + bsdf.varName() + "[3], " + bsdf.varName() + "[4], " + bsdf.varName() + "[5]);\n");
            builder.append("\tout_normal = vec3(" + bsdf.varName() + "[7]*0.5 + 0.5, " + bsdf.varName() + "[8]*0.5 + 0.5, " + bsdf.varName() + "[6]*0.5 + 0.5);\n");
            builder.append("\tout_radiance = vec3(" + bsdf.varName() + "[9], " + bsdf.varName() + "[10], " + bsdf.varName() + "[11])*" + EMIT_BRIGHTNESS + ";\n");
        }
        else builder.append('\n');
    }
}
