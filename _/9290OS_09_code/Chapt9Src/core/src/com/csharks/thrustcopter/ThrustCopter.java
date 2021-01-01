package com.csharks.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.csharks.IActivityRequestHandler;
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
	private Preferences preferences;
	public SaveManager saveManager;
	public IActivityRequestHandler handler;
	public boolean isAndroid=false;
	

	public ThrustCopter(IActivityRequestHandler IARH) {
		handler=IARH;
		fpsLogger=new FPSLogger();
		camera = new OrthographicCamera();
		camera.position.set(screenWidth/2,screenHeight/2,0);
		viewport = new FitViewport(screenWidth, screenHeight, camera);
		
	}
	
	protected Preferences getPrefs()
    {
		if(preferences==null){
	        preferences = Gdx.app.getPreferences("ThrustCopter");
	     }
		if(preferences==null){
			Gdx.app.log("info","null preferences");
		}
	   return preferences;
		
    }
	public void saveSoundStatus(){
		getPrefs().putBoolean( "soundstatus", soundEnabled );
	}
	public boolean loadSoundStatus( ){
		return getPrefs().getBoolean("soundstatus",true);
	}
	public void saveSoundVolume(){
		getPrefs().putFloat( "soundvolume", soundVolume );
	}
	public float loadSoundVolume( ){
		return getPrefs().getFloat("soundvolume",1.0f);
	}
	public void flushPref(){
		 getPrefs().flush();
	}
	public void saveAll(){
		Gdx.app.log("info","saving preferences");
		saveSoundVolume();
		saveSoundStatus();
		flushPref();
	}

	@Override
	public void create() {
		batch=new SpriteBatch();
		setScreen(new LoadingScreen(this));
		
		soundEnabled=loadSoundStatus();
		soundVolume=loadSoundVolume();
		
		prepareLocalScores();
		
		switch (Gdx.app.getType()) {
	    case Android:
	    	isAndroid=true;
	        break;
	    case Desktop:
	        // desktop specific code
	        break;
	    case WebGL:
	        // HTML5 specific code
	        break;
	    case iOS:
	    	break;
	    default:
	        // Other platforms specific code
		}
	}
	
	private void prepareLocalScores() {
		saveManager=new SaveManager(true);
		if(saveManager.loadDataValue("Score1", int.class)==null){//first run
			Gdx.app.log("info", "no save found");
			for(int i=1;i<=10;i++){
				saveManager.saveDataValue("Score"+i, 0);
			}
		}
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
