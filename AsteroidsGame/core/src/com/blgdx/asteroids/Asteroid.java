package com.blgdx.asteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Asteroid {
    private Vector2 position;
    private float speed;
    private float direction;
    private Texture texture;
    private float radius;
    private float rotationSpeed;
    private float rotation;
    private int spawnEdge;
    private int size;
    private static final String[] asteroidTextures = {
            "asteroid1.png",
            "asteroid2.png",
            "asteroid3.png"
    };

    public Asteroid(int spawnEdge) {
        this.spawnEdge = spawnEdge;
        spawnAsteroid();
    }

    public Asteroid(float x, float y, int size) {
        position = new Vector2(x, y);
        speed = MathUtils.random(50, 150);
        rotationSpeed = MathUtils.random(-30f, 30f);
        rotation = MathUtils.random(0f, 360f);

        // Choose a random asteroid texture
        int randomTextureIndex = MathUtils.random(asteroidTextures.length - 1);
        texture = new Texture(asteroidTextures[randomTextureIndex]);

        // Set the size based on the parameter
        this.size = size;

        // Adjust the radius based on size
        radius = 15f * size;
    }

    private void spawnAsteroid() {
        float x = 0, y = 0;

        switch (spawnEdge) {
            case 0: // Top
                x = MathUtils.random(Gdx.graphics.getWidth());
                y = Gdx.graphics.getHeight();
                direction = MathUtils.random(150f, 210f);
                break;
            case 1: // Bottom
                x = MathUtils.random(Gdx.graphics.getWidth());
                y = 0;
                direction = MathUtils.random(30f, 120f);
                break;
            case 2: // Left
                x = 0;
                y = MathUtils.random(Gdx.graphics.getHeight());
                // Set the direction to travel towards the right side (angle between 60-120 degrees)
                direction = MathUtils.random(300f, 360f);
                break;
            case 3: // Right
                x = Gdx.graphics.getWidth();
                y = MathUtils.random(Gdx.graphics.getHeight());
                // Set the direction to travel towards the left side (angle between 240-300 degrees)
                direction = MathUtils.random(120f, 240f);
                break;
        }

        position = new Vector2(x, y);
        speed = MathUtils.random(50, 150);
        rotationSpeed = MathUtils.random(-30f, 30f);
        rotation = MathUtils.random(0f, 360f);

        // Choose a random asteroid texture
        int randomTextureIndex = MathUtils.random(asteroidTextures.length - 1);
        texture = new Texture(asteroidTextures[randomTextureIndex]);

        // Randomize asteroid size (1, 2, or 3)
        size = MathUtils.random(1, 3);

        // Adjust the radius based on size
        radius = 15f * size;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getSpeed() {
        return speed;
    }

    public float getDirection() {
        return direction;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getRadius() {
        return radius;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    public int getSize() {
        return size;
    }
    public void decreaseSize() {
        if (size > 1) {
            size--;
            // Adjust the radius based on the new size
            radius = 15f * size;
        }
    }
}
