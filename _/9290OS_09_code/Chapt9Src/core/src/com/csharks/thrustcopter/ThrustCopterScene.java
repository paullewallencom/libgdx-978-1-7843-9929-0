package com.csharks.thrustcopter;

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
import com.badlogic.gdx.utils.Array;

public class ThrustCopterScene extends BaseScene {
	
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
	Vector2 planeVelocity= new Vector2();
	Vector2 scrollVelocity= new Vector2();
	Vector2 planePosition= new Vector2();
	Vector2 planeDefaultPosition= new Vector2();
	Vector2 gravity= new Vector2();
	Vector3 touchPosition=new Vector3();
	Vector2 tmpVector=new Vector2();
	private static final int touchImpulse=500;
	private static final Vector2 damping= new Vector2(0.99f,0.99f);
	GameState gameState = GameState.Init;
	Array<Vector2> pillars = new Array<Vector2>();
	Vector2 lastPillarPosition= new Vector2();
	float deltaPosition;
	Rectangle planeRect=new Rectangle();
	Rectangle obstacleRect=new Rectangle();
	Array<TextureAtlas.AtlasRegion> meteorTextures = new Array<TextureAtlas.AtlasRegion>();
	TextureRegion selectedMeteorTexture;
	boolean meteorInScene;
	private static final int meteorSpeed=50;
	Vector2 meteorPosition= new Vector2();
	Vector2 meteorVelocity= new Vector2();
	float nextMeteorIn;
	Music music;
	Sound tapSound;
	Sound crashSound;
	Sound spawnSound;
	SpriteBatch batch;
	OrthographicCamera camera;
	TextureAtlas atlas;
	Vector3 pickupTiming=new Vector3();
	Array<Pickup> pickupsInScene = new Array<Pickup>();
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
	private int trackedAchievement=0;
	
	public ThrustCopterScene(ThrustCopter thrustCopter) {
		super(thrustCopter);
		if(game.isAndroid){
			game.handler.setTrackerScreenName("com.csharks.thrustcopter.ThrustCopterScene");
			game.handler.showAds(false);
		}
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
		
		meteorTextures.add(atlas.findRegion("meteorBrown_med1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_med2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny2"));
		
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
	}

	@Override
	public void render (float delta) {
		super.render(delta);
		if(gamePaused){
			return;
		}
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		updateScene(delta);
		drawScene();
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
		trackedAchievement=1;
		shieldCount=15;
		fuelCount=100;
		fuelPercentage=114;
		planeVelocity.set(100, 0);
		scrollVelocity.set(5, 0);
		gravity.set(0, -3);
		planeDefaultPosition.set(250-88/2, 240-73/2);
		planePosition.set(planeDefaultPosition.x,planeDefaultPosition.y);
		pillars.clear();
		pickupsInScene.clear();
		addPillar();
		smoke.setPosition(planePosition.x+20, planePosition.y+30);
	}
	
