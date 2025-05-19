package light.tarek.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;

public class Bullet {
    float x, y;
    Texture texture;
    float speed = 500;

    public Bullet(float x, float y) {
        this.x = x;
        this.y = y;
        texture = new Texture("bullet.png");
    }

    public void update(float delta) {
        y += speed * delta;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 8, 30);
    }

    public boolean isOffScreen() {
        return y > Gdx.graphics.getHeight();
    }

    public void dispose() {
        texture.dispose();
    }
}
