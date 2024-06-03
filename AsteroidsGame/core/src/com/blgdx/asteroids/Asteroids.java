//**********************************************************************************************************************
// Asteroid Game Remake using LibGDX - CSC455 Assignment 1 - Written by Brandon LaPointe
//
//	NOTES:
//		Make sure to turn off Settings>FullKeyboardAccess on MacOS
//
//	TODO NOTES:
//		- Implement overheat function to laser
//			- Add gauge that displays laser heat level
//			- Disable laser when limit is hit until cooldown period ends
//			- Add sound effects for heat limit and cooldown
//		- Randomize direction of splitting asteroids better
//		- Increase difficultyScale after X seconds or every X points
//		- Implement saving and display of high scores
//		- Add explosion animations to ship and asteroids when hit
//		- Spawn repair power-up in random location on screen after X points gathered
//		- Add "rogue asteroid" (larger red asteroid) that will chase the player and take multiple hits to destroy -> Repair power-up drop?
//		- Add short duration boost when Shift is pressed, Add cooldown timer to boost (Levels of boost/cooldown time assigned to ship damage state)
//		- Add power-up to wipe the board of all asteroids. Carry limit (1-3?). Press Tab to use.
//
//**********************************************************************************************************************
package com.blgdx.asteroids;

// Import statements
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Asteroids extends ApplicationAdapter {
	private double difficultyScale;
	private double[] playerRotationMultipliers = {1.0, 1.5, 2.0}; // Set different rotation multipliers for different damage states
	private float[] maxPlayerVelocities = {150f, 250f, 350f}; // Set different maximum velocities for different damage states
	private float[] accelerationRates = {50f, 100f, 150f}; // Set different acceleration rates for different damage states
	private float[] decelerationRates = {50f, 100f, 150f}; // Set different deceleration rates for different damage states
	private float[] fireCooldowns = {0.4f, 0.2f, 0.1f}; // Set different cooldowns for different damage states
	private float[] laserTimers = {0f, 0f, 0f}; // Track individual cooldown timers for each damage state
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture menuBackgroundImage;
	private Texture backgroundImage;
	private Vector2 playerPosition;
	private float playerRadius;
	private float playerRotation;
	private float playerVelocity;
	private int playerHealthLevel;
	private Texture heartTexture;
	private Texture[] playerDamageTextures;
	private float thrustDirection;
	private List<Asteroid> asteroids;
	private List<Laser> lasers;
	private float asteroidSpawnTimer;
	private float laserSpeed;
	private int score;
	private SpriteBatch scoreBatch;
	private BitmapFont font;
	private Music backgroundMusic;
	private Sound laserSound;
	private Sound asteroidExplosionSound;
	private Sound shipDamageSound;
	private Sound shipExplosionSound;
	private boolean gameOver;
	private GameState gameState;

	// GameState enum to track the game state
	private enum GameState {
		MENU, PLAYING, GAME_OVER
	}

	@Override
	public void create() {
		// Set the input processor to detect key presses
		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				System.out.println("Key Down Event, keycode: " + keycode);
				return true; // return true to indicate the event was handled
			}
		});

		// Initialize the camera
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false);
		batch = new SpriteBatch();

		// Load background image for the menu
		menuBackgroundImage = new Texture("BML02096-Grayed.jpg");

		// Load background image
		backgroundImage = new Texture("BML02096.jpg");

		// Initialize the player
		playerPosition = new Vector2(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
		playerRadius = 20f;
		playerRotation = MathUtils.random(360f);
		playerVelocity = 0f;
		thrustDirection = playerRotation;
		playerHealthLevel = 3;

		// Load heart texture
		heartTexture = new Texture("heart.png");

		// Load player ship textures
		playerDamageTextures = new Texture[]{
				new Texture("SpaceShip_MaxDamage.png"),
				new Texture("SpaceShip_Damage.png"),
				new Texture("SpaceShip.png")
		};

		// Initialize the asteroids and lasers array lists
		asteroids = new ArrayList<>();
		lasers = new ArrayList<>();

		// Initialize the laser
		laserSpeed = 1000f;

		// Initialize the score and score batch
		score = 0;
		scoreBatch = new SpriteBatch();

		// Initialize the font
		font = new BitmapFont();
		font.setColor(Color.WHITE);

		// Load asteroid explosion sound
		asteroidExplosionSound = Gdx.audio.newSound(Gdx.files.internal("mixkit-war-explosion.wav"));

		// Load ship damage sound
		shipDamageSound = Gdx.audio.newSound(Gdx.files.internal("mixkit-fuel-explosion.mp3"));

		// Load ship explosion sound
		shipExplosionSound = Gdx.audio.newSound(Gdx.files.internal("mixkit-explosion-glass.mp3"));

		// Load background music (YouTube : Horror Dark Synthwave - Ghoul - Royalty Free Copyright Safe Music)
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Ghoul.mp3"));
		backgroundMusic.setLooping(true);  // Set to loop
		backgroundMusic.setVolume(0.5f);   // Adjust volume as needed
		backgroundMusic.play();

		// Initialize the game over state
		gameOver = false;

		// Initialize the current game state
		gameState = GameState.MENU;
	}

	@Override
	public void render() {
		switch (gameState) {
			case MENU:
				drawMenu();
				handleMenuInput();
				break;
			case PLAYING:
				updatePlayingState(Gdx.graphics.getDeltaTime());
				break;
			case GAME_OVER:
				drawGameOverScreen();
				handleGameOverInput();
				break;
		}
	}

	@Override
	public void dispose() {
		// Dispose of the batch
		batch.dispose();

		// Dispose of the score batch
		scoreBatch.dispose();

		// Dispose of the font
		font.dispose();

		// Dispose of the menu background image
		menuBackgroundImage.dispose();

		// Dispose of the background image
		backgroundImage.dispose();

		// Dispose of the heart texture
		heartTexture.dispose();

		// Dispose of the background music
		backgroundMusic.dispose();
	}

	private void handleInput(float delta) {
		// Get the player's damage state index
		int damageStateIndex = Math.max(0, playerHealthLevel - 1); // Adjust if playerHealthLevel can go below 1

		// Rotate the player based on input
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			playerRotation += 180 * delta * playerRotationMultipliers[damageStateIndex];
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			playerRotation -= 180 * delta * playerRotationMultipliers[damageStateIndex];
		}

		// Move the player forward or apply deceleration based on input
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			float targetThrustDirection = playerRotation;

			// Gradually update thrustDirection towards the target direction
			thrustDirection = MathUtils.lerpAngleDeg(thrustDirection, targetThrustDirection, 0.1f);

			// Update the player's velocity when thrusting
			playerVelocity += accelerationRates[damageStateIndex] * delta;
		} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			// Apply increased deceleration when pressing down arrow or 'S' key
			playerVelocity -= (decelerationRates[damageStateIndex] + 100f) * delta;
		} else {
			// Apply standard deceleration when not thrusting or pressing down arrow or 'S' key
			playerVelocity -= decelerationRates[damageStateIndex] * delta;
		}

		// Ensure that the velocity doesn't go below 0
		playerVelocity = Math.max(playerVelocity, 0);

		// Limit the player's velocity based on the damage state
		float maxVelocity = maxPlayerVelocities[damageStateIndex];
		playerVelocity = Math.min(playerVelocity, maxVelocity);

		// Update the player's position based on velocity
		float angleRad = MathUtils.degreesToRadians * thrustDirection;
		float xDelta = MathUtils.cos(angleRad) * playerVelocity * delta;
		float yDelta = MathUtils.sin(angleRad) * playerVelocity * delta;

		// Apply the deltas to calculate the new position
		float newX = playerPosition.x + xDelta;
		float newY = playerPosition.y + yDelta;

		// Ensure the player stays within the screen boundaries
		newX = MathUtils.clamp(newX, playerRadius, Gdx.graphics.getWidth() - playerRadius);
		newY = MathUtils.clamp(newY, playerRadius, Gdx.graphics.getHeight() - playerRadius);

		// Update the player's position
		playerPosition.x = newX;
		playerPosition.y = newY;

		// Keep playerRotation within a reasonable range, e.g., [0, 360)
		playerRotation = playerRotation % 360;

		// Shoot lasers with cooldown based on damage state
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && laserTimers[damageStateIndex] <= 0) {
			shoot();

			// Reset the timer to the cooldown for the current damage state
			laserTimers[damageStateIndex] = fireCooldowns[damageStateIndex];
		}

		// Update the laser timers
		for (int i = 0; i < laserTimers.length; i++) {
			laserTimers[i] = Math.max(0, laserTimers[i] - delta);
		}

		// Adjust asteroid speed using number keys
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
			difficultyScale = 0.25;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
			difficultyScale = 0.5;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
			difficultyScale = 0.75;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
			difficultyScale = 1.0;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) {
			difficultyScale = 1.5;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_6)) {
			difficultyScale = 2.0;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_7)) {
			difficultyScale = 3.5;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_8)) {
			difficultyScale = 7.0;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_9)) {
			difficultyScale = 10.0;
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_0)) {
			difficultyScale = 0.0; // Stop asteroid speed completely
		}
	}

	private void updatePlayingState(float delta) {
		if (gameOver) {
			// Draw game over screen
			drawGameOverScreen();

			// Handle input for game over screen
			handleGameOverInput();

		} else {
			// Handle input
			handleInput(Gdx.graphics.getDeltaTime());

			// Update game asteroid logic
			updateAsteroids((float) (Gdx.graphics.getDeltaTime() * difficultyScale));
			spawnAsteroids((float) (Gdx.graphics.getDeltaTime() * difficultyScale));

			// Update game laser logic
			updateLasers(Gdx.graphics.getDeltaTime());

			// Clear the screen
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			// Set the camera projection matrix
			batch.setProjectionMatrix(camera.combined);

			// Draw background image
			batch.begin();
			batch.draw(backgroundImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();

			// Draw player, asteroids, and lasers
			batch.begin();

			// Draw player with rotation and damage level
			batch.draw(getPlayerDamageTexture(), playerPosition.x - playerRadius, playerPosition.y - playerRadius,
					playerRadius, playerRadius, 2 * playerRadius, 2 * playerRadius, 1, 1, playerRotation, 0, 0,
					getPlayerDamageTexture().getWidth(), getPlayerDamageTexture().getHeight(), false, false);


			// Draw asteroids
			for (Asteroid asteroid : asteroids) {
				batch.draw(asteroid.getTexture(), asteroid.getPosition().x - asteroid.getRadius(),
						asteroid.getPosition().y - asteroid.getRadius(),
						asteroid.getRadius(), asteroid.getRadius(), 2 * asteroid.getRadius(), 2 * asteroid.getRadius(),
						1, 1, asteroid.getRotation(), 0, 0,
						asteroid.getTexture().getWidth(), asteroid.getTexture().getHeight(), false, false);
			}

			// Draw lasers
			for (Laser laser : lasers) {
				batch.draw(laser.getTexture(), laser.getPosition().x - laser.getRadius(),
						laser.getPosition().y - laser.getRadius(),
						laser.getRadius(), laser.getRadius(), 2 * laser.getRadius(), 2 * laser.getRadius(),
						1, 1);
			}

			batch.end();

			// Draw the score and health in a separate area
			scoreBatch.begin();
			font.draw(scoreBatch, "Score: " + score, Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() - 20);
			drawHearts();
			scoreBatch.end();
		}
	}

	private void drawHearts() {
		// Draw hearts based on player health level
		for (int i = 0; i < playerHealthLevel; i++) {
			float heartX = 20 + i * 40; // Adjust the spacing as needed
			float heartY = Gdx.graphics.getHeight() - 40;

			// Draw heart texture
			scoreBatch.draw(heartTexture, heartX, heartY, 30, 30);
		}
	}

	private Texture getPlayerDamageTexture() {
		if (playerHealthLevel >= 1 && playerHealthLevel <= 3) {
			return playerDamageTextures[playerHealthLevel - 1];
		} else {
			// Default to full damage texture if the damage level is out of bounds
			return playerDamageTextures[0];
		}
	}

	private void spawnAsteroids(float delta) {
		// Spawn asteroids at random intervals
		asteroidSpawnTimer += delta;
		if (asteroidSpawnTimer > MathUtils.random(0.5f, 2.5f)) {
			// Choose a random side (0, 1, 2, or 3)
			int randomSide = MathUtils.random(3);
			asteroids.add(new Asteroid(randomSide));
			asteroidSpawnTimer = 0f;
		}
	}

	private void updateAsteroids(float delta) {
		// Update asteroid positions and rotation
		Iterator<Asteroid> iterator = asteroids.iterator();
		while (iterator.hasNext()) {
			Asteroid asteroid = iterator.next();
			float angleRad = MathUtils.degreesToRadians * asteroid.getDirection();
			float xDelta = MathUtils.cos(angleRad) * asteroid.getSpeed() * delta;
			float yDelta = MathUtils.sin(angleRad) * asteroid.getSpeed() * delta;

			asteroid.getPosition().x += xDelta;
			asteroid.getPosition().y += yDelta;

			// Update the rotation of the asteroid
			asteroid.setRotation(asteroid.getRotation() + asteroid.getRotationSpeed() * delta); // Update rotation

			// Check for collisions with the player
			if (checkCollision(playerPosition, playerRadius, asteroid)) {
				handlePlayerAsteroidCollision();
				iterator.remove();
			}

			// Remove asteroids that are out of screen
			if (asteroid.getPosition().y + asteroid.getRadius() < 0 ||
					asteroid.getPosition().x - asteroid.getRadius() > Gdx.graphics.getWidth() ||
					asteroid.getPosition().x + asteroid.getRadius() < 0 ||
					asteroid.getPosition().y - asteroid.getRadius() > Gdx.graphics.getHeight()) {
				iterator.remove();
			}
		}
	}

	// Check if there's a collision between the player and an asteroid
	private boolean checkCollision(Vector2 objectPosition, float objectRadius, Asteroid asteroid) {
		float distance = objectPosition.dst(asteroid.getPosition());
		return distance < objectRadius + asteroid.getRadius();
	}

	private void handlePlayerAsteroidCollision() {
		// Reduce hits left by 1 or perform other actions
		playerHealthLevel--;

		// Check if the game is over
		if (playerHealthLevel <= 0) {
			gameOver();
		}

		// Play the ship damage sound
		shipDamageSound.play(1.0f);
	}

	private void shoot() {
		// Spawn a new laser
		lasers.add(new Laser(playerPosition.x, playerPosition.y, playerRotation));

		// Randomly choose between "laser.mp3" and "lazer.mp3"
		if (MathUtils.randomBoolean()) {
			laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.mp3"));
		} else {
			laserSound = Gdx.audio.newSound(Gdx.files.internal("lazer.mp3"));
		}

		//Play laser sound
		laserSound.play();
	}

	private void updateLasers(float delta) {
		List<Laser> lasersToRemove = new ArrayList<>();
		List<Asteroid> asteroidsToRemove = new ArrayList<>();
		List<Asteroid> newAsteroids = new ArrayList<>(); // New list to store asteroids to be added

		for (Laser laser : lasers) {
			laser.getPosition().x += laserSpeed * MathUtils.cosDeg(laser.getDirection()) * delta;
			laser.getPosition().y += laserSpeed * MathUtils.sinDeg(laser.getDirection()) * delta;

			// Remove lasers that are out of screen
			if (laser.getPosition().x < 0 || laser.getPosition().x > Gdx.graphics.getWidth() ||
					laser.getPosition().y < 0 || laser.getPosition().y > Gdx.graphics.getHeight()) {
				lasersToRemove.add(laser);
			} else {
				// Check for collisions with asteroids
				Iterator<Asteroid> asteroidIterator = asteroids.iterator();
				while (asteroidIterator.hasNext()) {
					Asteroid asteroid = asteroidIterator.next();
					if (checkCollision(laser, asteroid)) {
						if (asteroid.getSize() == 1) {
							asteroidsToRemove.add(asteroid); // Remove the asteroid if it's the smallest size
						} else {
							asteroid.decreaseSize(); // Decrease the size of the asteroid upon collision

							// CLONE ASTEROID HERE AFTER DECREASE SIZE
							Asteroid smallerAsteroid = new Asteroid(asteroid.getPosition().x, asteroid.getPosition().y, asteroid.getSize());
							// Add the smaller asteroid to the newAsteroids list
							newAsteroids.add(smallerAsteroid);
						}

						// Increment the score or perform other actions as needed
						score++;

						// Play asteroid explosion sound
						asteroidExplosionSound.play(1.0f); // Adjust volume as needed

						// Mark the laser for removal
						lasersToRemove.add(laser);
					}
				}
			}
		}

		// Remove lasers that need to be removed
		lasers.removeAll(lasersToRemove);

		// Remove asteroids that need to be removed
		asteroids.removeAll(asteroidsToRemove);

		// Add new asteroids after the iteration
		asteroids.addAll(newAsteroids);
	}

	// Check if there's a collision between a laser and an asteroid
	private boolean checkCollision(Laser laser, Asteroid asteroid) {
		float distance = laser.getPosition().dst(asteroid.getPosition());
		return distance < laser.getRadius() + asteroid.getRadius();
	}

	private void drawMenu() {
		// Clear the screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();

		// Draw menu background
		batch.draw(menuBackgroundImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// Calculate the positions to center the menu options
		float centerX = Gdx.graphics.getWidth() / 2f;

		// Create GlyphLayout instances to measure the width of each menu option
		GlyphLayout playLayout = new GlyphLayout(font, "1. Play Asteroids");
		GlyphLayout exitLayout = new GlyphLayout(font, "2. Close Application");

		// Draw menu options
		font.draw(batch, "1. Play Asteroids", centerX - playLayout.width / 2, Gdx.graphics.getHeight() / 2 + 50);
		font.draw(batch, "2. Close Application", centerX - exitLayout.width / 2, Gdx.graphics.getHeight() / 2);

		// Draw controls on two lines, centered
		float controlY = 125;  // Adjusted Y-coordinate to ensure it's on the screen

		// Draw "Controls" centered
		GlyphLayout controlsLayout = new GlyphLayout(font, "Controls:");
		float controlsX = centerX - controlsLayout.width / 2;
		font.draw(batch, "Controls:", controlsX, controlY);
		controlY -= 30;

		// Draw controls lines centered
		String controlsLine1 = "W : Thrust Forward     A : Spin Left     S : Brake     D : Spin Right";
		GlyphLayout controlsLine1Layout = new GlyphLayout(font, controlsLine1);
		float controlsLine1X = centerX - controlsLine1Layout.width / 2;
		font.draw(batch, controlsLine1, controlsLine1X, controlY);
		controlY -= 30;

		String controlsLine2 = "Space : Shoot Lasers     1-9 : Asteroid Speed Control     0 : Stop Asteroids";
		GlyphLayout controlsLine2Layout = new GlyphLayout(font, controlsLine2);
		float controlsLine2X = centerX - controlsLine2Layout.width / 2;
		font.draw(batch, controlsLine2, controlsLine2X, controlY);

		batch.end();
	}


	private void handleMenuInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
			// Start the game
			gameState = GameState.PLAYING;
			resetGame();  // Reset game state
		} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
			// Close the application
			Gdx.app.exit();
		}
	}

	private void resetGame() {
		// Reset game state, score, and other parameters
		playerHealthLevel = 3;
		score = 0;
		gameOver = false;
		asteroids.clear();
		lasers.clear();
		backgroundMusic.play();
	}

	private void gameOver() {
		// Set the game over flag
		gameOver = true;

		// Stop background music
		backgroundMusic.stop();

		// Play game over sound
		shipExplosionSound.play(1.0f);
	}

	private void drawGameOverScreen() {
		batch.begin();

		// Set a larger font size for the "Game Over" text
		font.getData().setScale(2.0f); // Adjust the scale as needed

		// Draw "Game Over" centered
		GlyphLayout layoutGameOver = new GlyphLayout(font, "Game Over");
		float gameOverTextWidth = layoutGameOver.width;
		float gameOverTextHeight = layoutGameOver.height;

		font.draw(batch, "Game Over", (Gdx.graphics.getWidth() - gameOverTextWidth) / 2f,
				Gdx.graphics.getHeight() / 2f + gameOverTextHeight); // Adjust the Y position

		// Reset the font scale to avoid affecting other parts of the game
		font.getData().setScale(1.0f);

		// Draw "R: Play Again" centered below the "Game Over" message with a smaller font size
		String replayText = "R : Play Again";
		GlyphLayout layoutReplay = new GlyphLayout(font, replayText);
		float replayTextWidth = layoutReplay.width;
		float replayTextHeight = layoutReplay.height;

		font.getData().setScale(1.0f); // Set a smaller scale for the smaller font
		font.draw(batch, replayText, (Gdx.graphics.getWidth() - replayTextWidth) / 2f,
				Gdx.graphics.getHeight() / 2f - 30 - replayTextHeight / 2f); // Adjust the Y position

		// Draw "ESC: Main Menu" centered below the "R: Play Again" message with a smaller font size
		String mainMenuText = "ESC : Main Menu";
		GlyphLayout layoutMainMenu = new GlyphLayout(font, mainMenuText);
		float mainMenuTextWidth = layoutMainMenu.width;
		float mainMenuTextHeight = layoutMainMenu.height;

		font.draw(batch, mainMenuText, (Gdx.graphics.getWidth() - mainMenuTextWidth) / 2f,
				Gdx.graphics.getHeight() / 2f - 60 - mainMenuTextHeight / 2f); // Adjust the Y position

		// Reset the font scale to avoid affecting other parts of the game
		font.getData().setScale(1.0f);

		batch.end();
	}


	private void handleGameOverInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.R)) {
			// Replay the game
			resetGame();
			gameState = GameState.PLAYING;
		} else if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			// Return to the main menu
			gameState = GameState.MENU;
			returnToMainMenu();
		}
	}

	private void returnToMainMenu() {
		drawMenu();
		handleMenuInput();
	}
}
