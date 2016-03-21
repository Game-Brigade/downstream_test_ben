/*
 * Tether.java
 *
 * A tether object. A player enters it's radius and begins orbitting
 *
 * Author: Dashiell Brown
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.downstream.models;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.downstream.*;
import edu.cornell.gdiac.downstream.obstacle.*;

public class PlayerModel extends BoxObstacle {

	/** Fist move and need to be affected by forces */
	private static final BodyDef.BodyType PLAYER_FISH_BODY_TYPE = BodyDef.BodyType.DynamicBody;

	/** The density of the player */
	private static final float DEFAULT_DENSITY  =  1.0f;
	/** The friction of the player */
	private static final float DEFAULT_FRICTION = 0.1f;
	/** The restitution of the player */
	private static final float DEFAULT_RESTITUTION = 0.4f;
	/** The thrust factor to convert player input into thrust */
	private static final float DEFAULT_THRUST = 30.0f;

	
	/** Cache object for transforming the force according the object angle */
	public Affine2 affineCache = new Affine2();
	/** Cache object for left afterburner origin */
	public Vector2 leftOrigin = new Vector2();
	/** Cache object for right afterburner origin */
	public Vector2 rghtOrigin = new Vector2();
	

	private int health;

	private Vector2 force;

	/** Create a new player at x,y. */
	public PlayerModel(float x, float y, float width, float height) {
		super(x, y, width, height);
		setBodyType(PLAYER_FISH_BODY_TYPE);
		setDensity(DEFAULT_DENSITY);
		setDensity(DEFAULT_DENSITY);
		setFriction(DEFAULT_FRICTION);
		setRestitution(DEFAULT_RESTITUTION);
		setGravityScale(0);
		setName("player");
		force = new Vector2();
		health = 1;
	}

	public boolean isAlive() {
		return health > 0;
	}

	public void applyTetherForce(Vector2 tetherForce) {
		body.applyForceToCenter(tetherForce, true);
	}

	public void applyTetherForce(TetherModel tether) {
		applyTetherForce(tether.calculateAttractiveForce(this));
	}

	public Vector2 getInitialTangentPoint(Vector2 tether) {
		if (getVX() == 0) setVX(.00001f);
		if (getVY() == 0) setVY(.00001f);
		float slope = getVY() / getVX();
		float xtan = (slope * getX() - getY() + tether.x / slope + tether.y) / (slope + 1 / slope);
		float ytan = slope * xtan - slope * getX() + getY();
		return new Vector2(xtan, ytan);
	}
	
	public Vector2 timeToIntersect(Vector2 target) {
		return new Vector2(target.x - getX() / getLinearVelocity().x,
						   target.y - getY() / getLinearVelocity().y);
	}
	
	public boolean willIntersect(Vector2 target) {
		Vector2 time = timeToIntersect(target);
		return time.x > -0.009 && time.y > -0.009;
	}

	
	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		super.draw(canvas);  
//		canvas.drawLeadingLine(body.getPosition(), new Vector2(0,0));

	}
	
	
	



	
	/**
	 * Returns the force applied to this rocket.
	 * 
	 * This method returns a reference to the force vector, allowing it to be modified.
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the force applied to this rocket.
	 */
	public Vector2 getForce() {
		return force;
	}

	/**
	 * Returns the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the x-component of the force applied to this rocket.
	 */
	public float getFX() {
		return force.x;
	}

	/**
	 * Sets the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @param value the x-component of the force applied to this rocket.
	 */
	public void setFX(float value) {
		force.x = value;
	}

	/**
	 * Returns the y-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the y-component of the force applied to this rocket.
	 */
	public float getFY() {
		return force.y;
	}

	/**
	 * Sets the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @param value the x-component of the force applied to this rocket.
	 */
	public void setFY(float value) {
		force.y = value;
	}
	
	/**
	 * Returns the amount of thrust that this rocket has.
	 *
	 * Multiply this value times the horizontal and vertical values in the
	 * input controller to get the force.
	 *
	 * @return the amount of thrust that this rocket has.
	 */
	public float getThrust() {
		return DEFAULT_THRUST;
	}
	
	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// Get the box body from our parent class
		if (!super.activatePhysics(world)) {
			return false;
		}
		
		//#region INSERT CODE HERE
		// Insert code here to prevent the body from rotating
		
		setFixedRotation(true);
		
		//#endregion
		
		return true;
	}
	
	
	/**
	 * Applies the force to the body of this ship
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}
		
		// Orient the force with rotation.
		affineCache.setToRotationRad(getAngle());
		affineCache.applyTo(force);
		
		//#region INSERT CODE HERE
		// Apply force to the rocket BODY, not the rocket
		
		body.applyForceToCenter(force,true);
		
		//#endregion
	}

	public void resolveDirection() {
		setAngle((float) Math.atan2(getVY(),getVX()));
		
	}



	
	
	
}