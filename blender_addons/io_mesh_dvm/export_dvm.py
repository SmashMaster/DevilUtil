import bpy
import struct
import os

DVM_FLAG_HAS_UVS = 1
DVM_FLAG_HAS_VERTEX_COLORS = 2

class LoopVertex:
    def __init__(self, mesh, poly, loop):
        self.poly = poly
        self.loop = loop
        self.uvLoop = mesh.uv_layers[0].data[loop.index] if mesh.uv_layers else None
        self.vertexColorLoop = mesh.vertex_colors[0].data[loop.index] if mesh.vertex_colors else None
        self.pointers = []

class Triangle:
    def __init__(self, indices):
        self.indices = indices

class LoopVertexPointer:
    def __init__(self, loopVertex):
        self.loopVertex = loopVertex
        loopVertex.pointers.append(self)
        
def loopVerticesEqual(lva, lvb):
    #Make sure every relevant property of the two given LoopVertex objects is the same
    if lva.loop.vertex_index != lvb.loop.vertex_index:
        return False

    for a, b in zip(lva.loop.normal, lvb.loop.normal):
        if a != b:
            return False
    
    lvaHasUV = lva.uvLoop is not None
    lvbHasUV = lvb.uvLoop is not None
    
    if lvaHasUV and lvbHasUV:
        for a, b in zip(lva.uvLoop.uv, lvb.uvLoop.uv):
            if a != b:
                return False
    elif lvaHasUV != lvbHasUV:
        return False
    
    lvaHasColor = lva.vertexColorLoop is not None
    lvbHasColor = lvb.vertexColorLoop is not None
    
    if lvaHasColor and lvbHasColor:
        for a, b in zip(lva.vertexColorLoop.color, lvb.vertexColorLoop.color):
            if a != b:
                return False
    elif lvaHasColor != lvbHasColor:
        return False
    
    return True

def writeJavaUTF(file, string):
    utf8 = string.encode('utf_8')
    strlen = len(utf8)
    file.write(struct.pack('>h', strlen))
    file.write(struct.pack('>' + str(strlen) + 's', utf8))
    return strlen + 2
    
def writePaddedJavaUTF(file, string):
    #Padded to multiples of 4 bytes
    bytesWritten = writeJavaUTF(file, string)
    padding = (4 - (bytesWritten % 4)) % 4
    file.write(struct.pack('>' + str(padding) + 'x'))
    
def prepareMesh(mesh):
    #Make sure we have all of the data we need
    mesh.calc_tessface()
    mesh.calc_normals_split()

    hasUVs = False
    hasVertexColors = False
    
    #Set up LoopVertex list
    loopVertexSets = [set() for i in range(len(mesh.vertices))]
    for poly in mesh.polygons:
        for loopIndex in range(poly.loop_start, poly.loop_start + poly.loop_total):
            loop = mesh.loops[loopIndex]
            loopVertex = LoopVertex(mesh, poly, loop)
            if loopVertex.uvLoop is not None:
                hasUVs = True
            if loopVertex.vertexColorLoop is not None:
                hasVertexColors = True
            loopVertexSets[loop.vertex_index].add(loopVertex)
    
    #Set up Triangle list
    triangles = []
    for face in mesh.tessfaces:
        verts = face.vertices
        if len(verts) == 4:
            triangles.append(Triangle([verts[0], verts[1], verts[2]]))
            triangles.append(Triangle([verts[2], verts[3], verts[0]]))
        else:
            triangles.append(Triangle(verts))
    
    for triangle in triangles:
        #Find which poly corresponds to this Triangle
        polysets = [set(loopVertex.poly for loopVertex in loopVertexSets[i]) for i in triangle.indices]
        triangle.poly = next(iter(set.intersection(*polysets)))
        
        #Find which LoopVertex objects correspond to each vertex of this Triangle
        #Also set up pointers
        triangle.loopVertexPointers = []
        for i in triangle.indices:
            for loopVertex in loopVertexSets[i]:
                if loopVertex.poly is triangle.poly:
                    triangle.loopVertexPointers.append(LoopVertexPointer(loopVertex))
                    break
            
    #Dissolve redundant LoopVertex objects
    for loopVertices in loopVertexSets:
        newLoopVertices = set()
        for loopVertex in loopVertices:
            identical = None
            for newLoopVertex in newLoopVertices:
                if loopVerticesEqual(loopVertex, newLoopVertex):
                    identical = newLoopVertex
                    for pointer in loopVertex.pointers:
                        pointer.loopVertex = newLoopVertex
                        newLoopVertex.pointers.append(pointer)
                    break
            if identical is None:
                newLoopVertices.add(loopVertex)
        loopVertices.clear()
        loopVertices |= newLoopVertices
        
    #Do some final housecleaning
    vertices = []
    i = 0
    
    for loopVertices in loopVertexSets:
        for loopVertex in loopVertices:
            loopVertex.index = i
            loopVertex.vert = mesh.vertices[loopVertex.loop.vertex_index]
            i += 1
            vertices.append(loopVertex)
    
    return vertices, triangles, hasUVs, hasVertexColors

