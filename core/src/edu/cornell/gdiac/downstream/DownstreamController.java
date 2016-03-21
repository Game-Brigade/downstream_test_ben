/*
 * FishController.java
 *
 * Author: Walker M. White && Dashiell Brown
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.downstream;

import java.util.ArrayList;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.downstream.*;
import edu.cornell.gdiac.downstream.obstacle.*;
import edu.cornell.gdiac.downstream.models.*;

/**
 * Gameplay specific controller for Downstream.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class DownstreamController extends WorldController implements ContactListener {
	/** Reference to the fish texture */
	private static final String KOI_TEXTURE = "koi/koi.png";
	/** The reference for the tether textures  */
	private static final String LILY_TEXTURE = "tethers/lilypad.png";
	/** Reference to the enemy image assets */
	private static final String ENEMY_TEXTURE = "enemy/enemy.png";

	/** The asset for the collision sound */
	//private static final String  COLLISION_SOUND = "fish/bump.mp3";
	
	
	/** Texture assets for the koi */
	private TextureRegion koiTexture;
	/** Texture assets for the lilypads */
	private TextureRegion lilyTexture;
	/** Texture assets for the enemy fish */
	private TextureRegion enemyTexture;

	/** Texture filmstrip for the main afterburner */
	//private FilmStrip mainTexture;
	
	
	/** Track asset loading from all instances and subclasses */
	private AssetState fishAssetState = AssetState.EMPTY;
	
	private boolean tethered;
	
	private float PLAYER_LINEAR_VELOCITY = 8f;
	private float CAMERA_LINEAR_VELOCITY = 8f;
	
	private boolean enableSlow = false;
	private boolean enableLeadingLine = false;
	private boolean enableTetherRadius = true;
	
	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (fishAssetState != AssetState.EMPTY) {
			return;
		}
		
		fishAssetState = AssetState.LOADING;

		
		manager.load(ENEMY_TEXTURE, Texture.class);
		assets.add(ENEMY_TEXTURE);
		
		// Ship textures
		manager.load(KOI_TEXTURE, Texture.class);
		assets.add(KOI_TEXTURE);
		
		manager.load(LILY_TEXTURE, Texture.class);
		assets.add(LILY_TEXTURE);
		
		//sounds
		//manager.load(MAIN_FIRE_SOUND, Sound.class);
		//assets.add(MAIN_FIRE_SOUND);
		

		super.preLoadContent(manager);
	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (fishAssetState != AssetState.LOADING) {
			return;
		}

		enemyTexture = createTexture(manager,ENEMY_TEXTURE,false);
		koiTexture = createTexture(manager,KOI_TEXTURE,false);
		lilyTexture = createTexture(manager,LILY_TEXTURE,false);
		
		SoundController sounds = SoundController.getInstance();
		//sounds.allocate(manager,MAIN_FIRE_SOUND);
		
		
		super.loadContent(manager);
		fishAssetState = AssetState.COMPLETE;
	}
	
	// Physics constants for initialization
	/** Density of non-enemy objects */
	private static final float BASIC_DENSITY   = 0.0f;
	/** Density of the enemy objects */
	private static final float ENEMY_DENSITY   = 1.0f;
	/** Friction of non-enemy objects */
	private static final float BASIC_FRICTION  = 0.1f;
	/** Friction of the enemy objects */
	private static final float ENEMY_FRICTION  = 0.3f;
	/** Collision restitution for all objects */
	private static final float BASIC_RESTITUTION = 0.1f;
	/** Threshold for generating sound on collision */
	private static final float SOUND_THRESHOLD = 1.0f;
	
	private static final float TETHER_DENSITY = ENEMY_DENSITY;
	private static final float TETHER_FRICTION = ENEMY_FRICTION;
	private static final float TETHER_RESTITUTION = BASIC_RESTITUTION;

	// Since these appear only once, we do not care about the magic numbers.
	// In an actual game, this information would go in a data file.
	// Wall vertices
	private static final float[][] LAND = {{}};

	private static final float[] WALL1 = { 0.0f, 18.0f, 16.0f, 18.0f, 16.0f, 17.0f,
										   8.0f, 15.0f,  1.0f, 17.0f,  2.0f,  7.0f,
										   3.0f,  5.0f,  3.0f,  1.0f, 16.0f,  1.0f,
										  16.0f,  0.0f,  0.0f,  0.0f};
	private static final float[] WALL2 = {32.0f, 18.0f, 32.0f,  0.0f, 16.0f,  0.0f,
										  16.0f,  1.0f, 31.0f,  1.0f, 30.0f, 10.0f,
										  31.0f, 16.0f, 16.0f, 17.0f, 16.0f, 18.0f};
	private static final float[] WALL3 = { 4.0f, 10.5f,  8.0f, 10.5f,
            							   8.0f,  9.5f,  4.0f,  9.5f};
	
	private static final float[] WALLX = { 0.0f, 0.0f, 32.0f, 0.0f,
										   16.0f, 32.0f, 0.0f, 0.0f, 16.0f};

	// The positions of the crate pyramid
