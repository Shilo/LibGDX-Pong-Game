package com.shilocity.pong;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

import box2dLight.PointLight;
import box2dLight.RayHandler;

public class PongGame extends ApplicationAdapter {
	final int NUM_OF_PADDLES = 4;
	final int MAX_NUM_OF_BALLS = 9999;
	final int LIGHT_RAYS = 200;
	final int LIGHT_ROWS = 2;
	final int LIGHT_COLUMNS = 3;
	final int BALL_SMOOTH_COLOR_SECONDS = 1;
	final int LIGHT_SMOOTH_COLOR_SECONDS = 5;
	final float LIGHT_ALPHA = 1.0f;
	
	final boolean CREATE_BALL_ON_POINTER = true;
	final boolean INFINITE_SPAWN_ON_LEFT_CLICK = true;
	final boolean INFINITE_FORCE_ON_RIGHT_CLICK = true;
	final boolean INVERT_FORCE = false;
	final boolean INVERT_PADDLE_MOVEMENT = false;
	final boolean RENDER_LIGHT = true;
	final boolean CENTER_LIGHT = true;
	final boolean REAL_SHADOWS = false;
	final boolean HIDE_PADDLE = false;
	final float INFINITE_SPAWN_DELAY = 0.3f;
	final float INFINITE_FORCE_DELAY = 0.3f;
	final boolean CIRCLE_PADDLE = false;
	final boolean SQUARE_BALL = false;
	final boolean SMOOTH_RANDOM_BALL_COLOR = true;
	final float FORCE_STRENGTH = 1000.0f;
	final float SCROLLING_TEXT_POS_PER_SEC = 100.0f;
	final float SCROLLING_TEXT_ALPHA_PER_SEC = 0.5f;
	final Color SCROLLING_TEXT_COLOR_POSITIVE = Color.YELLOW;
	final Color SCROLLING_TEXT_COLOR_NEGATIVE = Color.RED;
	final Color SCROLLING_TEXT_COLOR_WELCOME = Color.GREEN;
	final String SCROLLING_TEXT_WELCOME = "LEFT CLICK TO START";
	
	final Vector2 DISPLAY_RESOLUTION = new Vector2(1280, 768);
	final boolean START_FULLSCREEN = false;
	
	final int FULLSCREEN_KEY_CODE = 34;
	final int EXIT_FULLSCREEN_KEY_CODE = 131;
	final int TOGGLE_PADDLE_SHAPE_KEY_CODE = 44;
	final int TOGGLE_BALL_SHAPE_KEY_CODE = 30;
	final int TOGGLE_BALL_COLOR_STYLE_KEY_CODE = 31;
	final int INVERT_FORCE_KEY_CODE = 50;
	final int DRAW_MODE_KEY_CODE = 32;
	final int RENDER_LIGHTS_KEY_CODE = 40;
	final int REAL_SHADOWS_KEY_CODE = 47;
	final int HIDE_PADDLE_KEY_CODE = 36;
	final int CLEAR_BALLS_KEY_CODE = 45;
	final int INVERT_PADDLE_MOVEMENT_KEY_CODE = 41;
	
	private enum DrawStyle {
		NORMAL,
		DEBUG,
		NORMAL_AND_DEBUG
	}
	
	private enum LightStyle {
		GRID_AND_CENTER,
		GRID,
		CENTER,
		NONE
	}
	