def rotateYUp(vertex):
    return [vertex[0], vertex[2], -vertex[1]]

def exportMesh(file, object, mesh):
    vertices, triangles, hasUVs, hasVertexColors = prepareMesh(mesh)
    
    #Write mesh name
    writePaddedJavaUTF(file, object.name)
    
    #Write flag bits
    flagBits = 0
    if hasUVs:
        flagBits |= DVM_FLAG_HAS_UVS
    if hasVertexColors:
        flagBits |= DVM_FLAG_HAS_VERTEX_COLORS
    file.write(struct.pack('>i', flagBits))
    
    #Write textures
    textures = []
    if (mesh.materials):
        for textureSlot in mesh.materials[0].texture_slots:
            if textureSlot and textureSlot.use and hasattr(textureSlot.texture, 'image'):
                textures.append(textureSlot.texture)
                if len(textures) is 4:
                    break
    
    file.write(struct.pack('>i', len(textures)))
    for texture in textures:
        writePaddedJavaUTF(file, texture.image.name)
    
    #Write vertices
    file.write(struct.pack('>i', len(vertices)))
    #Positions
    for vertex in vertices:
        file.write(struct.pack('>3f', *rotateYUp(vertex.vert.co)))
    #Normals
    for vertex in vertices:
        file.write(struct.pack('>3f', *rotateYUp(vertex.loop.normal)))
    #UVs
    if hasUVs:
        for vertex in vertices: 
            if vertex.uvLoop is not None:
                file.write(struct.pack('>2f', *vertex.uvLoop.uv))
            else:
                file.write(struct.pack('>8x'))
    #Vertex Colors
    if hasVertexColors:
        for vertex in vertices: 
            if vertex.vertexColorLoop is not None:
                file.write(struct.pack('>3f', *vertex.vertexColorLoop.color))
            else:
                file.write(struct.pack('>12x'))
    
    #Write triangle indices
    file.write(struct.pack('>i', len(triangles)))
    for triangle in triangles:
        for pointer in triangle.loopVertexPointers:
            file.write(struct.pack('>i', pointer.loopVertex.index))

def export(context, filepath):
    os.system("cls")

    # Exit edit mode before exporting, so current object states are exported properly.
    if bpy.ops.object.mode_set.poll():
        bpy.ops.object.mode_set(mode='OBJECT')
    
    scene = context.scene
    objects = scene.objects
    
    meshes = []
    for object in objects:
        if isinstance(object.data, bpy.types.Mesh):
            meshes.append(object)
    
    file = open(filepath, "wb")
    try:
        writeJavaUTF(file, "DevilModel")
        file.write(struct.pack('>i', len(meshes)))
        for mesh in meshes:
            exportMesh(file, mesh, mesh.data)
    finally:
        file.close()
    
    return {'FINISHED'}
