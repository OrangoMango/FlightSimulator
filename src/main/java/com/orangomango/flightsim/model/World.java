package com.orangomango.flightsim.model;

import javafx.scene.paint.Color;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;

import java.util.Map;
import java.util.HashMap;

import com.orangomango.rendering3d.Engine3D;
import com.orangomango.rendering3d.model.Mesh;

public class World{
	private Point3D position;
	private HashMap<Point3D, Mesh> chunks = new HashMap<>();
	private int seed = (int)System.currentTimeMillis();

	public static final double PLANE_SIZE = 0.5;
	public static final int CHUNKS = 33;
	public static final float FREQUENCY = 0.35f;
	public static final double HEIGHT = 1.5;

	public World(Point3D pos){
		this.position = pos;
	}

	public void manage(Engine3D engine, Point3D pos){
		HashMap<Point3D, Mesh> temp = new HashMap<>();
		for (int i = -CHUNKS/2; i < -CHUNKS/2+CHUNKS; i++){
			for (int j = -CHUNKS/2; j < -CHUNKS/2+CHUNKS; j++){
				Point3D loc = pos.add(i, 0, j);
				Mesh m = this.chunks.getOrDefault(loc, null);
				if (m == null){
					m = build(loc);
					engine.addObject(m);
				}
				temp.put(loc, m);
			}
		}

		for (Map.Entry<Point3D, Mesh> entry : this.chunks.entrySet()){
			if (!temp.keySet().contains(entry.getKey())){
				engine.removeObject(entry.getValue());
			}
		}
		this.chunks = temp;
	}

	private Mesh build(Point3D chunkPos){
		PerlinNoise noise = new PerlinNoise(this.seed);
		final float cx = (float)chunkPos.getX();
		final float cy = (float)chunkPos.getY();
		final float cz = (float)chunkPos.getZ();

		final double aHeight = 0.3+(noise.noise(Math.abs(cx*FREQUENCY), 0, Math.abs(cz*FREQUENCY))+1)/2*HEIGHT;
		final double bHeight = 0.3+(noise.noise(Math.abs(cx*FREQUENCY), 0, Math.abs((cz-1)*FREQUENCY))+1)/2*HEIGHT;
		final double cHeight = 0.3+(noise.noise(Math.abs((cx+1)*FREQUENCY), 0, Math.abs((cz-1)*FREQUENCY))+1)/2*HEIGHT;
		final double dHeight = 0.3+(noise.noise(Math.abs((cx+1)*FREQUENCY), 0, Math.abs(cz*FREQUENCY))+1)/2*HEIGHT;

		Point3D a = this.position.add(new Point3D(PLANE_SIZE*cx, aHeight, PLANE_SIZE*cz));
		Point3D b = this.position.add(new Point3D(PLANE_SIZE*cx, bHeight, PLANE_SIZE*(cz-1)));
		Point3D c = this.position.add(new Point3D(PLANE_SIZE*(cx+1), cHeight, PLANE_SIZE*(cz-1)));
		Point3D d = this.position.add(new Point3D(PLANE_SIZE*(cx+1), dHeight, PLANE_SIZE*cz));

		Color color = Color.color(Math.random(), Math.random(), Math.random());
		Mesh object = new Mesh(new Point3D[]{a, b, c, d}, new int[][]{{0, 1, 2}, {0, 2, 3}}, null, null, new Color[]{color, color});
		object.setShowAllFaces(true);

		object.build();
		return object;
	}
}