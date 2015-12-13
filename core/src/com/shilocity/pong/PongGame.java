package com.shilocity.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

public class PongGame extends ApplicationAdapter {
	final int NUM_OF_PADDLES = 4;
	final int MAX_NUM_OF_BALLS = 9999;
	final boolean CREATE_BALL_ON_POINTER = true;
	final boolean INFINITE_SPAWN_ON_LEFT_CLICK = true;
	final float INFINITE_SPAWN_DELAY = 0.3f;
	final boolean CIRCLE_PADDLE = false;
	final boolean SQUARE_BALL = false;
	final Vector2 DISPLAY_RESOLUTION = new Vector2(1280, 768);
	final boolean START_FULLSCREEN = false;
	final int FULLSCREEN_KEY_CODE = 34;
	final int TOGGLE_PADDLE_SHAPE_KEY_CODE = 44;
	final int TOGGLE_BALL_SHAPE_KEY_CODE = 30;
	
	private enum DrawStyle {
		NORMAL,
		DEBUG,
		NORMAL_AND_DEBUG
	}
	
	OrthographicCamera _camera;
	ShapeRenderer _shapeRenderer;
	SpriteBatch _spriteBatch;
	Box2DDebugRenderer _debugRenderer;
	private Stage _stage;
	private Table _tableLeft;
	private Table _tableCenter;
	private Table _tableRight;
	private Table _tableBottomCenter;
	World _world;
	Body _wallBody;
	Body _dummyBody;
	
	float _scaleFactor = 0.1f;
	int _numOfBalls = 0;
	int _numOfHits = 0;
	int _numOfBallsLost = 0;
	Array<Body> _ballBodies = new Array<Body>();
	Array<Fixture> _ballFixtures = new Array<Fixture>();
    Body[] _paddleBodies = new Body[NUM_OF_PADDLES];
    Fixture[] _paddleFixtures = new Fixture[NUM_OF_PADDLES];
    MouseJoint[] _paddleMouseJoints = new MouseJoint[NUM_OF_PADDLES];
	float[] _paddleThickness = {2, 2, 2, 2};
	float[] _paddleWidth = {10, 10, 10, 10};
	float[] _paddlePadding = {5, 5, 5, 5};
	float[] _paddleMaxForce = {1000, 1000, 1000, 1000};
	float[] _paddleElasticity = {5.0f, 5.0f, 5.0f, 5.0f};
	float _ballRadius = 1.0f;
	float _ballVelocity = 100.0f;
	float _wallPadding = -_ballRadius*2;
	Vector2 _paddlePosition;
	boolean _gamePlaying = false;
	DrawStyle _drawStyle = DrawStyle.NORMAL;
	Array<Body> _deadBalls = new Array<Body>();
	boolean _godMode = false;
	boolean _infiniteSpawn = false;
	Timer _infiniteSpawnDelayTimer;
	int _frameCount = 0;
	double _totalTime = 0;
	boolean _circlePaddle = CIRCLE_PADDLE;
	boolean _squareBall = SQUARE_BALL;
	
	Label _fpsCounterLabel;
	Label _godModeLabel;
	Label _ballCounterLabel;
	Label _lostLabel;
	Label _hitsLabel;
	
	@Override
	public void create() {
		setupDisplay();
		
		_shapeRenderer = new ShapeRenderer();
		_spriteBatch = new SpriteBatch();
		_debugRenderer = new Box2DDebugRenderer();
		
		createCamera();
		createWorld();
        createStage();
		createWall();
		createPaddles();
		createUserInterface();
		createCollisionDetection();
		createInputHandling();
		
		startGame();
	}
	
	private void setupDisplay() {
		if (START_FULLSCREEN) {
			Gdx.graphics.setDisplayMode((int)DISPLAY_RESOLUTION.x, (int)DISPLAY_RESOLUTION.y, START_FULLSCREEN);
		}
	}
	
	private void toggleFullscreen() {
		Gdx.graphics.setDisplayMode((int)DISPLAY_RESOLUTION.x, (int)DISPLAY_RESOLUTION.y, !Gdx.graphics.isFullscreen());
	}
	
	private void togglePaddleShape() {
		_circlePaddle = !_circlePaddle;
		recreatePaddles();
	}
	
