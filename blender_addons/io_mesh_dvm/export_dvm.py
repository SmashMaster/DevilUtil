import bpy
import struct
import os

class LoopVertex:
    def __init__(self, mesh, poly, loop):
        self.poly = poly
        self.loop = loop
        self.uvLoops = [uvLayer[loop.index] for uvLayer in mesh.uv_layers[:4]] #First 4 uv loop layers (test this plz)
        self.vertexColorLoop = mesh.vertex_colors[0] if len(mesh.vertex_colors) > 0 else None
        self.pointers = []

class Triangle:
    def __init__(self, indices):
        self.indices = indices

class LoopVertexPointer:
    def __init__(self, loopVertex):
        self.loopVertex = loopVertex
        
def loopVerticesEqual(lva, lvb):
    #assumes they have the same index
    #Normals from MeshLoop must be the same
    #First 4 UV layers from MeshUVLoop must be the same
    #First color layer from MeshLoopColorLayer must be the same
    
    for a, b in zip(lva.loop.normal, lvb.loop.normal):
        if a != b:
            return False
    
    return True

def rotateYUp(vertex):
    return [vertex[0], vertex[2], -vertex[1]]

def writeJavaUTF(file, string):
    utf8 = string.encode('utf_8')
    strlen = len(utf8)
    file.write(struct.pack('>h', strlen))
    file.write(struct.pack('>' + str(strlen) + 's', utf8))

def exportMesh(file, mesh):
    os.system("cls")

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
    
    #Assign corresponding MeshPolygon and LoopVertex objects 
    for triangle in triangles:
        #Black magic ahead. Close your eyes and move along.
        polysets = [set(loopVertex.poly for loopVertex in loopVertexSets[i]) for i in triangle.indices]
        (triangle.poly,) = set.intersection(*polysets)
        
        #Ph'nglui mglw'nafh Cthulhu R'lyeh wgah'nagl fhtagn
        triangle.loopVertexPointers = []
        for i in triangle.indices:
            for loopVertex in loopVertexSets[i]:
                if loopVertex.poly is triangle.poly:
                    triangle.loopVertexPointers.append(LoopVertexPointer(loopVertex))
                    break
        
        for loopVertexPointer in triangle.loopVertexPointers:
            loopVertexPointer.loopVertex.pointers.append(loopVertexPointer)
            
    #Dissolve redudant LoopVertex objects
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
        
    #Now we finally have the privilege of exporting our mesh.
    #Make sure to finish implementing loopVerticesEqual() 
    
def export(context, filepath, type):
    # Exit edit mode before exporting, so current object states are exported properly.
    if bpy.ops.object.mode_set.poll():
        bpy.ops.object.mode_set(mode='OBJECT')
    
    scene = context.scene
    objects = scene.objects
    meshes = []
    
    for object in objects:
        try:
            mesh = object.to_mesh(scene, True, 'PREVIEW')
            meshes.append(mesh)
        except RuntimeError:
            pass
    
    file = open(filepath, "wb")
    writeJavaUTF(file, "DVLMDL")
    file.write(struct.pack('>i', len(meshes)))
    for mesh in meshes:
        exportMesh(file, mesh)
    file.close()
    return {'FINISHED'}
