package com.csharks.thrustcopter.thirdDimension;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.csharks.thrustcopter.BaseScene;
import com.csharks.thrustcopter.ThrustCopter;

public class BulletSample extends BaseScene {
	
	public static class MyContactListener extends ContactListener {
		
		@Override
		public void onContactStarted (btCollisionObject colObj0, btCollisionObject colObj1) {
				if(colObj0.userData=="plane" || colObj1.userData=="plane"){
					Gdx.app.log("ContactCallback", "Plane Collides" );
				}
		}
	}
	
	ModelBatch modelBatch;
	Environment environment;
	PerspectiveCamera camera;
	CameraInputController cameraController;
	Array<ModelInstance> instances = new Array<ModelInstance>();
	Array<Model> models =new Array<Model>();
	AnimationController controller;
	boolean started=false;

	private btDefaultCollisionConfiguration collisionConfiguration;
	private btCollisionDispatcher dispatcher;
	private btDbvtBroadphase broadphase;
	private btSequentialImpulseConstraintSolver solver;
	private btDiscreteDynamicsWorld world;
	private Array<btMotionState> motionStates = new Array<btMotionState>();
	private Array<btCollisionShape> shapes = new Array<btCollisionShape>();
	private Array<btRigidBody> bodies = new Array<btRigidBody>();
	ModelInstance groundInstance;
	MyContactListener contactListener;
	DirectionalShadowLight shadowLight;
	ModelBatch shadowBatch;

	public BulletSample(ThrustCopter thrustCopter) {
		super(thrustCopter);
		// Create ModelBatch that will render all models using a camera
		modelBatch = new ModelBatch(new DefaultShaderProvider());

		// Create a camera and point it to our model
		camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(10f, 10f, 12f);
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
		// environment.set(new ColorAttribute(ColorAttribute.Fog, 0.13f, 0.13f, 0.13f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.8f, 0.3f, -1f));
		shadowLight = new DirectionalShadowLight(1024, 1024, 60, 60, 1f, 300);
		shadowLight.set(0.8f, 0.8f, 0.8f, -1f, -.8f, -.2f);
		environment.add(shadowLight);
		environment.shadowMap = shadowLight;
		shadowBatch = new ModelBatch(new DepthShaderProvider());

		Bullet.init();

		collisionConfiguration = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfiguration);
		broadphase = new btDbvtBroadphase();
		solver = new btSequentialImpulseConstraintSolver();
		world = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		world.setGravity(new Vector3(0, -9.81f, 1f));

		ModelBuilder modelBuilder = new ModelBuilder();
		Model ground = modelBuilder.createBox(40f, 2f, 40f, 
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				Usage.Position | Usage.Normal);
		groundInstance=new ModelInstance(ground);

		btCollisionShape groundshape = new btBoxShape(new Vector3(20, 1f, 20));
		shapes.add(groundshape);
		btRigidBody body = new btRigidBody(0,null,groundshape);
		bodies.add(body);
		world.addRigidBody(body);

		Vector3 position=new Vector3();
		for(int i=0;i<10;i++){
			Model box = modelBuilder.createBox(1f, 1f, 1f, 
					new Material(ColorAttribute.createDiffuse(Color.BLUE)),
					Usage.Position | Usage.Normal);
			ModelInstance boxInstance=new ModelInstance(box);
			instances.add(boxInstance);
			models.add(box);
			if(i<5){
				position.set(-1, i+1, 0);
			}else{
				position.set(1, i-4, 0);
			}
			boxInstance.transform.setToTranslation(position);

			btDefaultMotionState motionState = new btDefaultMotionState(boxInstance.transform);
			motionState.setWorldTransform(boxInstance.transform.trn(0, 0, 0));
			motionStates.add(motionState);
			btCollisionShape boxshape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
			shapes.add(boxshape);
			btRigidBody boxbody = new btRigidBody(1, motionState, boxshape);
			bodies.add(boxbody);
			world.addRigidBody(boxbody);
		}


		//Loading model
		game.manager.load("planeanim1.g3db", Model.class);
		game.manager.finishLoading();

		Model model = game.manager.get("planeanim1.g3db", Model.class);
		ModelInstance plane = new ModelInstance(model);
		instances.add(plane);
		
		plane.transform.setToRotation(Vector3.Y, 180);
		plane.transform.trn(0, 7, 10);
		
		btDefaultMotionState motionState = new btDefaultMotionState(plane.transform);
		motionState.setWorldTransform(plane.transform.trn(0, 0, 0));
		motionStates.add(motionState);
		btCollisionShape planeshape = createConvexHullShape(model,true);
		shapes.add(planeshape);
		btRigidBody planebody = new btRigidBody(5, motionState, planeshape);
		bodies.add(planebody);
		world.addRigidBody(planebody);
		planebody.userData="plane";
		planebody.applyCentralImpulse(new Vector3(0,0,-65));

		// You use an AnimationController to control animations.  Each control is tied to the model instance
		controller = new AnimationController(plane);  
		// Pick the current animation by name
		controller.setAnimation("Scene",-1);
		
		contactListener = new MyContactListener();
	}
	public static btConvexHullShape createConvexHullShape (final Model model, boolean optimize) {
		final Mesh mesh = model.meshes.get(0);
		final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
		if (!optimize) return shape;
		// now optimize the shape
		final btShapeHull hull = new btShapeHull(shape);
		hull.buildHull(shape.getMargin());
		final btConvexHullShape result = new btConvexHullShape(hull);
		// delete the temporary shape
		shape.dispose();
		hull.dispose();
		return result;
	}

	@Override
	public void dispose() {
		groundInstance.model.dispose();
		instances.clear();
		modelBatch.dispose();
		for (Model model : models)
			model.dispose();
		for (btRigidBody body : bodies) {
			body.dispose();
		}
		for (btMotionState motion : motionStates)
			motion.dispose();
		for (btCollisionShape shape : shapes)
			shape.dispose();
		world.dispose();
		collisionConfiguration.dispose();
		dispatcher.dispose();
		broadphase.dispose();
		solver.dispose();
		contactListener.dispose();
		shadowBatch.dispose();
		shadowLight.dispose();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1);
		
		if(started){
		world.stepSimulation(Gdx.graphics.getDeltaTime(), 5);
		for (int i = 0; i < motionStates.size; i++) {
			motionStates.get(i).getWorldTransform(instances.get(i).transform);
		}
		}else{
			if(Gdx.input.isTouched()){
				started=true;
			}
		}

		// Respond to user events and update the camera
		cameraController.update();
		controller.update(delta);
		
		shadowLight.begin(Vector3.Zero, camera.direction);
		shadowBatch.begin(shadowLight.getCamera());
		shadowBatch.render(instances);
		
		shadowBatch.end();
		shadowLight.end();

		// Draw all model instances using the camera
		modelBatch.begin(camera);
		modelBatch.render(groundInstance, environment);
		modelBatch.render(instances, environment);
		modelBatch.end();

		super.render(delta);
	}

}
