package com.csharks.thrustcopter.box2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.csharks.thrustcopter.BaseScene;
import com.csharks.thrustcopter.Pickup;
import com.csharks.thrustcopter.ThrustCopter;

public class ThrustCopterSceneBox2D extends BaseScene {
	private Box2DDebugRenderer debugRenderer;
	private static final boolean DRAW_BOX2D_DEBUG=true;
	private World world;
	private static final int BOX2D_TO_CAMERA =10;
	private OrthographicCamera box2dCam;
	private Body planeBody;
	private Body terrainBodyDown;
	private Body terrainBodyUp;
	private Body meteorBody;
	private Body lastPillarBody;
	private Body bodyA;
	private Body bodyB;
	private Body unknownBody;
	private Body hitBody;
	private static final int touchImpulse=1000;
	Vector2 meteorPosition= new Vector2();
	Array<Body> pickupsInScene = new Array<Body>();
	Array<Body> pillars = new Array<Body>();
	Array<Body> setForRemoval = new Array<Body>();
	
	TextureRegion bgRegion;
	TextureRegion terrainBelow;
	TextureRegion terrainAbove;
	TextureRegion tap2;
	TextureRegion tap1;
	TextureRegion pillarUp;
	TextureRegion pillarDown;
	Texture fuelIndicator;
	Texture gameOver;	
	float tapDrawTime;
	private static final float tapDrawTimeMax=1.0f;
	float terrainOffset;
	Animation plane;
	float planeAnimTime;
	Vector2 planePosition= new Vector2();
	Vector2 planeDefaultPosition= new Vector2();
	Vector2 gravity= new Vector2();
	Vector3 touchPosition=new Vector3();
	Vector3 touchPositionBox2D=new Vector3();
	Vector2 tmpVector=new Vector2();
	TextureRegion toDraw;
	float previousCamXPos;
	
	GameState gameState = GameState.Init;
	float deltaPosition;
	Rectangle planeRect=new Rectangle();
	Rectangle obstacleRect=new Rectangle();
	TextureRegion selectedMeteorTexture;
	boolean meteorInScene;
	private static final int meteorSpeed=50;
	float nextMeteorIn;
	Music music;
	Sound tapSound;
	Sound crashSound;
	Sound spawnSound;
	SpriteBatch batch;
	OrthographicCamera camera;
	TextureAtlas atlas;
	Vector3 pickupTiming=new Vector3();
	Pickup tempPickup;
	int starCount;
	float fuelCount;
	float shieldCount;
	int fuelPercentage;
	Animation shield;
	BitmapFont font;
	float score;
	ParticleEffect smoke;
	ParticleEffect explosion;
	private boolean gamePaused=false;

	public ThrustCopterSceneBox2D(ThrustCopter thrustCopter) {
		super(thrustCopter);
		batch=game.batch;
		camera=game.camera;
		atlas=game.atlas;
		font =game.font;
		fuelIndicator=game.manager.get("life.png",Texture.class);
		bgRegion = atlas.findRegion("background");
		terrainBelow=atlas.findRegion("groundGrass");
		tap2 = atlas.findRegion("tap2");
		tap1 = atlas.findRegion("tap1");
		pillarUp = atlas.findRegion("rockGrassUp");
		pillarDown = atlas.findRegion("rockGrassDown");
		gameOver = game.manager.get("gameover.png",Texture.class);
		terrainAbove=new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);
		
		plane = new Animation(0.05f, atlas.findRegion("planeRed1"), 
				atlas.findRegion("planeRed2"), 
				atlas.findRegion("planeRed3"), 
				atlas.findRegion("planeRed2"));
		plane.setPlayMode(PlayMode.LOOP);
		
		shield = new Animation(0.1f, atlas.findRegion("shield1"), 
				atlas.findRegion("shield2"), 
				atlas.findRegion("shield3"), 
				atlas.findRegion("shield2"));
		shield.setPlayMode(PlayMode.LOOP);
		
