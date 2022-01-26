import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 * 
 * This is a 2D space game, created with LWJGL in Java
 * 
 * The aim of the game is to collect as many point as possible
 * 
 * Beware of the mines! You have three lives, but there is a possibility to earn
 * extra lives while playing
 * 
 * Each yellow present gives 500 points, each normal one - 100 points
 * 
 * Have fun and good luck! :)
 */
public class SpaceshipGame {

	/** Game title */
	public static final String GAME_TITLE = "Spaceship Game";

	/** Screen size */
	private static int SCREEN_SIZE_WIDTH = 800;
	private static int SCREEN_SIZE_HEIGHT = 600;

	/** Desired frame time */
	private static final int FRAMERATE = 60;

	private static final int MAX_LIVES = 3;
	private static final int SPACESHIP_START_X = 10;
	private static final int SPACESHIP_START_Y = 250;

	/** Exit the game */
	private boolean finished;

	/** Initializing all entities: */
	private LevelTile levelTile;
	private ArrayList<Entity> entities;
	private HeroEntity spaceshipEntity;
	ArrayList<Entity> presents;
	ArrayList<Entity> yellowPresents;
	ArrayList<Entity> levelMines;
	ArrayList<Entity> hearts;

	private int lives = MAX_LIVES;

	Date startingDate = new Date();

	/** This method returns the time in millis: */
	long startingTimeMilli = startingDate.getTime();

	/** Two different types of font are implemented */
	private TrueTypeFont font;
	private TrueTypeFont bigFont;

	/** The variable where the score will be saved and updated: */
	private int presentsCollected = 0;
	long previous;

	/**
	 * Application init
	 * 
	 */
	public static void main(String[] args) {
		SpaceshipGame myGame = new SpaceshipGame();
		myGame.start();
	}

	public void start() {
		try {
			init();
			run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Sys.alert(GAME_TITLE, "An error occured and the game will exit.");
		} finally {
			cleanup();
		}

		System.exit(0);
	}

	/**
	 * Initialise the game
	 * 
	 * @throws Exception if init fails
	 */
	private void init() throws Exception {
		// Create a fullscreen window with 1:1 orthographic 2D projection, and with
		// mouse, keyboard, and gamepad inputs.
		try {
			initGL(SCREEN_SIZE_WIDTH, SCREEN_SIZE_HEIGHT);

			initTextures();
		} catch (IOException e) {
			e.printStackTrace();
			finished = true;
		}
	}

	private void initGL(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setTitle(GAME_TITLE);
			Display.setFullscreen(false);
			Display.create();

			// Enable vsync if we can
			Display.setVSyncEnabled(true);

			// Start up the sound system
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		//give parameters to the two fonts
		Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
		Font endGameFont = new Font("Times New Roman", Font.BOLD, 50);
		font = new TrueTypeFont(awtFont, true);
		bigFont = new TrueTypeFont(endGameFont, true);
	}

	private void initTextures() throws IOException {
		entities = new ArrayList<Entity>();

		initLevel();

		Texture texture;

		// Load spaceship sprite
		texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/spaceship.png"));
		spaceshipEntity = new HeroEntity(this, new MySprite(texture), SPACESHIP_START_X, SPACESHIP_START_Y);
		entities.add(spaceshipEntity);

		presents = new ArrayList<Entity>();
		yellowPresents = new ArrayList<Entity>();
		levelMines = new ArrayList<Entity>();
		hearts = new ArrayList<Entity>();

	}

	private void createPresents() throws IOException {
		//load the image

		Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/present.png"));
		// a random value for the y axis, which creates every present on a different altitude
		Random rand = new Random();

		int objectX = (SCREEN_SIZE_WIDTH + texture.getImageWidth());
		int objectY = rand.nextInt(SCREEN_SIZE_HEIGHT - texture.getImageHeight());

		PresentEntity objectEntity = new PresentEntity(new MySprite(texture), objectX, objectY);

		presents.add(objectEntity);

	}

	private void createYellowPresents() throws IOException {

		Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/yellowPresent.png"));
		Random rand = new Random();

		int objectX = (SCREEN_SIZE_WIDTH + texture.getImageWidth());
		int objectY = rand.nextInt(SCREEN_SIZE_HEIGHT - texture.getImageHeight());

		SpecialPresentEntity objectEntity = new SpecialPresentEntity(new MySprite(texture), objectX, objectY);

		yellowPresents.add(objectEntity);

	}

