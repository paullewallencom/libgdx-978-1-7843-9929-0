package com.csharks.thrustcopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class LeaderboardScene extends BaseScene {
	private Stage stage;
	private Image screenBg;
	private Skin skin;
	private TextButton backButton;
	private Table table;

	public LeaderboardScene(ThrustCopter thrustCopter) {
		super(thrustCopter);
		
		stage = new Stage(game.viewport);
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("uiskin.json"));

		screenBg = new Image(game.atlas.findRegion("background"));
		
		table=new Table().debug();
		Label soundTitle=new Label("LEADERBOARD",skin);
		soundTitle.setColor(Color.NAVY);
		table.add(soundTitle).padBottom(15).colspan(2);
		table.row();
		Label score;
		for(int i=1;i<=10;i++){
			score=new Label(i+". "+ game.saveManager.loadDataValue("Score"+i, int.class), skin);
			table.add(score).padBottom(2).align(Align.left);
			table.row();
		}
		backButton=new TextButton("BACK", skin);
		table.add(backButton).padTop(20).colspan(2);
		
		stage.addActor(screenBg);
		stage.addActor(table);
		
		
		backButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	backToMenu();
            }
        });
	}
	@Override
	public void show() {
		table.setPosition(400, -200);
		
		MoveToAction actionMove = Actions.action(MoveToAction.class);
		actionMove.setPosition(400, 250);
		actionMove.setDuration(1);
		actionMove.setInterpolation(Interpolation.swingOut);
		table.addAction(actionMove);

	}
	private void backToMenu() {
		game.setScreen(new MenuScene(game));
	}
	@Override
	public void render(float delta) {
		// Clear the screen
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Show the loading screen
		stage.act();
		stage.draw();
		
		//Table.drawDebug(stage);
		super.render(delta);
	}
	@Override
	protected void handleBackPress() {
		backToMenu();
	}
	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

}
