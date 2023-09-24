package com.orangomango.flightsim.model;

import javafx.geometry.Point3D;

import java.io.File;
import java.util.function.Consumer;

import com.orangomango.rendering3d.meshloader.MeshLoader;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.Engine3D;

public class Plane{
	public static class AxisSystem{
		private Point3D xAxis, yAxis, zAxis;

		public AxisSystem(){
			this.xAxis = new Point3D(1, 0, 0);
			this.yAxis = new Point3D(0, -1, 0);
			this.zAxis = new Point3D(0, 0, 1);
		}

		private static Point3D rotatePointX(Point3D point, Point3D axis, double rx){
			double[] rot = Engine3D.multiply(Engine3D.getRotateAxis(axis, rx), new double[]{point.getX(), point.getY(), point.getZ()});
			return new Point3D(rot[0], rot[1], rot[2]);
		}

		private static Point3D rotatePointY(Point3D point, Point3D axis, double ry){
			double[] rot = Engine3D.multiply(Engine3D.getRotateAxis(axis, ry), new double[]{point.getX(), point.getY(), point.getZ()});
			return new Point3D(rot[0], rot[1], rot[2]);
		}

		private static Point3D rotatePointZ(Point3D point, Point3D axis, double rz){
			double[] rot = Engine3D.multiply(Engine3D.getRotateAxis(axis, rz), new double[]{point.getX(), point.getY(), point.getZ()});
			return new Point3D(rot[0], rot[1], rot[2]);
		}

		public void rotateX(double value){
			//this.xAxis = rotatePointX(this.xAxis, this.xAxis, value);
			this.yAxis = rotatePointX(this.yAxis, this.xAxis, value);
			this.zAxis = rotatePointX(this.zAxis, this.xAxis, value);
		}

		public void rotateY(double value){
			this.xAxis = rotatePointY(this.xAxis, this.yAxis, value);
			//this.yAxis = rotatePointY(this.yAxis, this.yAxis, value);
			this.zAxis = rotatePointY(this.zAxis, this.yAxis, value);
		}

		public void rotateZ(double value){
			this.xAxis = rotatePointZ(this.xAxis, this.zAxis, value);
			//this.yAxis = rotatePointZ(this.yAxis, this.zAxis, value);
			//this.zAxis = rotatePointZ(this.zAxis, this.zAxis, value);
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
		object.translate(this.position.getX()+0.4, this.position.getY(), this.position.getZ());

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

	private void turnPlane(){
		double v = -this.rz*0.01;
		rotateY(v);
	}

	public void move(double x, double y, double z){
		turnPlane();
		this.mesh.translate(x, y, z);
		this.mesh.build();
		this.position = this.position.add(x, y, z);
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
		this.axisSystem.rotateX(value);
	}

	private void rotateY(double value){
		this.mesh.setRotation(this.axisSystem.getYaxis(), value, this.position.add(this.pivot));
		this.mesh.build();
		this.ry += value;
		this.axisSystem.rotateY(value);
	}

	public void rotateZ(double value){
		this.mesh.setRotation(this.axisSystem.getZaxis(), value, this.position.add(this.pivot));
		this.mesh.build();
		this.rz += value;
		this.axisSystem.rotateZ(value);
	}

	public AxisSystem getAxisSystem(){
		return this.axisSystem;
	}

	@Override
	public String toString(){
		return String.format("RX: %.2f RY: %.2f RZ: %.2f\nX: %.2f Y: %.2f Z: %.2f", this.rx, this.ry, this.rz, this.position.getX(), this.position.getY(), this.position.getZ());
	}
}