	OrthographicCamera _camera;
	ShapeRenderer _shapeRenderer;
	SpriteBatch _spriteBatch;
	Box2DDebugRenderer _debugRenderer;
	RayHandler _rayHandler;
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
	Array<PointLight> _lights = new Array<PointLight>();
	Array<SmoothColorManager> _lightColorManagers = new Array<SmoothColorManager>();
	Array<Body> _ballBodies = new Array<Body>();
	Array<Fixture> _ballFixtures = new Array<Fixture>();
    Body[] _paddleBodies = new Body[NUM_OF_PADDLES];
    Fixture[] _paddleFixtures = new Fixture[NUM_OF_PADDLES];
    MouseJoint[] _paddleMouseJoints = new MouseJoint[NUM_OF_PADDLES];
    PrismaticJoint[] _paddleMousePrismaticJoints = new PrismaticJoint[NUM_OF_PADDLES];
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
	LightStyle _lightStyle = LightStyle.GRID_AND_CENTER;
	Array<Body> _deadBalls = new Array<Body>();
	boolean _godMode = false;
	boolean _infiniteSpawn = false;
	boolean _invertForce = INVERT_FORCE;
	boolean _invertPaddleMovement = INVERT_PADDLE_MOVEMENT;
	boolean _renderLight = RENDER_LIGHT;
	boolean _realShadows = REAL_SHADOWS;
	boolean _hidePaddle = HIDE_PADDLE;
	Timer _infiniteSpawnDelayTimer;
	boolean _infiniteForce = false;
	Timer _infiniteForceDelayTimer;
	int _frameCount = 0;
	double _totalTime = 0;
	double _currentTime = 0;
	boolean _circlePaddle = CIRCLE_PADDLE;
	boolean _squareBall = SQUARE_BALL;
	boolean _smoothRandomBallColor = SMOOTH_RANDOM_BALL_COLOR;
	SmoothColorManager _ballColorManager;
	int _ballColorSecondsPassed = 0;
	int _lightColorSecondsPassed = 0;
	Array<Label> _scrollingLabels = new Array<Label>();
	boolean _didRender = false;
	boolean _didToggleFullscreen = false;
	
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
		
		createColorManagers();
		createCamera();
		createWorld();
        createStage();
		createWall();
		createPaddles();
		createLights();
		createUserInterface();
		createCollisionDetection();
		createInputHandling();
		
