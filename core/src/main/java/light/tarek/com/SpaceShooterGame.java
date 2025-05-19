package light.tarek.com;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SpaceShooterGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture playerTexture;
    float playerX, playerY;
    ArrayList<Bullet> bullets;
    ArrayList<Enemy> enemies;

    Sound fireSound;
    Music backgroundMusic;
    BitmapFont font;

    boolean gameOver = false;
    int score = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture("spaceship.png");
        playerX = Gdx.graphics.getWidth() / 2f - 40;
        playerY = 50;

        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        spawnEnemyLines(1, 3);

        fireSound = Gdx.audio.newSound(Gdx.files.internal("bullet.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(3);
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= 300 * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += 300 * delta;
        }

        // Keep player on screen
        playerX = Math.max(0, Math.min(playerX, Gdx.graphics.getWidth() - 80));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            float bulletX = playerX + 40 - 4; // center bullet
            float bulletY = playerY + 90;
            bullets.add(new Bullet(bulletX, bulletY));
            fireSound.play();
        }

    }

    private void spawnEnemyLines(int rows, int enemiesPerRow) {
        float spacingX = 100;
        float spacingY = 80;
        float startX = 50;
        float startY = Gdx.graphics.getHeight();

        for (int row = 0; row < rows; row++) {
            for (int i = 0; i < enemiesPerRow; i++) {
                float x = startX + i * spacingX;
                float y = startY + row * spacingY;
                if (x < Gdx.graphics.getWidth() - 64) {
                    enemies.add(new Enemy(x, y));
                }
            }
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!gameOver) {
            handleInput(delta);

            // Update bullets
            Iterator<Bullet> bulletIter = bullets.iterator();
            while (bulletIter.hasNext()) {
                Bullet b = bulletIter.next();
                b.update(delta);
                if (b.isOffScreen()) {
                    bulletIter.remove();
                    b.dispose();
                }
            }

            // Update enemies
            Iterator<Enemy> enemyIter = enemies.iterator();
            while (enemyIter.hasNext()) {
                Enemy e = enemyIter.next();
                e.update(delta);

                // Collision with player
                if (e.hasCrossedPlayer(playerY) || e.collidesWithplayer(playerX, playerY)) {
                    gameOver = true;
                    backgroundMusic.stop();
                }

                // Bullet collision
                Iterator<Bullet> bulletIter2 = bullets.iterator();
                while (bulletIter2.hasNext()) {
                    Bullet b = bulletIter2.next();
                    if (e.collidesWithbullet(b.x, b.y)) {
                        bulletIter2.remove();
                        b.dispose();
                        enemyIter.remove();
                        e.dispose();
                        score += 10;
                        break;
                    }
                }

                // Enemy off screen
                if (e.y + 64 < 0) {
                    enemyIter.remove();
                    e.dispose();
                }
            }

            // Respawn enemies if none remain
            if (enemies.isEmpty()) {
                spawnEnemyLines(1 + new Random().nextInt(1), 3 + new Random().nextInt(3));
            }
        }

        // Drawing
        batch.begin();
        if (!gameOver) {
            batch.draw(playerTexture, playerX, playerY, 80, 100);
            for (Bullet b : bullets) b.render(batch);
            for (Enemy e : enemies) e.render(batch);
            font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
        } else {
            font.draw(batch, "GAME OVER", Gdx.graphics.getWidth() / 2f - 120, Gdx.graphics.getHeight() / 2f);
            font.draw(batch, "Final Score: " + score,
                Gdx.graphics.getWidth() / 2f - 140,
                Gdx.graphics.getHeight() / 2f - 60);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        fireSound.dispose();
        backgroundMusic.dispose();
        font.dispose();
        for (Bullet b : bullets) b.dispose();
        for (Enemy e : enemies) e.dispose();
    }
}
