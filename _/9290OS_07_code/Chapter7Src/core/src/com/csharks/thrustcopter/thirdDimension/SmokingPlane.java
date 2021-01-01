package com.csharks.thrustcopter.thirdDimension;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csharks.thrustcopter.BaseScene;
import com.csharks.thrustcopter.ThrustCopter;

public class SmokingPlane extends BaseScene {
	ModelBatch modelBatch;
	Environment environment;
	PerspectiveCamera camera;
	CameraInputController cameraController;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	AnimationController controller;
	ParticleSystem particleSystem;
	ParticleEffect effect;

	public SmokingPlane(ThrustCopter thrustCopter) {
		super(thrustCopter);
		// Create ModelBatch that will render all models using a camera
		modelBatch = new ModelBatch(new DefaultShaderProvider());

		// Create a camera and point it to our model
		camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//camera.position.set(0f, -4f, 3f);
		camera.position.set(0f, 4f, 3f);
		camera.lookAt(0, 0, 0);
		camera.near = 0.1f;
		camera.far = 300f;
		camera.update();

		// Create the generic camera input controller to make the app interactive
		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);

		// Set up environment with simple lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.8f, 0.3f, -1f));

		//Loading model
		game.manager.load("planeanim1.g3db", Model.class);
		game.manager.finishLoading();

		Model model = game.manager.get("planeanim1.g3db", Model.class);
		ModelInstance plane = new ModelInstance(model);
		instances.add(plane);
		plane.transform.setToTranslation(0, 0, -1f);

		// You use an AnimationController to control animations.  Each control is tied to the model instance
		controller = new AnimationController(plane);  
		// Pick the current animation by name
		controller.setAnimation("Scene",-1);

		// ParticleSystem is a singleton class, we get the instance instead of creating a new object:
		particleSystem = ParticleSystem.get();
		PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch();
		pointSpriteBatch.setCamera(camera);
		particleSystem.add(pointSpriteBatch);

		ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
		ParticleEffectLoader loader = new ParticleEffectLoader(new InternalFileHandleResolver());
		game.manager.setLoader(ParticleEffect.class, loader);
		game.manager.load("Smoke3D", ParticleEffect.class, loadParam);
		game.manager.finishLoading();

		ParticleEffect originalEffect = game.manager.get("Smoke3D");
		// we cannot use the originalEffect, we must make a copy each time we create new particle effect
		effect = originalEffect.copy();
		effect.init();
		effect.start();  // optional: particle will begin playing immediately
		particleSystem.add(effect);
		effect.translate(new Vector3(0,-0.25f,-1.8f));
	}

	@Override
	public void dispose() {
		for(ModelInstance mi : instances){
			mi.model.dispose();
		}
		instances.clear();
		modelBatch.dispose();
		particleSystem.removeAll();
		effect.dispose();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1);

		// Respond to user events and update the camera
		cameraController.update();
		controller.update(delta);

		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();

		// Draw all model instances using the camera
		modelBatch.begin(camera);
		modelBatch.render(particleSystem);
		// modelBatch.render(plane, environment);
		modelBatch.render(instances, environment);
		modelBatch.end();

		super.render(delta);
	}

}
