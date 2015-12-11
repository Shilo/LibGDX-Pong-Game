package com.shilocity.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class PongGame extends ApplicationAdapter {
	final Boolean BOX2D_DEBUG_DRAW = true;
	final Boolean SHAPE_RENDERER_DRAW = true;
	final int NUM_OF_PADDLES = 4;
	final int MAX_NUM_OF_BALLS = 1;
	
	OrthographicCamera _camera;
	ShapeRenderer _shapeRenderer;
	SpriteBatch _spriteBatch;
	Box2DDebugRenderer _debugRenderer;
	World _world;
	Body _groundBody;
	
	float _scaleFactor = 0.1f;
	Body[] _ballBodies = new Body[MAX_NUM_OF_BALLS];
	Fixture[] _ballFixtures = new Fixture[MAX_NUM_OF_BALLS];
    Body[] _paddleBodies = new Body[NUM_OF_PADDLES];
    Fixture[] _paddleFixtures = new Fixture[NUM_OF_PADDLES];
    MouseJoint[] _paddleMouseJoints = new MouseJoint[NUM_OF_PADDLES];
	float[] _paddleThickness = {2, 2, 2, 2};
	float[] _paddleWidth = {10, 10, 10, 10};
	float[] _paddlePadding = {0, 0, 0, 0};
	float[] _paddleMaxForce = {1000, 1000, 1000, 1000};
	float[] _paddleElasticity = {0.0f, 1.0f, 10.0f, 100.0f};
	float _ballRadius = 1.0f;
	float _ballVelocity = 200.0f;
	Vector2 _paddlePosition;
	Boolean _gamePlaying;
	
	
	@Override
	public void create() {
		_shapeRenderer = new ShapeRenderer();
		_spriteBatch = new SpriteBatch();
		_debugRenderer = new Box2DDebugRenderer();
		
		float screenWidth = Gdx.graphics.getWidth() * _scaleFactor;
		float screenHeight = Gdx.graphics.getHeight() * _scaleFactor;
		_camera = new OrthographicCamera(screenWidth, screenHeight);
		_camera.setToOrtho(true, screenWidth, screenHeight);
		
		_world = new World(new Vector2(0, 0), true);
		
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyDef.BodyType.KinematicBody;
		groundBodyDef.position.set(0, 0);
		
		_groundBody = _world.createBody(groundBodyDef);
		
		EdgeShape groundShape = new EdgeShape();
		FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundShape;
        fixtureDef.density = 1f;
        
        groundShape.set(0, 0, _camera.viewportWidth, 0);
        _groundBody.createFixture(fixtureDef);
        
        groundShape.set(0, _camera.viewportHeight, _camera.viewportWidth, _camera.viewportHeight);
        _groundBody.createFixture(fixtureDef);
        
        groundShape.set(0, 0, 0, _camera.viewportHeight);
        _groundBody.createFixture(fixtureDef);
        
        groundShape.set(_camera.viewportWidth, 0, _camera.viewportWidth, _camera.viewportHeight);
        _groundBody.createFixture(fixtureDef);
		
        _paddlePosition = new Vector2(_camera.viewportWidth/2, _camera.viewportHeight/2);
		_paddlePosition = new Vector2(0, 0);
		
		createPaddles();
		createBall();
		
		Gdx.input.setInputProcessor(new InputAdapter () {
			@Override
			public boolean mouseMoved (int x, int y) {
				updatePaddlePosition();
			    return true;
			}
		});
		
		startGame();
	}
	
	private void createPaddles() {
		float viewportWidth = _camera.viewportWidth;
		float viewportHeight = _camera.viewportHeight;
		
		for (int i=0; i<NUM_OF_PADDLES; i++) {
			boolean indexIsEven = (i % 2 == 0);
			float width = indexIsEven ? _paddleWidth[i] : _paddleThickness[i];
			float height = indexIsEven ? _paddleThickness[i] : _paddleWidth[i];
			float x = indexIsEven ? _paddlePosition.x - width/2 : _paddlePadding[i];
			float y = !indexIsEven ? _paddlePosition.y - height/2 : _paddlePadding[i];
			if (i == 2) {
				y = viewportHeight - y - height;
			} else if (i == 3) {
				x = viewportWidth - x - width;
			}
			
	        BodyDef bodyDef = new BodyDef();
	        bodyDef.type = BodyDef.BodyType.DynamicBody;
	        
	        
	        Body paddleBody = _paddleBodies[i] = _world.createBody(bodyDef);
	        
	        PolygonShape shape = new PolygonShape();
			shape.setAsBox(width/2, height/2);
			
			FixtureDef fixtureDef = new FixtureDef();
	        fixtureDef.shape = shape;
	        fixtureDef.density = 1f;
	        fixtureDef.friction = 0.0f;

	        _paddleFixtures[i] = paddleBody.createFixture(fixtureDef);
	        shape.dispose();
	        
	        MouseJointDef mouseJointDef = new MouseJointDef();
	        mouseJointDef.bodyA = _groundBody;
	        mouseJointDef.bodyB = paddleBody;
	        mouseJointDef.collideConnected = true;
	        mouseJointDef.maxForce = _paddleMaxForce[i] * paddleBody.getMass();
	        _paddleMouseJoints[i] = (MouseJoint)_world.createJoint(mouseJointDef);
	        
	        paddleBody.setTransform(x+width/2, y+height/2, 0.0f);
	        
	        Vector2 worldAxis = new Vector2(indexIsEven?1.0f:0.0f, indexIsEven?0.0f:1.0f);
	        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
	        prismaticJointDef.collideConnected = false;
	        prismaticJointDef.initialize(paddleBody, _groundBody, paddleBody.getWorldCenter(), worldAxis);
	        _world.createJoint(prismaticJointDef);
		}
	}
	
	private void createBall() {
		float viewportWidth = _camera.viewportWidth;
		float viewportHeight = _camera.viewportHeight;
		
		BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(viewportWidth/2, viewportHeight/2);
        
        Body ballBody = _ballBodies[0] = _world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
		shape.setRadius(_ballRadius);
		
		FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
       // fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 1.0f;

        _ballFixtures[0] = ballBody.createFixture(fixtureDef);
        shape.dispose();
        
        Vector2 impulse = new Vector2(_ballVelocity, _ballVelocity);
        ballBody.applyLinearImpulse(impulse, bodyDef.position, true);
        ballBody.applyAngularImpulse(_ballVelocity, true);
	}
	
	private void startGame() {
		_gamePlaying = true;
	}
 
	@Override
	public void render() {
		_world.step(Gdx.graphics.getDeltaTime(), 6, 2);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		_shapeRenderer.setProjectionMatrix(_camera.combined);
		
		renderPaddles();
		renderBall();
		
		if (BOX2D_DEBUG_DRAW) {
			_spriteBatch.begin();
			_debugRenderer.render(_world, _camera.combined);
			_spriteBatch.end();
		}
	}
	
	private void updatePaddlePosition() {
		Vector2 worldPosition = cursorToWorldPosition(Gdx.input.getX(), Gdx.input.getY());
		_paddlePosition = new Vector2(worldPosition.x, worldPosition.y);
	}
	
	private void renderBall() {
		Body body = _ballBodies[0];
		Vector2 position = body.getPosition();
		
		if (SHAPE_RENDERER_DRAW) {
			_shapeRenderer.begin(ShapeType.Filled);
			_shapeRenderer.setColor(0, 0, 1, 1);
			_shapeRenderer.circle(position.x, position.y, _ballRadius);
			_shapeRenderer.end();
		}
	}
		
	
	private void renderPaddles() {
		for (int i=0; i<NUM_OF_PADDLES; i++) {
			Body body = _paddleBodies[i];
			Fixture fixture = _paddleFixtures[i];
			Vector2 size = sizeOfPolygonShape((PolygonShape)fixture.getShape());
			Vector2 position = body.getPosition();
			float angle = body.getAngle() * MathUtils.radiansToDegrees;
			
			Vector2 paddlePosition = _paddlePosition.cpy();
			float paddleElasticity = (_paddleElasticity[i] * (i == 0 || i == 1 ? 1.0f : -1.0f));
			if (i == 0 || i == 2) {
				paddlePosition.y = position.y + paddleElasticity;
			} else {
				paddlePosition.x = position.x + paddleElasticity;
			}
			_paddleMouseJoints[i].setTarget(paddlePosition);
			
			if (SHAPE_RENDERER_DRAW) {
				_shapeRenderer.begin(ShapeType.Filled);
				_shapeRenderer.setColor(1, 0, 0, 1);
				_shapeRenderer.rect(position.x-size.x/2, position.y-size.y/2, size.x/2, size.y/2, size.x, size.y, 1.0f, 1.0f, angle);
				_shapeRenderer.end();
			}
		}
	}
	
	private Vector2 sizeOfPolygonShape(PolygonShape polygonShape) {
		Float minX = null;
        Float maxX = null;
        Float minY = null;
        Float maxY = null;
        for (int i = 0; i < polygonShape.getVertexCount(); i++) {
            Vector2 nextVertex = new Vector2();
            polygonShape.getVertex(i, nextVertex);
            float x = nextVertex.x;
            float y = nextVertex.y;
            if (minX == null || x < minX) {
                minX = x;
            }
            if (maxX == null || x > maxX) {
                maxX = x;
            }
            if (minY == null || y < minY) {
                minY = y;
            }
            if (maxY == null || y > maxY) {
                maxY = y;
            }
        }
        float width = maxX - minX;
        float height = maxY - minY;
        
        return new Vector2(width, height);
	}
	
	private Vector2 cursorToWorldPosition(int curX, int curY) {
		Vector3 vecCursorPos = new Vector3(curX, curY, 0);
		_camera.unproject(vecCursorPos);
		return new Vector2(vecCursorPos.x, vecCursorPos.y);
	}
	
	private void log(Object message) {
		System.out.println(message);
	}
}
