package com.samrj.devil.util;

import com.samrj.devil.geo3d.Box3;
import com.samrj.devil.geo3d.ConvexShape;
import com.samrj.devil.geo3d.GeoMesh;
import com.samrj.devil.geo3d.Geometry;
import com.samrj.devil.geo3d.IsectResult;
import com.samrj.devil.geo3d.RaycastResult;
import com.samrj.devil.geo3d.SweepResult;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.gl.VAO;
import com.samrj.devil.graphics.MeshDrawer;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.DataBlock;
import com.samrj.devil.model.DataPointer;
import com.samrj.devil.model.Mesh;
import com.samrj.devil.model.Model;
import com.samrj.devil.model.ModelObject;
import com.samrj.devil.model.Scene;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class which handles the loading rendering, and collision detection in a
 * static 3D space.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Level3D implements Geometry
{
    private final Map<Mesh, MeshDrawer> drawers;
    private final List<LvlObj> objects;
    private final ShaderProgram shader;

    /**
     * Creates a new level using the given model and shader. Uses mesh objects
     * from the scene named "Scene" to populate this level.
     * 
     * @param model The model to create a level from.
     * @param shader The shader to draw this level with.
     */
    public Level3D(Model model, ShaderProgram shader)
    {
        drawers = new IdentityHashMap<>();
        Scene scene = model.scenes.get("Scene");
        objects = new ArrayList<>();
        this.shader = shader;
        
        for (DataPointer<ModelObject<?>> p : scene.objects)
        {
            ModelObject<?> obj = p.get();
            if (obj.data.type == DataBlock.Type.MESH)
                objects.add(new LvlObj((ModelObject<Mesh>)obj));
        }
    }
    
    /**
     * Binds this level's shader and renders all of its objects.
     */
    public void render()
    {
        DGL.useProgram(shader);
        for (LvlObj object : objects) object.render();
    }
    
    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp)
    {
        Box3 bounds = Box3.empty().expand(p0).sweep(dp);
        
        return objects.stream()
                .filter((obj) -> Box3.touching(obj.geom.bounds, bounds))
                .flatMap((obj) -> obj.geom.raycastUnsorted(p0, dp));
    }

    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        Box3 bounds = shape.bounds();
        
        return objects.stream()
                .filter((obj) -> Box3.touching(obj.geom.bounds, bounds))
                .flatMap((obj) -> obj.geom.intersectUnsorted(shape));
    }

    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        Box3 bounds = shape.bounds().sweep(dp);
        
        return objects.stream()
                .filter((obj) -> Box3.touching(obj.geom.bounds, bounds))
                .flatMap((obj) -> obj.geom.sweepUnsorted(shape, dp));
    }
    
    /**
     * Releases any native memory associated with this level.
     */
    public void destroy()
    {
        for (MeshDrawer drawer : drawers.values()) drawer.destroy();
    }
    
    private class LvlObj
    {
        private final GeoMesh geom;
        private final MeshDrawer drawer;
        private final VAO vao;
        private final Mat4 transform;
        
        private LvlObj(ModelObject<Mesh> object)
        {
            Mesh mesh = object.data.get();
            geom = new GeoMesh(object);
            
            MeshDrawer d = drawers.get(mesh);
            if (d == null) drawers.put(mesh, d = new MeshDrawer(mesh));
            d.setPositionName("in_pos");
            d.setNormalName("in_normal");
            drawer = d;
            
            vao = drawer.link(shader);
            transform = object.transform.toMatrix();
        }
        
        private void render()
        {
            shader.uniformMat4("u_model_matrix", transform);
            DGL.bindVAO(vao);
            drawer.draw();
        }
    }
}