	private void updateScene(float deltaTime) {
		if(Gdx.input.justTouched()){
			if(game.soundEnabled)tapSound.play(game.soundVolume);
			if(gameState == GameState.Init) {
				gameState = GameState.Action;
				return;
			}
			if(gameState == GameState.GameOver) {
				gameState = GameState.Init;
				resetScene();
				return;
			}
			if(fuelCount>0){
			touchPosition.set(Gdx.input.getX(),Gdx.input.getY(),0);
			camera.unproject(touchPosition);
			tmpVector.set(planePosition.x,planePosition.y);
			tmpVector.sub( touchPosition.x, touchPosition.y).nor();
			planeVelocity.mulAdd(tmpVector, touchImpulse-MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y, planePosition.x, planePosition.y), 0, touchImpulse));
			tapDrawTime=tapDrawTimeMax;
			}
		}
		smoke.setPosition(planePosition.x+20, planePosition.y+30);
		smoke.update(deltaTime);
		if(gameState == GameState.Init || gameState == GameState.GameOver) {
			if(gameState == GameState.GameOver)explosion.update(deltaTime);
			return;
		}
		//float deltaTime = Gdx.graphics.getDeltaTime();
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
			if(shieldCount<0){
			obstacleRect.set(meteorPosition.x + 2, meteorPosition.y + 2, selectedMeteorTexture.getRegionWidth()-4, selectedMeteorTexture.getRegionHeight()-4);
			if(planeRect.overlaps(obstacleRect)) {
				endGame();		
			}}
		}
		
		for(Vector2 vec: pillars) {
			vec.x-=deltaPosition;
			if(vec.x+pillarUp.getRegionWidth()<-10){
				pillars.removeValue(vec, false);
			}
			if(shieldCount<=0){
			if(vec.y==1){
				obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth()-20, pillarUp.getRegionHeight()-10);
			}else{
				obstacleRect.set(vec.x + 10, 480-pillarDown.getRegionHeight()+10, pillarUp.getRegionWidth()-20, pillarUp.getRegionHeight());
			}
			if(planeRect.overlaps(obstacleRect)) {
				endGame();			
			}}
		}
		for(Pickup pickup: pickupsInScene) {
			pickup.pickupPosition.x-=deltaPosition;
			if(pickup.pickupPosition.x+pickup.pickupTexture.getRegionWidth()<-10){
				pickupsInScene.removeValue(pickup, false);
			}
			obstacleRect.set(pickup.pickupPosition.x, pickup.pickupPosition.y, pickup.pickupTexture.getRegionWidth(), pickup.pickupTexture.getRegionHeight());
			if(planeRect.overlaps(obstacleRect)) {
				pickIt(pickup);	
			}
		}
		if(lastPillarPosition.x<400){
			addPillar();
		}
		
		if(planePosition.y < terrainBelow.getRegionHeight() - 35 || 
			planePosition.y + 73 > 480 - terrainBelow.getRegionHeight() + 35) {
			endGame();
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
		for(Pickup pickup: pickupsInScene) {
			batch.draw(pickup.pickupTexture, pickup.pickupPosition.x, pickup.pickupPosition.y);
		}
		smoke.draw(batch);
		batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);
		if(shieldCount>0){
			batch.draw(shield.getKeyFrame(planeAnimTime), planePosition.x-20, planePosition.y);
			font.draw(batch, ""+((int)shieldCount), 390, 450);
		}
		if(meteorInScene){
			batch.draw(selectedMeteorTexture,meteorPosition.x,meteorPosition.y);
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
		if(game.soundEnabled)spawnSound.play(game.soundVolume);
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
	private boolean addPickup(int pickupType) {
		Vector2 randomPosition=new Vector2();
		randomPosition.x=820;
		randomPosition.y=(float) (80+Math.random()*320);
		for(Vector2 vec: pillars) {
			if(vec.y==1){
				obstacleRect.set(vec.x , 0, pillarUp.getRegionWidth(), pillarUp.getRegionHeight());
			}else{
				obstacleRect.set(vec.x , 480-pillarDown.getRegionHeight(), pillarUp.getRegionWidth(), pillarUp.getRegionHeight());
			}
			if(obstacleRect.contains(randomPosition)) {
				return false;			
			}
		}
		tempPickup=new Pickup(pickupType, game.manager);
		tempPickup.pickupPosition.set(randomPosition);
		pickupsInScene.add(tempPickup);
		return true;
	}
	private void pickIt(Pickup pickup) {
		if(game.soundEnabled)pickup.pickupSound.play(game.soundVolume);
		switch(pickup.pickupType){
		case Pickup.STAR:
			starCount+=pickup.pickupValue;
			if(trackedAchievement<6&&starCount/5>trackedAchievement*10){
				if(game.isAndroid){
				game.handler.unlockAchievement(trackedAchievement*10);
				trackedAchievement++;
				}
			}
			break;
		case Pickup.SHIELD:
			shieldCount=pickup.pickupValue;
			break;
		case Pickup.FUEL:
			fuelCount=pickup.pickupValue;
			break;
		}
		pickupsInScene.removeValue(pickup, false);	
	}
	private void endGame(){
		if(gameState != GameState.GameOver){
			if(game.soundEnabled)crashSound.play(game.soundVolume);
			gameState = GameState.GameOver;
			explosion.reset();
			explosion.setPosition(planePosition.x+40, planePosition.y+40);
			checkAndStoreScore();
		}	
	}
	private void checkAndStoreScore() {
		int finalScore=(int)(starCount+score);
		if(game.isAndroid){
			game.handler.submitScore(finalScore);
		}else{
		
		int lowestScore=game.saveManager.loadDataValue("Score10", int.class);
		if(finalScore>lowestScore){
			int[] scores = new int[10];
			for(int i=1;i<=10;i++){
				scores[i-1]=game.saveManager.loadDataValue("Score"+i, int.class);
			}
			scores[9]=finalScore;
			for(int i=9;i>0;i--){
				if(scores[i]>scores[i-1]){
					finalScore=scores[i-1];
					scores[i-1]=scores[i];
					scores[i]=finalScore;
				}else{
					break;
				}
			}
			Gdx.app.log("info", "saving new score");
			for(int i=1;i<=10;i++){
				game.saveManager.saveDataValue("Score"+i, scores[i-1]);
			}
		}
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
	
	@Override
	public void dispose () {
		tapSound.dispose();
		crashSound.dispose();
		spawnSound.dispose();
		music.dispose();
		pillars.clear();
		meteorTextures.clear();
		smoke.dispose();
		explosion.dispose();
	}
}
