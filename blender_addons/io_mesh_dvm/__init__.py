bl_info = {
    "name": "DevilModel format (.dvm)",
    "description": "Exports DevilModel files.",
    "author": "SmashMaster",
    "version": (0, 1),
    "blender": (2, 74, 0),
    "location": "File > Export > DevilModel (.dvm)",
    "category": "Import-Export"}

import imp
import bpy

from bpy.props import StringProperty, BoolProperty
from bpy_extras.io_utils import ExportHelper

class DVMExporter(bpy.types.Operator, ExportHelper):
    bl_idname = "export_mesh.dvm"
    bl_label = "Export DVM"

    filename_ext = ".dvm"
    filter_glob = StringProperty(default="*.dvm", options={'HIDDEN'})
    
    use_tangents = BoolProperty(
            name="Export Tangents",
            description="Export vertex tangents for normal mapping",
            default=False,
            )
    
    def execute(self, context):
        from . import export_dvm
        imp.reload(export_dvm)
        return export_dvm.export(self.filepath, self.use_tangents)

def menu_export(self, context):
    self.layout.operator(DVMExporter.bl_idname, text="DevilModel (.dvm)")

def register():
    bpy.utils.register_module(__name__)
    bpy.types.INFO_MT_file_export.append(menu_export)
 
def unregister():
    bpy.utils.unregister_module(__name__)
    bpy.types.INFO_MT_file_export.remove(menu_export)

if __name__ == "__main__":
    register()