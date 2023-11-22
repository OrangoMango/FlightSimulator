package com.orangomango.flightsim;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import com.orangomango.flightsim.model.Plane;
import com.orangomango.flightsim.model.World;
import com.orangomango.flightsim.ui.TiltIndicator;
import com.orangomango.rendering3d.Engine3D;
import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;

public class MainApplication extends Application{
	private static final int WIDTH = 360; //720;
	private static final int HEIGHT = 180; //360;

	private static final Point3D STARTPOS = new Point3D(0, -0.9, -3.2);

	private double planeSpeed = 0;
	
	@Override
	public void start(Stage stage){
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Camera camera = new Camera(STARTPOS, WIDTH, HEIGHT, Math.PI/4, 100, 0.3);
		engine.setCamera(camera);

		Plane plane = new Plane(Point3D.ZERO, camera);
		World world = new World(Point3D.ZERO);

		plane.setOnChunkChanged(pos -> {
			//System.out.println(pos);
			world.manage(engine, pos);
		});

		TiltIndicator indicator = new TiltIndicator(new Rectangle2D(WIDTH*0.035, HEIGHT-WIDTH*0.035-WIDTH*0.1, WIDTH*0.1, WIDTH*0.1), 0, 0, 0);

		// -------------------- DEBUG movement --------------------
		Engine3D.SHOW_LINES = true;
		final double speed = 0.2;
		engine.setOnKey(KeyCode.W, () -> {
			camera.move(new Point3D(speed*Math.cos(camera.getRy()+Math.PI/2), 0, speed*Math.sin(camera.getRy()+Math.PI/2)));
			plane.setPOV(camera);
		}, false);
		engine.setOnKey(KeyCode.A, () -> {
			camera.move(new Point3D(-speed*Math.cos(camera.getRy()), 0, -speed*Math.sin(camera.getRy())));
			plane.setPOV(camera);
		}, false);
		engine.setOnKey(KeyCode.S, () -> {
			camera.move(new Point3D(-speed*Math.cos(camera.getRy()+Math.PI/2), 0, -speed*Math.sin(camera.getRy()+Math.PI/2)));
			plane.setPOV(camera);
		}, false);
		engine.setOnKey(KeyCode.D, () -> {
			camera.move(new Point3D(speed*Math.cos(camera.getRy()), 0, speed*Math.sin(camera.getRy())));
			plane.setPOV(camera);
		}, false);
		engine.setOnKey(KeyCode.SPACE, () -> {
			camera.move(new Point3D(0, -speed, 0));
			plane.setPOV(camera);
		}, false);
		engine.setOnKey(KeyCode.SHIFT, () -> {
			camera.move(new Point3D(0, speed, 0));
			plane.setPOV(camera);
		}, false);
		// --------------------------------------------------------

		final double angle = 0.02;
		engine.setOnKey(KeyCode.UP, () -> {
			plane.rotateX(-angle*0.5);
			indicator.setPitch(indicator.getPitch()-angle);
		}, true);
		engine.setOnKey(KeyCode.DOWN, () -> {
			plane.rotateX(angle*0.5);
			indicator.setPitch(indicator.getPitch()+angle);
		}, true);
		engine.setOnKey(KeyCode.RIGHT, () -> {
			plane.rotateZ(angle);
			indicator.setRoll(indicator.getRoll()+angle);
		}, true);
		engine.setOnKey(KeyCode.LEFT, () -> {
			plane.rotateZ(-angle);
			indicator.setRoll(indicator.getRoll()-angle);
		}, true);

		// Move the plane
		final double speedFactor = 0.005;
		engine.setOnKey(KeyCode.Z, () -> this.planeSpeed -= speedFactor, true);
		engine.setOnKey(KeyCode.X, () -> this.planeSpeed += speedFactor, true);

		engine.addObject(plane.build());
		world.manage(engine, Point3D.ZERO);

		Light light = new Light(new Camera(new Point3D(0, -4, 0), WIDTH, HEIGHT, Math.PI/4, 100, 0.3));
		engine.getLights().add(light);

		engine.setOnKey(KeyCode.R, () -> {
			camera.setPosition(plane.getPosition().add(plane.getDirection().multiply(-3.5)).add(0, -0.5, 0));
			camera.setRx(plane.getRx());
			camera.setRy(plane.getRy());
		}, true);

		final Font mainFont = new Font("sans-serif", 15);
		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.setFont(mainFont);
			gc.fillText(plane.toString()+String.format("\nSpeed: %.3f", this.planeSpeed), WIDTH-20, 30);

			//gc.setStroke(Color.BLUE);
			//gc.strokeLine(WIDTH/2, 0, WIDTH/2, HEIGHT);

			indicator.render(gc);

			if (Math.abs(this.planeSpeed) >= speedFactor){
				Point3D vector = plane.getDirection().multiply(this.planeSpeed);
				plane.move(camera, vector);
				indicator.setYaw(plane.getRy());
				light.getCamera().move(vector);
			}

			stage.setTitle("FlightSim - FPS: "+engine.getFPS());
		});

		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}

	private static Point3D rotatePointX(Point3D point, Point3D pivot, double rx){
		Point3D p = point.subtract(pivot);
		double[] rot = Engine3D.multiply(Engine3D.getRotateX(rx), new double[]{p.getX(), p.getY(), p.getZ()});
		Point3D out = new Point3D(rot[0], rot[1], rot[2]);
		out = out.add(pivot);
		return out;
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
