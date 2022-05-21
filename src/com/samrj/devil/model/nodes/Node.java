package com.samrj.devil.model.nodes;

import com.samrj.devil.model.BlendFile;

import java.util.*;
import java.util.function.Supplier;

class Node
{
    private static final int NODE_FLAG_DO_OUTPUT = 1 << 6;

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


    private static final int TYPE_VALUE = 102;
    private static final int TYPE_VECTOR_MATH = 116;
    private static final int TYPE_SEPRGB_LEGACY = 120;
    private static final int TYPE_OUTPUT_MATERIAL = 124;
    private static final int TYPE_TEX_IMAGE = 143;
    private static final int TYPE_TEX_COORD = 155;
    private static final int TYPE_BSDF_PRINCIPLED = 193;

    private static final int VECTOR_MATH_ADD = 0;
    private static final int VECTOR_MATH_SUBTRACT = 1;
    private static final int VECTOR_MATH_MULTIPLY = 2;
    private static final int VECTOR_MATH_DIVIDE = 3;

    private static final int TEX_IMAGE_PROJ_FLAT = 0;
    private static final int TEX_IMAGE_PROJ_BOX = 1;
    private static final int TEX_IMAGE_PROJ_SPHERE = 2;
    private static final int TEX_IMAGE_PROJ_TUBE = 3;

    void buildExpressions()
    {
        if (isUsed) return; //Prevent this from being called multiple times.
        isUsed = true;

        switch (type)
        {
            case TYPE_VALUE ->
            {
                OutputNodeSocket valueSock = outputs.get("Value");
                float value = valueSock.ptr.getField("default_value").cast("bNodeSocketValueFloat").dereference().getField("value").asFloat();
                valueSock.expression = () -> Float.toString(value);
            }
            case TYPE_VECTOR_MATH ->
            {
                InputNodeSocket vector1 = inputs.get("Vector1");
                InputNodeSocket vector2 = inputs.get("Vector2");
                InputNodeSocket vector3 = inputs.get("Vector3");
                InputNodeSocket scale = inputs.get("Scale");

                int mode = ptr.getField("custom1").asShort() & 0xFFFF; //Thanks Blender, this was fun to figure out.
                switch (mode)
                {
                    case VECTOR_MATH_ADD      -> outputs.get("Vector").expression = () -> vector1.getVector() + " + " + vector2.getVector();
                    case VECTOR_MATH_SUBTRACT -> outputs.get("Vector").expression = () -> vector1.getVector() + " - " + vector2.getVector();
                    case VECTOR_MATH_MULTIPLY -> outputs.get("Vector").expression = () -> vector1.getVector() + "*" + vector2.getVector();
                    case VECTOR_MATH_DIVIDE   -> outputs.get("Vector").expression = () -> vector1.getVector() + "/" + vector2.getVector();
                }
            }
            case TYPE_SEPRGB_LEGACY ->
            {
            }
            case TYPE_TEX_IMAGE ->
            {
                InputNodeSocket vector = inputs.get("Vector");

                BlendFile.Pointer id = ptr.getField("id").dereference();
                String imgName = varNames.getImageName(id.getField("name").asString().substring(2));

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

                        //TODO: Generate simplified code if blend = 0.
                        String blendName = varNames.newVarName();
                        innerExpressions.add(() -> "vec3 " + blendName + " = pow(abs(v_normal), vec3(" + invBlendAmt + "));");
                        innerExpressions.add(() -> blendName + " /= dot(" + blendName + ", vec3(1.0));");

                        String xName = varNames.newVarName(), yName = varNames.newVarName(), zName = varNames.newVarName();
                        innerExpressions.add(() -> "vec4 " + xName + " = texture(" + imgName + ", vec2(" + vector.getVectorZ() + ", " + vector.getVectorY() + "));");
                        innerExpressions.add(() -> "vec4 " + yName + " = texture(" + imgName + ", vec2(" + vector.getVectorX() + ", " + vector.getVectorZ() + "));");
                        innerExpressions.add(() -> "vec4 " + zName + " = texture(" + imgName + ", vec2(" + vector.getVectorX() + ", " + vector.getVectorY() + "));");

                        outputs.get("Color").expression = () -> xName + "*" + blendName + ".x + " + yName + "*" + blendName + ".y + " + zName + "*" + blendName + ".z";
                    }
                    case TEX_IMAGE_PROJ_SPHERE -> throw new UnsupportedOperationException();
                    case TEX_IMAGE_PROJ_TUBE -> throw new UnsupportedOperationException();
                }
            }
            case TYPE_TEX_COORD ->
            {
                outputs.get("Generated").expression = null; //TODO: Gives [0,1] in the range of the object's bounding box.
                outputs.get("Normal").expression = () -> "v_normal";
                outputs.get("UV").expression = null; //TODO: UV Mapping.
                outputs.get("Object").expression = () -> "v_obj_pos";
                outputs.get("Camera").expression = null;
                outputs.get("Window").expression = null;
                outputs.get("Reflection").expression = null;
            }
            case TYPE_BSDF_PRINCIPLED ->
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

        if (type == TYPE_OUTPUT_MATERIAL)
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
