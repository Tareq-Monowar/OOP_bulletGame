package light.tarek.com;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

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

    enum GameState { MENU, GAME, GAME_OVER }
    GameState currentState = GameState.MENU;

    Rectangle startBounds, exitBounds;

    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture("spaceship.png");
        playerX = Gdx.graphics.getWidth() / 2f - 40;
        playerY = 50;

        bullets = new ArrayList<>();
        enemies = new ArrayList<>();

        fireSound = Gdx.audio.newSound(Gdx.files.internal("bullet.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(4);

        float centerX = Gdx.graphics.getWidth() / 2f - 100;
        startBounds = new Rectangle(centerX, 300, 200, 80);
        exitBounds = new Rectangle(centerX, 200, 200, 80);
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= 300 * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += 300 * delta;
        }

        playerX = Math.max(0, Math.min(playerX, Gdx.graphics.getWidth() - 80));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            float bulletX = playerX + 40 - 4;
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

    private void renderGame(float delta) {
        handleInput(delta);

        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            b.update(delta);
            if (b.isOffScreen()) {
                bulletIter.remove();
                b.dispose();
            }
        }

        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy e = enemyIter.next();
            e.update(delta);

            if (e.hasCrossedPlayer(playerY) || e.collWithplayer(playerX, playerY)) {
                gameOver = true;
                backgroundMusic.stop();
                currentState = GameState.GAME_OVER;
            }

            Iterator<Bullet> bulletIter2 = bullets.iterator();
            while (bulletIter2.hasNext()) {
                Bullet b = bulletIter2.next();
                if (e.collWithbullet(b.x, b.y)) {
                    bulletIter2.remove();
                    b.dispose();
                    enemyIter.remove();
                    e.dispose();
                    score += 10;
                    break;
                }
            }

            if (e.y + 64 < 0) {
                enemyIter.remove();
                e.dispose();
            }
        }

        if (enemies.isEmpty()) {
            spawnEnemyLines(1 + new Random().nextInt(1), 3 + new Random().nextInt(3));
        }

        batch.draw(playerTexture, playerX, playerY, 80, 100);
        for (Bullet b : bullets) b.render(batch);
        for (Enemy e : enemies) e.render(batch);
        font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
    }

    private void resetGame() {
        bullets.clear();
        enemies.clear();
        score = 0;
        playerX = Gdx.graphics.getWidth() / 2f - 40;
        playerY = 50;
        spawnEnemyLines(1, 3);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        if (currentState == GameState.MENU) {
            font.draw(batch, "SPACE SHOOTER", Gdx.graphics.getWidth() / 2f - 160, 500);
            font.draw(batch, "START", startBounds.x + 30, startBounds.y + 60);
            font.draw(batch, "EXIT", exitBounds.x + 50, exitBounds.y + 60);

            if (Gdx.input.justTouched()) {
                float x = Gdx.input.getX();
                float y = Gdx.graphics.getHeight() - Gdx.input.getY();

                if (startBounds.contains(x, y)) {
                    currentState = GameState.GAME;
                    backgroundMusic.play();
                } else if (exitBounds.contains(x, y)) {
                    Gdx.app.exit();
                }
            }

        } else if (currentState == GameState.GAME) {
            renderGame(delta);

        } else if (currentState == GameState.GAME_OVER) {
            font.draw(batch, "GAME OVER", Gdx.graphics.getWidth() / 2f - 160, 400);
            font.draw(batch, "Final Score: " + score, Gdx.graphics.getWidth() / 2f - 140, 300);
            font.draw(batch, "Press ENTER to Restart", Gdx.graphics.getWidth() / 2f - 220, 200);

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetGame();
                currentState = GameState.MENU;
            }
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