	private void createMines() throws IOException {

		Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/rocket_mine.png"));
		Random rand = new Random();

		int objectX = (SCREEN_SIZE_WIDTH + texture.getImageWidth());
		int objectY = rand.nextInt(SCREEN_SIZE_HEIGHT - texture.getImageHeight());

		MineEntity objectEntity = new MineEntity(new MySprite(texture), objectX, objectY);

		levelMines.add(objectEntity);

	}

	private void createHearts() throws IOException {
		
		Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/heart.png"));
		Random rand = new Random();

		int objectX = (SCREEN_SIZE_WIDTH + texture.getImageWidth());
		int objectY = rand.nextInt(SCREEN_SIZE_HEIGHT - texture.getImageHeight());

		HeartEntity objectEntity = new HeartEntity(new MySprite(texture), objectX, objectY);

		hearts.add(objectEntity);

	}

	private void initLevel() throws IOException {
		Texture texture;

//		 Load background image
		texture = TextureLoader.getTexture("PNG",
				ResourceLoader.getResourceAsStream("res/spaceTile.png"));

		levelTile = new LevelTile(texture);

	}

	/**
	 * Runs the game (the "main loop")
	 * 
	 * @throws IOException
	 */
	private void run() throws IOException {
		while (!finished) {
			// Always call Window.update(), all the time
			Display.update();

			if (Display.isCloseRequested()) {
				// Check for O/S close requests
				finished = true;
			} else if (Display.isActive()) {
				// The window is in the foreground, so we should play the game
				logic();
				render();
				drawObjects();
				drawHUD();
				checkForLives();
				Display.sync(FRAMERATE);
			} else {
				// The window is not in the foreground, so we can allow other
				// stuff to run and
				// infrequently update
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				logic();
				if (Display.isVisible() || Display.isDirty()) {
					// Only bother rendering if the window is visible or dirty
					render();
				}
			}
		}

	}

	/**
	 * Do any game-specific cleanup
	 */
	private void cleanup() {
		// TODO: save anything you want to disk here

		// Stop the sound
		AL.destroy();

		// Close the window
		Display.destroy();
	}

	/**
	 * Render the current frame
	 */
	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		Color.white.bind();

		levelTile.getTexture().bind();
		//draws the background tiles
		for (int a = 0; a * levelTile.getHeight() < SCREEN_SIZE_HEIGHT; a++) {
			for (int b = 0; b * levelTile.getWidth() < SCREEN_SIZE_WIDTH; b++) {
				int textureX = levelTile.getWidth() * b;
				int textureY = levelTile.getHeight() * a;
				levelTile.draw(textureX, textureY);
			}
		}

		if (entities != null) {
			for (Entity entity : entities) {
				if (entity.isVisible()) {
					entity.draw();
				}
			}
		}

