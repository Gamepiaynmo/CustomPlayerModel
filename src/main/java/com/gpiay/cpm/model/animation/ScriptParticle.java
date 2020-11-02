package com.gpiay.cpm.model.animation;

import com.gpiay.cpm.model.element.ParticleEmitterInstance;

public class ScriptParticle {
    private final ParticleEmitterInstance particle;

    public ScriptParticle(ParticleEmitterInstance particle) {
        this.particle = particle;
    }

    public String getTexture() { return particle.texture; }
    public void setTexture(String texture) { particle.setTexture(texture); }

    public ScriptBone getBone() { return new ScriptBone(particle.bone); }

    public double getPositionRangeX() { return particle.posRange.x; }
    public double getPositionRangeY() { return particle.posRange.y; }
    public double getPositionRangeZ() { return particle.posRange.z; }
    public double getDirectionRange() { return particle.dirRange; }
    public double getMinAngle() { return particle.angle[0]; }
    public double getMaxAngle() { return particle.angle[1]; }
    public double getMinSpeed() { return particle.speed[0]; }
    public double getMaxSpeed() { return particle.speed[1]; }
    public double getMinRotationSpeed() { return particle.rotSpeed[0]; }
    public double getMaxRotationSpeed() { return particle.rotSpeed[1]; }
    public double getMinLifeSpan() { return particle.lifeSpan[0]; }
    public double getMaxLifeSpan() { return particle.lifeSpan[1]; }
    public double getDensity() { return particle.density; }
    public double getMinColorR() { return particle.color[0][0]; }
    public double getMaxColorR() { return particle.color[0][1]; }
    public double getMinColorG() { return particle.color[1][0]; }
    public double getMaxColorG() { return particle.color[1][1]; }
    public double getMinColorB() { return particle.color[2][0]; }
    public double getMaxColorB() { return particle.color[2][1]; }
    public double getMinColorA() { return particle.color[3][0]; }
    public double getMaxColorA() { return particle.color[3][1]; }
    public double getMinSize() { return particle.size[0]; }
    public double getMaxSize() { return particle.size[1]; }
    public double getGravity() { return particle.gravity; }

    public boolean isCollide() { return particle.collide; }

    public void setPositionRangeX(double value) { particle.posRange.x = value; }
    public void setPositionRangeY(double value) { particle.posRange.y = value; }
    public void setPositionRangeZ(double value) { particle.posRange.z = value; }
    public void setDirectionRange(double value) { particle.dirRange = value; }
    public void setMinAngle(double value) { particle.angle[0] = value; }
    public void setMaxAngle(double value) { particle.angle[1] = value; }
    public void setMinSpeed(double value) { particle.speed[0] = value; }
    public void setMaxSpeed(double value) { particle.speed[1] = value; }
    public void setMinRotationSpeed(double value) { particle.rotSpeed[0] = value; }
    public void setMaxRotationSpeed(double value) { particle.rotSpeed[1] = value; }
    public void setMinLifeSpan(double value) { particle.lifeSpan[0] = value; }
    public void setMaxLifeSpan(double value) { particle.lifeSpan[1] = value; }
    public void setDensity(double value) { particle.density = value; }
    public void setMinColorR(double value) { particle.color[0][0] = value; }
    public void setMaxColorR(double value) { particle.color[0][1] = value; }
    public void setMinColorG(double value) { particle.color[1][0] = value; }
    public void setMaxColorG(double value) { particle.color[1][1] = value; }
    public void setMinColorB(double value) { particle.color[2][0] = value; }
    public void setMaxColorB(double value) { particle.color[2][1] = value; }
    public void setMinColorA(double value) { particle.color[3][0] = value; }
    public void setMaxColorA(double value) { particle.color[3][1] = value; }
    public void setMinSize(double value) { particle.size[0] = value; }
    public void setMaxSize(double value) { particle.size[1] = value; }
    public void setGravity(double value) { particle.gravity = value; }
}
