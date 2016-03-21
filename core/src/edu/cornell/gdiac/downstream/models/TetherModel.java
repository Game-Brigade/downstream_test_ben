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
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.downstream.obstacle.*;

public class TetherModel extends WheelObstacle {

  /** Width of this tether, used for collision detection */
  private static final int TETHER_DEFAULT_WIDTH = 40;
  
  /** Height of this tether, used for collision detection */
  private static final int TETHER_DEFAULT_HEIGHT = 40;
  
  /** The range at which the player can enter orbit around this tether */
  private static final int TETHER_DEFAULT_RANGE = 400;

  /** The radius at which the player orbits a tether */
  private static final float TETHER_DEFAULT_RADIUS = .1f;
  
  private static final BodyDef.BodyType TETHER_BODY_TYPE = BodyDef.BodyType.StaticBody;

  /** The type of this tether */
  private TetherType type;

  /** Tethers can be lilipads, lanterns, or lotus flowers */
  public enum TetherType {
    Lilipad,
    Lantern,
    Lotus
  };

  public TetherModel(float x, float y, TetherType type) {
    //super(x, y, TETHER_DEFAULT_WIDTH, TETHER_DEFAULT_HEIGHT);
	  super(x,y,TETHER_DEFAULT_RADIUS);
	  setType(type);
    setBodyType(TETHER_BODY_TYPE);
  }
  
  public TetherModel(float x, float y, float w, float h) {
    //super(x,y,w/4,h/4);
	  super(x,y,TETHER_DEFAULT_RADIUS);
	 setType(TetherType.Lilipad);
    setBodyType(TETHER_BODY_TYPE);
  }

  public void setType(TetherType newType) {
    type = newType;
  }

  public Vector2 calculateAttractiveForce(Obstacle player) {
    Vector2 direction = this.getPosition().sub(player.getPosition());
    float radius = direction.len();
    float forceMagnitude = (float) (player.getMass() * player.getLinearVelocity().len2() / radius);
    return direction.setLength(forceMagnitude);
  }
  
  public float getRadius() {
    return TETHER_DEFAULT_RADIUS;
  }

}