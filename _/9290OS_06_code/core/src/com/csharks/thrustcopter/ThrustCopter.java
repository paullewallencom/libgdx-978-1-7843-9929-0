package com.csharks.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.matsemann.libgdxloadingscreen.screen.LoadingScreen;

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
	public boolean soundEnabled;
	public float soundVolume;

	public ThrustCopter() {
		fpsLogger=new FPSLogger();
		camera = new OrthographicCamera();
		camera.position.set(screenWidth/2,screenHeight/2,0);
		viewport = new FitViewport(screenWidth, screenHeight, camera);
		soundEnabled=true;
		soundVolume=1;
	}

	@Override
	public void create() {
		batch=new SpriteBatch();
		setScreen(new LoadingScreen(this));
	}
	
	@Override
	public void render(){
		//fpsLogger.log();
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
