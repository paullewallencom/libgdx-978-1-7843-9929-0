package com.csharks.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopter extends Game {
	public AssetManager manager = new AssetManager();
	public SpriteBatch batch;
	public FPSLogger fpsLogger;
	public OrthographicCamera camera;
	public TextureAtlas atlas;
	public Viewport viewport;
	public static final int screenWidth=800;
	public static final int screenHeight=480;
	public BitmapFont font;

	public ThrustCopter() {
		fpsLogger=new FPSLogger();
		camera = new OrthographicCamera();
		camera.position.set(screenWidth/2,screenHeight/2,0);
		viewport = new FitViewport(screenWidth, screenHeight, camera);
	}

	@Override
	public void create() {
		manager.load("gameover.png", Texture.class);
		manager.load("life.png", Texture.class);
		manager.load("sounds/journey.mp3", Music.class);
		manager.load("sounds/pop.ogg", Sound.class);
		manager.load("sounds/crash.ogg", Sound.class);
		manager.load("sounds/alarm.ogg", Sound.class);
		manager.load("sounds/shield.ogg", Sound.class);
		manager.load("sounds/fuel.ogg", Sound.class);
		manager.load("sounds/star.ogg", Sound.class);
		manager.load("thrustcopterassets.txt", TextureAtlas.class);
		manager.load("impact-40.fnt", BitmapFont.class);
		manager.load("Smoke", ParticleEffect.class);
		manager.load("Explosion", ParticleEffect.class);
		manager.finishLoading();
		
		batch=new SpriteBatch();
		atlas=manager.get("thrustcopterassets.txt", TextureAtlas.class);
		font=manager.get("impact-40.fnt", BitmapFont.class);
		setScreen(new ThrustCopterScene(this));
	}
	
	@Override
	public void render(){
		fpsLogger.log();
		super.render();
	}
	
	@Override
	public void resize (int width, int height) {
		viewport.update(width, height);
	}
	@Override
	public void dispose () {
		batch.dispose();
		atlas.dispose();
		manager.dispose();
	}

}
