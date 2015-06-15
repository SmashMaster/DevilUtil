import bpy
import struct
import os

class LoopVertex:
    def __init__(self, mesh, poly, loop):
        self.poly = poly
        self.loop = loop
        self.uvLoops = [uvLayer[loop.index] for uvLayer in mesh.uv_layers[:4]] #First 4 uv loop layers (test this plz)
        self.vertexColorLoop = mesh.vertex_colors[0][loop.index] if mesh.vertex_colors else None
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
    
    if len(lva.uvLoops) != len(lvb.uvLoops):
        return False
    
    for uva, uvb in zip(lva.uvLoops, lvb.uvLoops):
        for a, b in zip(uva.uv, uvb.uv):
            if a != b:
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

    #Set up LoopVertex list
    loopVertexSets = [set() for i in range(len(mesh.vertices))]
    for poly in mesh.polygons:
        for loopIndex in range(poly.loop_start, poly.loop_start + poly.loop_total):
            loop = mesh.loops[loopIndex]
            loopVertex = LoopVertex(mesh, poly, loop)
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
    
    return vertices, triangles

def rotateYUp(vertex):
    return [vertex[0], vertex[2], -vertex[1]]

def exportMesh(file, dvmType, object, mesh):
    vertices, triangles = prepareMesh(mesh)
    
    if dvmType is not 0:
        raise Exception("I haven't programmed that yet.")
    
    writePaddedJavaUTF(file, object.name)
    
    #Write vertices
    file.write(struct.pack('>i', len(vertices)))
    for vertex in vertices: #Position
        file.write(struct.pack('>3f', *rotateYUp(vertex.vert.co)))
    for vertex in vertices: #Normal
        file.write(struct.pack('>3f', *rotateYUp(vertex.loop.normal)))
    for vertex in vertices: #UV
        if vertex.uvLoops:
            file.write(struct.pack('>2f', *vertex.uvLoops[0].uv))
        else:
            file.write(struct.pack('>8x'))
    
    #Write triangle indices
    file.write(struct.pack('>i', len(triangles)))
    for triangle in triangles:
        for pointer in triangle.loopVertexPointers:
            file.write(struct.pack('>i', pointer.loopVertex.index))
    
    
def export(context, filepath, dvmType):
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
    
    TYPE_NAMES = ["ST", "MT", "AN"]
    
    file = open(filepath, "wb")
    try:
        writeJavaUTF(file, "DevilModel")
        writeJavaUTF(file, TYPE_NAMES[dvmType])
        file.write(struct.pack('>i', len(meshes)))
        for mesh in meshes:
            exportMesh(file, dvmType, mesh, mesh.data)
    finally:
        file.close()
    
    return {'FINISHED'}
