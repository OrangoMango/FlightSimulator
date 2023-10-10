package com.orangomango.flightsim.model;

import javafx.geometry.Point3D;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import com.orangomango.rendering3d.meshloader.MeshLoader;
import com.orangomango.rendering3d.model.*;
import com.orangomango.rendering3d.Engine3D;

public class Plane{
	public static class AxisSystem{
		private Point3D xAxis, yAxis, zAxis;

		public AxisSystem(){
			this.xAxis = new Point3D(1, 0, 0);
			this.yAxis = new Point3D(0, -1, 0);
			this.zAxis = new Point3D(0, 0, 1);
		}

		public void rotateX(double value){
			//this.xAxis = Plane.rotatePointAxis(this.xAxis, this.xAxis, value, Point3D.ZERO);
			this.yAxis = Plane.rotatePointAxis(this.yAxis, this.xAxis, value, Point3D.ZERO);
			this.zAxis = Plane.rotatePointAxis(this.zAxis, this.xAxis, value, Point3D.ZERO);
		}

		public void rotateY(double value){
			this.xAxis = Plane.rotatePointAxis(this.xAxis, this.yAxis, value, Point3D.ZERO);
			//this.yAxis = Plane.rotatePointAxis(this.yAxis, this.yAxis, value, Point3D.ZERO);
			this.zAxis = Plane.rotatePointAxis(this.zAxis, this.yAxis, value, Point3D.ZERO);
		}

		public void rotateZ(double value){
			this.xAxis = Plane.rotatePointAxis(this.xAxis, this.zAxis, value, Point3D.ZERO);
			//this.yAxis = Plane.rotatePointAxis(this.yAxis, this.zAxis, value, Point3D.ZERO);
			//this.zAxis = Plane.rotatePointAxis(this.zAxis, this.zAxis, value, Point3D.ZERO);
		}

		public Point3D getXaxis(){
			return this.xAxis;
		}

		public Point3D getYaxis(){
			return this.yAxis;
		}

		public Point3D getZaxis(){
			return this.zAxis;
		}
	}

	private Point3D position, chunkPosition;
	private Mesh mesh;
	private Consumer<Point3D> onChunkChanged;
	private AxisSystem axisSystem;

	private static final int TRIANGLE_INDEX_X = 8;
	private static final int TRIANGLE_INDEX_Y = 4; 
	private static final int TRIANGLE_INDEX_Z = 22;

	public Plane(Point3D pos){
		this.position = pos;
		this.chunkPosition = new Point3D((int)(pos.getX()/World.PLANE_SIZE), (int)(pos.getY()/World.PLANE_SIZE), (int)(pos.getZ()/World.PLANE_SIZE));
		this.axisSystem = new AxisSystem();
	}

	public void setOnChunkChanged(Consumer<Point3D> onChunkChanged){
		this.onChunkChanged = onChunkChanged;
	}