		startGame();
	}
	
	private void didRender() {
		addScrollingText(SCROLLING_TEXT_WELCOME, SCROLLING_TEXT_COLOR_WELCOME);
	}
	
	private void addScrollingText(String text, Color color) {
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = new BitmapFont();
		labelStyle.fontColor = color;
		
		Label label = new Label(text, labelStyle);
		
		try {
			float y = _tableBottomCenter.getRowHeight(0)+_tableBottomCenter.getRowHeight(1)+_tableBottomCenter.getPadBottom()+_tableBottomCenter.getPadTop();
			float x = (Gdx.graphics.getWidth()-label.getWidth()*label.getFontScaleX())/2;
			label.setPosition(x, y);
			
			if (_scrollingLabels.size > 0) {
				Label lastLabel = _scrollingLabels.peek();
				if (lastLabel != null && lastLabel.getY()<y+label.getHeight()) {
					float offset = y+label.getHeight()-lastLabel.getY();
					adjustScrollingText(offset);
				}
			}
			
			_stage.addActor(label);
			
			_scrollingLabels.add(label);
		} catch(Exception e) {}
	}
	
	private void adjustScrollingText(float y) {
		float deltaTime = y/SCROLLING_TEXT_POS_PER_SEC;
		renderScrollingText(deltaTime);
	}
	
	private void renderScrollingText(float deltaTime) {
		if (_scrollingLabels.size < 1) return;
		
		float screenWidth = Gdx.graphics.getWidth();
		float positionPerSecond = SCROLLING_TEXT_POS_PER_SEC;
		float alphaPerSecond = SCROLLING_TEXT_ALPHA_PER_SEC;
		
		for (Label label : _scrollingLabels) {
			float x = (screenWidth-label.getWidth()*label.getFontScaleX())/2;
			float y = label.getY()+positionPerSecond*deltaTime;
			label.setPosition(x, y);
			
			Color curFontColor = label.getStyle().fontColor;
			float alpha = curFontColor.a-alphaPerSecond*deltaTime;
			label.getStyle().fontColor = new Color(curFontColor.r, curFontColor.g, curFontColor.b, alpha);
			
			if (alpha <= 0.0f) {
				_scrollingLabels.removeValue(label, true);
			}
		}
	}
	
	private void createColorManagers() {
		_ballColorManager = new SmoothColorManager();
		
		_ballColorManager.setTargetAndLastIncrementColor(Utils.randomHex());
		if (_smoothRandomBallColor) {
			setBallRandomColor();
		}
		
		
		int lightCount = LIGHT_ROWS*LIGHT_COLUMNS;
		if (CENTER_LIGHT) {
			lightCount++;
		}
		
		double currentTime = _currentTime;
		
		for (int i=0; i<lightCount; i++) {
			SmoothColorManager lightColorManager = new SmoothColorManager();
			lightColorManager.setTargetAndLastIncrementColor(Utils.randomHex());
			setLightRandomColor(lightColorManager, currentTime);
			_lightColorManagers.add(lightColorManager);
		}
	}
	
	private void createLights() {
		float stageWidth = Gdx.graphics.getWidth() * _scaleFactor;
		float stageHeight = Gdx.graphics.getHeight() * _scaleFactor;
		
		_rayHandler = new RayHandler(_world);
		_rayHandler.setCombinedMatrix(_camera);
		
		float rows = LIGHT_ROWS;
		float columns = LIGHT_COLUMNS;
		float rowDistance = stageHeight/(rows-1);
		float colDistance = stageWidth/(columns-1);
		float distance = Math.min(rowDistance, colDistance);
		
		int i = 0;
		for (int r=0; r<rows; r++) {
			for (int c=0; c<columns; c++) {
				float y = rowDistance*r;
				float x = colDistance*c;
				PointLight pointLight = createPointLight(i, x, y, distance);
				_lights.add(pointLight);
				i++;
			}
		}
		
		if (CENTER_LIGHT) {
			PointLight pointLight = createPointLight(i, stageWidth/2, stageHeight/2, distance);
			_lights.add(pointLight);
		}
	}
	
	private PointLight createPointLight(int index, float x, float y, float distance) {
		SmoothColorManager lightColorManager = _lightColorManagers.get(index);
		Color color = lightColorManager.incrementedColor(_currentTime, LIGHT_ALPHA);
		return new PointLight(_rayHandler, LIGHT_RAYS, color, distance, x, y);
	}
	
	private void setupDisplay() {
		if (START_FULLSCREEN) {
			Gdx.graphics.setDisplayMode((int)DISPLAY_RESOLUTION.x, (int)DISPLAY_RESOLUTION.y, START_FULLSCREEN);
		}
	}
	
	private void toggleFullscreen() {
		_didToggleFullscreen = true;
		Gdx.graphics.setDisplayMode((int)DISPLAY_RESOLUTION.x, (int)DISPLAY_RESOLUTION.y, !Gdx.graphics.isFullscreen());
	}
	
	private void exitFullscreen() {
		if (Gdx.graphics.isFullscreen()) {
			_didToggleFullscreen = true;
			Gdx.graphics.setDisplayMode((int)DISPLAY_RESOLUTION.x, (int)DISPLAY_RESOLUTION.y, false);
		}
	}
	
	private void togglePaddleShape() {
		_circlePaddle = !_circlePaddle;
		recreatePaddles();
		if (_circlePaddle) {
			addScrollingText("CIRCLE PADDLES", SCROLLING_TEXT_COLOR_POSITIVE);
		} else {
			addScrollingText("RECTANGLE PADDLES", SCROLLING_TEXT_COLOR_POSITIVE);
		}
	}
	
	private void toggleHidePaddle() {
		_hidePaddle = !_hidePaddle;
		if (_hidePaddle) {
			destroyPaddles();
			addScrollingText("PADDLES HIDDEN", SCROLLING_TEXT_COLOR_NEGATIVE);
		} else {
			recreatePaddles();
			addScrollingText("PADDLES SHOWN", SCROLLING_TEXT_COLOR_POSITIVE);
		}
	}
	
	private void toggleInvertPaddleMovement() {
		_invertPaddleMovement = !_invertPaddleMovement;
		if (_invertPaddleMovement) {
			addScrollingText("INVERTED PADDLE MOVEMENT", SCROLLING_TEXT_COLOR_NEGATIVE);
		} else {
			addScrollingText("NORMAL PADDLE MOVEMENT", SCROLLING_TEXT_COLOR_POSITIVE);
		}
		updatePaddlePosition();
	}
	
	private void toggleBallShape() {
		_squareBall = !_squareBall;
		if (_squareBall) {
			addScrollingText("SQUARE BALLS", SCROLLING_TEXT_COLOR_POSITIVE);
		} else {
			addScrollingText("CIRCLE BALLS", SCROLLING_TEXT_COLOR_POSITIVE);
		}
	}
	
	private void toggleBallColorStyle() {
		_smoothRandomBallColor = !_smoothRandomBallColor;
		if (_smoothRandomBallColor) {
			addScrollingText("SMOOTH BALL COLORS", SCROLLING_TEXT_COLOR_POSITIVE);
		} else {
			addScrollingText("RANDOM BALL COLORS", SCROLLING_TEXT_COLOR_POSITIVE);
		}
	}
	
	private void toggleInvertForce() {
		_invertForce = !_invertForce;
		if (_invertForce) {
			addScrollingText("PUSH FORCE MODE", SCROLLING_TEXT_COLOR_POSITIVE);
		} else {
			addScrollingText("PULL FORCE MODE", SCROLLING_TEXT_COLOR_POSITIVE);
		}
	}
	
	private void toggleRealShadows() {
		_realShadows = !_realShadows;
		if (_realShadows) {
			addScrollingText("FULL SHADOWS", SCROLLING_TEXT_COLOR_POSITIVE);
		} else {
			addScrollingText("GROUND SHADOWS", SCROLLING_TEXT_COLOR_POSITIVE);
		}
	}
	
	private void toggleLightStyle() {
		float stageWidth = Gdx.graphics.getWidth() * _scaleFactor;
		float stageHeight = Gdx.graphics.getHeight() * _scaleFactor;
		float rowDistance = stageHeight/(LIGHT_ROWS-1);
		float colDistance = stageWidth/(LIGHT_COLUMNS-1);
		float distance = Math.min(rowDistance, colDistance);
		float maxDistance = Math.max(stageWidth, stageHeight);
		
		switch (_lightStyle) {
			case GRID_AND_CENTER:
				_lightStyle = LightStyle.GRID;
				addScrollingText("GRID LIGHTS", SCROLLING_TEXT_COLOR_POSITIVE);
				break;
			case GRID:
				_lightStyle = LightStyle.CENTER;
				addScrollingText("CENTER LIGHT", SCROLLING_TEXT_COLOR_POSITIVE);
				break;
			case CENTER:
				_lightStyle = LightStyle.NONE;
				addScrollingText("NO LIGHTS", SCROLLING_TEXT_COLOR_NEGATIVE);
				break;
			case NONE:
				_lightStyle = LightStyle.GRID_AND_CENTER;
				addScrollingText("GRID AND CENTER LIGHTS", SCROLLING_TEXT_COLOR_POSITIVE);
		}
		
		int i = 0;
		for (PointLight light : _lights) {
			boolean last = (i == _lights.size-1);
			float alpha = LIGHT_ALPHA;
			if (last) {
				if (_lightStyle == LightStyle.GRID) {
					alpha = 0.0f;
				}
				if (_lightStyle == LightStyle.CENTER) {
					light.setDistance(maxDistance);
				} else {
					light.setDistance(distance);
				}
			} else {
				if (_lightStyle == LightStyle.CENTER) {
					alpha = 0.0f;
				}
			}
			setLightAlpha(light, alpha);
			i++;
		}
	
		_renderLight = (_lightStyle != LightStyle.NONE);
	}
	
	private void setLightAlpha(PointLight light, float alpha) {
		Color color = light.getColor();
		light.setColor(color.r, color.g, color.b, alpha);
	}
	
	public void resize(int width, int height) {
	    _stage.getViewport().update(width, height, true);
	    
	    if (_didToggleFullscreen) {
	    	_didToggleFullscreen = false;
		    new Timer().scheduleTask( 
			        new Timer.Task() {
			            @Override
			            public void run() {
			            	if (Gdx.graphics.isFullscreen()) {
			    				addScrollingText("FULLSCREEN MODE", SCROLLING_TEXT_COLOR_POSITIVE);
			    			} else {
			    				addScrollingText("WINDOWED MODE", SCROLLING_TEXT_COLOR_POSITIVE);
			    			}
			            }
			        }, 
			        0.5f
			);
	    }
	}
	
	public void dispose() {
	    _stage.dispose();
	    _spriteBatch.dispose();
	    _shapeRenderer.dispose();
	    _debugRenderer.dispose();
	    _rayHandler.dispose();
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
					createForce();
					if (INFINITE_FORCE_ON_RIGHT_CLICK) {
						if (_infiniteForceDelayTimer != null) {
							_infiniteForceDelayTimer.stop();
							_infiniteForceDelayTimer.clear();
						}
						_infiniteForceDelayTimer = new Timer();
						_infiniteForceDelayTimer.scheduleTask( 
						        new Timer.Task() {
						            @Override
						            public void run() {
						            	_infiniteForce = true;
						            	addScrollingText("FORCE ON", SCROLLING_TEXT_COLOR_POSITIVE);
						            }
						        }, 
						        INFINITE_FORCE_DELAY 
						);
					}
				} else if (button == 2) {
					toggleGodMode();
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
						            	addScrollingText("SPAWN ON", SCROLLING_TEXT_COLOR_POSITIVE);
						            }
						        }, 
						        INFINITE_SPAWN_DELAY 
						);
					}
				}
				return true;
			}
			
			public boolean touchUp (int screenX, int screenY, int pointer, int button) {
				if (button == 0) {
					if (INFINITE_SPAWN_ON_LEFT_CLICK) {
						if (_infiniteSpawnDelayTimer != null) {
							_infiniteSpawnDelayTimer.stop();
							_infiniteSpawnDelayTimer.clear();
							_infiniteSpawnDelayTimer = null;
						}
						if (_infiniteSpawn) {
							_infiniteSpawn = false;
							addScrollingText("SPAWN OFF", SCROLLING_TEXT_COLOR_NEGATIVE);
						}
						return true;
					}
				} else if (button == 1) {
					if (INFINITE_FORCE_ON_RIGHT_CLICK) {
						if (_infiniteForceDelayTimer != null) {
							_infiniteForceDelayTimer.stop();
							_infiniteForceDelayTimer.clear();
							_infiniteForceDelayTimer = null;
						}
						if (_infiniteForce) {
							_infiniteForce = false;
							addScrollingText("FORCE OFF", SCROLLING_TEXT_COLOR_NEGATIVE);
						}
						return true;
					}
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
				} else if (keycode == EXIT_FULLSCREEN_KEY_CODE) {
					exitFullscreen();
					return true;
				} else if (keycode == TOGGLE_PADDLE_SHAPE_KEY_CODE) {
					togglePaddleShape();
					return true;
				} else if (keycode == TOGGLE_BALL_SHAPE_KEY_CODE) {
					toggleBallShape();
					return true;
				} else if (keycode == TOGGLE_BALL_COLOR_STYLE_KEY_CODE) {
					toggleBallColorStyle();
					return true;
				} else if (keycode == DRAW_MODE_KEY_CODE) {
					toggleDrawMode();
					return true;
				} else if (keycode == INVERT_FORCE_KEY_CODE) {
					toggleInvertForce();
					return true;
				} else if (keycode == RENDER_LIGHTS_KEY_CODE) {
					toggleLightStyle();
					return true;
				} else if (keycode == REAL_SHADOWS_KEY_CODE) {
					toggleRealShadows();
					return true;
				} else if (keycode == HIDE_PADDLE_KEY_CODE) {
					toggleHidePaddle();
					return true;
				} else if (keycode == CLEAR_BALLS_KEY_CODE) {
					int count = _ballBodies.size;
					requestDestroyBalls();
					if (count > 0) {
						addScrollingText("BALLS CLEARED", SCROLLING_TEXT_COLOR_NEGATIVE);
					}
					return true;
				} else if (keycode == INVERT_PADDLE_MOVEMENT_KEY_CODE) {
					toggleInvertPaddleMovement();
					return true;
				}
				return false;
			}
		});
	}
	
	private void toggleDrawMode() {
		switch (_drawStyle) {
			case NORMAL:
				_drawStyle = DrawStyle.DEBUG;
				addScrollingText("DEBUG DRAW MODE", SCROLLING_TEXT_COLOR_POSITIVE);
				break;
			case DEBUG:
				_drawStyle = DrawStyle.NORMAL_AND_DEBUG;
				addScrollingText("NORMAL AND DEBUG DRAW MODE", SCROLLING_TEXT_COLOR_POSITIVE);
				break;
			case NORMAL_AND_DEBUG:
				_drawStyle = DrawStyle.NORMAL;
				addScrollingText("NORMAL DRAW MODE", SCROLLING_TEXT_COLOR_POSITIVE);
		}
		boolean debugDraw = (_drawStyle == DrawStyle.DEBUG || _drawStyle == DrawStyle.NORMAL_AND_DEBUG);
		_tableLeft.setDebug(debugDraw);
		_tableCenter.setDebug(debugDraw);
		_tableRight.setDebug(debugDraw);
		_tableBottomCenter.setDebug(debugDraw);
	}
	
	private void createUserInterface() {
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = new BitmapFont();
		float topPadding = 50;
		float bottomPadding = 10;

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
		
		Label fullscreenTextLabel = null;
		Label fullscreenLabel = null;
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			fullscreenTextLabel = new Label("Fullscreen", labelStyle);
			fullscreenLabel = new Label(Input.Keys.toString(FULLSCREEN_KEY_CODE) + " / " + Input.Keys.toString(EXIT_FULLSCREEN_KEY_CODE), labelStyle);
		}
		
		Label godModeTextLabel = new Label("God Mode", labelStyle);
		Label godModeLabel = new Label("Middle Click", labelStyle);
		
		Label ballForceTextLabel = new Label("Apply Force", labelStyle);
		Label ballForceLabel = new Label("Right Click / Hold", labelStyle);
		
		Label drawModeTextLabel = new Label("Draw Mode", labelStyle);
		Label drawModeLabel = new Label(Input.Keys.toString(DRAW_MODE_KEY_CODE), labelStyle);
		
		Label spawnBallsTextLabel = new Label("Spawn Balls", labelStyle);
		Label spawnBallsLabel = new Label("Left Click / Hold, Scroll", labelStyle);
		
		Label hidePaddleTextLabel = new Label("Hide Paddles", labelStyle);
		Label hidePaddleLabel = new Label(Input.Keys.toString(HIDE_PADDLE_KEY_CODE), labelStyle);
		
		Label paddleMovementTextLabel = new Label("Paddle Movement", labelStyle);
		Label paddleMovementLabel = new Label(Input.Keys.toString(INVERT_PADDLE_MOVEMENT_KEY_CODE), labelStyle);
		
		Label paddleShapeTextLabel = new Label("Paddle Shape", labelStyle);
		Label paddleShapeLabel = new Label(Input.Keys.toString(TOGGLE_PADDLE_SHAPE_KEY_CODE), labelStyle);
		
		Label ballShapeTextLabel = new Label("Ball Shape", labelStyle);
		Label ballShapeLabel = new Label(Input.Keys.toString(TOGGLE_BALL_SHAPE_KEY_CODE), labelStyle);
		
		Label ballColorStyleTextLabel = new Label("Ball Color", labelStyle);
		Label ballColorStyleLabel = new Label(Input.Keys.toString(TOGGLE_BALL_COLOR_STYLE_KEY_CODE), labelStyle);
		
		Label invertForceTextLabel = new Label("Invert Force", labelStyle);
		Label invertForceLabel = new Label(Input.Keys.toString(INVERT_FORCE_KEY_CODE), labelStyle);
		
		Label lightsTextLabel = new Label("Lights", labelStyle);
		Label lightsLabel = new Label(Input.Keys.toString(RENDER_LIGHTS_KEY_CODE), labelStyle);
		
		Label realShadowsTextLabel = new Label("Shadows", labelStyle);
		Label realShadowsLabel = new Label(Input.Keys.toString(REAL_SHADOWS_KEY_CODE), labelStyle);
		
		Label clearBallsTextLabel = new Label("Clear Balls", labelStyle);
		Label clearBallsLabel = new Label(Input.Keys.toString(CLEAR_BALLS_KEY_CODE), labelStyle);
		
		_tableLeft.top().left();
		_tableLeft.add(fpsCounterTextLabel);
		_tableLeft.row();
		_tableLeft.add(_fpsCounterLabel);
		
		_tableCenter.top();
		_tableCenter.add(_godModeLabel);
		
		_tableRight.top().right();
		_tableRight.add(ballCounterTextLabel).padRight(topPadding);
		_tableRight.add(lostTextLabel).padRight(topPadding);
		_tableRight.add(hitsTextLabel);
		_tableRight.row();
		_tableRight.add(_ballCounterLabel).padRight(topPadding);
		_tableRight.add(_lostLabel).padRight(topPadding);
		_tableRight.add(_hitsLabel);
		
		_tableBottomCenter.bottom();
		_tableBottomCenter.add(spawnBallsTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(godModeTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(ballForceTextLabel).padRight(bottomPadding);
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			_tableBottomCenter.add(fullscreenTextLabel).padRight(bottomPadding);
		}
		_tableBottomCenter.add(drawModeTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(hidePaddleTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(paddleMovementTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(paddleShapeTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(ballShapeTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(ballColorStyleTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(invertForceTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(lightsTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(realShadowsTextLabel).padRight(bottomPadding);
		_tableBottomCenter.add(clearBallsTextLabel);
		_tableBottomCenter.row();
		_tableBottomCenter.add(spawnBallsLabel).padRight(bottomPadding);
		_tableBottomCenter.add(godModeLabel).padRight(bottomPadding);
		_tableBottomCenter.add(ballForceLabel).padRight(bottomPadding);
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			_tableBottomCenter.add(fullscreenLabel).padRight(bottomPadding);
		}
		_tableBottomCenter.add(drawModeLabel).padRight(bottomPadding);
		_tableBottomCenter.add(hidePaddleLabel).padRight(bottomPadding);
		_tableBottomCenter.add(paddleMovementLabel).padRight(bottomPadding);
		_tableBottomCenter.add(paddleShapeLabel).padRight(bottomPadding);
		_tableBottomCenter.add(ballShapeLabel).padRight(bottomPadding);
		_tableBottomCenter.add(ballColorStyleLabel).padRight(bottomPadding);
		_tableBottomCenter.add(invertForceLabel).padRight(bottomPadding);
		_tableBottomCenter.add(lightsLabel).padRight(bottomPadding);
		_tableBottomCenter.add(realShadowsLabel).padRight(bottomPadding);
		_tableBottomCenter.add(clearBallsLabel);
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
		
		if (_godMode) {
			addScrollingText("GOD MODE ON", SCROLLING_TEXT_COLOR_POSITIVE);
		} else {
			addScrollingText("GOD MODE OFF", SCROLLING_TEXT_COLOR_NEGATIVE);
		}
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
	
	private void requestDestroyBalls() {
		for (Body ballBody : _ballBodies) {
			requestDestroyBall(ballBody);
		}
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
		if (_hidePaddle) return;
		
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
			//bodyUserData.fillColor = BodyUserData.randomColor();
			bodyUserData.fillColor = Color.RED;
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
	        _paddleMousePrismaticJoints[i] = (PrismaticJoint)_world.createJoint(prismaticJointDef);
		}
	}
	
	private void recreatePaddles() {
		destroyPaddles();
		createPaddles();
	}
	
	private void destroyPaddles() {
		for (int i=0; i<NUM_OF_PADDLES; i++) {
			if (_paddleBodies[i] == null) continue;
			
			_world.destroyJoint(_paddleMouseJoints[i]);
			_world.destroyJoint(_paddleMousePrismaticJoints[i]);
			_world.destroyBody(_paddleBodies[i]);
			
			_paddleFixtures[i] = null;
			_paddleMouseJoints[i] = null;
			_paddleMousePrismaticJoints[i] = null;
			_paddleBodies[i] = null;
		}
	}
	
	private void createForce() {
		Vector2 forcePosition;
		if (CREATE_BALL_ON_POINTER) {
			forcePosition = cursorToWorldPosition(Gdx.input.getX(), Gdx.input.getY());
		} else {
			forcePosition = new Vector2(_camera.viewportWidth/2, _camera.viewportHeight/2);
		}
		
		for (Body ballBody : _ballBodies) {
			Vector2 position = ballBody.getPosition();
			double forceAngle = Math.atan2(forcePosition.y - position.y, forcePosition.x - position.x);
			
			float force = FORCE_STRENGTH;
			if (_invertForce) {
				force = -force;
			}
			Vector2 forceVector = new Vector2((float)Math.cos(forceAngle)*force, (float)Math.sin(forceAngle)*force);
			ballBody.applyForceToCenter(forceVector, true);
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
		bodyUserData.fillColor = (_smoothRandomBallColor?_ballColorManager.incrementedColor(_currentTime):Utils.randomColor());
		
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
	
	private void setBallRandomColor() {
		float delay = BALL_SMOOTH_COLOR_SECONDS;
		_ballColorManager.setTargetIncrementColor(Utils.randomHex(), _currentTime+delay);
	}
	
	private void setLightRandomColors() {
		double currentTime = _currentTime;
		for (SmoothColorManager lightColorManager : _lightColorManagers) {
			setLightRandomColor(lightColorManager, currentTime);
		}
	}
	private void setLightRandomColor(SmoothColorManager lightColorManager, double currentTime) {
		float delay = LIGHT_SMOOTH_COLOR_SECONDS;
		lightColorManager.setTargetIncrementColor(Utils.randomHex(), currentTime+delay);
	}
	
	private void updateColorManagers() {
		if (_smoothRandomBallColor) {
        	_ballColorSecondsPassed++;
        	if (_ballColorSecondsPassed >= BALL_SMOOTH_COLOR_SECONDS) {
        		_ballColorSecondsPassed = 0;
        		setBallRandomColor();
        	}
        }
        
		_lightColorSecondsPassed++;
        if (_lightColorSecondsPassed >= LIGHT_SMOOTH_COLOR_SECONDS) {
        	_lightColorSecondsPassed = 0;
        	setLightRandomColors();
    	}
	}
	
	public void update(float deltaTime) {
		_currentTime += deltaTime;
		_totalTime += deltaTime;
		_frameCount++;
		
		if (_totalTime > 1.0)
	    {
	        updateFPSCounterLabel(_frameCount/_totalTime);
	        _frameCount = 0;
	        _totalTime = 0;
	        
	        updateColorManagers();
	    }
		
		if (_infiniteSpawn) {
			createBall();
		}
		if (_infiniteForce) {
			createForce();
		}
		destroyDeadBalls();
		
		_camera.update();
		_shapeRenderer.setProjectionMatrix(_camera.combined);
		
		_world.step(deltaTime, 6, 2);
	}
 
	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		update(deltaTime);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		renderLightColors();
		if (_renderLight && !_realShadows) {
			_rayHandler.updateAndRender();
		}
		renderBalls();
		renderPaddles();
		if (_renderLight && _realShadows) {
			_rayHandler.updateAndRender();
		}
		
		renderScrollingText(deltaTime);
		
		_stage.act(deltaTime);
		_stage.draw();
		
		if (_drawStyle == DrawStyle.DEBUG || _drawStyle == DrawStyle.NORMAL_AND_DEBUG) {
			_spriteBatch.begin();
			_debugRenderer.render(_world, _camera.combined);
			_spriteBatch.end();
		}
		
		if (!_didRender) {
			_didRender = true;
			didRender();
		}
	}
	
	private void renderLightColors() {
		int i = 0;
		for (PointLight light : _lights) {
			SmoothColorManager lightColorManager = _lightColorManagers.get(i);
			light.setColor(lightColorManager.incrementedColor(_currentTime, light.getColor().a));
			i++;
		}
	}
	
	private void updatePaddlePosition() {
		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		if (_invertPaddleMovement) {
			x = Gdx.graphics.getWidth()-x;
			y = Gdx.graphics.getHeight()-y;
		}
		Vector2 worldPosition = cursorToWorldPosition(x, y);
		_paddlePosition = new Vector2(worldPosition.x, worldPosition.y);
	}
	
	private void renderBalls() {
		for (int i=0; i<_ballBodies.size; i++) {
			Body body = _ballBodies.get(i);
			Vector2 position = body.getPosition();
			
			if (_drawStyle == DrawStyle.NORMAL || _drawStyle == DrawStyle.NORMAL_AND_DEBUG) {
				Fixture fixture = _ballFixtures.get(i);
				Shape shape = fixture.getShape();
				BodyUserData bodyUserData = (BodyUserData)body.getUserData();
				
				for (int d=0; d<2; d++) {
					if (d==0) {
						_shapeRenderer.begin(ShapeType.Filled);
						_shapeRenderer.setColor(bodyUserData.fillColor);
					} else {
						_shapeRenderer.begin(ShapeType.Line);
						_shapeRenderer.setColor(bodyUserData.lineColor);
					}
					
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
				BodyUserData bodyUserData = (BodyUserData)body.getUserData();
				float x = position.x-size.x/2;
				float y = position.y-size.y/2;
				
				for (int d=0; d<2; d++) {
					if (d==0) {
						_shapeRenderer.begin(ShapeType.Filled);
						_shapeRenderer.setColor(bodyUserData.fillColor);
					} else {
						_shapeRenderer.begin(ShapeType.Line);
						_shapeRenderer.setColor(bodyUserData.lineColor);
					}
					
					if (shape instanceof CircleShape) {
						_shapeRenderer.circle(x+size.x/2, y+size.y/2, shape.getRadius());
					} else {
						_shapeRenderer.rect(x, y, size.x/2, size.y/2, size.x, size.y, 1.0f, 1.0f, angle);
					}
					
					_shapeRenderer.end();
				}
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
