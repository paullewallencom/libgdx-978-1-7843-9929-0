package com.csharks.thrustcopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TiledLevelDemo extends BaseScene {
	private TiledMap map;
	private TiledMapRenderer renderer;
	
	public TiledLevelDemo(ThrustCopter thrustCopter) {
		super(thrustCopter);
		
		game.manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		game.manager.load("platformer.tmx", TiledMap.class);
		game.manager.finishLoading();
		map = game.manager.get("platformer.tmx");
		renderer=new OrthogonalTiledMapRenderer(map);
		
		//TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
		//Cell cell= layer.getCell(3, 5);
	}
	
	@Override
	public void render (float delta) {
		super.render(delta);
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		game.camera.update();
		renderer.setView(game.camera);
		renderer.render();
	}

}