	private void toggleBallShape() {
		_squareBall = !_squareBall;
	}
	
	public void resize(int width, int height) {
	    _stage.getViewport().update(width, height, true);
	}
	
	public void dispose() {
	    _stage.dispose();
	}

	
	private void createCamera() {
		float screenWidth = Gdx.graphics.getWidth() * _scaleFactor;
		float screenHeight = Gdx.graphics.getHeight() * _scaleFactor;
		_camera = new OrthographicCamera(screenWidth, screenHeight);
		_camera.setToOrtho(true, screenWidth, screenHeight);
		_paddlePosition = new Vector2(_camera.viewportWidth/2, _camera.viewportHeight/2);
	}
	
	private void createWorld() {
		_world = new World(new Vector2(0, 0), true);
		_dummyBody = _world.createBody(new BodyDef());
	}
	
	private void createStage() {
		boolean debugDraw = (_drawStyle == DrawStyle.DEBUG || _drawStyle == DrawStyle.NORMAL_AND_DEBUG);
		
		_stage = new Stage();
	    Gdx.input.setInputProcessor(_stage);

	    _tableLeft = new Table();
	    _tableLeft.setFillParent(true);
	    _tableLeft.setDebug(debugDraw);
	    _tableLeft.pad(10);
	    _stage.addActor(_tableLeft);
	    
	    _tableCenter = new Table();
	    _tableCenter.setFillParent(true);
	    _tableCenter.setDebug(debugDraw);
	    _tableCenter.pad(10);
	    _stage.addActor(_tableCenter);
	    
	    _tableRight = new Table();
	    _tableRight.setFillParent(true);
	    _tableRight.setDebug(debugDraw);
	    _tableRight.pad(10);
	    _stage.addActor(_tableRight);
	    
	    _tableBottomCenter = new Table();
	    _tableBottomCenter.setFillParent(true);
	    _tableBottomCenter.setDebug(debugDraw);
	    _tableBottomCenter.pad(10);
	    _stage.addActor(_tableBottomCenter);
	   
	}
	
	private void createCollisionDetection() {
		_world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
            	Fixture fixtureA = contact.getFixtureA();
            	Fixture fixtureB = contact.getFixtureB();
            	
            	if (fixtureIsCollisionType(fixtureA, BodyUserData.CollisionType.BALL)) {
            		if (fixtureIsCollisionType(fixtureB,  BodyUserData.CollisionType.WALL)) {
            			if (!_godMode) {
            				requestDestroyBall(fixtureA.getBody());
            			}
            		} else if (fixtureIsCollisionType(fixtureB,  BodyUserData.CollisionType.PADDLE)) {
            			ballHit(fixtureA.getBody());
            		}
            	} else if (fixtureIsCollisionType(fixtureB,  BodyUserData.CollisionType.BALL)) {
            		if (fixtureIsCollisionType(fixtureA,  BodyUserData.CollisionType.WALL)) {
            			if (!_godMode) {
            				requestDestroyBall(fixtureB.getBody());
            			}
            		} else if (fixtureIsCollisionType(fixtureA,  BodyUserData.CollisionType.PADDLE)) {
            			ballHit(fixtureB.getBody());
            		}
            	}
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
	}
	