		spaceshipEntity.draw();
	}

	private void drawObjects() {

		for (Entity entity : presents) {
			if (entity.isVisible()) {
				entity.draw();
			}
		}

		for (Entity entity : yellowPresents) {
			if (entity.isVisible()) {
				entity.draw();
			}
		}

		for (Entity entity : levelMines) {
			if (entity.isVisible()) {
				entity.draw();
			}
		}

		for (Entity entity : hearts) {
			if (entity.isVisible()) {
				entity.draw();
			}
		}

	}

	private void drawHUD() throws IOException {
		font.drawString(10, 0, String.format("Score: %d", presentsCollected), Color.white);

		font.drawString(SCREEN_SIZE_WIDTH - 155, 0, String.format("Lives: "), Color.white);

		Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/heart.png"));

		int objectX;
		int objectY;

		for (int i = lives; i > 0; i--) {
			objectX = (SCREEN_SIZE_WIDTH - i * texture.getImageWidth());
			objectY = 0;
			HeartEntity objectEntity = new HeartEntity(new MySprite(texture), objectX, objectY);

			objectEntity.draw();

		}
	}

	/**
	 * Do all calculations, handle input, etc.
	 */
	private void logic() throws IOException {
		// Example input handler: we'll check for the ESC key and finish the
		// game instantly when it's pressed
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			finished = true;
		}

		if (lives > 0) {
			// we check the time (get current ms)
			// 1 variable is used to store the current ms
			// every time we deduct the variable to the current ms

			logicSpaceship();

			Date date = new Date();
			// This method returns the time in millisecs
			long timeMilli = date.getTime();

			long difference = timeMilli - previous;

			if (difference > 1000) {
				// Generate normal presents
				createPresents();

				// Generate special presents
				createYellowPresents();

				// Generate the mines
				createMines();

				// Generate lives only if we have less that three:
				Random rand = new Random();
				if (lives < 3) {
					int createALife = rand.nextInt(5);
					if(createALife == 1) {
						createHearts();
					}
				
				}

				previous = timeMilli;
			}

			logicPresents();
			logicYellowPresents();
			logicMines();
			logicHearts();

			checkForCollision();
		}
	}

	private void logicSpaceship() {

		//the spaceship can move up or down
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			if (spaceshipEntity.getY() > 0) {
				spaceshipEntity.setY(spaceshipEntity.getY() - 15);
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			if (spaceshipEntity.getY() + spaceshipEntity.getHeight() < Display.getDisplayMode().getHeight()) {
				spaceshipEntity.setY(spaceshipEntity.getY() + 15);
			}
		}
	}

	private void logicPresents() {
		for (Entity present : presents) {
			if (present.isVisible()) {
				present.setX(present.getX() - 7);
			}
		}

	}

	//yellow presents move faster than the regular ones
	private void logicYellowPresents() {
		for (Entity present : yellowPresents) {
			if (present.isVisible()) {
				present.setX(present.getX() - 9);
			}
		}

	}

	private void logicMines() {
		for (Entity mine : levelMines) {
			if (mine.isVisible()) {
				mine.setX(mine.getX() - 9);
			}
		}

	}

	private void logicHearts() {
		for (Entity heart : hearts) {
			if (heart.isVisible()) {
				heart.setX(heart.getX() - 11);
			}
		}

	}

	private void checkForCollision() {
		// check for collision:
		for (int p = 0; p < entities.size(); p++) {
			for (int s = p + 1; s < entities.size(); s++) {
				Entity me = entities.get(p);
				Entity him = entities.get(s);

				if (me.collidesWith(him)) {
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}

		for (int i = 0; i < presents.size(); i++) {
			Entity him = presents.get(i);

			if (spaceshipEntity.collidesWith(him)) {
				spaceshipEntity.collidedWith(him);
				him.collidedWith(spaceshipEntity);
			}
		}

		for (int i = 0; i < yellowPresents.size(); i++) {
			Entity him = yellowPresents.get(i);

			if (spaceshipEntity.collidesWith(him)) {
				spaceshipEntity.collidedWith(him);
				him.collidedWith(spaceshipEntity);
			}
		}

		for (int i = 0; i < levelMines.size(); i++) {
			Entity him = levelMines.get(i);

			if (spaceshipEntity.collidesWith(him)) {
				spaceshipEntity.collidedWith(him);
				him.collidedWith(spaceshipEntity);
			}
		}

		for (int i = 0; i < hearts.size(); i++) {
			Entity him = hearts.get(i);

			if (spaceshipEntity.collidesWith(him)) {
				spaceshipEntity.collidedWith(him);
				him.collidedWith(spaceshipEntity);
			}
		}
	}

	public void notifyObjectCollision(Entity notifier, Object object) {
		// every time the spaceship collides with any other object, we get notified
		// based on the type of object in the collision, we can remove/add a life or add points to the score:
		
		if (object instanceof SpecialPresentEntity) {
			SpecialPresentEntity specialPresentEntity = (SpecialPresentEntity) object;
			yellowPresents.remove(specialPresentEntity);
			presentsCollected += 500;
		} else if (object instanceof PresentEntity) {
			PresentEntity presentEntity = (PresentEntity) object;
			presents.remove(presentEntity);
			presentsCollected += 100;
		} else if (object instanceof MineEntity) {
			MineEntity mineEntity = (MineEntity) object;
			levelMines.remove(mineEntity);
			lives--;
		} else if (object instanceof HeartEntity) {
			HeartEntity heartEntity = (HeartEntity) object;
			hearts.remove(heartEntity);
			lives++;
		}
	}

	public void checkForLives() throws IOException {
		if (lives == 0) {
			// we loose the game when we don't have any lives left

			// we load an image with text, saying to the player that he lost the game and presenting his score
			Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/astronaut.png"));

			Rectangle objectEntity = new Rectangle(new MySprite(texture), 50, 70);

			objectEntity.draw();

			bigFont.drawString(280, 110, String.format("You lost!"), Color.white);
			bigFont.drawString(200, 160, String.format("Your score is: %d", presentsCollected), Color.white);
		}
	}

}