	public Mesh build(){
		if (this.mesh != null){
			throw new IllegalStateException("Mesh has been already built");
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
		calculatePosition();

		return object;
	}

	public double getRx(){
		Point3D a = this.mesh.getTriangles()[TRIANGLE_INDEX_X].getVertex1().getPosition();
		Point3D b = this.mesh.getTriangles()[TRIANGLE_INDEX_X].getVertex2().getPosition();
		Point3D vec = b.subtract(a);
		vec = vec.subtract(vec.getX(), 0, 0);
		double angle = Math.acos(vec.normalize().dotProduct(new Point3D(0, 0, vec.getZ()/Math.abs(vec.getZ()))));
		if (vec.getY() < 0) angle *= -1;
		return angle;
	}

	public double getRy(){
		Point3D a = this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex1().getPosition();
		Point3D b = this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex2().getPosition();
		Point3D vec = b.subtract(a);
		vec = vec.subtract(0, vec.getY(), 0);
		double angle = Math.acos(vec.normalize().dotProduct(new Point3D(1, 0, 0)));
		if (vec.getZ() < 0) angle *= -1;
		return angle;
	}

	public double getRz(){
		Point3D a = this.mesh.getTriangles()[TRIANGLE_INDEX_Z].getVertex1().getPosition();
		Point3D b = this.mesh.getTriangles()[TRIANGLE_INDEX_Z].getVertex2().getPosition();
		Point3D vec = b.subtract(a);
		vec = vec.subtract(0, 0, vec.getZ());
		double angle = Math.acos(vec.normalize().dotProduct(new Point3D(1, 0, 0)));
		if (vec.getY() < 0) angle *= -1;
		return angle;
	}

	private void calculatePosition(){
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
		//System.out.format("Min %.2f %.2f %.2f  Max %.2f %.2f %.2f\n", minX, minY, minZ, maxX, maxY, maxZ);
		this.position = new Point3D((maxX-minX)/2+minX, (maxY-minY)/2+minY, (maxZ-minZ)/2+minZ);
	}

	public void turnPlane(){ // private
		double v = -(getRz()%(2*Math.PI))*0.01;
		rotateY(v);
	}

	public void move(Camera camera, Point3D vector){
		turnPlane();
		camera.setPosition(this.position.add(this.axisSystem.getZaxis().multiply(-2.5).add(0, -0.15, 0)));
		camera.setRx(getRx());
		camera.setRy(getRy());
		//camera.setPosition(camera.getPosition().add(vector));
		this.mesh.translate(vector.getX(), vector.getY(), vector.getZ());
		this.mesh.build();
		calculatePosition();
		Point3D chunkPos = new Point3D((int)(this.position.getX()/World.PLANE_SIZE), (int)(this.position.getY()/World.PLANE_SIZE), (int)(this.position.getZ()/World.PLANE_SIZE));
		if (!this.chunkPosition.equals(chunkPos)){
			this.chunkPosition = chunkPos;
			if (this.onChunkChanged != null) this.onChunkChanged.accept(chunkPos);
		}
	}

	public void rotateX(double value){
		this.mesh.setRotation(this.axisSystem.getXaxis(), value, this.position);
		this.mesh.build();
		calculatePosition();
		//this.position = rotatePointAxis(this.position, this.axisSystem.getXaxis(), value, this.position.add(this.pivot));
		this.axisSystem.rotateX(value);
	}

	public void rotateY(double value){
		this.mesh.setRotation(this.axisSystem.getYaxis(), value, this.position);
		this.mesh.build();
		calculatePosition();
		//this.position = rotatePointAxis(this.position, this.axisSystem.getYaxis(), value, this.position.add(this.pivot));
		this.axisSystem.rotateY(value);
	}

	public void rotateZ(double value){
		this.mesh.setRotation(this.axisSystem.getZaxis(), value, this.position);
		this.mesh.build();
		calculatePosition();
		//this.position = rotatePointAxis(this.position, this.axisSystem.getZaxis(), value, this.position.add(this.pivot));
		this.axisSystem.rotateZ(value);
	}

	public AxisSystem getAxisSystem(){
		return this.axisSystem;
	}

	private static Point3D rotatePointAxis(Point3D point, Point3D axis, double r, Point3D pivot){
		point = point.subtract(pivot);
		double[] rot = Engine3D.multiply(Engine3D.getRotateAxis(axis, r), new double[]{point.getX(), point.getY(), point.getZ()});
		Point3D output = new Point3D(rot[0], rot[1], rot[2]);
		return output.add(pivot);
	}

	@Override
	public String toString(){
		Function<Point3D, String> pretty = p -> String.format("%.3f %.3f %.3f", p.getX(), p.getY(), p.getZ());

		String rot = String.format("RX: %.2f RY: %.2f RZ: %.2f", getRx(), getRy(), getRz());
		String pos = String.format("Pos: %s", pretty.apply(this.position));
		String xAxis = String.format("xAxis: %s", pretty.apply(this.axisSystem.getXaxis()));
		String yAxis = String.format("yAxis: %s", pretty.apply(this.axisSystem.getYaxis()));
		String zAxis = String.format("zAxis: %s", pretty.apply(this.axisSystem.getZaxis()));
		String v1 = String.format("v1: %s", pretty.apply(this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex1().getPosition()));
		String v2 = String.format("v2: %s", pretty.apply(this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex2().getPosition()));
		String v3 = String.format("v3: %s", pretty.apply(this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex3().getPosition()));

		return rot+"\n"+pos+"\n"+xAxis+"\n"+yAxis+"\n"+zAxis+"\n"+v1+"\n"+v2+"\n"+v3;
	}
}