	private void createInputHandling() {
		Gdx.input.setInputProcessor(new InputAdapter () {
			@Override
			public boolean mouseMoved (int x, int y) {
				updatePaddlePosition();
			    return true;
			}
			
			@Override
			public boolean touchDragged (int screenX, int screenY, int pointer) {
				updatePaddlePosition();
				return true;
			}
			
			@Override
			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				if (button == 1) {
					toggleGodMode();
				} else if (button == 2) {
					switch (_drawStyle) {
						case NORMAL:
							_drawStyle = DrawStyle.DEBUG;
							break;
						case DEBUG:
							_drawStyle = DrawStyle.NORMAL_AND_DEBUG;
							break;
						case NORMAL_AND_DEBUG:
							_drawStyle = DrawStyle.NORMAL;
					}
					boolean debugDraw = (_drawStyle == DrawStyle.DEBUG || _drawStyle == DrawStyle.NORMAL_AND_DEBUG);
					_tableLeft.setDebug(debugDraw);
					_tableCenter.setDebug(debugDraw);
					_tableRight.setDebug(debugDraw);
					_tableBottomCenter.setDebug(debugDraw);
				} else {
					createBall();
					if (INFINITE_SPAWN_ON_LEFT_CLICK) {
						if (_infiniteSpawnDelayTimer != null) {
							_infiniteSpawnDelayTimer.stop();
							_infiniteSpawnDelayTimer.clear();
						}
						_infiniteSpawnDelayTimer = new Timer();
						_infiniteSpawnDelayTimer.scheduleTask( 
						        new Timer.Task() {
						            @Override
						            public void run() {
						            	_infiniteSpawn = true;
						            }
						        }, 
						        INFINITE_SPAWN_DELAY 
						);
					}
				}
				return true;
			}
			
			public boolean touchUp (int screenX, int screenY, int pointer, int button) {
				if (INFINITE_SPAWN_ON_LEFT_CLICK) {
					if (_infiniteSpawnDelayTimer != null) {
						_infiniteSpawnDelayTimer.stop();
						_infiniteSpawnDelayTimer.clear();
						_infiniteSpawnDelayTimer = null;
					}
					_infiniteSpawn = false;
					return true;
				}
				return false;
			}
			
			@Override
			public boolean scrolled (int amount) {
				int count = Math.abs(amount);
				for (int i=0; i<count; i++) {
					createBall();
				}
				return true;
			}
			
			@Override
			public boolean keyUp (int keycode) {
				if (keycode == FULLSCREEN_KEY_CODE) {
					toggleFullscreen();
					return true;
				} else if (keycode == TOGGLE_PADDLE_SHAPE_KEY_CODE) {
					togglePaddleShape();
					return true;
				} else if (keycode == TOGGLE_BALL_SHAPE_KEY_CODE) {
					toggleBallShape();
					return true;
				}
				return false;
			}
		});
	}
	
	private void createUserInterface() {
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = new BitmapFont();
		float padding = 50;

		Label fpsCounterTextLabel = new Label("FPS", labelStyle);
		_fpsCounterLabel = new Label("...", labelStyle);
		
		_godModeLabel = new Label("[GOD MODE]", labelStyle);
		_godModeLabel.setVisible(_godMode);
		
		Label ballCounterTextLabel = new Label("Balls", labelStyle);
		_ballCounterLabel = new Label(""+_numOfBalls, labelStyle);
		
		Label lostTextLabel = new Label("Lost", labelStyle);
		_lostLabel = new Label(""+_numOfBallsLost, labelStyle);
		
		Label hitsTextLabel = new Label("Hits", labelStyle);
		_hitsLabel = new Label(""+_numOfHits, labelStyle);
		
		Label fullscreenTextLabel = new Label("Fullscreen", labelStyle);
		Label fullscreenLabel = new Label(Input.Keys.toString(FULLSCREEN_KEY_CODE), labelStyle);
		
		Label godModeTextLabel = new Label("God Mode", labelStyle);
		Label godModeLabel = new Label("Right Click", labelStyle);
		
		Label drawModeTextLabel = new Label("Draw Mode", labelStyle);
		Label drawModeLabel = new Label("Middle Click", labelStyle);
		
		Label spawnBallsTextLabel = new Label("Spawn Balls", labelStyle);
		Label spawnBallsLabel = new Label("Left Click / Hold, Scroll", labelStyle);
		
		Label paddleShapeTextLabel = new Label("Paddle Shape", labelStyle);
		Label paddleShapeLabel = new Label(Input.Keys.toString(TOGGLE_PADDLE_SHAPE_KEY_CODE), labelStyle);
		
		Label ballShapeTextLabel = new Label("Ball Shape", labelStyle);
		Label ballShapeLabel = new Label(Input.Keys.toString(TOGGLE_BALL_SHAPE_KEY_CODE), labelStyle);
		
		_tableLeft.top().left();
		_tableLeft.add(fpsCounterTextLabel);
		_tableLeft.row();
		_tableLeft.add(_fpsCounterLabel);
		
		_tableCenter.top();
		_tableCenter.add(_godModeLabel);
		
		_tableRight.top().right();
		_tableRight.add(ballCounterTextLabel).padRight(padding);
		_tableRight.add(lostTextLabel).padRight(padding);
		_tableRight.add(hitsTextLabel);
		_tableRight.row();
		_tableRight.add(_ballCounterLabel).padRight(padding);
		_tableRight.add(_lostLabel).padRight(padding);
		_tableRight.add(_hitsLabel);
		
		_tableBottomCenter.bottom();
		_tableBottomCenter.add(spawnBallsTextLabel).padRight(padding);
		_tableBottomCenter.add(godModeTextLabel).padRight(padding);
		_tableBottomCenter.add(drawModeTextLabel).padRight(padding);
		_tableBottomCenter.add(fullscreenTextLabel).padRight(padding);
		_tableBottomCenter.add(paddleShapeTextLabel).padRight(padding);
		_tableBottomCenter.add(ballShapeTextLabel);
		_tableBottomCenter.row();
		_tableBottomCenter.add(spawnBallsLabel).padRight(padding);
		_tableBottomCenter.add(godModeLabel).padRight(padding);
		_tableBottomCenter.add(drawModeLabel).padRight(padding);
		_tableBottomCenter.add(fullscreenLabel).padRight(padding);
		_tableBottomCenter.add(paddleShapeLabel).padRight(padding);
		_tableBottomCenter.add(ballShapeLabel);
	}
	
	private void updateFPSCounterLabel(double fps) {
		_fpsCounterLabel.setText(""+Math.round(fps));
	}
	
	private void updateBallCounterLabel() {
		_ballCounterLabel.setText(""+_numOfBalls);
	}
	
	private void updateLostLabel() {
		_lostLabel.setText(""+_numOfBallsLost);
	}
	
	private void updateHitsLabel() {
		_hitsLabel.setText(""+_numOfHits);
	}
	
	private void toggleGodMode() {
		_godMode = !_godMode;
		_godModeLabel.setVisible(_godMode);
	}
	
	private boolean fixtureIsCollisionType(Fixture fixture,  BodyUserData.CollisionType collisionType) {
		Body body = fixture.getBody();
		if (body != null) {
			BodyUserData bodyUserData = (BodyUserData)body.getUserData();
			if (bodyUserData != null) {
				return (bodyUserData.collisionType == collisionType);
			}
		}
		return false;
	}
	
	private void requestDestroyBall(Body ballBody) {
		if (_deadBalls.indexOf(ballBody, true) == -1) {
			_deadBalls.add(ballBody);
		}
	}
	
	private void ballHit(Body ball) {
		_numOfHits++;
		updateHitsLabel();
	}
	
	private void createWall() {
		BodyDef wallBodyDef = new BodyDef();
		wallBodyDef.type = BodyDef.BodyType.KinematicBody;
		wallBodyDef.position.set(0, 0);
		
		_wallBody = _world.createBody(wallBodyDef);
		
		BodyUserData bodyUserData = new BodyUserData();
		bodyUserData.collisionType = BodyUserData.CollisionType.WALL;
		_wallBody.setUserData(bodyUserData);
		
		EdgeShape wallShape = new EdgeShape();
		FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = wallShape;
        fixtureDef.density = 1f;
        
        wallShape.set(_wallPadding, _wallPadding, _camera.viewportWidth-_wallPadding, _wallPadding);
        _wallBody.createFixture(fixtureDef);
        
        wallShape.set(_wallPadding, _camera.viewportHeight-_wallPadding, _camera.viewportWidth-_wallPadding, _camera.viewportHeight-_wallPadding);
        _wallBody.createFixture(fixtureDef);
        
        wallShape.set(_wallPadding, _wallPadding, _wallPadding, _camera.viewportHeight-_wallPadding);
        _wallBody.createFixture(fixtureDef);
        
        wallShape.set(_camera.viewportWidth-_wallPadding, _wallPadding, _camera.viewportWidth-_wallPadding, _camera.viewportHeight-_wallPadding);
        _wallBody.createFixture(fixtureDef);
	}
	
	private void createPaddles() {
		float viewportWidth = _camera.viewportWidth;
		float viewportHeight = _camera.viewportHeight;
		
		for (int i=0; i<NUM_OF_PADDLES; i++) {
			boolean indexIsEven = (i % 2 == 0);
			float width = indexIsEven ? _paddleWidth[i] : _paddleThickness[i];
			float height = indexIsEven ? _paddleThickness[i] : _paddleWidth[i];
			if (_circlePaddle) {
				width = Math.max(width, height);
				height = width;
			}
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
	        
	        BodyUserData bodyUserData = new BodyUserData();
			bodyUserData.collisionType = BodyUserData.CollisionType.PADDLE;
			paddleBody.setUserData(bodyUserData);
	        
	        Shape shape;
	        if (_circlePaddle) {
	        	CircleShape circleShape = new CircleShape();
	        	circleShape.setRadius(width/2);
	        	shape = circleShape;
	        } else {
	        	PolygonShape polygonShape = new PolygonShape();
	        	polygonShape.setAsBox(width/2, height/2);
	        	shape = polygonShape;
	        }
			
			FixtureDef fixtureDef = new FixtureDef();
	        fixtureDef.shape = shape;
	        fixtureDef.density = 1f;
	        fixtureDef.friction = 0.0f;

	        _paddleFixtures[i] = paddleBody.createFixture(fixtureDef);
	        shape.dispose();
	        
	        MouseJointDef mouseJointDef = new MouseJointDef();
	        mouseJointDef.bodyA = _dummyBody;
	        mouseJointDef.bodyB = paddleBody;
	        mouseJointDef.collideConnected = true;
	        mouseJointDef.maxForce = _paddleMaxForce[i] * paddleBody.getMass();
	        _paddleMouseJoints[i] = (MouseJoint)_world.createJoint(mouseJointDef);
	        
	        paddleBody.setTransform(x+width/2, y+height/2, 0.0f);
	        
	        Vector2 worldAxis = new Vector2(indexIsEven?1.0f:0.0f, indexIsEven?0.0f:1.0f);
	        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
	        prismaticJointDef.collideConnected = false;
	        prismaticJointDef.initialize(paddleBody, _dummyBody, paddleBody.getWorldCenter(), worldAxis);
	        _world.createJoint(prismaticJointDef);
		}
	}
	
	private void recreatePaddles() {
		destroyPaddles();
		createPaddles();
	}
	
	private void destroyPaddles() {
		for (int i=0; i<NUM_OF_PADDLES; i++) {
			Body paddleBody = _paddleBodies[i];
			if (paddleBody == null) continue;
			
			final Array<JointEdge> jointList = paddleBody.getJointList();
		    while (jointList.size > 0) {
		    	_world.destroyJoint(jointList.get(0).joint);
		    }
		    
			_world.destroyBody(_paddleBodies[i]);
			
			_paddleFixtures[i] = null;
			_paddleMouseJoints[i] = null;
			_paddleBodies[i] = null;
		}
	}
	
	private void createBall() {
		if (_numOfBalls >= MAX_NUM_OF_BALLS) return;
		
		Vector2 position;
		if (CREATE_BALL_ON_POINTER) {
			position = cursorToWorldPosition(Gdx.input.getX(), Gdx.input.getY());
		} else {
			position = new Vector2(_camera.viewportWidth/2, _camera.viewportHeight/2);
		}
		
		float ballAngle = (float) (Math.random()*2*Math.PI);
		
		BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.angle = ballAngle;
        
        Body ballBody = _world.createBody(bodyDef);
        
        BodyUserData bodyUserData = new BodyUserData();
		bodyUserData.collisionType = BodyUserData.CollisionType.BALL;
		ballBody.setUserData(bodyUserData);
		
        _ballBodies.add(ballBody);
        
        Shape shape;
		if (_squareBall) {
			PolygonShape polygonShape = new PolygonShape();
        	polygonShape.setAsBox(_ballRadius, _ballRadius);
        	shape = polygonShape;
        } else {
        	CircleShape circleShape = new CircleShape();
        	circleShape.setRadius(_ballRadius);
        	shape = circleShape;
        }
		
		FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 1.0f;

        Fixture ballFixture = ballBody.createFixture(fixtureDef);
        _ballFixtures.add(ballFixture);
        shape.dispose();
        
        float velocityX = (float) (_ballVelocity*Math.cos(ballAngle));
        float velocityY = (float) (_ballVelocity*Math.sin(ballAngle));
        
        Vector2 impulse = new Vector2(velocityX, velocityY);
        ballBody.applyLinearImpulse(impulse, bodyDef.position, true);
        
        _numOfBalls++;
        updateBallCounterLabel();
	}
	
	private void destroyDeadBalls() {
		for (Body body : _deadBalls) {
			destroyBall(body);
		}
		_deadBalls.clear();
	}
	
	private void destroyBall(Body ballBody) {
		Fixture ballFixture = ballBody.getFixtureList().first();
		
		_ballBodies.removeValue(ballBody, true);
		_ballFixtures.removeValue(ballFixture, true);
		_world.destroyBody(ballBody);
		_numOfBalls--;
		_numOfBallsLost++;
		updateBallCounterLabel();
		updateLostLabel();
	}
	
	private void startGame() {
		_gamePlaying = true;
	}
 
	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		_totalTime += deltaTime;
		_frameCount++;
		
		if (_totalTime > 1.0)
	    {
	        updateFPSCounterLabel(_frameCount/_totalTime);
	        _frameCount = 0;
	        _totalTime = 0;
	    }
		
		_camera.update();
		_world.step(deltaTime, 6, 2);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		_shapeRenderer.setProjectionMatrix(_camera.combined);
		
		renderPaddles();
		renderBalls();
		
		if (_drawStyle == DrawStyle.DEBUG || _drawStyle == DrawStyle.NORMAL_AND_DEBUG) {
			_spriteBatch.begin();
			_debugRenderer.render(_world, _camera.combined);
			_spriteBatch.end();
		}
		
		_stage.act(deltaTime);
		_stage.draw();
		
		destroyDeadBalls();
		
		if (_infiniteSpawn) {
			createBall();
		}
	}
	
	private void updatePaddlePosition() {
		Vector2 worldPosition = cursorToWorldPosition(Gdx.input.getX(), Gdx.input.getY());
		_paddlePosition = new Vector2(worldPosition.x, worldPosition.y);
	}
	
	private void renderBalls() {
		for (int i=0; i<_ballBodies.size; i++) {
			Body body = _ballBodies.get(i);
			Vector2 position = body.getPosition();
			
			if (_drawStyle == DrawStyle.NORMAL || _drawStyle == DrawStyle.NORMAL_AND_DEBUG) {
				Fixture fixture = _ballFixtures.get(i);
				Shape shape = fixture.getShape();
				
				_shapeRenderer.begin(ShapeType.Filled);
				_shapeRenderer.setColor(0, 0, 1, 1);
				if (shape instanceof CircleShape) {
					_shapeRenderer.circle(position.x, position.y, shape.getRadius());
				} else {
					Vector2 size = sizeOfShape(shape);
					float angle = body.getAngle() * MathUtils.radiansToDegrees;
					_shapeRenderer.rect(position.x-size.x/2, position.y-size.y/2, size.x/2, size.y/2, size.x, size.y, 1.0f, 1.0f, angle);
				}
				_shapeRenderer.end();
			}
		}
	}
		
	
	private void renderPaddles() {
		for (int i=0; i<NUM_OF_PADDLES; i++) {
			Body body = _paddleBodies[i];
			if (body == null) continue;
			
			Fixture fixture = _paddleFixtures[i];
			Shape shape = fixture.getShape();
			Vector2 size = sizeOfShape(shape);
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
			
			if (_drawStyle == DrawStyle.NORMAL || _drawStyle == DrawStyle.NORMAL_AND_DEBUG) {
				float x = position.x-size.x/2;
				float y = position.y-size.y/2;
				
				_shapeRenderer.begin(ShapeType.Filled);
				_shapeRenderer.setColor(1, 0, 0, 1);
				
				if (shape instanceof CircleShape) {
					_shapeRenderer.circle(x+size.x/2, y+size.y/2, shape.getRadius());
				} else {
					_shapeRenderer.rect(x, y, size.x/2, size.y/2, size.x, size.y, 1.0f, 1.0f, angle);
				}
				_shapeRenderer.end();
			}
		}
	}
	
	private Vector2 sizeOfShape(Shape shape) {
		if (!(shape instanceof PolygonShape)) {
			float radius = shape.getRadius();
			return new Vector2(radius*2, radius*2);
		}
		
		PolygonShape polygonShape = (PolygonShape)shape;
		
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
