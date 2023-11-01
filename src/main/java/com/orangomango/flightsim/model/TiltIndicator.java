package com.orangomango.flightsim.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TiltIndicator{
	private double pitch, yaw, roll;
	private Rectangle2D rect;

	public TiltIndicator(Rectangle2D rect, double pitch, double yaw, double roll){
		this.rect = rect;
		this.pitch = pitch;
		this.yaw = yaw;
		this.roll = roll;
	}

	public double getPitch(){
		return this.pitch;
	}

	public double getYaw(){
		return this.yaw;
	}

	public double getRoll(){
		return this.roll;
	}

	public void setPitch(double value){
		this.pitch = value;
	}

	public void setYaw(double value){
		this.yaw = value;
	}

	public void setRoll(double value){
		this.roll = value;
	}

	public void render(GraphicsContext gc){
		gc.save();
		gc.beginPath();
		gc.arc(this.rect.getMinX()+this.rect.getWidth()/2, this.rect.getMinY()+this.rect.getHeight()/2, this.rect.getWidth()/2, this.rect.getHeight()/2, 180, 360);
		gc.closePath();

		gc.setFill(Color.BLUE);
		gc.clip();
		gc.fillRect(this.rect.getMinX(), this.rect.getMinY(), this.rect.getWidth(), this.rect.getHeight());
		gc.setFill(Color.LIME);
		gc.translate(this.rect.getMinX()+this.rect.getWidth()/2, this.rect.getMinY()+this.rect.getHeight()/2);
		gc.rotate(Math.toDegrees(this.roll));
		double height = Math.cos(this.pitch) <= 0 ? this.rect.getHeight() : this.rect.getHeight()/Math.cos(this.pitch)*Math.sin(this.pitch);
		gc.fillRect(-this.rect.getWidth()/2, -height, this.rect.getWidth(), this.rect.getHeight()/2+height);
		gc.restore();

		gc.save();
		gc.translate(this.rect.getMinX()+this.rect.getWidth()/2, this.rect.getMinY()+this.rect.getHeight()/2);
		gc.rotate(-Math.toDegrees(this.yaw));
		gc.setFill(Color.BLACK);
		final double osize = this.rect.getHeight()*0.2;
		gc.fillOval(-osize/2, -this.rect.getHeight()/2-osize/2, osize, osize);
		gc.restore();

		gc.save();
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		gc.strokeOval(this.rect.getMinX(), this.rect.getMinY(), this.rect.getWidth(), this.rect.getHeight());
		gc.strokeLine(this.rect.getMinX(), this.rect.getMinY()+this.rect.getHeight()/2, this.rect.getMaxX(), this.rect.getMinY()+this.rect.getHeight()/2);
		gc.restore();
	}
}