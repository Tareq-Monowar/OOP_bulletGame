package light.tarek.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    float x, y;
    Texture texture;
    float speed = 80;

    public Enemy(float x, float y) {
        texture = new Texture("enemy.png");
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        y -= speed * delta;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 64, 64);
    }

    public boolean hasCrossedPlayer(float playerY) {
        return y + 64 < playerY;
    }

    public boolean collWithplayer(float px, float py) {
        return px < x + 64 && px + 64 > x && py < y + 64 && py + 100 > y;
    }

    public boolean collWithbullet(float bx, float by) {
        return bx < x + 64 && bx + 8 > x && by < y + 64 && by + 30 > y;
    }

    public void dispose() {
        texture.dispose();
    }
}
