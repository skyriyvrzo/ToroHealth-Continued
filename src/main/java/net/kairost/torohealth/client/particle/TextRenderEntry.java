package net.kairost.torohealth.client.particle;

public record TextRenderEntry(
    String text,
    float x,
    float y,
    float z,
    float u,
    float v,
    int color,
    int light
) {}