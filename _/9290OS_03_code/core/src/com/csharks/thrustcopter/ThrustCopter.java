package com.csharks.thrustcopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopter extends ApplicationAdapter {
	SpriteBatch batch;
	FPSLogger fpsLogger;
	OrthographicCamera camera;
	TextureRegion bgRegion;
	TextureRegion terrainBelow;
	TextureRegion terrainAbove;
	TextureRegion tap2;
	TextureRegion tap1;
	TextureRegion pillarUp;
	TextureRegion pillarDown;
	Texture gameOver;	
	float tapDrawTime;
	private static final float tapDrawTimeMax=1.0f;
	float terrainOffset;
	Animation plane;
	float planeAnimTime;
	Vector2 planeVelocity= new Vector2();
	Vector2 scrollVelocity= new Vector2();
	Vector2 planePosition= new Vector2();
	Vector2 planeDefaultPosition= new Vector2();
	Vector2 gravity= new Vector2();
	Vector3 touchPosition=new Vector3();
	Vector2 tmpVector=new Vector2();
	private static final int touchImpulse=500;
	private static final Vector2 damping= new Vector2(0.99f,0.99f);
	TextureAtlas atlas;
	Viewport viewport;
	GameState gameState = GameState.Init;
	Array<Vector2> pillars = new Array<Vector2>();
	Vector2 lastPillarPosition= new Vector2();
	float deltaPosition;
	Rectangle planeRect=new Rectangle();
	Rectangle obstacleRect=new Rectangle();
	Array<TextureAtlas.AtlasRegion> meteorTextures = new Array<TextureAtlas.AtlasRegion>();
	TextureRegion selectedMeteorTexture;
	boolean meteorInScene;
	private static final int meteorSpeed=60;
	Vector2 meteorPosition= new Vector2();
	Vector2 meteorVelocity= new Vector2();
	float nextMeteorIn;
	Music music;
	Sound tapSound;
	Sound crashSound;
	Sound spawnSound;
	@Override
	public void create () {
		fpsLogger=new FPSLogger();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		//camera.setToOrtho(false, 800, 480);
		camera.position.set(400,240,0);
		viewport = new FitViewport(800, 480, camera);
		//atlas = new TextureAtlas(Gdx.files.internal("ThrustCopter.pack")); 
		atlas = new TextureAtlas(Gdx.files.internal("thrustcopterassets.txt"));  
		bgRegion = atlas.findRegion("background");
		terrainBelow=atlas.findRegion("groundGrass");
		tap2 = atlas.findRegion("tap2");
		tap1 = atlas.findRegion("tap1");
		pillarUp = atlas.findRegion("rockGrassUp");
		pillarDown = atlas.findRegion("rockGrassDown");
		gameOver = new Texture("gameover.png");	
		terrainAbove=new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);
		
		plane = new Animation(0.05f, atlas.findRegion("planeRed1"), 
				atlas.findRegion("planeRed2"), 
				atlas.findRegion("planeRed3"), 
				atlas.findRegion("planeRed2"));
		plane.setPlayMode(PlayMode.LOOP);
		
		meteorTextures.add(atlas.findRegion("meteorBrown_med1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_med2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny2"));
		
		music = Gdx.audio.newMusic(Gdx.files.internal("sounds/journey.mp3"));
		music.setLooping(true);
		music.play();
		
		tapSound = Gdx.audio.newSound(Gdx.files.internal("sounds/pop.ogg"));
		crashSound = Gdx.audio.newSound(Gdx.files.internal("sounds/crash.ogg"));
		spawnSound = Gdx.audio.newSound(Gdx.files.internal("sounds/alarm.ogg"));
		
		resetScene();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fpsLogger.log();
		updateScene();
		drawScene();
	}
	private void resetScene(){
		meteorInScene=false;
		nextMeteorIn=(float)Math.random()*5;
		terrainOffset=0;
		planeAnimTime=0;
		tapDrawTime=0;
		planeVelocity.set(100, 0);
		scrollVelocity.set(5, 0);
		gravity.set(0, -3);
		planeDefaultPosition.set(250-88/2, 240-73/2);
		planePosition.set(planeDefaultPosition.x,planeDefaultPosition.y);
		pillars.clear();
		addPillar();
	}
	
	private void updateScene() {
		if(Gdx.input.justTouched()){
			tapSound.play();
			if(gameState == GameState.Init) {
				gameState = GameState.Action;
				return;
			}
			if(gameState == GameState.GameOver) {
				gameState = GameState.Init;
				resetScene();
				return;
			}
			touchPosition.set(Gdx.input.getX(),Gdx.input.getY(),0);
			camera.unproject(touchPosition);
			tmpVector.set(planePosition.x,planePosition.y);
			tmpVector.sub( touchPosition.x, touchPosition.y).nor();
			planeVelocity.mulAdd(tmpVector, touchImpulse-MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y, planePosition.x, planePosition.y), 0, touchImpulse));
			tapDrawTime=tapDrawTimeMax;
		}
		if(gameState == GameState.Init || gameState == GameState.GameOver) {
			return;
		}
		float deltaTime = Gdx.graphics.getDeltaTime();
		planeAnimTime+=deltaTime;
		planeVelocity.scl(damping);
		planeVelocity.add(gravity);
		planeVelocity.add(scrollVelocity);
		planePosition.mulAdd(planeVelocity, deltaTime);
		deltaPosition=planePosition.x-planeDefaultPosition.x;
		terrainOffset-=deltaPosition;
		planePosition.x=planeDefaultPosition.x;
		if(terrainOffset*-1>terrainBelow.getRegionWidth()){
			terrainOffset=0;
		}
		if(terrainOffset>0){
			terrainOffset=-terrainBelow.getRegionWidth();
		}
		planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);
		
		if(meteorInScene){
			meteorPosition.mulAdd(meteorVelocity, deltaTime);
			meteorPosition.x-=deltaPosition;
			if(meteorPosition.x<-10){
				meteorInScene=false;
			}
			obstacleRect.set(meteorPosition.x + 2, meteorPosition.y + 2, selectedMeteorTexture.getRegionWidth()-4, selectedMeteorTexture.getRegionHeight()-4);
			if(planeRect.overlaps(obstacleRect)) {
				if(gameState != GameState.GameOver){
					crashSound.play();
					gameState = GameState.GameOver;
				}			
			}
		}
		
		
		for(Vector2 vec: pillars) {
			vec.x-=deltaPosition;
			if(vec.x+pillarUp.getRegionWidth()<-10){
				pillars.removeValue(vec, false);
			}
			if(vec.y==1){
				obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth()-20, pillarUp.getRegionHeight()-10);
			}else{
				obstacleRect.set(vec.x + 10, 480-pillarDown.getRegionHeight()+10, pillarUp.getRegionWidth()-20, pillarUp.getRegionHeight());
			}
			if(planeRect.overlaps(obstacleRect)) {
				if(gameState != GameState.GameOver){
					crashSound.play();
					gameState = GameState.GameOver;
				}			
			}
		}
		if(lastPillarPosition.x<400){
			addPillar();
		}
		
		if(planePosition.y < terrainBelow.getRegionHeight() - 35 || 
			planePosition.y + 73 > 480 - terrainBelow.getRegionHeight() + 35) {
			if(gameState != GameState.GameOver) {
				crashSound.play();
				gameState = GameState.GameOver;
			}
		}
		tapDrawTime-=deltaTime;
		nextMeteorIn-=deltaTime;
		if(nextMeteorIn<=0){
			launchMeteor();
		}
	}
	
	private void drawScene() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(bgRegion,0,0);
		batch.enableBlending();
		for(Vector2 vec: pillars) {
			if(vec.y==1){
				batch.draw(pillarUp, vec.x, 0);
			}else{
				batch.draw(pillarDown, vec.x, 480-pillarDown.getRegionHeight());
			}
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
		batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);
		if(meteorInScene){
			batch.draw(selectedMeteorTexture,meteorPosition.x,meteorPosition.y);
		}
		batch.end();
	}
	private void addPillar() {
		Vector2 pillarPosition=new Vector2();
		if(pillars.size==0){
			pillarPosition.x=(float) (800 + Math.random()*600);
		}else{
			pillarPosition.x=lastPillarPosition.x+(float) (600 + Math.random()*600);
		}
		if(MathUtils.randomBoolean()){
			pillarPosition.y=1;
		}else{
			pillarPosition.y=-1;//upside down
		}
		lastPillarPosition=pillarPosition;
		pillars.add(pillarPosition);
	}
	private void launchMeteor() {
		nextMeteorIn=1.5f+(float)Math.random()*5;
		if(meteorInScene){
			return;
		}
		spawnSound.play();
		meteorInScene=true;
		int id= (int)(Math.random()*meteorTextures.size);
		selectedMeteorTexture=meteorTextures.get(id);
		meteorPosition.x=810;
		meteorPosition.y=(float) (80+Math.random()*320);
		Vector2 destination=new Vector2();
		destination.x=-10;
		destination.y=(float) (80+Math.random()*320);
		destination.sub(meteorPosition).nor();
		meteorVelocity.mulAdd(destination, meteorSpeed);
	}

	static enum GameState {
		Init, Action, GameOver
	}
	
	@Override
	public void resize (int width, int height) {
		viewport.update(width, height);
	}
	@Override
	public void dispose () {
		tapSound.dispose();
		crashSound.dispose();
		spawnSound.dispose();
		music.dispose();
		batch.dispose();
		pillars.clear();
		atlas.dispose();
		meteorTextures.clear();
	}
}
