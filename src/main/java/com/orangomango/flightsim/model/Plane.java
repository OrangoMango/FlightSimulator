package com.orangomango.flightsim.model;

import javafx.geometry.Point3D;

import java.io.File;
import java.util.function.Consumer;

import com.orangomango.rendering3d.meshloader.MeshLoader;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.Camera;
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
			this.yAxis = Plane.rotatePointAxis(this.yAxis, this.zAxis, value, Point3D.ZERO);
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
	private double rx, ry, rz;
	private Mesh mesh;
	private Point3D pivot;
	private Consumer<Point3D> onChunkChanged;
	private AxisSystem axisSystem;

	public Plane(Point3D pos){
		this.position = pos;
		this.chunkPosition = new Point3D((int)(pos.getX()/World.PLANE_SIZE), (int)(pos.getY()/World.PLANE_SIZE), (int)(pos.getZ()/World.PLANE_SIZE));
		this.pivot = new Point3D(0, -0.5, -1);
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
		return object;
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

	public void turnPlane(){ // private
		double v = -(getRz()%(2*Math.PI))*0.01;
		rotateY(v);
	}

	/*private Point3D getMeshPosition(){
		return this.mesh.getTriangles()[0].getVertex1().getPosition();
	}*/

	public void move(Camera camera, Point3D vector){
		turnPlane();
		camera.setPosition(this.position.add(this.axisSystem.getZaxis().multiply(-2.5)));
		camera.setRx(getRx());
		camera.setRy(getRy());
		this.mesh.translate(vector.getX(), vector.getY(), vector.getZ());
		this.mesh.build();
		this.position = this.position.add(vector);
		Point3D chunkPos = new Point3D((int)(this.position.getX()/World.PLANE_SIZE), (int)(this.position.getY()/World.PLANE_SIZE), (int)(this.position.getZ()/World.PLANE_SIZE));
		if (!this.chunkPosition.equals(chunkPos)){
			this.chunkPosition = chunkPos;
			if (this.onChunkChanged != null) this.onChunkChanged.accept(chunkPos);
		}
	}

	public void rotateX(double value){
		this.mesh.setRotation(this.axisSystem.getXaxis(), value, this.position.add(this.pivot));
		this.mesh.build();
		this.rx += value;
		this.position = rotatePointAxis(this.position, this.axisSystem.getXaxis(), value, this.position.add(this.pivot));
		this.axisSystem.rotateX(value);
	}

	public void rotateY(double value){
		this.mesh.setRotation(this.axisSystem.getYaxis(), value, this.position.add(this.pivot));
		this.mesh.build();
		this.ry += value;
		this.position = rotatePointAxis(this.position, this.axisSystem.getYaxis(), value, this.position.add(this.pivot));
		this.axisSystem.rotateY(value);
	}

	public void rotateZ(double value){
		this.mesh.setRotation(this.axisSystem.getZaxis(), value, this.position.add(this.pivot));
		this.mesh.build();
		this.rz += value;
		this.position = rotatePointAxis(this.position, this.axisSystem.getZaxis(), value, this.position.add(this.pivot));
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
		String rot = String.format("RX: %.2f RY: %.2f RZ: %.2f", getRx(), getRy(), getRz());
		String pos = String.format("X: %.2f Y: %.2f Z: %.2f", this.position.getX(), this.position.getY(), this.position.getZ());
		String xAxis = String.format("xAxis: %.3f %.3f %.3f", this.axisSystem.getXaxis().getX(), this.axisSystem.getXaxis().getY(), this.axisSystem.getXaxis().getZ());
		String yAxis = String.format("yAxis: %.3f %.3f %.3f", this.axisSystem.getYaxis().getX(), this.axisSystem.getYaxis().getY(), this.axisSystem.getYaxis().getZ());
		String zAxis = String.format("zAxis: %.3f %.3f %.3f", this.axisSystem.getZaxis().getX(), this.axisSystem.getZaxis().getY(), this.axisSystem.getZaxis().getZ());
		return rot+"\n"+pos+"\n"+xAxis+"\n"+yAxis+"\n"+zAxis;
	}
}