public class HeroEntity extends Entity {
	private Object game;
	
	

	public HeroEntity(Object game, MySprite sprite, int x, int y) {
		super(sprite, x, y);
		this.game = game;
	}
	
	@Override
	public void collidedWith(Entity other) {
		SpaceshipGame spaceshipGame = (SpaceshipGame) game;
//		System.out.println("Collision detected HeroEntity");
		spaceshipGame.notifyObjectCollision(this, other);
	}
}
