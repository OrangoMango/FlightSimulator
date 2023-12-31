package com.orangomango.flightsim.model;

import javafx.geometry.Point3D;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import com.orangomango.rendering3d.meshloader.MeshLoader;
import com.orangomango.rendering3d.model.*;
import com.orangomango.rendering3d.Engine3D;

public class Plane{
	private Point3D position, chunkPosition, pov;
	private Mesh mesh;
	private Consumer<Point3D> onChunkChanged;
	private double rx, ry, rz;

	private static final int TRIANGLE_INDEX_X = 8;
	private static final int TRIANGLE_INDEX_Y = 4;
	private static final int TRIANGLE_INDEX_Z = 22;

	public Plane(Point3D pos, Camera camera){
		this.position = pos;
		this.chunkPosition = new Point3D((int)(pos.getX()/World.PLANE_SIZE), (int)(pos.getY()/World.PLANE_SIZE), (int)(pos.getZ()/World.PLANE_SIZE));
		setPOV(camera);
	}

	public void setOnChunkChanged(Consumer<Point3D> onChunkChanged){
		this.onChunkChanged = onChunkChanged;
	}

	public Mesh build(){
		if (this.mesh != null){
			throw new IllegalStateException("Mesh has already been built");
		}

		MeshLoader loader = null;
		try {
			loader = new MeshLoader(new File(getClass().getResource("/plane.obj").toURI()));
			loader.setScale(0.1);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		Mesh object = loader.load(false);
		object.setRotation(Math.PI/2, -Math.PI/2, 0, Point3D.ZERO);
		object.translate(this.position.getX()+0.33, this.position.getY(), this.position.getZ());
		object.build();
		this.mesh = object;

		this.position = calculateCenter();

		return object;
	}

	private Point3D calculateCenter(){
		MeshTriangle[] triangles = this.mesh.getTriangles();
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < triangles.length; i++){
			MeshVertex[] vertices = new MeshVertex[]{triangles[i].getVertex1(), triangles[i].getVertex2(), triangles[i].getVertex3()};
			for (int j = 0; j < 3; j++){
				if (i == 0 || vertices[j].getPosition().getX() < minX) minX = vertices[j].getPosition().getX();
				if (i == 0 || vertices[j].getPosition().getY() < minY) minY = vertices[j].getPosition().getY();
				if (i == 0 || vertices[j].getPosition().getZ() < minZ) minZ = vertices[j].getPosition().getZ();
				if (i == 0 || vertices[j].getPosition().getX() > maxX) maxX = vertices[j].getPosition().getX();
				if (i == 0 || vertices[j].getPosition().getY() > maxY) maxY = vertices[j].getPosition().getY();
				if (i == 0 || vertices[j].getPosition().getZ() > maxZ) maxZ = vertices[j].getPosition().getZ();
			}
		}
		return new Point3D((maxX-minX)/2+minX, (maxY-minY)/2+minY, (maxZ-minZ)/2+minZ);
	}

	public double getRx(){
		return this.rx;
	}

	public double getRy(){
		return this.ry;
	}

	public double getRz(){
		return this.rz;
	}

	public void setPOV(Camera camera){
		this.pov = camera.getPosition().subtract(this.position);
	}

	public void move(Camera camera, Point3D vector){
		rotateY(-this.rz*0.025);

		// Point the camera to the plane
		Point3D	pos = this.position.subtract(camera.getPosition());
		camera.setRx(-Math.atan2(pos.getY(), Math.sqrt(Math.pow(pos.getX(), 2)+Math.pow(pos.getZ(), 2))));
		camera.setRy(Math.atan2(-pos.getX(), pos.getZ()));

		this.rx = this.rx % (2*Math.PI);
		this.ry = this.ry % (2*Math.PI);
		this.rz = this.rz % (2*Math.PI);

		this.position = this.position.add(vector);
		camera.setPosition(this.position.add(pov));
		this.mesh.translate(vector.getX(), vector.getY(), vector.getZ());
		this.mesh.build();
		Point3D chunkPos = new Point3D((int)(this.position.getX()/World.PLANE_SIZE), (int)(this.position.getY()/World.PLANE_SIZE), (int)(this.position.getZ()/World.PLANE_SIZE));
		if (!this.chunkPosition.equals(chunkPos)){
			this.chunkPosition = chunkPos;
			if (this.onChunkChanged != null) this.onChunkChanged.accept(chunkPos);
		}
	}

	public void rotateX(double value){
		Point3D axis = getDirection().crossProduct(new Point3D(0, -1, 0));
		this.mesh.setRotation(axis, value, this.position);
		this.mesh.build();
		this.rx += value;
		this.pov = rotatePointAxis(this.pov, axis, value, Point3D.ZERO);
	}

	public void rotateY(double value){
		this.mesh.setRotation(new Point3D(0, -1, 0), value, this.position);
		this.mesh.build();
		this.ry += value;
		this.pov = rotatePointAxis(this.pov, new Point3D(0, -1, 0), value, Point3D.ZERO);
	}

	public void rotateZ(double value){
		this.mesh.setRotation(getDirection(), value, this.position);
		this.mesh.build();
		this.rz += value;
	}

	public Point3D getDirection(){
		double stepX = Math.cos(getRx())*Math.cos(getRy()+Math.PI/2);
		double stepY = -Math.sin(getRx());
		double stepZ = Math.cos(getRx())*Math.sin(getRy()+Math.PI/2);

		return new Point3D(stepX, stepY, stepZ);
	}

	private static Point3D rotatePointAxis(Point3D point, Point3D axis, double r, Point3D pivot){
		point = point.subtract(pivot);
		double[] rot = Engine3D.multiply(Engine3D.getRotateAxis(axis, r), new double[]{point.getX(), point.getY(), point.getZ()});
		Point3D output = new Point3D(rot[0], rot[1], rot[2]);
		return output.add(pivot);
	}

	public Point3D getPosition(){
		return this.position;
	}

	@Override
	public String toString(){
		Function<Point3D, String> pretty = p -> String.format("%.3f %.3f %.3f", p.getX(), p.getY(), p.getZ());

		String rot = String.format("RX: %.2f RY: %.2f RZ: %.2f", getRx() % (2*Math.PI), getRy() % (2*Math.PI), getRz() % (2*Math.PI));
		String pos = String.format("Pos: %s", pretty.apply(this.position));
		String direction = String.format("Dir: %s", pretty.apply(getDirection()));

		return rot+"\n"+pos+"\n"+direction;
	}
}