		selectedMeteorTexture=atlas.findRegion("meteorBrown_med1");
		
		if(game.soundEnabled){
		music = game.manager.get("sounds/journey.mp3", Music.class);
		music.setLooping(true);
		music.play();
		music.setVolume(game.soundVolume);
		
		tapSound = game.manager.get("sounds/pop.ogg", Sound.class);
		crashSound = game.manager.get("sounds/crash.ogg", Sound.class);
		spawnSound = game.manager.get("sounds/alarm.ogg", Sound.class);
		}
		
		smoke=game.manager.get("Smoke", ParticleEffect.class);
		explosion=game.manager.get("Explosion", ParticleEffect.class);
		
		resetScene();
		
		initPhysics();
		addPillar();
	}
	private void resetScene(){
		meteorInScene=false;
		nextMeteorIn=(float)Math.random()*5;
		pickupTiming.x=1+(float)Math.random()*2;
		pickupTiming.y=3+(float)Math.random()*2;
		pickupTiming.z=1+(float)Math.random()*3;
		terrainOffset=0;
		planeAnimTime=0;
		tapDrawTime=0;
		starCount=0;
		score=0;
		shieldCount=15;
		fuelCount=100;
		fuelPercentage=114;
		planeDefaultPosition.set(250-88/2, 240-73/2);
		planePosition.set(planeDefaultPosition.x,planeDefaultPosition.y);
		smoke.setPosition(planePosition.x+20, planePosition.y+30);
		if(gameState == GameState.GameOver)resetPhysics();
	}

	private void resetPhysics() {
		for(Body vec: pillars) {
			world.destroyBody(vec);
		}
		pillars.clear();
		for(Body vec: pickupsInScene) {
			world.destroyBody(vec);
		}
		pickupsInScene.clear();
		tmpVector.set(800,500);
		meteorBody.setTransform(tmpVector,0);
		tmpVector.set(planePosition);
		planeBody.setTransform(tmpVector.x/BOX2D_TO_CAMERA,tmpVector.y/BOX2D_TO_CAMERA, 0);
		planeBody.setAwake(true);
		box2dCam.position.set(40, 24, 0);
		previousCamXPos=40;
		terrainBodyUp.setTransform(box2dCam.position.x+0.4f, 44.5f, 0);
		terrainBodyDown.setTransform(box2dCam.position.x+0.4f, 3.5f, 0);
		lastPillarBody=null;
		addPillar();
	}
	private void initPhysics() {
		world = new World(new Vector2(8, -10), true);
		debugRenderer = new Box2DDebugRenderer();
		box2dCam=new OrthographicCamera(80, 48);
		box2dCam.position.set(40, 24, 0);
		previousCamXPos=40;
		
		planeBody=createPhysicsObjectFromGraphics(plane.getKeyFrame(0),planePosition,BodyType.DynamicBody);
		//planeBody.setSleepingAllowed(false);
		terrainBodyUp=createPhysicsObjectFromGraphics(terrainAbove,new Vector2(terrainAbove.getRegionWidth()/2,480-terrainAbove.getRegionHeight()/2),BodyType.StaticBody);
		terrainBodyDown=createPhysicsObjectFromGraphics(terrainBelow,new Vector2(terrainBelow.getRegionWidth()/2,terrainBelow.getRegionHeight()/2),BodyType.StaticBody);
		meteorBody=createPhysicsObjectFromGraphics(selectedMeteorTexture,new Vector2(800,500),BodyType.KinematicBody);
		
		// You can savely ignore the rest of this method :)
		world.setContactListener(new ContactListener() {
				@Override
				public void beginContact (Contact contact) {
				}

				@Override
				public void endContact (Contact contact) {
					bodyA=contact.getFixtureA().getBody();
					bodyB=contact.getFixtureB().getBody();
					boolean planeFound=false;
					if(bodyA.equals(planeBody)){
						planeFound=true;
						unknownBody=bodyB;
					}else if(bodyB.equals(planeBody)){
						planeFound=true;
						unknownBody=bodyA;
					}
					if(planeFound){
						ItemType itemType=getItemType(unknownBody);
						if(itemType==ItemType.Terrain){
							endGame();
						}else if(shieldCount<=0 && (itemType==ItemType.Meteor||itemType==ItemType.Pillar)){
							endGame();
						}else if(itemType==ItemType.Pickup){
							pickIt(unknownBody);
						}
					}
				}

				@Override
				public void preSolve (Contact contact, Manifold oldManifold) {
					bodyA=contact.getFixtureA().getBody();
					bodyB=contact.getFixtureB().getBody();
					boolean planeFound=false;
					if(bodyA.equals(planeBody)){
						planeFound=true;
						unknownBody=bodyB;
					}else if(bodyB.equals(planeBody)){
						planeFound=true;
						unknownBody=bodyA;
					}
					if(planeFound){
						ItemType itemType=getItemType(unknownBody);
						if(shieldCount>0 && (itemType==ItemType.Meteor||itemType==ItemType.Pillar)){
							contact.setEnabled(false);
						}else if(itemType==ItemType.Pickup){
							contact.setEnabled(false);
						}
					}
				}

				@Override
				public void postSolve(Contact contact, ContactImpulse impulse) {
				}
		});
	}

	private Body createPhysicsObjectFromGraphics(TextureRegion region, Vector2 position, BodyType bodyType) {
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = bodyType;
		boxBodyDef.position.x = position.x/BOX2D_TO_CAMERA;
		boxBodyDef.position.y = position.y/BOX2D_TO_CAMERA;
		Body boxBody = world.createBody(boxBodyDef);
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(region.getRegionWidth()/(2*BOX2D_TO_CAMERA), region.getRegionHeight()/(2*BOX2D_TO_CAMERA));
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = boxPoly;
		fixtureDef.density=1;
		fixtureDef.restitution=0.2f;
		boxBody.createFixture(fixtureDef);
		
		//boxBody.createFixture(boxPoly, 1);
		boxPoly.dispose();
		boxBody.setUserData(region);
		return boxBody;
	}

	@Override
	public void render (float delta) {
		super.render(delta);
		if(gamePaused){
			return;
		}
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		updateSceneBox2D(delta);
		drawSceneBox2D();
		if(DRAW_BOX2D_DEBUG){
			box2dCam.update();
			debugRenderer.render(world, box2dCam.combined);
		}
	}
	private void updateSceneBox2D(float deltaTime) {
		if(Gdx.input.justTouched()){
			if(game.soundEnabled)tapSound.play(game.soundVolume);
			if(gameState == GameState.Init) {
				gameState = GameState.Action;
				return;
			}
			if(gameState == GameState.GameOver) {
				resetScene();
				gameState = GameState.Init;
				return;
			}
			if(fuelCount>0){
			touchPosition.set(Gdx.input.getX(),Gdx.input.getY(),0);
			touchPositionBox2D.set(touchPosition);
			box2dCam.unproject(touchPositionBox2D);
			tmpVector.set(planeBody.getPosition());
			tmpVector.sub( touchPositionBox2D.x, touchPositionBox2D.y).nor();
			tmpVector.scl(touchImpulse-MathUtils.clamp(25*Vector2.dst(touchPositionBox2D.x, touchPositionBox2D.y, planeBody.getPosition().x, planeBody.getPosition().y), 0, touchImpulse));
			planeBody.applyLinearImpulse(tmpVector, planeBody.getPosition(), true);
			tapDrawTime=tapDrawTimeMax;
			camera.unproject(touchPosition);
			}
		}
		smoke.update(deltaTime);
		if(gameState == GameState.Init || gameState == GameState.GameOver) {
			if(gameState == GameState.GameOver)explosion.update(deltaTime);
			return;
		}
		planeAnimTime+=deltaTime;
		deltaPosition=(box2dCam.position.x-previousCamXPos)*BOX2D_TO_CAMERA;
		previousCamXPos=box2dCam.position.x;
		terrainOffset-=deltaPosition;
		if(terrainOffset*-1>terrainBelow.getRegionWidth()){
			terrainOffset=0;
		}
		if(terrainOffset>0){
			terrainOffset=-terrainBelow.getRegionWidth();
		}
		
		if(meteorInScene){
			meteorPosition=meteorBody.getPosition();
			if(meteorPosition.x-box2dCam.position.x<-42){
				meteorInScene=false;
			}
			meteorPosition.scl(BOX2D_TO_CAMERA);
		}
		for(Body vec: pillars) {
			if(vec.getPosition().x+5.4-box2dCam.position.x<-42){
				pillars.removeValue(vec, false);
				world.destroyBody(vec);
			}
		}
		for(Body pickup: pickupsInScene) {
			if(pickup.getPosition().x+1.9-box2dCam.position.x<-42){
				pickupsInScene.removeValue(pickup, false);
				world.destroyBody(pickup);
			}
		}
		
		if(lastPillarBody.getPosition().x<box2dCam.position.x){
			addPillar();
		}
		tapDrawTime-=deltaTime;
		nextMeteorIn-=deltaTime;
		if(nextMeteorIn<=0){
			launchMeteor();
		}
		checkAndCreatePickup(deltaTime);
		fuelCount-=6*deltaTime;
		fuelPercentage=(int) (114*fuelCount/100);
		shieldCount-=deltaTime;
		score+=deltaTime;
		
		world.step(deltaTime, 8, 3);
		box2dCam.position.x=planeBody.getPosition().x+19.4f;
		terrainBodyUp.setTransform(box2dCam.position.x+0.4f, 44.5f, 0);
		terrainBodyDown.setTransform(box2dCam.position.x+0.4f, 3.5f, 0);
		
		if(setForRemoval.size>0){
		for(Body pickup: setForRemoval) {
			world.destroyBody(pickup);
		}
		setForRemoval.clear();
		}
		//planePosition=planeBody.getPosition();
	}
	
	private void drawSceneBox2D() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(bgRegion,0,0);
		batch.enableBlending();
		
		for(Body vec: pillars) {
			tmpVector.set(vec.getPosition());
			tmpVector.scl(BOX2D_TO_CAMERA);
			tmpVector.x-=(box2dCam.position.x-40)*BOX2D_TO_CAMERA;
			toDraw=(TextureRegion) vec.getUserData();
			batch.draw(toDraw, tmpVector.x-toDraw.getRegionWidth()/2, tmpVector.y-toDraw.getRegionHeight()/2);
		}
		
		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480 - terrainAbove.getRegionHeight());
		
		if(tapDrawTime>0){
			batch.draw(tap2, touchPosition.x-29.5f, touchPosition.y-29.5f);
		}
		if(gameState == GameState.Init){
			batch.draw(tap1, planePosition.x, planePosition.y-80);
		}
		if(gameState == GameState.GameOver){
			batch.draw(gameOver, 400-206, 240-80);
		}
		for(Body pickup: pickupsInScene) {
			tempPickup=(Pickup)pickup.getUserData();
			tmpVector.set(pickup.getPosition());
			tmpVector.scl(BOX2D_TO_CAMERA);
			tmpVector.x-=(box2dCam.position.x-40)*BOX2D_TO_CAMERA;
			batch.draw(tempPickup.pickupTexture, tmpVector.x-19, tmpVector.y-19);
		}
		planePosition=planeBody.getPosition();
		planePosition.scl(BOX2D_TO_CAMERA);
		smoke.setPosition(planePosition.x+20-(box2dCam.position.x-40)*BOX2D_TO_CAMERA-44, planePosition.y-7);
		smoke.draw(batch);
		batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x-(box2dCam.position.x-40)*BOX2D_TO_CAMERA-44, planePosition.y-36.5f);
		if(shieldCount>0){
			batch.draw(shield.getKeyFrame(planeAnimTime), planePosition.x-20-(box2dCam.position.x-40)*BOX2D_TO_CAMERA-44, planePosition.y-36.5f);
			font.draw(batch, ""+((int)shieldCount), 390, 450);
		}
		if(meteorInScene){
			batch.draw(selectedMeteorTexture,meteorPosition.x-(box2dCam.position.x-40)*BOX2D_TO_CAMERA-selectedMeteorTexture.getRegionWidth()/2,meteorPosition.y-selectedMeteorTexture.getRegionHeight()/2);
		}
		font.draw(batch, ""+(int)(starCount+score), 700, 450);
		batch.setColor(Color.BLACK);
		batch.draw(fuelIndicator, 10, 350);
		batch.setColor(Color.WHITE);
		batch.draw(fuelIndicator,10,350,0,0,fuelPercentage,119);
		if(gameState == GameState.GameOver)explosion.draw(batch);
		batch.end();
	}

	private void addPillar() {
		if(pillars.size==0){
			tmpVector.x=(float) (800 + Math.random()*400);
		}else{
			tmpVector.x=lastPillarBody.getPosition().x*BOX2D_TO_CAMERA+(float) (600 + Math.random()*400);
		}
		Body pillar;
		if(MathUtils.randomBoolean()){
			pillar=createPillarBody(pillarUp,new Vector2(tmpVector.x+pillarUp.getRegionWidth()/2,pillarUp.getRegionHeight()/2),BodyType.StaticBody);
		}else{
			pillar=createPillarBody(pillarDown,new Vector2(tmpVector.x+pillarDown.getRegionWidth()/2,480-pillarDown.getRegionHeight()/2),BodyType.StaticBody);
		}
		lastPillarBody=pillar;
		pillars.add(pillar);
	}
	private Body createPillarBody(TextureRegion region, Vector2 position, BodyType bodyType) {
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = bodyType;
		boxBodyDef.position.x = position.x/BOX2D_TO_CAMERA;
		boxBodyDef.position.y = position.y/BOX2D_TO_CAMERA;
		Body boxBody = world.createBody(boxBodyDef);
		PolygonShape trianglePoly = new PolygonShape();
		
		if(region == pillarUp){
			float[] vertices = {-5.4f, -11.95f, 1.1f, 11.95f, 5.4f, -11.95f};
			trianglePoly.set(vertices);
		}else{
			float[] vertices = {-5.4f, 11.95f, 5.4f, 11.95f, 1.1f, -11.95f};
			trianglePoly.set(vertices);
		}
		boxBody.createFixture(trianglePoly, 1);
		trianglePoly.dispose();
		boxBody.setUserData(region);
		return boxBody;
	}
	private void launchMeteor() {
		nextMeteorIn=1.5f+(float)Math.random()*5;
		if(meteorInScene){
			return;
		}
		tmpVector.set(box2dCam.position.x+42,0);
		if(game.soundEnabled)spawnSound.play(game.soundVolume);
		meteorInScene=true;
		tmpVector.y=(float)(80+Math.random()*320)/BOX2D_TO_CAMERA;
		meteorBody.setTransform(tmpVector,0);
		Vector2 destination=new Vector2();
		destination.x=box2dCam.position.x-42;
		destination.y=(float) (80+Math.random()*320)/BOX2D_TO_CAMERA;
		destination.sub(tmpVector).nor();
		destination.scl(meteorSpeed);
		meteorBody.setLinearVelocity(destination);
	}
	private void checkAndCreatePickup(float delta){
		pickupTiming.sub(delta);
		if(pickupTiming.x<=0){
			pickupTiming.x=(float)(0.5+Math.random()*0.5);
			if(addPickup(Pickup.STAR))
				pickupTiming.x=1+(float)Math.random()*2;
		}
		if(pickupTiming.y<=0){
			pickupTiming.y=(float)(0.5+Math.random()*0.5);
			if(addPickup(Pickup.FUEL))
				pickupTiming.y=3+(float)Math.random()*2;
		}
		if(pickupTiming.z<=0){
			pickupTiming.z=(float)(0.5+Math.random()*0.5);
			if(addPickup(Pickup.SHIELD))
				pickupTiming.z=10+(float)Math.random()*3;
		}
	}
	
	/** we instantiate this vector and the callback here so we don't irritate the GC **/
	Vector3 testPoint = new Vector3();
	QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture (Fixture fixture) {
			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};
	
	private boolean addPickup(int pickupType) {
		testPoint.x=box2dCam.position.x+42;
		testPoint.y=(float)(80+Math.random()*320)/BOX2D_TO_CAMERA;
		
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 1.9f, testPoint.y - 1.9f, testPoint.x + 1.9f, testPoint.y + 1.9f);
		
		if (hitBody != null) {
			return false;
		}
		testPoint.scl(BOX2D_TO_CAMERA);
		tempPickup=new Pickup(pickupType, game.manager);
		Body pickupBody=createPhysicsObjectFromGraphics(tempPickup.pickupTexture,new Vector2(testPoint.x,testPoint.y),BodyType.StaticBody);
		pickupBody.setUserData(tempPickup);
		pickupsInScene.add(pickupBody);
		return true;
	}
	private void pickIt(Body pickupBody) {
		Pickup pickup=(Pickup)pickupBody.getUserData();
		if(game.soundEnabled)pickup.pickupSound.play(game.soundVolume);
		switch(pickup.pickupType){
		case Pickup.STAR:
			starCount+=pickup.pickupValue;
			break;
		case Pickup.SHIELD:
			shieldCount=pickup.pickupValue;
			break;
		case Pickup.FUEL:
			fuelCount=pickup.pickupValue;
			break;
		}
		setForRemoval.add(pickupBody);
		pickupsInScene.removeValue(pickupBody, false);	
	}
	private void endGame(){
		if(gameState != GameState.GameOver){
			if(game.soundEnabled)crashSound.play(game.soundVolume);
			planeBody.setAwake(false);
			gameState = GameState.GameOver;
			explosion.reset();
			planePosition=planeBody.getPosition();
			planePosition.scl(BOX2D_TO_CAMERA);
			explosion.setPosition(planePosition.x-(box2dCam.position.x-40)*BOX2D_TO_CAMERA+10, planePosition.y);
			
		}	
	}
	
	private ItemType getItemType(Body body) {
		if(body.equals(meteorBody)){
			return ItemType.Meteor;
		}else if(body.equals(terrainBodyDown)||body.equals(terrainBodyUp)){
			return ItemType.Terrain;
		}else if(pickupsInScene.contains(body, false)){// body.getUserData() instanceof Pickup){
			return ItemType.Pickup;
		}else {
			return ItemType.Pillar;
		}
	}

	@Override
	protected void handleBackPress() {System.out.println("back");
		if(gamePaused){
			resume();
		}else{
			pause();
		}
	}
	@Override
	public void pause () {
		gamePaused=true;
	}

	@Override
	public void resume () {
		gamePaused=false;
	}

	static enum GameState {
		Init, Action, GameOver
	}
	static enum ItemType {
		Pickup, Terrain, Meteor, Pillar
	}
	
	@Override
	public void dispose () {
		tapSound.dispose();
		crashSound.dispose();
		spawnSound.dispose();
		music.dispose();
		pillars.clear();
		smoke.dispose();
		explosion.dispose();
	}
}