//	private static final float[] BOXES = { 14.5f, 14.25f,
//            							   13.0f, 12.00f, 16.0f, 12.00f,
//            							   11.5f,  9.75f, 14.5f,  9.75f, 17.5f, 9.75f,
//            							   13.0f,  7.50f, 16.0f,  7.50f,
//            							   11.5f,  5.25f, 14.5f,  5.25f, 17.5f, 5.25f,
//            							   10.0f,  3.00f, 13.0f,  3.00f, 16.0f, 3.00f, 19.0f, 3.0f};
	private static final float[] BOXES = {};
	
	private ArrayList<TetherModel> tethers = new ArrayList<TetherModel>();

	// Other game objects
	/** The initial koi position */
	private static Vector2 KOI_POS = new Vector2(24, 4);
	/** The goal door position */
	private static Vector2 GOAL_POS = new Vector2( 6, 12);

	// Physics objects for the game
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;
	/** Reference to the player avatar */
	private PlayerModel koi;
	
	private EnemyModel eFish;

	/**
	 * Creates and initialize a new instance of Downstream
	 *
	 * The game has no  gravity and deafault settings
	 */
	public DownstreamController() {
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		tethered = false;
	}
	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );
		
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
		canvas.setCameraPosition(koi.getPosition().cpy().scl(scale));
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Add level goal
		float dwidth  = getBackground().getWidth()/scale.x;
		float dheight = getBackground().getHeight()/scale.y;
		
		boolean sensorTethers = true;
		
		float rad = lilyTexture.getRegionWidth()/2;

		TetherModel lily = new TetherModel(12, 2, dwidth, dheight);
		lily.setBodyType(BodyDef.BodyType.StaticBody);
		lily.setName("lily"+ 1);
		lily.setDensity(TETHER_DENSITY);
		lily.setFriction(TETHER_FRICTION);
		lily.setRestitution(TETHER_RESTITUTION);
		lily.setSensor(sensorTethers);
		lily.setDrawScale(scale);
		lily.setTexture(lilyTexture);
		addObject(lily);
		tethers.add(lily);
		
		lily = new TetherModel(6, 12, dwidth, dheight);
		lily.setBodyType(BodyDef.BodyType.StaticBody);
		lily.setName("lily"+ 2);
		lily.setDensity(TETHER_DENSITY);
		lily.setFriction(TETHER_FRICTION);
		lily.setRestitution(TETHER_RESTITUTION);
		lily.setSensor(sensorTethers);
		lily.setDrawScale(scale);
		lily.setTexture(lilyTexture);
		addObject(lily);
		tethers.add(lily);
		
		lily = new TetherModel(28, 10, dwidth, dheight);
		lily.setBodyType(BodyDef.BodyType.StaticBody);
		lily.setName("lily"+ 3);
		lily.setDensity(TETHER_DENSITY);
		lily.setFriction(TETHER_FRICTION);
		lily.setRestitution(TETHER_RESTITUTION);
		lily.setSensor(sensorTethers);
		lily.setDrawScale(scale);
		lily.setTexture(lilyTexture);
		addObject(lily);
		tethers.add(lily);
		
		lily = new TetherModel(16, 14, dwidth, dheight);
		lily.setBodyType(BodyDef.BodyType.StaticBody);
		lily.setName("lily"+ 4);
		lily.setDensity(TETHER_DENSITY);
		lily.setFriction(TETHER_FRICTION);
		lily.setRestitution(TETHER_RESTITUTION);
		lily.setSensor(sensorTethers);
		lily.setDrawScale(scale);
		lily.setTexture(lilyTexture);
		addObject(lily);
		tethers.add(lily);


		TextureRegion texture = enemyTexture;
		dwidth  = texture.getRegionWidth()/scale.x;
		dheight = texture.getRegionHeight()/scale.y;
		eFish = new EnemyModel(20, 0, dwidth, dheight);
		eFish.setDensity(ENEMY_DENSITY);
		eFish.setFriction(ENEMY_FRICTION);
		eFish.setRestitution(BASIC_RESTITUTION);
		eFish.setName("enemy");
		eFish.setDrawScale(scale);
		eFish.setTexture(texture);
		eFish.setAngle((float) (Math.PI/2));
		eFish.setBodyType(BodyDef.BodyType.StaticBody);
		eFish.setGoal(0, 0);
		addObject(eFish);

		// Create the fish avatar
		dwidth  = koiTexture.getRegionWidth()/scale.x;
		dheight = koiTexture.getRegionHeight()/scale.y;
		koi = new PlayerModel(KOI_POS.x, KOI_POS.y, dwidth, dheight);
		koi.setDrawScale(scale);
		koi.setName("koi");
		koi.setTexture(koiTexture);
	  
		addObject(koi);
		
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float dt) {
//		System.out.println(canvas.camera.viewportWidth);
//		System.out.println(canvas.camera.viewportHeight);
		
		float thrust = koi.getThrust();
		InputController input = InputController.getInstance();
		koi.setFX(thrust * input.getHorizontal());
		koi.setFY(thrust * input.getVertical());
		koi.applyForce();
		koi.setLinearVelocity(koi.getLinearVelocity().setLength(PLAYER_LINEAR_VELOCITY));
		
		if (enableSlow && input.slow) koi.setLinearVelocity(koi.getLinearVelocity().setLength(4));
		
		if (input.didTether()) tethered = !tethered;
//		if (input.space) tethered = true; else tethered = false;
		
		TetherModel closestTether = getClosestTether();
		
//		if (tethered &&
		
		
		int camera_mode = 2;
		boolean camera_zoom = true;
		switch(camera_mode) {
			// laggy catch up
			// if tethered, move quickly to center on tether, 
			// else move slowly to fish
			case 0:
				if (tethered && 
					koi.getPosition().sub(koi.getInitialTangentPoint(closestTether.getPosition())).len2() < .01) {
					koi.applyTetherForce(closestTether);
					canvas.moveCameraTowards(closestTether.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY);
					if (camera_zoom) canvas.zoomOut();
				} else {
					canvas.moveCameraTowards(koi.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY/2);
					if (camera_zoom) canvas.zoomIn();
				}
				break;
			// quick catch up
			// if tethered, move slowly to tether, 
			// else move quickly to fish
			case 1:
				if (tethered && 
					koi.getPosition().sub(koi.getInitialTangentPoint(closestTether.getPosition())).len2() < .01) {
					koi.applyTetherForce(closestTether);
					canvas.moveCameraTowards(closestTether.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY/2);
					if (camera_zoom) canvas.zoomOut();
				} else {
					canvas.moveCameraTowards(koi.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY);
					if (camera_zoom) canvas.zoomIn();
				}
				break;
			// laggy catch up with space
			// if tethered, move slowly to tether; 
			// else if pressing space move quickly to fish, 
			// else slowly to fish
			case 2:
				if (tethered && 
					koi.getPosition().sub(koi.getInitialTangentPoint(closestTether.getPosition())).len2() < .01) {
					koi.applyTetherForce(closestTether);
					canvas.moveCameraTowards(closestTether.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY/2);
					if (camera_zoom) canvas.zoomOut();
				} else {
					if (tethered) canvas.moveCameraTowards(koi.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY);
					else 			 canvas.moveCameraTowards(koi.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY/2);
					if (camera_zoom) canvas.zoomIn();
				}
				break;
			// follow player
			case 3:
				if (tethered && 
					koi.getPosition().sub(koi.getInitialTangentPoint(closestTether.getPosition())).len2() < .01) {
					koi.applyTetherForce(closestTether);
				}
				canvas.moveCameraTowards(koi.getPosition().cpy().scl(scale), CAMERA_LINEAR_VELOCITY);
		}
		
		
		koi.resolveDirection();

		
		
		float angV = 3f;
		float radius = closestTether.getPosition().dst(koi.getPosition());
		float tetherSpeed = angV*radius;
		
		float MAX_SPEED = 7f;
		float MIN_SPEED = 6f;
		
		int motionType = 0;
		
		eFish.moveTowardsGoal();
		eFish.patrol(20, 0, 20, 18);
		eFish.getGoal();
		
	    SoundController.getInstance().update();
	}
	
	private TetherModel getClosestTether() {
		TetherModel closestTether = tethers.get(0);
		float closestDistance = tethers.get(0).getPosition().sub(koi.getPosition()).len2();
		for (TetherModel tether : tethers) {
			float newDistance = tether.getPosition().sub(koi.getPosition()).len2();
			if (newDistance < closestDistance) {
				closestDistance = newDistance;
				closestTether = tether;
			}
		}
		return closestTether;
	}
	
	public void draw(float delta) {
		super.draw(delta);
		
		if (enableLeadingLine) {
			Vector2 farOff = koi.getPosition().cpy();
			farOff.add(koi.getLinearVelocity().cpy().scl(1000));
			canvas.drawLeadingLine(koi.getPosition().cpy(), farOff);
		}
		if (enableTetherRadius) {
			Vector2 closestTether = getClosestTether().getPosition().cpy().scl(scale);
			Vector2 initialTangent = koi.getInitialTangentPoint(getClosestTether().getPosition()).scl(scale);
			float radius = closestTether.dst(initialTangent);
			canvas.drawTetherCircle(closestTether, radius);
		}
		
	}
	
	/// CONTACT LISTENER METHODS
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();

		if( (body1.getUserData() == koi   && body2.getUserData() == goalDoor) ||
			(body1.getUserData() == goalDoor && body2.getUserData() == koi)) {
			setComplete(true);
		}
	}
	
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  We do not use it.
	 */ 
	public void endContact(Contact contact) {}
	
	private Vector2 cache = new Vector2();
	
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	/**
	 * Handles any modifications necessary before collision resolution
	 *
	 * This method is called just before Box2D resolves a collision.  We use this method
	 * to implement sound on contact, using the algorithms outlined similar to those in
	 * Ian Parberry's "Introduction to Game Physics with Box2D".  
	 * 
	 * However, we cannot use the proper algorithms, because LibGDX does not implement 
	 * b2GetPointStates from Box2D.  The danger with our approximation is that we may
	 * get a collision over multiple frames (instead of detecting the first frame), and
	 * so play a sound repeatedly.  Fortunately, the cooldown hack in SoundController
	 * prevents this from happening.
	 *
	 * @param  contact  	The two bodies that collided
	 * @param  oldManfold  	The collision manifold before contact
	 */

	public void preSolve(Contact contact, Manifold oldManifold) {
		float speed = 0;

		// Use Ian Parberry's method to compute a speed threshold
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();
		WorldManifold worldManifold = contact.getWorldManifold();
		Vector2 wp = worldManifold.getPoints()[0];
		cache.set(body1.getLinearVelocityFromWorldPoint(wp));
		cache.sub(body2.getLinearVelocityFromWorldPoint(wp));
		speed = cache.dot(worldManifold.getNormal());
		
		/*
		// Play a sound if above threshold
		if (speed > SOUND_THRESHOLD) {
			String s1 = ((Obstacle)body1.getUserData()).getName();
			String s2 = ((Obstacle)body2.getUserData()).getName();
			if (s1.equals("koi") || s1.startsWith("enemy") || s1.startsWith("tether")) {
				SoundController.getInstance().play(s1, COLLISION_SOUND, false, 0.5f);
			}
			if (s2.equals("koi") || s2.startsWith("enemy") || s2.startsWith("tether")) {
				SoundController.getInstance().play(s2, COLLISION_SOUND, false, 0.5f);
			}
		}
		*/
	}
	
	
}