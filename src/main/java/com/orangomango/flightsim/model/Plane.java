package com.orangomango.flightsim.model;

import javafx.geometry.Point3D;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import com.orangomango.rendering3d.meshloader.MeshLoader;
import com.orangomango.rendering3d.model.*;
import com.orangomango.rendering3d.Engine3D;

public class Plane{
	private Point3D position, chunkPosition;
	private Mesh mesh;
	private Consumer<Point3D> onChunkChanged;
	private double rx, ry, rz;

	private static final int TRIANGLE_INDEX_X = 8;
	private static final int TRIANGLE_INDEX_Y = 4;
	private static final int TRIANGLE_INDEX_Z = 22;

	public Plane(Point3D pos){
		this.position = pos;
		this.chunkPosition = new Point3D((int)(pos.getX()/World.PLANE_SIZE), (int)(pos.getY()/World.PLANE_SIZE), (int)(pos.getZ()/World.PLANE_SIZE));
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

	public void move(Camera camera, Point3D vector){
		this.position = this.position.add(vector);
		camera.setPosition(this.position.add(getDirection().multiply(-4.5)).add(0, -1, 0));
		camera.setRx(getRx());
		camera.setRy(getRy());
		this.mesh.translate(vector.getX(), vector.getY(), vector.getZ());
		this.mesh.build();
		Point3D chunkPos = new Point3D((int)(this.position.getX()/World.PLANE_SIZE), (int)(this.position.getY()/World.PLANE_SIZE), (int)(this.position.getZ()/World.PLANE_SIZE));
		if (!this.chunkPosition.equals(chunkPos)){
			this.chunkPosition = chunkPos;
			if (this.onChunkChanged != null) this.onChunkChanged.accept(chunkPos);
		}
	}

	public void rotateX(double value){
		this.mesh.setRotation(getDirection().crossProduct(new Point3D(0, -1, 0)), value, this.position);
		this.mesh.build();
		this.rx += value;
		//this.position = rotatePointAxis(this.position, this.axisSystem.getXaxis(), value, this.position.add(this.pivot));
		//this.axisSystem.rotateX(value);
	}

	public void rotateY(double value){
		this.mesh.setRotation(new Point3D(0, -1, 0), value, this.position);
		this.mesh.build();
		this.ry += value;
		//rotateZ(-value);
		//this.position = rotatePointAxis(this.position, this.axisSystem.getYaxis(), value, this.position.add(this.pivot));
		//this.axisSystem.rotateY(value);
	}

	public void rotateZ(double value){
		this.mesh.setRotation(getDirection(), value, this.position);
		this.mesh.build();
		this.rz += value;
		//this.position = rotatePointAxis(this.position, this.axisSystem.getZaxis(), value, this.position.add(this.pivot));
		//this.axisSystem.rotateZ(value);
	}

	public Point3D getDirection(){
		double stepX = Math.cos(getRx())*Math.cos(getRy()+Math.PI/2);
		double stepY = -Math.sin(getRx());
		double stepZ = Math.cos(getRx())*Math.sin(getRy()+Math.PI/2);

		return new Point3D(stepX, stepY, stepZ);
	}

	/*private static Point3D rotatePointAxis(Point3D point, Point3D axis, double r, Point3D pivot){
		point = point.subtract(pivot);
		double[] rot = Engine3D.multiply(Engine3D.getRotateAxis(axis, r), new double[]{point.getX(), point.getY(), point.getZ()});
		Point3D output = new Point3D(rot[0], rot[1], rot[2]);
		return output.add(pivot);
	}*/

	@Override
	public String toString(){
		Function<Point3D, String> pretty = p -> String.format("%.3f %.3f %.3f", p.getX(), p.getY(), p.getZ());

		String rot = String.format("RX: %.2f RY: %.2f RZ: %.2f", getRx() % (2*Math.PI), getRy() % (2*Math.PI), getRz() % (2*Math.PI));
		String pos = String.format("Pos: %s", pretty.apply(this.position));
		//String xAxis = String.format("xAxis: %s", pretty.apply(this.axisSystem.getXaxis()));
		//String yAxis = String.format("yAxis: %s", pretty.apply(this.axisSystem.getYaxis()));
		//String zAxis = String.format("zAxis: %s", pretty.apply(this.axisSystem.getZaxis()));
		//String v1 = String.format("v1: %s", pretty.apply(this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex1().getPosition()));
		//String v2 = String.format("v2: %s", pretty.apply(this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex2().getPosition()));
		//String v3 = String.format("v3: %s", pretty.apply(this.mesh.getTriangles()[TRIANGLE_INDEX_Y].getVertex3().getPosition()));

		return rot+"\n"+pos; //+"\n"+xAxis+"\n"+yAxis+"\n"+zAxis+"\n"+v1+"\n"+v2+"\n"+v3;
	}
}