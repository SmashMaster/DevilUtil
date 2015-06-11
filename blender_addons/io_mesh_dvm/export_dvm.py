import bpy
import struct

class Triangle:
    def __init__(self, vertices, normal):
        self.vertices = vertices
        self.normal = normal

def makeYUp(vertex):
    return [vertex[0], vertex[2], -vertex[1]]

def write(context, filepath):
    # Exit edit mode before exporting, so current object states are exported properly.
    if bpy.ops.object.mode_set.poll():
        bpy.ops.object.mode_set(mode='OBJECT')
    
    scene = context.scene
    objects = scene.objects
    
    meshes = []
    
    for object in objects:
        try:
            mesh = object.to_mesh(scene, True, 'PREVIEW')
        except RuntimeError:
            mesh = None
        
        if mesh is not None:
            meshes.append(mesh)
    
    file = open(filepath, "wb")
    #Print DVLMDL in Java modified UTF-8.
    file.write(b'\x00\x06\x44\x56\x4c\x4d\x44\x4c')
    #All data must be big-endian.
    file.write(struct.pack('>i', len(meshes)))
    for mesh in meshes:
        file.write(struct.pack('>i', len(mesh.vertices)))
        for vertex in mesh.vertices:
            file.write(struct.pack('>3f', *makeYUp(vertex.co)))
            file.write(struct.pack('>3f', *makeYUp(vertex.normal)))
        
        triangles = []
        
        for face in mesh.tessfaces:
            verts = face.vertices
            if len(verts) == 4:
                triangles.append(Triangle([verts[0], verts[1], verts[2]], face.normal))
                triangles.append(Triangle([verts[2], verts[3], verts[0]], face.normal))
            else:
                triangles.append(Triangle(verts, face.normal))
        
        file.write(struct.pack('>i', len(triangles)))
        for triangle in triangles:
            file.write(struct.pack('>3i', *triangle.vertices))
            file.write(struct.pack('>3f', *makeYUp(triangle.normal)))
        
    file.close()
    return {'FINISHED'}
