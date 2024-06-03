package com.blgdx.asteroids;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Laser {
    private Vector2 position;
    private float radius;
    private float direction;
    private Texture texture;

    public Laser(float x, float y, float direction) {
        position = new Vector2(x, y);
        radius = 5f;
        this.direction = direction;
        texture = new Texture("laser.jpg");
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public float getDirection() {
        return direction;
    }

    public Texture getTexture() {
        return texture;
    }
}